# Amazing Note — QA Test Plan and Test Cases

This file outlines a comprehensive, manual QA session to validate core features, data consistency, offline/online sync, and recent changes to clear local data and Firestore offline cache on logout or account switch.

## 1) Scope & Objectives
- Validate that notes are created/edited/deleted locally and synced to Firestore correctly.
- Validate two-way sync: remote changes appear locally while the app is running and after relaunch.
- Validate logout/account-switch behavior: local SQLDelight DB is wiped and Firestore offline cache is cleared; no cross-account leakage.
- Validate offline behavior: actions queue locally and sync once online.
- Validate one-time migration of pre-login local notes to cloud after first login.
- Validate UI states: empty lists, filters, trash, error states, and preferences persistence.
- Validate immediate post-login sync population and the Home loading indicator behavior.
- Validate Firestore Timestamp handling for createdAt/updatedAt on cloud.

## 2) Test Environment
- Platforms: Android (primary); iOS (parity checks if available).
- Build types: Debug.
- Network: Real internet with ability to toggle airplane mode/offline.
- Firebase: Project configured; Firestore and Auth enabled; Google Sign-In configured.
- Test accounts:
  - Account A (userA@example.com)
  - Account B (userB@example.com)
- Devices/Emulators: 1 Android device/emulator (SDK 26+), optionally 1 iOS device/simulator.
 - Firestore Rules: Ensure per-user read/write access. Example:
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId}/notes/{noteId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
     }
   }

## 3) Pre-Test Setup
- Clean install the app (clear app data/uninstall first).
- Ensure both test accounts exist in Firebase Auth (email/password) and Google Sign-In as applicable.
- If testing iOS, confirm Firebase iOS config and sign-in works.

## 4) Conventions
- “Local DB” = SQLDelight `note` table.
- “Cloud” = Firestore collection `users/{uid}/notes`.
- “Immediate” = visible in the UI within 1–5 seconds given an active connection.

## 5) Test Cases

### 5.1 Authentication
- Email/password login success
  - Steps: Open app → Login screen → Enter valid A credentials → Submit.
  - Expected: Navigates to Home, “Your Notes” visible, no error.
- Email/password login failure
  - Steps: Enter invalid credentials → Submit.
  - Expected: Error message; remains on login; no navigation.
- Google Sign-In success
  - Steps: Tap Google sign-in; pick account.
  - Expected: Returns success callback; navigates to Home.
- Google Sign-In canceled/failure
  - Steps: Cancel account selection or revoke consent.
  - Expected: Error/canceled message surfaced; remain on login.

### 5.2 Create Note → Push to Firestore
- Create note while online
  - Steps: From Home → Add → Fill title/description → Save.
  - Expected: New note appears in list; within seconds, Firestore document created under `users/{A}/notes/{id}` with matching fields; fields include `id` (int), `title`, `description`, `deleted` (bool), `createdAt` (Timestamp), `updatedAt` (Timestamp; server-side).
- Create multiple notes rapidly (3–5)
  - Expected: All appear locally and in Firestore with correct ordering (sorted by updatedAt desc in UI).

### 5.3 Update & Delete
- Update an existing note
  - Steps: Open note → change fields → Save.
  - Expected: Local update; Firestore document updated; `updatedAt` changes.
- Move to Trash (soft delete)
  - Steps: Swipe/dismiss or tap delete → Confirm.
  - Expected: Leaves Home; appears in Trash; Firestore note `deleted=true`.
- Restore from Trash
  - Steps: From Trash → Restore.
  - Expected: Returns to Home; Firestore `deleted=false`.
- Permanent delete (if supported in UI)
  - Steps: Trigger permanent delete (if available) or verify no permanent delete.
  - Expected: If permanent delete exists: doc removed from Firestore and local DB.

### 5.4 Remote → Local Sync (Realtime)
- New note from Web reflects in app
  - Steps: With app open and account A logged in → Create a note in Firestore Web (or companion web app) under `users/{A}/notes`.
  - Expected: Within seconds, note appears in the app’s Home; Home loading indicator is not required here (only for initial post-login fetch).
- Update note from Web
  - Steps: Modify title/description remotely.
  - Expected: App updates field values shortly after; ordering updates by `updatedAt`.
- Delete from Web
  - Steps: Set `deleted=true` remotely or delete the doc.
  - Expected: App moves note to Trash or removes it accordingly.

### 5.5 Conflict Resolution (UpdatedAt)
- Local newer wins
  - Steps: Create/edit note locally; immediately update same doc on Web with older timestamp field; then trigger a sync (wait or perform another local save).
  - Expected: Local content remains; app pushes local state to Firestore.
- Remote newer wins
  - Steps: Update note on Web; wait 5–10 seconds; then open app and edit the same note but do not save yet; observe.
  - Expected: App first pulls the newer remote version; local UI reflects latest remote on fresh load; subsequent local save pushes new content with newer `updatedAt`.

