package com.statsup

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object UserRepository {

    private var user = User()
    private val listeners: MutableList<Listener<User>> = mutableListOf()
    private val userDatabaseRef = FirebaseDatabase.getInstance().getReference("users/${currentUser().uid}/")

    init {
        userDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var item = dataSnapshot.getValue(User::class.java)
                if (item == null) {
                    item = User(name = currentUser().displayName!!, image = "none", height = 0).apply {
                        id = currentUser().uid
                    }
                }
                else {
                    item.id = dataSnapshot.key!!
                }

                user = item
                listeners.forEach { it.update(item) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun listen(listener: Listener<User>) {
        listeners.add(listener)
        listener.update(user)
    }

    fun update(user: User) {
        userDatabaseRef.updateChildren(mapOf("height" to user.height))
    }

    fun removeListener(vararg listeners: Listener<User>) {
        listeners.forEach { this.listeners.remove(it) }
    }

    fun cleanListeners() {
        listeners.clear()
    }

    private fun currentUser(): FirebaseUser {
        return FirebaseAuth.getInstance().currentUser!!
    }
}

