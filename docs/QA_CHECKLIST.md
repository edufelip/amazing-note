# Amazing Note — Quick Regression Checklist

Use this abbreviated checklist for fast validation of core flows, sync, and logout/account-switch data clearing.

## Auth
- [ ] Email/password login success navigates to Home.
- [ ] Email/password invalid credentials show error, no navigation.
- [ ] Google Sign-In success navigates to Home.
- [ ] Google Sign-In canceled shows message, no navigation.
 - [ ] After login, Home shows a brief loading bar, then remote notes appear without relaunch.

## Notes CRUD (Online)
- [ ] Create note → appears in list within seconds.
- [ ] Firestore doc created under `users/{uid}/notes/{id}` with fields: id, title, description, deleted, createdAt (Timestamp), updatedAt (Timestamp).
- [ ] Update note → UI reflects change; Firestore updated.
- [ ] Move to Trash → leaves Home; visible in Trash; Firestore `deleted=true`.
- [ ] Restore from Trash → back to Home; Firestore `deleted=false`.

## Remote → Local Sync
- [ ] Add a note on Web → appears in app within seconds.
- [ ] Edit on Web → app updates title/description shortly.
- [ ] Delete/mark deleted on Web → app reflects removal/trash.

## Offline Behavior
- [ ] Create note while offline → visible locally.
- [ ] Reconnect → note uploads to Firestore within seconds.
- [ ] Update/Delete offline → local change immediate; on reconnect, Firestore reflects.

## Logout / Account Switch (Data Clearing)
- [ ] Logged in as A, create notes → Logout → lists empty immediately.
- [ ] Relaunch app while offline (still logged out) → no notes shown (Firestore cache cleared).
- [ ] Login as B → only B’s notes visible; no A notes leak.
- [ ] Switch back to A → only A’s notes visible; no B notes leak.

## One-Time Migration (First Login)
- [ ] Create local notes while logged out → Login as A → notes upload once to Firestore with correct Timestamp fields.
- [ ] Logout and login as A again → no duplicate uploads.

## UI & Preferences
- [ ] Empty state shows correct title/hint when no notes.
- [ ] Search works; “no matches” message appears appropriately.
- [ ] Trash screen actions (restore/delete if available) operate correctly.
- [ ] Dark theme toggle persists across app restarts.

## Stability & Performance
- [ ] Scroll smoothly with 50–100 notes; no crashes.
- [ ] Rapid add/update/delete actions do not crash; data consistent after a short delay.
- [ ] Background the app, apply remote change on Web, return → app reflects change quickly.

## Advanced (optional)
- [ ] With no net content changes, repeated remote snapshots do not repeatedly merge (throttling after 3 runs).
- [ ] Equal content in different order is treated as equal (no redundant merges).
