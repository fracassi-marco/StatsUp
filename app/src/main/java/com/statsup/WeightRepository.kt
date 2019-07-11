package com.statsup

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList
import java.util.HashMap

object WeightRepository {
    private val listeners: MutableList<Listener<List<Weight>>> = ArrayList()
    private var weights: MutableList<Weight> = mutableListOf()
    private val user = FirebaseAuth.getInstance().currentUser!!
    private val databaseRef = FirebaseDatabase.getInstance().getReference("users/${user.uid}/weights/")

    init {
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = dataSnapshot.children.map { it.getValue(Weight::class.java)!! }
                weights = items.sortedByDescending { it.dateInMillis }.toMutableList()
                listeners.forEach { it.update(items) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }

        }
        databaseRef.addValueEventListener(eventListener)
    }

    fun listen(vararg listeners: Listener<List<Weight>>) {
        listeners.forEach {
            this.listeners.add(it)
            it.update(weights)
        }
    }

    fun addIfNotExists(newActivities: List<Weight>) {
        val toAdd = newActivities.minus(weights)
        if(toAdd.isNotEmpty()) {
            saveAll(toAdd)
            weights.union(toAdd)
        }
    }

    fun delete(weight: Weight) {
        weights.remove(weight)
        databaseRef.child(weight.id).removeValue()
    }

    private fun saveAll(toAdd: List<Weight>) {
        val children = HashMap<String, Any>()
        toAdd.forEach {
            val key = databaseRef.push().key!!
            children[key] = it.apply { id = key }

        }
        databaseRef.updateChildren(children)
    }

    fun cleanListeners() {
        listeners.clear()
    }

    fun removeListener(vararg listeners: Listener<List<Weight>>) {
        listeners.forEach { this.listeners.remove(it) }
    }
}
