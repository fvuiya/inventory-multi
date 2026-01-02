const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
const dayjs = require("dayjs");
const utc = require("dayjs/plugin/utc");
const timezone = require("dayjs/plugin/timezone");

dayjs.extend(utc);
dayjs.extend(timezone);

admin.initializeApp();

/**
 * Helper function to send notifications to Admins/Managers and save to Firestore.
 * 
 * @param {string} title - Notification title
 * @param {string} body - Notification body
 * @param {string} type - Event type (SALE, PURCHASE, RETURN_SALE, RETURN_PURCHASE, DAMAGE)
 * @param {number} amount - Total amount associated with event
 * @param {Object} metadata - Additional data (e.g., saleId, employeeId)
 */
async function sendAdminNotification(title, body, type, amount, metadata) {
    const db = admin.firestore();

    // 1. Persist notification to Firestore 'notifications' collection
    const notificationData = {
        title: title,
        body: body,
        type: type,
        amount: amount,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        metadata: metadata,
        isRead: false
        // Note: In a real app, you might duplicate this for each user 
        // or have a 'user_notifications' subcollection to track isRead per user.
        // For simplicity, we store one global record and notify admins.
    };

    try {
        await db.collection("notifications").add(notificationData);
        console.log(`Notification saved to Firestore: ${title}`);
    } catch (e) {
        console.error("Error saving notification to Firestore:", e);
    }

    // 2. Notify Admins via FCM
    const employeesRef = db.collection("employees");
    const snapshot = await employeesRef
        .where("permissions.can_manage_employees.granted", "==", true)
        .where("isActive", "==", true)
        .get();

    if (snapshot.empty) {
        console.log("No admins found to notify.");
        return;
    }

    const tokens = [];
    snapshot.forEach((doc) => {
        const data = doc.data();
        if (data.fcmToken) {
            tokens.push(data.fcmToken);
        }
    });

    if (tokens.length === 0) {
        console.log("No FCM tokens found for admins.");
        return;
    }

    const message = {
        notification: {
            title: title,
            body: body,
        },
        data: {
            type: type,
            ...metadata
        },
        tokens: tokens,
    };

    try {
        const response = await admin.messaging().sendEachForMulticast(message);
        console.log(`FCM sent: ${response.successCount} successes, ${response.failureCount} failures.`);
    } catch (error) {
        console.error("Error sending FCM:", error);
    }
}

/**
 * Trigger: New Sale
 */
exports.onSaleCreated = functions.firestore
    .document("sales/{saleId}")
    .onCreate(async (snap, context) => {
        const sale = snap.data();
        const saleId = context.params.saleId;
        const totalAmount = sale.totalAmount || 0;

        // Try to get employee name from metadata or user ID if available
        // For now, we'll keep it generic or assume the UI sends it contextually?
        // Ideally the sale document has 'employeeName' or 'employeeId'. 
        // Let's assume we can fetch it or just say "New Sale".
        // To keep it simple and robust:

        const title = "New Sale!";
        const body = `Sale of $${totalAmount.toFixed(2)} recorded.`;

        await sendAdminNotification(
            title,
            body,
            "SALE",
            totalAmount,
            { saleId: saleId }
        );
    });

/**
 * Trigger: New Purchase
 */
exports.onPurchaseCreated = functions.firestore
    .document("purchases/{purchaseId}")
    .onCreate(async (snap, context) => {
        const purchase = snap.data();
        const purchaseId = context.params.purchaseId;
        const totalAmount = purchase.totalAmount || 0;
        const supplierName = purchase.supplierName || "Supplier";

        const title = "New Purchase";
        const body = `Purchase of $${totalAmount.toFixed(2)} from ${supplierName}.`;

        await sendAdminNotification(
            title,
            body,
            "PURCHASE",
            totalAmount,
            { purchaseId: purchaseId }
        );
    });

/**
 * Trigger: New Sale Return
 */
exports.onSaleReturnCreated = functions.firestore
    .document("sale_returns/{returnId}")
    .onCreate(async (snap, context) => {
        const ret = snap.data();
        const returnId = context.params.returnId;
        const refundAmount = ret.refundAmount || 0;

        const title = "Sale Return";
        const body = `Return processed for $${refundAmount.toFixed(2)}.`;

        await sendAdminNotification(
            title,
            body,
            "RETURN_SALE",
            refundAmount,
            { returnId: returnId }
        );
    });

/**
 * Trigger: New Purchase Return
 */
exports.onPurchaseReturnCreated = functions.firestore
    .document("purchase_returns/{returnId}")
    .onCreate(async (snap, context) => {
        const ret = snap.data();
        const returnId = context.params.returnId;
        const refundAmount = ret.refundAmount || 0;

        const title = "Purchase Return";
        const body = `Purchase return processed for $${refundAmount.toFixed(2)}.`;

        await sendAdminNotification(
            title,
            body,
            "RETURN_PURCHASE",
            refundAmount,
            { returnId: returnId }
        );
    });

