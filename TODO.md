# Project: Inventory Manager

**Last Updated:** 2025-12-21

## 1. Master Plan & Core Architecture
> This section is managed by the user (the architect). It contains the high-level vision, core features, and non-negotiable architectural decisions. Gemini reads for context but **MUST NOT** edit it.

### Part A: The Immediate Vision (Current Focus)

**Vision:** To create the central nervous system for a small business, right in their pocket.

**The Problem We Solve:** Small business owners are overwhelmed by disconnected tools. This app will replace the chaos of spreadsheets, notebooks, and mental notes with a single, intelligent, and mobile-first platform that provides clarity and control.

**Core Pillars:**

1.  **Unified Operations:** We will provide a seamless, traceable flow for the entire business lifecycle: **Suppliers** provide **Products** for our **Inventory**, which are then sold to **Customers**.

2.  **Actionable Intelligence:** The app will not just store data; it will provide strategic insights. The **Reports** feature is designed to automatically answer critical business questions about profitability, sales trends, and customer behavior, turning data into a competitive advantage.

3.  **Secure, Delegated Control:** A business owner can delegate tasks with confidence. The **Role-Based Access Control (RBAC)** system allows for the creation of employees with granular, time-limited permissions, enabling the business to grow securely.

4.  **Intelligent, Role-Based Notifications:** The app will proactively alert users to critical business events (like low stock or expiring products). The notification system will be deeply integrated with the Role-Based Access Control (RBAC) system, ensuring that employees only receive notifications relevant to their specific roles and permissions. This fosters team awareness without creating unnecessary noise.

### Part B: The Ultimate Vision (Long-Term Goal)

**Vision:** To evolve from a small business tool into a scalable, cross-platform Enterprise Resource Planning (ERP) ecosystem.

**The Goal:** The Firebase backend will serve as the central hub for a full suite of clients (Android, iOS, Web, Desktop), allowing the system to scale with a growing business and support more complex operations like multiple warehouses.

**Architectural Implications (Unlocked by Firebase Blaze Plan):**

1.  **Server-Side Logic:** To ensure consistency and scalability across all platforms, all critical business logic (reporting, transactions, complex validation) **must** eventually be migrated from the client-side Android app to server-side **Cloud Functions**.

2.  **Scalable Data Models:** Our data models must evolve. For example, `Product.quantity` will need to become a map to support multi-warehouse inventory.

### Part C: Core Architectural Principles
> Derived from the INVENTORY_APP_ANALYSIS_REPORT.md, these principles are non-negotiable for all new and refactored code.

1.  **Data Integrity is Paramount:** All database operations that modify inventory or financial records **must** be atomic and executed within transactions. We will implement strict server-side (or, for now, client-side transactional) validation to prevent race conditions and ensure data consistency.

2.  **Zero-Tolerance for Financial Errors:** All financial calculations (pricing, taxes, discounts) **must** be centralized into a single, reliable utility. We will enforce strict validation to ensure profit margins are respected and calculations are consistent across the entire app.

3.  **Strict Separation of Concerns:** We will enforce a clear separation between the Presentation, Domain, and Data layers. Activities and Fragments will **only** handle UI. ViewModels will manage UI state and delegate to the Domain layer. Repositories will be the sole entry point to the Data layer.

4.  **Dependency Injection is Standard:** All dependencies (Repositories, ViewModels, etc.) will be provided through a dependency injection framework (like Hilt) to improve testability and decouple our components. Manual instantiation is to be avoided.

5.  **Performant Data Loading:** We will move away from loading entire datasets at startup. All lists of data (products, sales, etc.) **must** be paginated to ensure the app remains fast and responsive as data grows.

6.  **Code Readability and Maintainability:** To ensure our codebase remains manageable, individual files **must not** exceed 500 lines. For components that require more extensive logic, the functionality must be broken down into smaller, focused helper classes or use cases and referenced from the main file.

7.  **Data Model Documentation:** All data models must be documented in the `Data Model Registry` section of this file. This includes the model's purpose, key fields, and its relationships to other models.

8.  **No Legacy Support:** This is a new project in active development. There is no real-world legacy data. When refactoring or fixing logic, remove the old/incorrect implementation entirely. Do not write fallback logic for temporary test data.

9.  **Local TimeZone Authority:** All user-facing date interpretations (e.g., "Today", "Start of Day", "End of Day") **must** uses the device's **Local TimeZone** (`Calendar.getInstance()`) and NOT UTC. This ensures that a user's business day aligns with their physical clock. Data is stored as standard Timestamps/Dates (UTC points in time), but querying for a "day" is a local concept.

### Part D: Working Configuration Versions (Android)
> **CRITICAL:** These versions were verified working on 2025-12-22. This is the source of truth - any KMP integration must NOT break these.

| Component | Version | Notes |
|-----------|---------|-------|
| **Gradle Wrapper** | 8.13 | From `gradle-wrapper.properties` |
| **Android Gradle Plugin (AGP)** | 8.13.2 | From `libs.versions.toml` |
| **Hilt** | 2.55 | Works with Gradle 8.13 |
| **Firebase BOM** | 34.0.0 | Manages all Firebase versions |
| **core-splashscreen** | 1.2.0 | Works with AGP 8.13.2 |
| **lifecycle-viewmodel** | 2.8.1 | |
| **lifecycle-livedata-ktx** | 2.9.2 | |
| **lifecycle-viewmodel-ktx** | 2.9.2 | |

**Key Files (Android-only, working):**
- `settings.gradle` - Only includes `:app`
- `build.gradle` - Simple plugins block (android, google-services, hilt)
- `gradle/libs.versions.toml` - Version catalog
- `app/build.gradle` - App module config

**KMP Strategy:**
When adding KMP modules, they must use their own compatible versions without modifying the Android config. The `:shared` module should NOT be added to `:app` dependencies until fully tested.

### Part E: KMP Migration Plan (Multi-Platform)
> **Goal:** Share code across Android, iOS, Desktop (JVM), and Web while keeping the Android app stable.

#### Current Status (2025-12-23) ✅ VERIFIED WORKING
- ✅ `:shared` module builds for Android + JVM
- ✅ KMP dependencies (Kotlin 2.1.0, kotlinx-serialization 1.8.0, kotlinx-coroutines 1.10.1, kotlinx-datetime 0.6.1)
- ✅ Firebase deps in androidMain (firebase-firestore-ktx 25.1.4)
- ✅ `:shared` connected to `:app`
- ✅ Kotlin plugin added to `:app` module (enables .kt files)
- ✅ `ModelMappers.kt` utility created for Java↔Kotlin model conversion
- ✅ **Runtime verified** - App installed and all features working!
- ⏸️ iOS targets disabled (no Xcode configured)

