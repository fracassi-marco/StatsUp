package com.statsup

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

object UserRepository {

    private val listeners: MutableList<Listener<User>> = ArrayList()
    private val userDatabaseRef = FirebaseDatabase.getInstance().getReference("users/${currentUser().uid}/")

    fun listen(listener: Listener<User>) {
        if (listeners.isEmpty()) {

            val userListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var user = dataSnapshot.getValue(User::class.java)
                    if (user == null) {
                        user = User(name = currentUser().displayName!!, image = randomImage(), id = currentUser().uid)
                    }
                    else {
                        user.id = dataSnapshot.key!!
                    }

                    listeners.forEach { it.update(user) }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            userDatabaseRef.addValueEventListener(userListener)
        }
        listeners.add(listener)
    }

    private fun randomImage(): String {
        return "hero${Random().nextInt(19)}"
    }

    fun listenOnce(listener: Listener<User>) {
        val userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val subject = dataSnapshot.getValue<User>(User::class.java)!!
                subject.id = dataSnapshot.key!!
                listener.update(subject)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        userDatabaseRef.addListenerForSingleValueEvent(userListener)
    }

    private fun currentUser(): FirebaseUser {
        return FirebaseAuth.getInstance().currentUser!!
    }

    fun cleanListeners() {
        listeners.clear()
    }
}

