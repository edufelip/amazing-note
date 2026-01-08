import { onRequest } from "firebase-functions/v2/https";
import { setGlobalOptions } from "firebase-functions/v2";
import * as admin from "firebase-admin";
import type { DocumentData, DocumentReference, QueryDocumentSnapshot } from "firebase-admin/firestore";

const storageBucket =
  process.env.STORAGE_BUCKET ??
  process.env.FIREBASE_STORAGE_BUCKET ??
  "amazing-note-7eb16.firebasestorage.app";
admin.initializeApp({ storageBucket });

setGlobalOptions({ region: "us-central1" });

export const deleteAccount = onRequest({ cors: true }, async (req, res) => {
  if (req.method !== "POST") {
    res.status(405).json({ error: "Method not allowed" });
    return;
  }

  const authHeader = req.header("Authorization") ?? "";
  const tokenMatch = authHeader.match(/^Bearer (.+)$/i);
  if (!tokenMatch) {
    res.status(401).json({ error: "Missing auth token" });
    return;
  }

  try {
    const decoded = await admin.auth().verifyIdToken(tokenMatch[1]);
    const uid = decoded.uid;

    const db = admin.firestore();
    const userRef = db.collection("users").doc(uid);
    const [notesSnap, foldersSnap, userDoc] = await Promise.all([
      userRef.collection("notes").get(),
      userRef.collection("folders").get(),
      userRef.get(),
    ]);

    const storagePaths = collectStoragePaths(notesSnap.docs, userDoc.data() ?? null);

    const errors: string[] = [];
    await safeRun(async () => deleteStorageFiles(storagePaths), errors, "storage");
    await safeRun(async () => deleteUserDocuments(userRef, notesSnap.docs, foldersSnap.docs), errors, "firestore");
    await safeRun(async () => deleteAuthUser(uid), errors, "auth");

    if (errors.length > 0) {
      res.status(500).json({ error: "Account deletion failed", details: errors });
      return;
    }

    res.status(200).json({ status: "ok" });
  } catch (error) {
    console.error("deleteAccount failed", error);
    res.status(401).json({ error: "Invalid auth token" });
  }
});

async function deleteUserDocuments(
  userRef: DocumentReference<DocumentData>,
  notes: QueryDocumentSnapshot<DocumentData>[],
  folders: QueryDocumentSnapshot<DocumentData>[],
) {
  const writer = admin.firestore().bulkWriter();
  notes.forEach((doc) => writer.delete(doc.ref));
  folders.forEach((doc) => writer.delete(doc.ref));
  writer.delete(userRef);
  await writer.close();
}

function collectStoragePaths(
  notes: QueryDocumentSnapshot<DocumentData>[],
  userData: DocumentData | null,
): Set<string> {
  const paths = new Set<string>();
  notes.forEach((doc) => addStoragePathsFromNote(paths, doc.data()));
  const legacyNotes = userData?.notes;
  if (legacyNotes && typeof legacyNotes === "object") {
    Object.values(legacyNotes as Record<string, unknown>).forEach((note) => {
      if (note && typeof note === "object") {
        addStoragePathsFromNote(paths, note as Record<string, unknown>);
      }
    });
  }
  return paths;
}

function addStoragePathsFromNote(paths: Set<string>, data: Record<string, unknown>) {
  const attachments = parseJsonArray(data.attachments);
  attachments.forEach((attachment) => {
    addIfStoragePath(paths, attachment?.storagePath);
    addIfStoragePath(paths, attachment?.thumbnailStoragePath);
    addIfStoragePath(paths, attachment?.downloadUrl);
    addIfStoragePath(paths, attachment?.thumbnailUrl);
  });

  const content = parseJsonObject(data.contentJson);
  const blocks = Array.isArray(content?.blocks) ? content?.blocks : [];
  blocks.forEach((block: Record<string, unknown>) => {
    if (block?.type === "image") {
      addIfStoragePath(paths, block.storagePath);
      addIfStoragePath(paths, block.thumbnailStoragePath);
    }
  });
}

function parseJsonArray(raw: unknown): Array<Record<string, unknown>> {
  if (!raw) return [];
  if (Array.isArray(raw)) return raw as Array<Record<string, unknown>>;
  if (typeof raw !== "string") return [];
  try {
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function parseJsonObject(raw: unknown): Record<string, unknown> | null {
  if (!raw) return null;
  if (typeof raw === "object") return raw as Record<string, unknown>;
  if (typeof raw !== "string") return null;
  try {
    const parsed = JSON.parse(raw);
    return typeof parsed === "object" && parsed !== null ? (parsed as Record<string, unknown>) : null;
  } catch {
    return null;
  }
}

function addIfStoragePath(paths: Set<string>, raw: unknown) {
  if (typeof raw !== "string") return;
  const trimmed = raw.trim();
  if (!trimmed) return;
  if (trimmed.includes("://")) return;
  if (trimmed.toLowerCase().startsWith("file:")) return;
  if (trimmed.toLowerCase().startsWith("content:")) return;
  paths.add(trimmed);
}

async function deleteStorageFiles(paths: Set<string>) {
  if (paths.size === 0) return;
  const bucket = admin.storage().bucket();
  const errors: string[] = [];
  for (const path of paths) {
    try {
      await bucket.file(path).delete({ ignoreNotFound: true });
    } catch (error) {
      console.warn("Failed to delete storage", path, error);
      errors.push(path);
    }
  }
  if (errors.length > 0) {
    throw new Error(`Failed to delete ${errors.length} storage objects.`);
  }
}

async function deleteAuthUser(uid: string) {
  try {
    await admin.auth().deleteUser(uid);
  } catch (error: any) {
    if (error?.code === "auth/user-not-found") return;
    throw error;
  }
}

async function safeRun(task: () => Promise<void>, errors: string[], label: string) {
  try {
    await task();
  } catch (error) {
    console.error("Account deletion step failed", label, error);
    errors.push(label);
  }
}