#### Phase 1: Data Models → `:shared/commonMain` 
> Pure Kotlin data classes with @Serializable. No platform-specific code.

| Priority | Android Model (Java) | Shared Model (Kotlin) | Status |
|----------|---------------------|----------------------|--------|
| 🔴 HIGH | `Product.java` | `Product.kt` | ✅ Exists |
| 🔴 HIGH | `Purchase.java` | `Purchase.kt` | ✅ Exists |
| 🔴 HIGH | `PurchaseItem.java` | `PurchaseItem.kt` | ✅ Exists |
| 🔴 HIGH | `Supplier.java` | `Supplier.kt` | ✅ Exists |
| 🔴 HIGH | `Person.java` | `Person.kt` | ✅ Exists |
| 🔴 HIGH | `ReturnableItem.java` | `ReturnableItem.kt` | ✅ Exists |
| 🟡 MED | `Sale.java` | `Sale.kt` | ✅ Created |
| 🟡 MED | `SaleItem.java` | `SaleItem.kt` | ✅ Created |
| 🟡 MED | `Customer.java` | `Customer.kt` | ✅ Created |
| 🟡 MED | `SaleReturn.java` | `SaleReturn.kt` | ✅ Created |
| 🟡 MED | `SaleReturnItem.java` | `SaleReturnItem.kt` | ✅ Created |
| 🟡 MED | `PurchaseReturn.java` | `PurchaseReturn.kt` | ✅ Created |
| 🟡 MED | `PurchaseReturnItem.java` | `PurchaseReturnItem.kt` | ✅ Created |
| 🟢 LOW | `Expense.java` | `Expense.kt` | ✅ Created |
| 🟢 LOW | `Damage.java` | `Damage.kt` | ✅ Created |
| 🟢 LOW | `PassiveIncome.java` | `PassiveIncome.kt` | ✅ Created |
| 🟢 LOW | `Order.java` | `Order.kt` | ✅ Created |
| ⚪ SKIP | `ApiProduct.java` | - | (API-specific) |
| ⚪ SKIP | `ProductResponse.java` | - | (API-specific) |
| ⚪ SKIP | `ProductSelection.java` | - | (UI-specific) |
| ⚪ SKIP | `Message.java` | - | (Notification) |
| ⚪ SKIP | `ActivityEvent.java` | - | (Android-specific) |
| ⚪ SKIP | `PaymentDraft.java` | - | (UI-specific) |
| ⚪ SKIP | `Offer.java` | - | (Future feature) |

#### Phase 2: Repository Interfaces → `:shared/commonMain`
> Define interfaces in `commonMain`, implementations in platform-specific source sets.

| Interface | Location | Status |
|-----------|----------|--------|
| `PurchaseRepository` | `commonMain` | ✅ Exists |
| `SaleRepository` | `commonMain` | ✅ Created |
| `ProductRepository` | `commonMain` | ✅ Created |
| `CustomerRepository` | `commonMain` | ✅ Created |
| `SupplierRepository` | `commonMain` | ✅ Created |

#### Phase 3: Platform Implementations → `:shared/androidMain`
> Firebase implementations for Android. Each platform gets its own impl.

| Implementation | Location | Status |
|----------------|----------|--------|
| `PurchaseRepositoryImpl` | `androidMain` | ✅ Restored |
| `SaleRepositoryImpl` | `androidMain` | ✅ Created |
| `ProductRepositoryImpl` | `androidMain` | ✅ Created |

#### Phase 4: Connect `:shared` to `:app`
> Only after all above phases verified working.

1. Add `implementation(project(":shared"))` to `app/build.gradle`
2. Update `:app` to import models from `com.bsoft.inventorymanager.model`
3. Gradually migrate each feature to use shared repository interfaces
4. Test thoroughly before each commit

#### Migration Rules
1. **Never break Android** - Test `:app:assembleDebug` after every change
2. **Kotlin-first** - All shared code is Kotlin with `@Serializable`
3. **Long → Instant** - Use `Long` (epoch millis) for timestamps in KMP
4. **No Android imports** - `commonMain` must have ZERO `android.*` imports
5. **Expect/Actual** - Use for platform-specific implementations

#### Phase 5: Feature-by-Feature Migration (Detailed Guide)

> **Key Lesson:** You cannot migrate one file in isolation. Each feature flow must be migrated together.

##### Why Cascades Happen
```
Adapter uses Purchase →
  Activity uses Purchase →
    ViewModel returns List<Purchase> →
      Repository returns Purchase from Firestore
```
If you change the Adapter's import, everything above it breaks because they return the wrong Purchase type.

##### Bottom-Up Migration Strategy

**Step 1: Migrate Repository Implementation**
```
shared/src/androidMain/.../PurchaseRepositoryImpl.kt
↓ Returns: com.bsoft.inventorymanager.model.Purchase (shared)
```

**Step 2: Create Bridge ViewModel (or migrate existing)**
```java
// Option A: Create new Kotlin ViewModel that uses shared types
class PurchaseViewModelKmp : ViewModel() {
    val purchases: StateFlow<List<Purchase>> // shared model
}

// Option B: Update existing ViewModel's return types
// This requires migrating the ViewModel to Kotlin
```

**Step 3: Migrate UI Layer**
```
- PurchaseAdapter.java → Change import, update date handling
- PurchaseActivity.java → Change import, update any Timestamp usages
```

##### Migration Checklist for "Purchase" Feature

| Step | File | Import Change | Other Changes |
|------|------|---------------|---------------|
| 1 | `shared/androidMain/PurchaseRepositoryImpl.kt` | Already uses shared | ✅ Done |
| 2 | `MainViewModel.java` | `models.Purchase` → `model.Purchase` | Convert Timestamp→Long |
| 3 | `PurchaseActivity.java` | `models.Purchase` → `model.Purchase` | Update interface methods |
| 4 | `PurchaseAdapter.java` | `models.Purchase` → `model.Purchase` | `Timestamp.toDate()` → `new Date(long)` |

##### Key API Differences (Java → Kotlin Shared)

| Java Model | Kotlin Shared | Conversion |
|------------|---------------|------------|
| `Timestamp purchaseDate` | `Long purchaseDate` | `timestamp.toDate().time` ↔ `Date(long)` |
| `Timestamp.toDate()` | `Date(purchaseDate)` | Direct constructor |
| Getters: `getPurchaseDate()` | Property: `purchaseDate` | Java can still use getters |
| Nullable via null | `String?` explicit | Same behavior |

##### When to Use Each Approach

| Approach | When to Use |
|----------|-------------|
| **New Features First** | Building new screens/features - use shared models from start |
| **Bottom-Up Full Migration** | Rewriting a feature entirely - more work but cleaner |
| **Wrapper/Adapter Pattern** | Need quick migration - create extension functions to convert |

