package com.statsup

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

object ActivityRepository {

    private val listeners: MutableList<Listener<List<Activity>>> = ArrayList()
    private var activities: MutableList<Activity> = mutableListOf()
    private val user = FirebaseAuth.getInstance().currentUser!!
    private val activitiesDatabaseRef = FirebaseDatabase.getInstance().getReference("users/${user.uid}/activities/")

    fun listen(listener: Listener<List<Activity>>) {
        if (listeners.isEmpty()) {

            val eventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val items = dataSnapshot.children.map { it.getValue(Activity::class.java)!! }
                    activities = items.toMutableList()
                    listeners.forEach { it.update(items) }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            activitiesDatabaseRef.addValueEventListener(eventListener)
        }
        listeners.add(listener)
    }

    fun addIfNotExists(newActivities: List<Activity>) {
        val toAdd = newActivities.minus(activities)
        if(toAdd.isNotEmpty()) {
            saveAll(toAdd)
            activities.union(toAdd)
        }
    }

    private fun saveAll(toAdd: List<Activity>) {
        val children = HashMap<String, Any>()
        toAdd.forEach {
            val key = activitiesDatabaseRef.push().key!!
            children.put(key, it.apply { id = key })

        }
        activitiesDatabaseRef.updateChildren(children)
    }

    fun cleanListeners() {
        listeners.clear()
    }

}
