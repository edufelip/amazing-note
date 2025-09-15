package com.edufelip.shared.cloud

import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.Date

private fun col(uid: String): CollectionReference =
    FirebaseFirestore.getInstance().collection("users").document(uid).collection("notes")

private data class Dto(
    val id: Int? = null,
    val title: String? = null,
    val priority: Int? = null,
    val description: String? = null,
    val deleted: Boolean? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
)

private fun Note.toDto(): Map<String, Any?> = mapOf(
    "id" to id,
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

private fun Dto.toNote(docId: String): Note? {
    val pid = priority ?: 1
    return Note(
        id = id ?: docId.toIntOrNull() ?: -1,
        title = title ?: return null,
        priority = when (pid) {
            0 -> Priority.HIGH
            1 -> Priority.MEDIUM
            else -> Priority.LOW
        },
        description = description ?: "",
        deleted = deleted ?: false,
        createdAt = createdAt ?: 0L,
        updatedAt = updatedAt ?: 0L,
    )
}

private fun parseDto(map: Map<String, Any?>): Dto = Dto(
    id = (map["id"] as? Number)?.toInt(),
    title = map["title"] as? String,
    priority = (map["priority"] as? Number)?.toInt(),
    description = map["description"] as? String,
    deleted = map["deleted"] as? Boolean,
    createdAt = map["createdAt"].toMillis(),
    updatedAt = map["updatedAt"].toMillis(),
)

private fun Any?.toMillis(): Long? = when (this) {
    is Timestamp -> this.toDate().time
    is Number -> this.toLong()
    else -> null
}

actual fun provideCloudNotesDataSource(): CloudNotesDataSource {
    val db = FirebaseFirestore.getInstance()
    val settings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(
            PersistentCacheSettings.newBuilder()
                .build()
        )
        .build()
    db.firestoreSettings = settings
    return object : CloudNotesDataSource {
        override fun observe(uid: String): Flow<List<Note>> = callbackFlow {
            val reg: ListenerRegistration = col(uid)
                .addSnapshotListener { snap, _ ->
                    val items = snap?.documents?.mapNotNull { doc ->
                        val dto = parseDto(doc.data ?: emptyMap())
                        dto.toNote(doc.id)
                    }?.sortedBy { it.updatedAt } ?: emptyList()
                    trySend(items)
                }
            awaitClose { reg.remove() }
        }

        override suspend fun getAll(uid: String): List<Note> {
            val res = col(uid).get().await()
            return res.documents.mapNotNull { doc ->
                parseDto(
                    doc.data ?: emptyMap()
                ).toNote(doc.id)
            }
                .sortedBy { it.updatedAt }
        }

        override suspend fun upsert(uid: String, note: Note) {
            val ref = col(uid).document(note.id.toString())
            val data = mutableMapOf<String, Any?>(
                "id" to note.id,
                "title" to note.title,
                "description" to note.description,
                "deleted" to note.deleted,
                "priority" to when (note.priority) {
                    Priority.HIGH -> 0
                    Priority.MEDIUM -> 1
                    Priority.LOW -> 2
                },
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            data["createdAt"] = if (note.createdAt == 0L) FieldValue.serverTimestamp() else Timestamp(Date(note.createdAt))
            ref.set(data, SetOptions.merge()).await()
        }

        override suspend fun delete(uid: String, id: Int) {
            col(uid).document(id.toString()).delete().await()
        }
    }
}

actual fun provideCurrentUserProvider(): CurrentUserProvider = object : CurrentUserProvider {
    override val uid: Flow<String?> =
        MutableStateFlow(FirebaseAuth.getInstance().currentUser?.uid).also { flow ->
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                val previous = flow.value
                val current = auth.currentUser?.uid
                flow.value = current
                // When user logs out or switches accounts, clear Firestore's offline cache
                if (previous != null && previous != current) {
                    val db = FirebaseFirestore.getInstance()
                    // Terminate active listeners/clients before clearing persistence
                    db.terminate().continueWithTask { db.clearPersistence() }
                }
            }
        }
}

private suspend fun <T> Task<T>.await(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result -> cont.resume(result) }
        addOnFailureListener { e -> cont.resumeWithException(e) }
    }