##### Wrapper/Adapter Example
```kotlin
// Extension to convert Java model to Shared model
fun com.bsoft.inventorymanager.models.Purchase.toShared(): com.bsoft.inventorymanager.model.Purchase {
    return com.bsoft.inventorymanager.model.Purchase(
        documentId = this.documentId ?: "",
        purchaseDate = this.purchaseDate?.toDate()?.time ?: 0L,
        // ... map all fields
    )
}
```

---


## 2. Task Board
> Tracks day-to-day progress.

### Our Current Strategy (The Unified Plan)

1.  **Prioritize Critical Fixes:** We will immediately address all `[CRITICAL]` tasks in our backlog. This is our top priority, superseding the original `EXECUTION_PLAN.md`.
2.  **Integrate Refactoring:** As we address these critical issues, we will simultaneously apply our "Core Architectural Principles." This combines immediate bug fixing with long-term architectural improvement.
3.  **Defer Long-Term Planning:** The `EXECUTION_PLAN.md` will serve as a high-level guide. We will only revisit and formally update it after the application's core data integrity and financial calculations are secure and stable.

*This strategy is informed by our three key planning documents:*
-   ***TODO.md (This file):*** *Our definitive master plan and active task board.*
-   ***INVENTORY_APP_ANALYSIS_REPORT.md:*** *Provides the detailed technical analysis and critical issues that shape our immediate priorities.*
-   ***EXECUTION_PLAN.md:*** *Our high-level, long-term roadmap.*

### Backlog (Upcoming Tasks)
- [x] **[REFACTOR]** Align Customer data model with Supplier data model to ensure they are true mirror opposites.
- [x] **[CRITICAL]** Create a centralized `FinancialCalculator` utility to standardize all pricing, tax, and discount calculations.
- [x] **[CRITICAL]** Add validation to ensure `sellingPrice` is never less than `costPrice` when creating or updating products.
- [x] Refactor `MainRepository` to use pagination for all data lists (products, sales, etc.).
- [x] Begin integrating Hilt for dependency injection, starting with repositories.
- [x] **[TESTING]** Set up Testing Infrastructure (JUnit, Espresso, Hilt). <!-- id: 55 -->
    - [x] Add testing dependencies to `libs.versions.toml`. <!-- id: 56 -->
    - [x] Write Unit Tests for `FinancialCalculator` and `InputValidator`. <!-- id: 57 -->
    - [x] Configure Hilt for Instrumented Tests. <!-- id: 58 -->
- [x] **Phase 1, Day 1-2:** Identify existing bugs and performance issues (Audit Complete: See Section 6)
- [x] **Phase 1, Day 1-2:** Document current functionality gaps (Documented in "Known Bugs & Gaps" below)
- [x] **Phase 1, Day 1-2:** Create bug tracking system (Section 6 finalized)
- [x] **[OPTIMIZATION]** Refactor Reports to use denormalized data and Activity-scoped ViewModel.
- [ ] Reference: Review the full project roadmap in EXECUTION_PLAN.md
- [x] Migrate reporting logic to server-side Cloud Functions.
- [x] **Notifications (Android):** Implement logic to retrieve and store FCM tokens for employees.
- [x] **Notifications (Cloud Functions):** Set up a new Firebase Functions project.
- [x] **Notifications (Cloud Functions):** Implement a function triggered by new sales to send notifications.

### In Progress
- [x] **[CRITICAL]** Implement transactional updates for all inventory changes to ensure data integrity.
- [x] **Phase 1, Day 1-2:** Analyze current codebase structure

### Done
- [x] **UX Improvement:** Correctly implemented a two-step splash screen to show a brief static logo followed by a data-driven animated screen.
- [x] Create an "About" screen to house the attribution for third-party libraries and assets.
- [x] **Architecture:** Implemented Repository and ViewModel patterns for data pre-loading.
- [x] Implement a theme-aware animated splash screen using Lottie.
- [x] **Notifications (Android):** Add `firebase-messaging` dependency and create `MyFirebaseMessagingService`.
- [x] Implement the empty `RefreshableFragment.java`, `ActivityHistory.java` and `ActivityHistoryAdapter.java` files.
- [x] Fixed: Offer edit screen now correctly pre-populates with existing data.
- [x] Implement edit and delete functionality in `OfferActivity`.
- [x] **Architectural Principle:** Ensure `CreateSaleActivity` and `CreatePurchaseActivity` (and their "Return" counterparts) are always visually identical and functionally opposite.
- [x] Implement "add new customer" functionality in `CreateSaleActivity`.
- [x] Remove unused image loading library (Picasso or Glide).
- [x] Refactor `CreateSaleReturnActivity.java` and `CreatePurchaseReturnActivity.java` to reduce code duplication.
- [x] Refactor `CreateSaleActivity.java` and `CreatePurchaseActivity.java` to reduce code duplication.
- [x] Refactor `AddEditCustomerSheet.java` and `AddEditSupplierSheet.java` to reduce code duplication.
- [x] Consolidate all dependencies into the `libs.versions.tovml` file.
- [x] Fix missing `CreateSaleReturnActivity` and `SelectSaleToReturnActivity` declarations in `AndroidManifest.xml`.

---

## 3. Changelog & Decisions
> A log of completed tasks and key micro-decisions made during development. Gemini proposes entries here after completing a task.

- **2026-01-04 (Session 12 - Supplier Feature Debugging):**
    -   **Bug Fix:** Resolved `SupplierActivity` crash (`NoSuchMethodException`) by enabling `kotlin-kapt` and fixing Hilt configuration for Kotlin ViewModels.
    -   **Bug Fix:** Resolved `SupplierProfileActivity` crash (`Timestamp` deserialization error) by refactoring `SupplierProfileViewModel` to Kotlin/Clean Architecture and fixing `Timestamp` mapping in `SupplierRepositoryImpl`.

- **2025-01-01 (Session 11 - FCM Notifications):**
    -   **Feature:** Implemented end-to-end Notification System for new sales.
    -   **Android:** Added `fcmToken` to Employee model and implemented automatic token sync in `SplashActivity` and `LoginActivity`.
    -   **Cloud Functions:** Deployed `onSaleCreated` function to listen for new sales and send multicast notifications to all "Admin/Manager" users.
    -   **Fix:** Resolved a critical `TypeError` by replacing the deprecated `sendMulticast` with `sendEachForMulticast` (required for modern `firebase-admin` SDKs).