### 5.6 Offline Scenarios
- Create note while offline
  - Steps: Turn on airplane mode → Add note → Save.
  - Expected: Note appears locally; Firestore write is queued.
  - Then: Turn online.
  - Expected: Note is uploaded to Firestore automatically within seconds.
- Update/Delete while offline
  - Steps: Edit or soft-delete notes offline.
  - Expected: Local state changes immediately; upon reconnect, Firestore reflects changes.
- Remote changes during offline
  - Steps: While device is offline, add/edit notes from Web;
  - Then: Turn online.
  - Expected: App fetches remote changes and merges into local promptly.

### 5.7 Logout Behavior — Local DB Wipe
- Logout clears local SQLDelight DB
  - Steps: Login as A → Create 1–2 notes → Logout from drawer or relevant UI.
  - Expected: After logout, Home/notes views show empty state immediately; local DB table `note` contains 0 rows (if verifiable via logs/dev build);
    re-login as A → notes load from cloud correctly.
- Account switch
  - Steps: Login as A → create notes → switch to B via logout → login as B.
  - Expected: No A notes visible under B; local DB is cleared on switch; only B’s notes show. Switching back to A shows only A’s notes.

### 5.8 Logout Behavior — Firestore Offline Cache Clear
- No stale cache after logout
  - Android:
    - Steps: Login as A → ensure notes visible → Logout → force-kill app → Relaunch while offline.
    - Expected: App shows no A notes (empty), not even from Firestore offline cache.
  - iOS:
    - Steps: Same flow as Android.
    - Expected: No A notes are shown while logged out, even offline.
- Account switch with cache
  - Steps: Login as A; observe notes → Logout; login as B; go offline; force-kill and relaunch.
  - Expected: No residual A notes shown; only B’s cached data (if any post-login) appears; on fresh login to B, cache starts clean.

### 5.9 One-Time Local → Cloud Migration on First Login
- Migrate once per uid
  - Steps: Start logged out; create 2–3 local notes → Login as A.
  - Expected: On first login, local notes are uploaded to Firestore and marked migrated; notes appear on Web with the correct Timestamps for `createdAt`/`updatedAt`.
  - Then: Logout → Login as A again.
  - Expected: No duplicate uploads; Firestore remains with single copies.
 - Immediate population after login
   - Steps: After the first login, observe Home.
   - Expected: A brief LinearProgressIndicator appears at top; remote notes populate immediately without relaunch; indicator hides after first sync completes.

### 5.10 UI States & Preferences
- Empty state (never created notes)
  - Steps: Fresh login with no notes in cloud.
  - Expected: Shows empty state title and hint.
- Search
  - Steps: Create notes with different text; perform searches yielding matches and no matches.
  - Expected: Correct filtering; shows “no notes match …” text when relevant.
- Trash screen UI
  - Steps: Move items to Trash; verify listings, restore, and delete actions (if permanent delete exists).
  - Expected: UI updates correctly after each action.
- Dark theme preference
  - Steps: Toggle dark mode in each screen that exposes it; kill/relaunch app.
  - Expected: Preference persists.

### 5.11 Lifecycle & Backgrounding
- Background receive
  - Steps: Put app in background; on Web, add/edit a note; return to app.
  - Expected: List updates quickly with remote changes.
- Process recreation
  - Steps: Rotate device; or enable “Don’t keep activities” and navigate around.
  - Expected: State restores without data loss; lists rebind from local DB.

### 5.12 Performance & Stability
- Smooth scrolling with 50–100 notes
  - Steps: Seed cloud with many notes; scroll list.
  - Expected: No jank or crashes.
- Rapid actions
  - Steps: Quickly add/update/delete multiple notes.
  - Expected: No crashes; eventual consistency with cloud.

## 6) Pass/Fail Criteria
- All expected outcomes met; no cross-account data leakage after logout or switch.
- No stale Firestore cached data visible after logout (both Android and iOS).
- Sync consistency: local and cloud match after short delay online.
- Offline actions apply locally and reconcile once online.

## 7) Known Limitations / Notes
- Firestore timestamps (`updatedAt`) are server-assigned on upsert; ordering may briefly change after server writes.
- Some checks (e.g., local DB row count) may require developer logging or a debug build tool.
### 5.13 Sync Throttling & Hash Equality (advanced)
- Repeated remote snapshots
  - Steps: With stable content, cause 3+ consecutive remote snapshot deliveries (e.g., minor metadata-only changes server-side or toggling network).
  - Expected: After 3 consecutive merges, further merges are throttled until an explicit sync or a remote content hash change occurs; no user-visible regressions.
- Equality with different order
  - Steps: Ensure remote/local contain same notes but in different order.
  - Expected: No redundant merges; content-hash equality detects equivalence regardless of order.
