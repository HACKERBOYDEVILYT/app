# REWARDLY — Production-Ready Rewards & Earnings Platform

**Rewardly** is a production-quality rewards platform where users earn verified platform rewards from legitimate activities including supported rewarded advertisements (AdMob SSV mediation), accredited market research surveys (TheoremReach, Dynata), partner milestone offers (TapJoy), daily streaks with multipliers, and referral network incentives.

The platform is designed around strict policy compliance, anti-fraud telemetry, cryptographic server-side validation, and a double-entry ledger wallet.

---

## 1. Core Architecture & Highlights

- **Legitimate Verification Layer (`AdProviderAdapter`)**:
  - Implements real-time cryptographic validation tokens (HMAC-SHA256 signatures).
  - Rewards are never credited from arbitrary frontend button clicks or client-side mutations.
  - Abstraction architecture enables seamless transition from `MockRewardProvider` (used for local sandbox verification) to live networks (Google AdMob SSV, Unity Ads, AppLovin MAX).

- **Double-Entry Ledger & Idempotency**:
  - Room Local Database persistence with atomic wallet balances and immutable transaction records.
  - Every transaction includes a unique `idempotencyKey` preventing replay attacks or double-credits.
  - Auditable receipt modal displaying transaction hash, timestamp, and verification reference.

- **Fraud & Abuse Protection Engine (`FraudRiskEngine`)**:
  - Real-time risk scoring (`0–29 LOW`, `30–59 MEDIUM`, `60–79 HIGH`, `80–100 CRITICAL`).
  - Automated detection of impossible completion frequencies, duplicate event replays, daily quota circumvention, and suspicious withdrawal amounts.

- **Role-Based Access Control (RBAC)**:
  - Supports `USER`, `ADMIN`, `SUPER_ADMIN`, `SUPPORT`, `FINANCE`, and `ANALYST` roles.
  - Comprehensive Admin Console with live system health monitors, user suspension/restoration, auditable ledger adjustments, withdrawal queue approvals/rejections, and community campaign management.

- **Interactive User Experience (Material 3 & Jetpack Compose)**:
  - **Home**: Real-time spendable/pending balance card, daily streak claim widget with multipliers (1.0x to 2.0x), quick earn shortcuts, and recent ledger entries.
  - **Earn**: Categorized activities (Ads, Surveys, Offers, Campaigns) with live interactive countdown player, server callback simulation, and cryptographic proof verification.
  - **Wallet**: Financial ledger overview, filterable history, and verifiable cryptographic receipts.
  - **Withdraw**: Multi-method payouts (PayPal, Crypto USDC on Base/Polygon, Direct ACH Bank Transfer, Amazon Gift Cards) with fee calculations and compliance review timeline.
  - **Referrals**: Unique invite codes, 1-tap sharing, milestone progress tracking ($2.00 per verified friend).
  - **Helpdesk & Support**: Real-time ticket lifecycle (`OPEN`, `IN_PROGRESS`, `WAITING_USER`, `RESOLVED`) with message threads.

---

## 2. Seed Accounts & Perspectives

For testing and demonstration, two pre-configured profiles are available via the top-bar role switcher or profile tab:
1. **Alex Johnson (Verified Earner)**:
   - Username: `@alex_earner`
   - Balance: `$24.50 USD` (+$5.00 pending)
   - Lifetime Earned: `$42.00 USD`
   - Referral Code: `ALEX777`
   - Risk Score: `15/100 (LOW RISK)`

2. **Sarah Chen (Super Admin)**:
   - Username: `@sarah_admin`
   - Role: `SUPER_ADMIN`
   - Features: Access to user administration, withdrawal review queue, campaign creation, and audit trails.

---

## 3. Project Structure

```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── DatabaseInitializer.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   └── entity/
│   │   │   │   │   └── repository/
│   │   │   │   │       └── RewardlyRepository.kt
│   │   │   │   ├── domain/
│   │   │   │   │   └── engine/
│   │   │   │   │       ├── AdProviderAdapter.kt
│   │   │   │   │       ├── MockRewardProvider.kt
│   │   │   │   │       └── FraudRiskEngine.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── admin/
│   │   │   │   │   │   ├── earn/
│   │   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── notifications/
│   │   │   │   │   │   ├── profile/
│   │   │   │   │   │   ├── referrals/
│   │   │   │   │   │   ├── support/
│   │   │   │   │   │   ├── wallet/
│   │   │   │   │   │   └── withdraw/
│   │   │   │   │   ├── theme/
│   │   │   │   │   └── viewmodel/
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/
│   │   └── test/java/com/example/
│   │       └── ExampleRobolectricTest.kt
│   └── build.gradle.kts
└── settings.gradle.kts
```

---

## 4. Building the Release APK & Device Installation

### Quick Command-Line Build (Single Script)
We provide an automated script that checks or generates the release keystore, signs the build, and outputs the installable APK:

**On Linux / macOS / Terminal:**
```bash
chmod +x ./build-release-apk.sh
./build-release-apk.sh
```

**On Windows:**
```cmd
build-release-apk.bat
```

### Manual Command-Line Build Steps:
If you prefer running commands manually:

1. **Generate the Release Signing Keystore (one-time setup)**:
   ```bash
   keytool -genkeypair -v \
     -keystore my-upload-key.jks \
     -alias upload \
     -keyalg RSA \
     -keysize 2048 \
     -validity 10000 \
     -storepass rewardlypass123 \
     -keypass rewardlypass123 \
     -dname "CN=Rewardly, OU=Mobile, O=Rewardly, L=San Francisco, ST=CA, C=US"
   ```

2. **Assemble the Release APK**:
   ```bash
   export KEYSTORE_PATH="$(pwd)/my-upload-key.jks"
   export STORE_PASSWORD="rewardlypass123"
   export KEY_PASSWORD="rewardlypass123"

   ./gradlew :app:assembleRelease
   ```
   *(Or with system gradle: `gradle :app:assembleRelease`)*

3. **Locate the Output APK**:
   The generated release APK is located at:
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

---

## 5. Installing the APK on an Android Phone

### Method A: Install via USB & ADB (Recommended for Developers)
1. Enable **Developer Options** on your Android device:
   - Go to **Settings > About Phone > Tap 'Build Number' 7 times**.
2. Turn on **USB Debugging** in **Settings > Developer Options**.
3. Connect your phone to your PC via USB cable.
4. Run:
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```
5. Rewardly will launch immediately or appear in your app drawer.

### Method B: Direct File Transfer (No PC/ADB required)
1. Send or copy `app-release.apk` to your phone via:
   - Google Drive, OneDrive, or Dropbox
   - Messaging app (WhatsApp, Telegram) to yourself
   - Direct USB file transfer
2. On your Android phone, open the **Files** or **Downloads** app.
3. Tap on `app-release.apk`.
4. If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings** and toggle **Allow from this source**.
5. Tap **Install** and then **Open**.

---

## 6. Third-Party Provider Integration Guide (Production Mode)

To deploy with live third-party ad networks:
1. **Google AdMob SSV**: Configure AdMob Rewarded Video Unit ID and point the Server-Side Verification (SSV) callback URL to your backend endpoint with your public key verification.
2. **TheoremReach / Dynata**: Plug in your Publisher API Key and configure postback URLs.
3. **Payout Gateways**: Connect PayPal Payouts SDK / Stripe Treasury / Circle USDC API for automated dispatching upon admin approval.
