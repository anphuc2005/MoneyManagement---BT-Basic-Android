package com.example.moneymanagement.data.data_class

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

object UserManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun getUserDisplayName(): String? {
        return auth.currentUser?.displayName
    }

    suspend fun saveUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
            usersRef.child(userId).setValue(userProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
            val snapshot = usersRef.child(userId).get().await()
            val userProfile = snapshot.getValue(UserProfile::class.java)
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
            val updateWithTimestamp = updates.toMutableMap()
            updateWithTimestamp["updatedAt"] = System.currentTimeMillis()
            usersRef.child(userId).updateChildren(updateWithTimestamp).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUserProfile(onProfileChanged: (UserProfile?) -> Unit) {
        val userId = getCurrentUserId() ?: return
        usersRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue(UserProfile::class.java)
                onProfileChanged(userProfile)
            }

            override fun onCancelled(error: DatabaseError) {
                onProfileChanged(null)
            }
        })
    }
}