- **2025-12-23 (Session 10 - KMP Integration & UI Fix):**
    -   **Architecture (KMP):** Completed Phase 1-5 of Kotlin Multiplatform migration. Created 17 shared data models, 5 repository interfaces, and 3 Firebase implementations (`PurchaseRepositoryImpl`, `SaleRepositoryImpl`, `ProductRepositoryImpl`) in `shared/androidMain`.
    -   **Infrastructure:** Added Kotlin Android plugin to `:app` module, created `ModelMappers.kt` utility for Java↔Kotlin model conversion, verified app builds and runs with shared module.
    -   **Bug Fix:** Fixed `PaymentSheet` displaying incorrect transaction type text. Added `transactionType` parameter to dynamically show "Sale Finalized!", "Purchase Finalized!", etc. based on context.

- **[Feature] Advanced Notifications & Reports Migration:**
    - **Notifications:**
        - Implemented Cloud Functions triggers: `onPurchaseCreated`, `onSaleReturnCreated`, `onPurchaseReturnCreated`, `onDamageRecorded`.
        - Created `sendAdminNotification` helper to centralize FCM and Firestore notification logic.
        - Added `Notification` model, adapter, and fragment for a dedicated notification center in the app.
        - Integrated FCM tokens for admin push notifications.
    - **Reports Migration:**
        - Deployed `getDashboardStats` Cloud Function (HTTP Callable) to handle heavy reporting aggregations server-side.
        - Integrated `dayjs` in Cloud Functions for robust User-Timezone handling.
        - Refactored `ReportsViewModel` to remove legacy client-side aggregation logic and consume the Cloud Function response.
        - Updated `SalesFragment`, `FinancialFragment`, and `ProfitFragment` flow to trigger server-side fetch.
- **2026-01-04 (Session 12 - Supplier Feature Debugging):**
    -   **Bug Fix:** Resolved `SupplierActivity` crash (`NoSuchMethodException`) by enabling `kotlin-kapt` and fixing Hilt configuration for Kotlin ViewModels.
    -   **Bug Fix:** Resolved `SupplierProfileActivity` crash (`Timestamp` deserialization error) by refactoring `SupplierProfileViewModel` to Kotlin/Clean Architecture and fixing `Timestamp` mapping in `SupplierRepositoryImpl`.
    -   **Bug Fix:** Fixed `RuntimeException` in `PurchaseRepositoryImpl` due to mixed `Timestamp`/`Long` formats for `creationDate` and `lastDeliveryDate` in Firestore.
    -   **Bug Fix:** Fixed `ClassCastException` in `ReportsActivity` by correctly parsing the `getInventoryAnalysis` Cloud Function response map.
    -   **KMP Migration:** Fully migrated **Customer Feature** to use Kotlin Multiplatform shared models and repositories.
        -   Converted `AddEditCustomerSheet` to Kotlin (`.kt`) to support safe `kotlinx-serialization` of KMP models.
        -   Refactored `CustomerActivity`, `SelectCustomerActivity`, `CustomerProfileActivity` and their ViewModels to use `CustomerRepository`.
        -   Updated `CustomerAdapter` to use shared `Customer` model.
    -   **KMP Migration:** Migrated **Product Feature** to use Kotlin Multiplatform shared models and repositories.
        -   Converted `MainViewModel` from Java to Kotlin.
        -   Updated `MainViewModel` to use shared `ProductRepository` for product list loading and CRUD operations.
        -   Removed product loading from `MainRepository` (now Sales/Purchases only).
        -   Updated `ProductAdapter` and `ProductActivity` to use shared `Product` model.
        -   Added Hilt bindings for shared `ProductRepository` and `ProductRepositoryImpl`.
    -   **KMP Migration:** Migrated **Sales Feature** (list view) to use Kotlin Multiplatform shared models.
        -   Updated `SalesAdapter` to use shared `Sale` model (saleDate as Long, totalAmount directly).
        -   Added Sale/SaleItem mappers to `ModelMappers.kt` for bidirectional conversion.
        -   Updated `MainViewModel` to expose shared `Sale` list via LiveData mapping.
        -   Updated `SalesActivity` to use shared `Sale` model.
        -   Note: `CreateSaleViewModel` remains on legacy models (deferred, low priority - works correctly).
- **2025-12-22 (Session 9 - UI Consistency & Product Metadata Optimization):**
    -   **Reports Migration:** Migrated "Slow Moving Products" and "Lapsed Customers" logic to Cloud Functions (`getInventoryAnalysis`) for improved performance.
    -   **Performance:** Optimized `fetchUniqueBrandsAndCategories` in `ProductRepositoryImpl` to use a dedicated `metadata/products` document. This eliminates the need to scan the entire product collection to populate dropdowns, significantly reducing Firestore reads and latency.
    -   **Refactor:** Refactored `SalesActivity` and `PurchaseActivity` to use `MainViewModel` and `SwipeRefreshLayout`. This removes direct Firestore listeners, ensures consistent pagination logic across the dashboard, and enables manual "pull-to-refresh" functionality.
    -   **Architecture:** Centralized Product operations into `ProductRepository` and exposed them via `MainViewModel`. `ProductActivity` now delegates all data operations to the ViewModel, removing direct Firestore dependencies for write operations.
    -   **Robostness:** Implemented atomic updates for Product Metadata (brands/categories) within the `saveProduct` flow, ensuring the filter dropdowns remain up-to-date without expensive re-scans.
- **2025-12-21 (Session 8 - Phase 1 Remediation & Pagination):**
    -   **Performance:** Implemented **paginated loading for Expenses** in `MainRepository`, resolving a potential memory bottleneck as the expense history grows.
    -   **Performance & Arch:** Refactored **Product Selection** for both Sales and Purchases. Replaced bulk loading with server-side paginated fetching and migrated the logic to a lifecycle-aware `SelectProductViewModel` using Hilt. Added a scroll listener to `SelectProductActivity` for infinite loading.
    -   **Bug Fix:** Identified and fixed critical **MainRepository Pagination Failures** where sales and purchases were being sorted by a non-existent `"timestamp"` field instead of `"saleDate"` and `"purchaseDate"`.
    -   **Architecture:** Eliminated the **"Double Source of Truth"** risk in `CreateSaleActivity` and `CreatePurchaseActivity` by removing local list copies of selected products. The UI now observes the ViewModel's state directly, preventing state desynchronization.
    -   **Utility:** Extended `PaginationHelper` and `FinancialCalculator` to support complex filtered queries and precise arithmetic across the application.
- **2025-12-21 (Session 7 - Reports Crash Fix & Robustness):**
    -   **Bug Fix:** Resolved the critical **Reports Crash** (NullPointerException) by converting financial fields in the `Sale` model from primitive `double` to boxed `Double`. This allows the Firestore SDK to safely handle missing or explicitly `null` data in legacy documents.
    -   **Robustness:** Implemented defensive `try-catch` blocks and explicit null checks in `ReportsViewModel.java` to handle corrupted or inconsistent Firestore data without crashing the entire reporting module.
    -   **Performance & Sync:** Migrated all report fragments to a shared Activity-scoped `ReportsViewModel`. This ensures consistent state across all report tabs and allows the Activity's `SwipeRefreshLayout` to correctly synchronize with the loading state of any fragment.
