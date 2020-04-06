package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

abstract class ActivityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        return onCreate(inflater, container)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater!!.inflate(R.menu.sport_filter, menu)
        val spinner = menu!!.findItem(R.id.sport_filter).actionView as Spinner

        val sports = ActivityRepository.sports()
        val adapter = ArrayAdapter(
            context!!,
            R.layout.spinner_dropdown_item,
            sports.map { resources.getString(it.title) }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(ActivityRepository.selectedSportPosition())

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                ActivityRepository.changeSport(0) { activities ->
                    onActivityUpdate(activities)
                }
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                ActivityRepository.changeSport(position) { activities ->
                    onActivityUpdate(activities)
                }
            }
        }
    }

    protected abstract fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View
    protected abstract fun onActivityUpdate(activities: List<Activity>)
}