/**
 * Trigger: Inventory Damage
 * Assuming damage is recorded in 'inventory_events' with type 'DAMAGE' or a 'damage_reports' collection.
 * Based on the file list, I see 'dialog_add_damage.xml' and 'Damage.java'.
 * Let's assume there is a 'damages' collection based on 'Damage.java'.
 */
exports.onDamageRecorded = functions.firestore
    .document("damages/{damageId}")
    .onCreate(async (snap, context) => {
        const damage = snap.data();
        const damageId = context.params.damageId;
        const lossAmount = damage.lossAmount || 0; // Assuming field name

        const title = "Damage Recorded";
        const body = `Damage reported value: $${lossAmount.toFixed(2)}.`;

        await sendAdminNotification(
            title,
            body,
            "DAMAGE",
            lossAmount,
            { damageId: damageId }
        );
    });

/**
 * Callable Function: Get Dashboard Statistics
 * Calculates revenue, profit, expenses, and charts server-side.
 */
exports.getDashboardStats = functions.https.onCall(async (data, context) => {
    const startDate = data.startDate;
    const endDate = data.endDate;
    const userTimeZone = data.timeZone || "UTC";

    if (!startDate || !endDate) {
        throw new functions.https.HttpsError("invalid-argument", "Missing startDate or endDate.");
    }

    const db = admin.firestore();
    const startTs = admin.firestore.Timestamp.fromMillis(startDate);
    const endTs = admin.firestore.Timestamp.fromMillis(endDate);

    // Run queries in parallel
    const salesProxy = db.collection("sales")
        .where("saleDate", ">=", startTs)
        .where("saleDate", "<=", endTs)
        .get();

    const expensesProxy = db.collection("expenses")
        .where("date", ">=", startTs)
        .where("date", "<=", endTs)
        .get();

    const [salesSnap, expensesSnap] = await Promise.all([salesProxy, expensesProxy]);

    let totalRevenue = 0;
    let totalProfit = 0;
    let totalTransactions = salesSnap.size;

    const salesOverTime = {};
    const profitOverTime = {};
    const productQuantities = {};
    const productNames = {};
    const categoryRevenue = {};

    // Determine granularity
    const daysDiff = dayjs(endDate).diff(dayjs(startDate), "day");
    const granularity = daysDiff > 30 ? "day" : "hour";

    salesSnap.forEach((doc) => {
        const sale = doc.data();
        const amount = sale.totalAmount || 0;
        const profit = sale.totalProfit || 0;
        const saleDate = sale.saleDate.toDate();

        totalRevenue += amount;
        totalProfit += profit;

        // 1. Charts
        // Bucket by User TimeZone
        const key = dayjs(saleDate).tz(userTimeZone).startOf(granularity).valueOf();
        salesOverTime[key] = (salesOverTime[key] || 0) + amount;
        profitOverTime[key] = (profitOverTime[key] || 0) + profit;

        // 2. Top Products & Categories
        if (sale.items && Array.isArray(sale.items)) {
            sale.items.forEach((item) => {
                const pid = item.productId;
                const pname = item.productName || "Unknown";
                const qty = item.quantity || 0;
                const itemTotal = item.totalPrice || 0;
                const cat = item.category || "Uncategorized";

                if (pid) {
                    productQuantities[pid] = (productQuantities[pid] || 0) + qty;
                    productNames[pid] = pname;
                }

                categoryRevenue[cat] = (categoryRevenue[cat] || 0) + itemTotal;
            });
        }
    });

    let totalExpenses = 0;
    expensesSnap.forEach((doc) => {
        const expense = doc.data();
        totalExpenses += (expense.amount || 0);
    });

    // Helper to format chart data
    const formatChart = (map) => {
        return Object.keys(map).sort().map((k) => ({
            x: parseInt(k),
            y: map[k],
        }));
    };

    // Helper to format Top Products
    const topProducts = Object.keys(productQuantities)
        .map((pid) => ({
            label: productNames[pid],
            value: productQuantities[pid]
        }))
        .sort((a, b) => b.value - a.value)
        .slice(0, 10); // Top 10

    // Helper to format Categories
    const salesByCategory = Object.keys(categoryRevenue)
        .map((cat) => ({
            label: cat,
            value: categoryRevenue[cat]
        }))
        .sort((a, b) => b.value - a.value);

    return {
        totalRevenue: totalRevenue,
        totalProfit: totalProfit,
        netProfit: totalProfit - totalExpenses,
        totalExpenses: totalExpenses,
        totalTransactions: totalTransactions,
        averageOrderValue: totalTransactions > 0 ? totalRevenue / totalTransactions : 0,
        salesOverTime: formatChart(salesOverTime),
        profitOverTime: formatChart(profitOverTime),
        topSellingProducts: topProducts,
        salesByCategory: salesByCategory,
        granularity: granularity
    };
});