- **2025-12-21 (Session 6 - "80/20" High-Impact Optimization):**
    -   **Financial Integrity:** Updated `SaleItem` to capture the historical `costPrice` at the moment of sale. This ensures that profit reports remain accurate even if product costs change in the future.
    -   **Performance Optimization:** Denormalized `totalCost` and `totalProfit` directly into the `Sale` document. This allows for instantaneous profit reporting without needing to fetch every associated product document.
    -   **Reporting Overhaul:** Refactored `ReportsViewModel` to prioritize these pre-calculated fields. Profit calculations are now significantly faster and more cost-effective (reducing Firestore read operations by ~90% for large datasets).
    -   **Architecture:** Updated `CreateSaleViewModel` to automatically populate these new financial metrics during transaction finalization.
- [x] **[FEATURE]** **Time-Precise Reports:** Enhanced `ReportsActivity` and `ReportsViewModel` to support custom date ranges with dynamic data granularity.
    - [x] Implemented "Custom" date picker that defaults to full-day range (00:00 - 23:59 Local Time).
    - [x] **[PRECISION]** Reports now display **Minute-level precision** for "Today", "Week", and "Month" (ranges <= 32 days), identifying exact transaction times (e.g., 2:01 vs 3:05).
    - [x] **[FIX]** Enforced **Local TimeZone** parsing for all report date ranges to prevent data loss due to UTC offsets.
    - [x] Fixed report loading issues on tab switch by implementing `refreshTrigger` pattern.
- [x] **[DEBUG]** **Purchase Refactoring:** Apply Clean Architecture and MVVM to `CreatePurchaseActivity`.
    - [x] Create `PurchaseRepository` and `CreatePurchaseViewModel`.
 to handle business logic and data persistence, decoupling them from the UI.
- **2025-12-21 (Session 5 - Purchase Flow Refactoring & Bug Fixes):**
    -   **Bug Fix:** Resolved a critical **Input Focus Loss** issue in `CreateSaleActivity` and `CreatePurchaseActivity` where the cursor would jump or disappear during quantity input. This was fixed by optimizing the `TextWatcher` and preventing unnecessary adapter notifications during active typing in the ViewModel.
    -   **Architecture:** Applied the **Clean Architecture and MVVM** pattern to the Purchase flow. Created `PurchaseRepository` and `CreatePurchaseViewModel` to handle business logic and data persistence, decoupling them from the UI.
    -   **Refactor:** Migrated `CreatePurchaseActivity` to use Hilt for dependency injection and the new ViewModel-driven architecture, ensuring consistency with the Sale flow.
    -   **Hilt Integration:** Added missing Hilt bindings for `PurchaseRepository` in the `AppModule` to support the new refactored flow.
    -   **UX Improvement:** Synchronized the quantity input logic between Sale and Purchase flows to ensure a consistent and stable user experience when managing inventory transactions.
- **2025-12-20 (Session 4 - Hilt Integration):**
    -   **Architecture:** Fully implemented **Hilt Dependency Injection**. Migrated `MainRepository`, `MainViewModel`, `ReportsViewModel`, and all key Activities (`LoginActivity`, `ProductActivity`, etc.) to use `@Inject`, `@HiltViewModel`, and `@AndroidEntryPoint`.
    -   **Build Fix:** Resolved a critical **Java 24 incompatibility** ("Unsupported class file major version 68") by explicitly configuring `gradle.properties` to use Android Studio's embedded JDK (Java 17/21).
    -   **Refactor:** Restored missing Android UI imports in `LoginActivity` to fix compilation errors during the migration.
    -   **Build Fix:** Resolved compilation errors in `CreateSaleViewModel`, `SaleRepositoryImpl`, and `CreateSaleActivity` (fixed syntax errors, restored missing fields/imports, and added missing Hilt binding for `SaleRepository`).
    -   **Bug Fix:** Resolved a "No Data Visible" issue by adding missing `android.permission.INTERNET` and `android.permission.ACCESS_NETWORK_STATE` permissions to `AndroidManifest.xml` (critical for Firebase Firestore connectivity).
- **2025-12-19 (Session 4):**
    - **UI Standard:** Implemented a **Universal Card Design** for all transactions (Sales, Purchases, Returns, Damage, Profile History). Cards now consistently display Date (Top Right), Name/Type (Top Left), and Amount (Bottom Right), improving readability and consistency across the app.
    - **Feature:** Expanded Return Selection Search to include **Invoice Number**, **PO Number**, **Total Amount** (exact match), and Customer/Supplier Name for both local and remote queries.
    - **Refactor:** Removed the "Status" field from Return Selection cards to reduce clutter, as per user request.
    - **Bug Fix:** Fixed a critical issue in `CreatePurchaseReturnActivity` where returning an item blindly decremented inventory, potentially leading to negative stock. Added strict validation to ensure `currentStock >= returnQuantity`.
    - **Refactor:** Standardized `item_sale`, `item_purchase`, `item_purchase_history`, and `item_activity_event` layouts to match the new universal design pattern.
    - **Build Fix:** Resolved missing `Return` class and `RETURN` enum dependencies in `ActivityEventAdapter`.
- **2025-12-16 (Session 3):**
    - **Feature:** Processed the "Return Feature Overhaul" to track `returnedQuantity` in `SaleItem` and `PurchaseItem`, enforcing strict return limits.
    - **Feature:** Standardized `CreateSaleReturnActivity` and `CreatePurchaseReturnActivity` to mimic their creation counterparts, including full PaymentSheet integration for refunds/credits.
    - **Feature:** Implemented **Return History** on Return tabs and added **Search/Filter** capabilities to find past sales/purchases for return.
    - **UX:** Simplified the Sale Return flow: FAB now skips "Select Customer" and opens the "Select Sale" list directly (similar to Purchase Return flow).
    - **Refactor:** Removed legacy private UI helpers (`showLoadingIndicator`, etc.) from `CreateSaleActivity` and `CreatePurchaseActivity` in favor of standardized `BaseActivity` methods.
