package com.statsup

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

object ActivityRepository {

    private val listeners: MutableList<Listener<List<Activity>>> = mutableListOf()
    private var activities: MutableList<Activity> = mutableListOf()
    private val user = FirebaseAuth.getInstance().currentUser!!
    private val activitiesDatabaseRef = FirebaseDatabase.getInstance().getReference("users/${user.uid}/activities/")

    init {
        activitiesDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = dataSnapshot.children.map { it.getValue(Activity::class.java)!! }
                activities = items.sortedByDescending { it.dateInMillis }.toMutableList()
                listeners.forEach { it.update(items) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }

        })
    }

    fun listen(vararg listeners: Listener<List<Activity>>) {
        listeners.forEach {
            this.listeners.add(it)
            it.update(activities)
        }
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

    fun removeListener(vararg listeners: Listener<List<Activity>>) {
        listeners.forEach { this.listeners.remove(it) }
    }
}
