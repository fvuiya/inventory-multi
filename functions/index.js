const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Triggered when a new document is created in the 'sales' collection.
 * Sends a notification to all Admin/Manager employees.
 */
exports.onSaleCreated = functions.firestore
    .document("sales/{saleId}")
    .onCreate(async (snap, context) => {
        const sale = snap.data();
        const saleId = context.params.saleId;
        const totalAmount = sale.totalAmount;

        console.log(`New sale created: ${saleId} for ${totalAmount}`);

        // 1. Find all Admin/Manager employees
        // We identify them by the permission 'can_manage_employees.granted' == true
        const employeesRef = admin.firestore().collection("employees");
        const snapshot = await employeesRef
            .where("permissions.can_manage_employees.granted", "==", true)
            .where("isActive", "==", true)
            .get();

        if (snapshot.empty) {
            console.log("No admins found to notify.");
            return null;
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
            return null;
        }

        console.log(`Sending notification to ${tokens.length} admins.`);

        // 2. Construct the notification message
        const message = {
            notification: {
                title: "New Sale!",
                body: `Sale of $${totalAmount.toFixed(2)} recorded.`,
            },
            data: {
                saleId: saleId,
                type: "SALE_CREATED",
            },
            tokens: tokens,
        };

        // 3. Send Multicast
        try {
            const response = await admin.messaging().sendEachForMulticast(message);
            console.log(`Successfully sent message: ${response.successCount} successes, ${response.failureCount} failures.`);

            if (response.failureCount > 0) {
                const failedTokens = [];
                response.responses.forEach((resp, idx) => {
                    if (!resp.success) {
                        failedTokens.push(tokens[idx]);
                    }
                });
                console.log("Failed tokens:", failedTokens);
                // In a real app, you might want to remove invalid tokens here
            }
        } catch (error) {
            console.error("Error sending notification:", error);
        }

        return null;
    });