- **2025-12-16 (Session 2):**
    - **Fixed:** Resolved `CreateSaleActivity` crash by gracefully handling `GmsBarcodeScanner` unavailability and adding session expiry checks in `BaseActivity`.
    - **Feature:** Implemented `FinancialCalculator` utility to centralize math and ensure data integrity.
    - **Security:** Enforced validation in `ProductActivity` to prevent selling products below cost price.
    - **Performance:** Implemented `Debouncer` for all search inputs and quantity fields to prevent UI lag and excessive database queries.
    - **Refactor:** Converted Search filtering to asynchronous `ExecutorService` tasks to unblock the main thread.
    - **UX Fixed:** Resolved "Input Focus Loss" in `CreateSaleActivity` and `CreatePurchaseActivity` by removing destructive `notifyItemChanged` calls during typing.
    - **Feature:** Enforced Strict Stock Limits. Sales are blocked if quantity > stock. Input fields now auto-cap to max stock with visual error feedback.
    - **UX:** Fixed Keyboard and FAB focus interactions in `SelectProduct`, `SelectCustomer`, and `SelectSupplier` activities.
- **2025-12-14:**
    - **Fixed:** Standardized permission checks to canonical `Permissions.PERMISSION_GROUPS` keys (`can_create_*`, `can_edit_*`, `can_delete_*`) in both `CreateSaleActivity` and `CreatePurchaseActivity`, removing incorrect `write_*`/`read_*` usage.
    - **Decision:** Keep one system only—use the `can_*` keys everywhere (employee creation, storage, and `SecurityManager`) to prevent future mismatches.
    - **Architecture:** Established a `Data Model Registry` in `TODO.md` and added a new architectural principle to enforce its use. This registry will document all data models, their fields, and their relationships.
    - **Backlog:** Added a new task to align the `Customer` and `Supplier` data models.
- **2025-12-13:**
    - **Fixed:** Resolved a critical permission bug caused by the app process being killed in the background. Implemented a `BaseActivity` to enforce user session validation and centralized user data loading in `SplashActivity` to ensure the `CurrentUser` is always available.
- **2025-12-09:**
    - **Fixed:** Corrected a critical bug in `CreatePurchaseActivity` where the product list was not being correctly passed to the validation method, causing a validation failure even when products were present.
    - **Refactor:** Conducted a comprehensive refactoring of the sale and purchase workflows (`CreateSaleActivity`, `CreatePurchaseActivity`, `SalesActivity`, `PurchaseActivity`, and `PaymentSheet`) to ensure they are visually identical and functionally opposite, in accordance with architectural principles.
    - **Fixed:** Explicitly set the visibility of the "Add New Customer" button in `CreateSaleActivity` to ensure it is always displayed correctly.
    - **Decision:** Renamed the `onPurchaseFinalized` method in the `PaymentSheetListener` to `onTransactionFinalized` to better reflect its purpose in both sale and purchase contexts.
- **2025-12-08:**
    - **UX Improvement:** Correctly implemented the two-step splash screen to show a brief static logo followed by the main Lottie animation, which now plays for the full duration of background data loading.
    - **Feature:** Created an "About" screen to display attribution for third-party libraries.
    - **Architecture:** Implemented a Repository and ViewModel pattern to pre-load `MainActivity` data during the splash screen, significantly improving startup performance.
    - **Feature:** Implemented a theme-aware animated splash screen using Lottie.
- **2025-12-05:**
    - **Feature:** Added the `firebase-messaging` dependency and created the `MyFirebaseMessagingService` class to lay the foundation for the notification system.
    - **Feature:** Implemented the foundational components for a refreshable activity feed, including `RefreshableFragment`, `ActivityHistory`, and `ActivityHistoryAdapter`.
    - **Fixed:** The offer edit screen now correctly pre-populates with the existing offer data, and the save logic properly updates the existing record.
    - **Feature:** Implemented edit and delete functionality in `OfferActivity`.
    - **Refactor:** Aligned the "add new supplier" workflow in `CreatePurchaseActivity` with the `CreateSaleActivity` by using the `AddEditSupplierSheet` bottom sheet, creating a more consistent user experience.
    - **Refactor:** Removed the Picasso image loading library to standardize on Glide, reducing the app's footprint and simplifying dependency management.
    - **Refactor:** Refactored `CreateSaleReturnActivity` and `CreatePurchaseReturnActivity` into a common `BaseCreateReturnActivity` to reduce code duplication and fixed a critical bug where purchase returns incorrectly incremented stock.
    - **Refactor:** Refactored `CreateSaleActivity` to align with the design of `CreatePurchaseActivity`, ensuring a consistent user experience. This included adopting the unified `PaymentSheet` for a standardized payment flow and consolidating the product selection adapters to reduce code duplication.
    - **Refactor:** Refactored `AddEditCustomerSheet` and `AddEditSupplierSheet` by creating a `BaseAddEditSheet` to reduce code duplication.
    - **Decision:** Introduced a `Person` interface to abstract common properties between `Customer` and `Supplier` models. This allows the base sheet to handle common UI and data binding logic, making the code more maintainable and scalable.
    - **Refactor:** Consolidated all project dependencies into the `gradle/libs.versions.toml` file.
    - **Decision:** This aligns the project with modern Android development best practices, providing a single source of truth for dependency management and improving build script readability and maintainability.

- **2025-12-04:**
    - **Fixed:** Added missing declarations for `CreateSaleReturnActivity` and `SelectSaleToReturnActivity` to `AndroidManifest.xml` to prevent application crashes.
    - **Fixed:** Corrected a method signature mismatch in `PaymentSheet.java`. The `onPurchaseFinalized` method call was passing 8 arguments to a listener that expected only 6.
    - **Decision:** Removed the `taxPercent` and `discountPercent` arguments from the method call to align with the `PaymentSheetListener` interface definition. This resolves the critical bug that would have caused a crash when finalizing a purchase.
- **2025-12-04:**
    - **Fixed:** Bottom sheet now returns the newly created customer directly to `CreateSaleActivity` without leaving the screen.
    - **Decision:** Preserve legacy `RETURN_ON_ADD` behavior for `CustomerActivity` to maintain backward compatibility.
- **2025-12-04:**
    - **Fix:** Resolved crash in Customer/Supplier activities due to XML typo (`ic_editl` -> `ic_edit`).
- **Fix:** Improved navigation safety in `HomeFragment` using `View.getContext()`.
- **Fix:** Fixed `NullPointerException` when loading Customers/Suppliers if Firestore timestamps are missing (added null checks in models).
- **2025-12-15:**
    - **Fixed:** PaymentSheet resizes and scrolls above the keyboard; IME Next moves focus through fields into Notes; Notes remains visible and scrollable.
    - **Decision:** Standardize form sheets on ADJUST_RESIZE + NestedScrollView + explicit IME focus chain to ensure consistent keyboard behavior.
    - **Decision:** All TextInputEditText fields must use a minimum 250–300ms debounce for afterTextChanged handlers to prevent race conditions and UI-triggered calculation loops.
    - **Rule:** Centralize this via a reusable DebouncedTextWatcher utility and apply it to PaymentSheet and other forms when touched next.


