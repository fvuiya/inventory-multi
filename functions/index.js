const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

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
