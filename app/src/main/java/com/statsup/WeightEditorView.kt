package com.statsup

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.weight_editor_view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class WeightEditorView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weight_editor_view)

        val latestKilograms = intent.getDoubleExtra("latestKilograms", 50.0)

        findViewById<RelativeLayout>(R.id.weight_editor_value).setOnClickListener { showWeightDialog(latestKilograms) }

        findViewById<RelativeLayout>(R.id.weight_editor_date).setOnClickListener { showDatePickerDialog() }

        setKilogramsValue(latestKilograms.toString())
        setDateValue(DateTime())

        animateViewsEntrance(R.id.weight_editor_content_main)
    }

    private fun showWeightDialog(value: Double) {
        DecimalDialog(
            applicationContext.getString(R.string.decimal_dialog_weight),
            applicationContext.getString(R.string.weight_unit_kg),
            value
        ) { integer, decimal -> onValueDialogPositiveButton(integer, decimal) }
            .makeDialog(this)
            .show()
    }

    private fun onValueDialogPositiveButton(wholeNum: Int, fractionalNum: Int) {
        setKilogramsValue("$wholeNum.$fractionalNum")
    }

    private fun setKilogramsValue(value: String) {
        weight_editor_value_input.text = applicationContext.getString(R.string.weight_editor_value_label, value)
        weight_editor_value_input.tag = value
    }

    private fun showDatePickerDialog() {
        val today = DateTime()
        DatePickerDialog(this, { _, year, month, day ->
            setDateValue(DateTime(year, month + 1, day, 0, 0))
        }, today.year, today.monthOfYear, today.dayOfMonth).show()
    }

    private fun setDateValue(dateTime: DateTime) {
        weight_editor_date_input.text = datetimeToString(dateTime)
        weight_editor_date_input.tag = dateTime.millis
    }

    private fun datetimeToString(dateTime: DateTime): String {
        return formatDateString(DateTimeFormat.forPattern("E, d/M/y").print(dateTime))
    }

    private fun formatDateString(value: String): String {
        var result = value
        if (result.isEmpty()) {
            return result
        }

        result = result.replace(".", "")
        result = result.substring(0, 1).toUpperCase() + result.substring(1)

        return result
    }

    private fun animateViewsEntrance(view: Int) {
        val linearLayout = findViewById<LinearLayout>(view)
        for (i in 0 until linearLayout.childCount) {
            val child = linearLayout.getChildAt(i)
            child.animate()
                .setDuration(750)
                .alpha(1.0f)

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> supportFinishAfterTransition()
            R.id.editor_save -> onSaveButtonPressed()
        }

        return true
    }

    private fun onSaveButtonPressed() {
        val dateMillis = findViewById<TextView>(R.id.weight_editor_date_input).tag
        val value = findViewById<TextView>(R.id.weight_editor_value_input).tag

        WeightRepository.addIfNotExists(applicationContext, listOf(Weight(value.toString().toDouble(), dateMillis as Long)))
        supportFinishAfterTransition()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.editor_toolbar_menu, menu)
        return true
    }
}