---
## 4. Architectural Records
> A log of significant implementation sprints and architectural decisions.

### Security & Validation Improvements Summary
This document outlines all the security and validation improvements implemented to address the identified issues in the inventory management application.

#### 1. Input Validation & Sanitization

##### InputValidator Utility Class
- **Email Validation**: Uses Android's built-in Patterns.EMAIL_ADDRESS for robust email validation
- **Phone Number Validation**: Validates common phone number formats
- **Name Validation**: Validates names with letters, spaces, hyphens, and apostrophes (2-50 characters)
- **Price/Quantity Validation**: Ensures positive numbers with optional decimals
- **Barcode Validation**: Validates alphanumeric barcodes (1-20 characters)
- **Input Sanitization**: Removes potentially dangerous characters like `<>'\"&;`

##### Implemented in ProductActivity
- Added validation for all product fields (name, brand, category, product code, prices, quantities)
- Added sanitization of all user inputs before saving to database
- Implemented proper error handling with user-friendly messages

#### 2. Role-Based Access Control (RBAC)

##### SecurityManager Utility Class
- **Permission Constants**: Defined comprehensive permission constants for all operations
- **Permission Checking**: Methods to check individual permissions, any of multiple permissions, or all permissions
- **Role Identification**: Methods to identify admin and manager roles
- **Access Validation**: Methods to validate access with automatic error messaging

##### Implemented in ProductActivity
- Added permission checks for product creation/editing
- Added permission checks for product deletion
- Implemented proper error handling with denied access

#### 3. Error Handling Improvements

##### ErrorHandler Utility Class
- **General Error Handling**: Comprehensive error handling with logging and user feedback
- **Firestore-Specific Handling**: Specialized handling for Firestore errors with appropriate user messages
- **Validation Error Handling**: Proper validation error messages for users
- **Network Error Handling**: Handling for network errors with fallback options
- **Security Event Logging**: Logging for security-related events
- **Data Consistency Handling**: Handling for data conflicts and consistency issues

##### Implemented in ProductActivity
- Replaced basic Toast messages with proper error handling using ErrorHandler
- Added specific error handling for Firestore operations
- Added proper exception handling for image processing

#### 4. Performance Improvements

##### PaginationHelper Utility Class
- **Paginated Data Fetching**: Implements Firestore pagination to handle large datasets
- **Configurable Page Size**: Allows customization of page sizes
- **Has More Detection**: Detects if more data is available

##### ImageCacheHelper Utility Class
- **Memory Caching**: LRU cache for frequently accessed images
- **Disk Caching**: Persistent caching of images to reduce network usage
- **Image Compression**: Automatic compression to reduce memory usage
- **Cache Management**: Methods to clear caches when needed

#### 5. Data Consistency & Audit Trail

##### TransactionManager Utility Class
- **Atomic Transactions**: Ensures data consistency with Firestore transactions
- **Inventory Updates**: Safe inventory updates with audit trails
- **Multiple Product Transactions**: Handles bulk operations safely
- **Financial Transactions**: Proper handling of financial data with audit trails
- **Audit Trail Creation**: Automatic creation of audit entries for all operations

#### 6. Offline Support

##### OfflineSyncHelper Utility Class
- **Network Management**: Methods to enable/disable network for testing
- **Background Sync**: Implementation of background synchronization
- **Offline Detection**: Methods to detect offline status

#### 7. Security Enhancements Applied

##### ProductActivity Security Updates
- **Create/Update Security**: Added WRITE_PRODUCTS permission check
- **Delete Security**: Added DELETE_PRODUCTS permission check
- **Input Validation**: All inputs are now validated and sanitized
- **Error Handling**: Comprehensive error handling for all operations
- **Audit Trail**: All operations now create audit trail entries (via TransactionManager)

##### SecurityManager Integration
- Centralized permission checking
- Role-based access control enforcement
- Security event logging
- Comprehensive permission system

#### 8. Validation Rules Implemented

##### Product Data Validation
- Product name: 2-50 characters, letters/spaces/hyphens/apostrophes
- Product code (barcode): Alphanumeric, 1-20 characters
- Brand/Category: Same as product name format
- Quantities: Non-negative integers
- Prices: Positive numbers with up to 2 decimal places
- Unit: Sanitized string input

##### Validation Flow
1. Input validation at UI level
2. Sanitization of all inputs
3. Business rule validation (e.g., prices vs MRP)
4. Permission validation
5. Database transaction with audit trail

#### 9. Error Handling Strategy

##### User-Facing Error Messages
- Network errors with connection advice
- Permission errors with access denial messages
- Validation errors with specific field information
- Data conflict errors with refresh instructions

##### Logging Strategy
- Security events logged separately
- Detailed error logging for debugging
- Firestore-specific error code handling
- Comprehensive exception handling

#### 10. Performance Optimizations

##### Data Loading
- Pagination for large datasets
- Efficient Firestore queries
- Proper listener management

##### Image Handling
- Memory-efficient image caching
- Automatic compression
- Disk caching to reduce network usage

This comprehensive implementation addresses all the security and validation issues identified while providing a robust foundation for future enhancements.

---

## 5. Data Model Registry
> This section documents the core data models of the application.

