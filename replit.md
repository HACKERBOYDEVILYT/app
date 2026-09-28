# REWARDLY Deployment & Runtime Guide

## 1. Environment & Build Setup
Rewardly is built on modern Android with Jetpack Compose, Kotlin 2.2, Material 3, and Room 2.7.

- **To build the app**:
  ```bash
  gradle assembleDebug
  ```

- **To run unit & Robolectric tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```

## 2. Configuration & Secrets
Sensitive API keys and provider tokens should be added via the AI Studio Secrets panel or environment variables:
- `ADMOB_APP_ID`: AdMob Application ID (when in production)
- `ADMOB_SSV_KEY`: Server-Side Verification key
- `SUPABASE_URL` / `SUPABASE_ANON_KEY`: If configuring remote database mirror

## 3. Switching User & Admin Perspectives
A 1-tap role switcher is located in the top bar and the profile tab:
- **Alex Johnson (Earner)**: Tests normal earning, streaks, referrals, and withdrawal submissions.
- **Sarah Chen (Super Admin)**: Tests reviewing/approving payouts, managing activities, reviewing fraud alerts, and managing campaigns.
