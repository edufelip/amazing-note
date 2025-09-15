package com.edufelip.shared.cloud

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseFirestore.FIRCollectionReference
import cocoapods.FirebaseFirestore.FIRFirestore
import cocoapods.FirebaseFirestore.FIRListenerRegistration
import cocoapods.FirebaseFirestore.FIRQuerySnapshot
import cocoapods.FirebaseFirestore.FIRFirestoreSettings
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import kotlinx.cinterop.objcPtr
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberCompanion
import platform.Foundation.NSObject
import cocoapods.FirebaseFirestore.FIRFieldValue
import cocoapods.FirebaseFirestore.FIRSetOptions
import cocoapods.FirebaseFirestore.FIRTimestamp
import platform.Foundation.NSDate

private fun col(uid: String): FIRCollectionReference =
    FIRFirestore.firestore().collectionWithPath("users")
        .documentWithPath(uid)
        .collectionWithPath("notes")

private fun Note.toMap(): Map<Any?, *> = mapOf<Any?, Any?>(
    "id" to id.toLong(),
    "title" to title,
    "priority" to when (priority) {
        Priority.HIGH -> 0
        Priority.MEDIUM -> 1
        Priority.LOW -> 2
    },
    "description" to description,
    "deleted" to deleted,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
)

private fun Any?.toLongOrZero(): Long = when (this) {
    is NSNumber -> this.longLongValue
    is kotlin.Number -> this.toLong()
    is FIRTimestamp -> ((this.dateValue().timeIntervalSince1970) * 1000.0).toLong()
    else -> 0L
}

private fun Any?.toIntOrNullCompat(): Int? = when (this) {
    is NSNumber -> this.intValue
    is kotlin.Number -> this.toInt()
    is String -> this.toIntOrNull()
    else -> null
}

private fun Any?.toStringOrEmpty(): String = (this as? String) ?: ""
private fun Any?.toBool(): Boolean = (this as? Boolean) ?: false

private fun mapToNote(id: String, data: Map<Any?, *>): Note? {
    val pid = data["priority"].toIntOrNullCompat() ?: 1
    val title = data["title"] as? String ?: return null
    return Note(
        id = (data["id"].toIntOrNullCompat() ?: id.toIntOrNull() ?: -1),
        title = title,
        priority = when (pid) {
            0 -> Priority.HIGH
            1 -> Priority.MEDIUM
            else -> Priority.LOW
        },
        description = data["description"].toStringOrEmpty(),
        deleted = data["deleted"].toBool(),
        createdAt = data["createdAt"].toLongOrZero(),
        updatedAt = data["updatedAt"].toLongOrZero(),
    )
}

actual fun provideCloudNotesDataSource(): CloudNotesDataSource {
    val db = FIRFirestore.firestore()
    val settings = db.settings ?: FIRFirestoreSettings()
    settings.persistenceEnabled = true
    db.settings = settings
    return object : CloudNotesDataSource {
    override fun observe(uid: String): Flow<List<Note>> = callbackFlow {
        val registration: FIRListenerRegistration = col(uid).addSnapshotListener { snapshot: FIRQuerySnapshot?, error: NSError? ->
            if (snapshot != null) {
                val list = snapshot.documents().mapNotNull { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val data = doc.data() as? Map<Any?, *> ?: emptyMap<Any?, Any?>()
                    mapToNote(doc.documentID, data)
                }.sortedBy { it.updatedAt }
                trySend(list)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getAll(uid: String): List<Note> =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            col(uid).getDocumentsWithCompletion { snapshot, error ->
                if (snapshot != null) {
                    val list = snapshot.documents().mapNotNull { doc ->
                        @Suppress("UNCHECKED_CAST")
                        val data = doc.data() as? Map<Any?, *> ?: emptyMap<Any?, Any?>()
                        mapToNote(doc.documentID, data)
                    }.sortedBy { it.updatedAt }
                    cont.resume(list) {}
                } else {
                    cont.resume(emptyList()) {}
                }
            }
        }

    override suspend fun upsert(uid: String, note: Note) {
        kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
            val data: MutableMap<Any?, Any?> = mutableMapOf(
                "id" to note.id.toLong(),
                "title" to note.title,
                "description" to note.description,
                "deleted" to note.deleted,
                "priority" to when (note.priority) {
                    Priority.HIGH -> 0
                    Priority.MEDIUM -> 1
                    Priority.LOW -> 2
                },
                "updatedAt" to FIRFieldValue.fieldValueForServerTimestamp(),
            )
            data["createdAt"] = if (note.createdAt == 0L) {
                FIRFieldValue.fieldValueForServerTimestamp()
            } else {
                // NSDate will be stored as Firestore Timestamp on iOS
                NSDate.dateWithTimeIntervalSince1970(note.createdAt.toDouble() / 1000.0)
            }
            val opts = FIRSetOptions.setOptionsWithMerge(true)
            col(uid).documentWithPath(note.id.toString()).setData(data, opts) { error ->
                if (error != null) cont.resumeWith(Result.failure(Throwable(error.localizedDescription ?: "error")))
                else cont.resume(Unit) {}
            }
        }
    }

    override suspend fun delete(uid: String, id: Int) {
        kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
            col(uid).documentWithPath(id.toString()).deleteDocumentWithCompletion { error ->
                if (error != null) cont.resumeWith(Result.failure(Throwable(error.localizedDescription ?: "error")))
                else cont.resume(Unit) {}
            }
        }
    }
    }
}

actual fun provideCurrentUserProvider(): CurrentUserProvider = object : CurrentUserProvider {
    override val uid: Flow<String?> = MutableStateFlow(FIRAuth.auth()?.currentUser?.uid).also { state ->
        var previous: String? = state.value
        FIRAuth.auth()?.addAuthStateDidChangeListener { _, user ->
            val current = user?.uid
            val prev = previous
            previous = current
            state.value = current
            // When user logs out or switches accounts, clear Firestore's offline cache
            if (prev != null && prev != current) {
                val db = FIRFirestore.firestore()
                db.terminateWithCompletion { _ ->
                    db.clearPersistenceWithCompletion { _ ->
                        // no-op
                    }
                }
            }
        }
    }
}