### Customer.java
- **Description:** Represents a customer entity.
- **Key Fields:**
    - `documentId`: String (Firestore document ID)
    - `name`: String
    - `contactNumber`: String
    - `address`: String
    - `age`: int
    - `photo`: String (Base64 encoded image)
    - `isActive`: boolean
    - `creationDate`: long (Timestamp of creation)
    - `creditLimit`: double
    - `outstandingBalance`: double
    - `customerType`: String (e.g., "retail", "wholesale")
    - `customerTier`: String (e.g., "gold", "silver")
    - `lastPurchaseDate`: long (Timestamp of the last purchase)
    - `totalPurchaseAmount`: double
    - `purchaseFrequency`: int (Number of purchases)
    - `paymentTerms`: String (e.g., "Net 30", "Net 60")
    - `discountRate`: double
    - `rating`: double (Customer rating)
    - `leadTime`: int (Lead time in days for customer orders)
    - `performanceScore`: double (Customer performance rating)
    - `preferredCustomer`: boolean (Is this a preferred customer?)
    - `contractDetails`: String (Contract terms)
    - `productsPurchased`: String (Categories of products purchased)
    - `bankAccount`: String (Customer's bank account details)
    - `taxId`: String (Tax identification number)
- **Data Passing:** Passed as a `Serializable` object in `Intents`.
- **Relationships:**
    - **Is the mirror opposite of:** `Supplier.java`.
    - **Links to:** `Sale.java` (A `Sale` is always associated with a `Customer`).
- **Usage:**
    - Created and edited in `AddEditCustomerSheet`.
    - Selected in `SelectCustomerActivity`.
    - Used in `CreateSaleActivity` and `CreateSaleReturnActivity`.

### Supplier.java
- **Description:** Represents a supplier of products.
- **Key Fields:**
    - `documentId`: String (Firestore document ID)
    - `name`: String
    - `contactNumber`: String
    - `address`: String
    - `age`: int
    - `photo`: String (Base64 encoded image)
    - `isActive`: boolean
    - `rating`: double (Supplier rating)
    - `paymentTerms`: String (e.g., "Net 30", "Net 60")
    - `leadTime`: int (Lead time in days)
    - `performanceScore`: double (Performance rating)
    - `preferredSupplier`: boolean (Is this a preferred supplier?)
    - `outstandingPayment`: double (Amount owed to the supplier)
    - `contractDetails`: String (Contract terms)
    - `productsSupplied`: String (Categories of products supplied)
    - `lastDeliveryDate`: long (Timestamp of the last delivery)
    - `bankAccount`: String (Supplier's bank account details)
    - `taxId`: String (Tax identification number)
- **Data Passing:** Passed as a `Serializable` object in `Intents`.
- **Relationships:**
    - **Is the mirror opposite of:** `Customer.java`.
    - **Links to:** `Purchase.java` (A `Purchase` is always associated with a `Supplier`).
- **Usage:**
    - Created and edited in `AddEditSupplierSheet`.
    - Selected in `SelectSupplierActivity`.
    - Used in `CreatePurchaseActivity` and `CreatePurchaseReturnActivity`.
    - **Contains:** `PurchaseItem.java` objects.

### Role Definitions
> As defined in `SecurityManager.java` and queried in Cloud Functions.

- **Admin / Manager:**
    - **Identified by:** `permissions.can_manage_employees.granted == true`
    - **Privileges:** Full access, receives all admin notifications.
    - **Note:** `designation` field is free-text and commonly contains "Admin" or "Manager", but it is NOT the source of truth for logic.

### Sale.java
- **Description:** Represents a completed sales transaction.
- **Key Fields:**
    - `documentId`: String (Firestore document ID)
    - `customerId`: String (Reference to Customer)
    - `items`: List<SaleItem> (Detailed line items)
    - `totalAmount`: double (Final price after tax/discount)
    - `totalCost`: Double (Boxed for null-safety; pre-calculated sum of SaleItem.costPrice)
    - `totalProfit`: Double (Boxed for null-safety; totalAmount - totalCost)
    - `saleDate`: Timestamp
- **Notes:** Boxed `Double` fields allow Firestore to safely handle legacy documents missing these fields without triggering NullPointerExceptions.
- **Relationships:**
    - **Links to:** `Customer.java` via `customerId`.
    - **Contains:** `SaleItem.java` objects.

### Purchase.java
- **Description:** Represents an inventory replenishment transaction from a supplier.
- **Key Fields:**
    - `documentId`: String
    - `supplierId`: String (Reference to Supplier)
    - `items`: List<PurchaseItem>
    - `totalAmount`: double
    - `purchaseDate`: Timestamp
    - `purchaseStatus`: String (pending, approved, received)
- **Relationships:**
    - **Links to:** `Supplier.java` via `supplierId`.
    - **Contains:** `PurchaseItem.java` objects.

---

## 6. Current Known Bugs & Gaps (Phase 1 Tracking)
> Active tracker for issues identified during Phase 1 audits.

### [CRITICAL] MainRepository Pagination Failures
- [x] **Description:** `loadNextPageSales()` and `loadNextPagePurchases()` order by a field named `"timestamp"`. However, the corresponding models use `saleDate` and `purchaseDate`.
- [x] **Impact:** Pagination on the main dashboard is likely broken or returning incorrect sequences.
- [x] **Fix:** Align field names in `MainRepository.java`

### [CRITICAL] Return Flow inflates Profit Reports
- [x] **Description:** `CreateSaleReturnActivity` updates item quantities and inventory, but does **not** recalculate or update the `totalCost` and `totalProfit` fields on the original `Sale` document.
- [x] **Impact:** Reporting will show inflated profitability because it doesn't account for reduced profit from returned goods.
- [x] **Fix:** Update `Sale.totalCost` and `Sale.totalProfit` during the return transaction.

### [PERFORMANCE] Missing Pagination for Expenses
- [x] **Description:** `fetchExpenses()` in `MainRepository` loads the entire collection via `.get()`.
- [x] **Impact:** Memory and cost issues as data grows.
- [x] **Fix:** Implement paginated loading for Expenses.

### [PERFORMANCE] Bulk Loading in Product Selection
- [x] **Description:** `SelectProductActivity` loads the entire `products` collection via `.get()` without pagination.
- [x] **Impact:** Significant UI lag and memory pressure as the product catalog grows.
- [x] **Fix:** Implement paginated loading using `PaginationHelper`.

### [DESIGN] Manual Math in Returns
- [x] **Description:** `CreateSaleReturnActivity` uses manual `double` multiplication for refund totals instead of the `FinancialCalculator` utility.
- [x] **Risk:** Floating point precision errors in financial data.
- [x] **Fix:** Integrate `FinancialCalculator` into Return activities.

### [DESIGN] Expense Model lacks DocumentId
- [x] **Description:** `Expense.java` does not have a `@DocumentId` field or setter.
- [x] **Impact:** Difficulty in editing or deleting specific expense records.
- [x] **Fix:** Update model to include `documentId`.

### [ARCH] Double Source of Truth in Sale/Purchase Flow
- [x] **Description:** `CreateSaleActivity` and `CreatePurchaseActivity` maintain local `List<ProductSelection>` instances while the ViewModels track them as well.
- [x] **Risk:** Potential for UI desync if adapters or direct list manipulations bypass the ViewModel logic.
- [x] **Fix:** Ensure the Adapter only operates on data provided by the ViewModel and avoid local list copies in the Activity.

### [ARCH] Missing ViewModel in SelectProductActivity
- [x] **Description:** This activity manages its own data state and Firestore listeners directly.
- [x] **Risk:** Potential memory leaks (confirmed in thread executor) and lack of lifecycle-aware state management.
- [x] **Fix:** Refactor to use a `HiltViewModel` and Repository pattern.

### [PERFORMANCE] Missing Refresh/Reset Logic
- [x] **Description:** `MainRepository` and `MainViewModel` lack an explicit `refresh()` method that resets pagination state. `preloadMainData()` guards against reloading if data exists.
- [x] **Impact:** Users cannot pull-to-refresh effectively to see new data if the initial page is already loaded.
- [x] **Fix:** Implement `resetPagination()` in Repository.
