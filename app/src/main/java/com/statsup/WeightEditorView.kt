package com.statsup

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.statsup.databinding.WeightEditorViewBinding
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class WeightEditorView : AppCompatActivity() {

    private lateinit var binding: WeightEditorViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WeightEditorViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val latestKilograms = intent.getDoubleExtra("latestKilograms", 50.0)

        binding.weightEditorValue.setOnClickListener { showWeightDialog(latestKilograms) }

        binding.weightEditorDate.setOnClickListener { showDatePickerDialog() }

        setKilogramsValue(latestKilograms.toString())
        setDateValue(DateTime())

        animateViewsEntrance(binding.weightEditorContentMain)
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
        binding.weightEditorValueInput.text = applicationContext.getString(R.string.weight_editor_value_label, value)
        binding.weightEditorValueInput.tag = value
    }

    private fun showDatePickerDialog() {
        val today = DateTime()
        DatePickerDialog(this, { _, year, month, day ->
            setDateValue(DateTime(year, month + 1, day, 0, 0))
        }, today.year, today.monthOfYear, today.dayOfMonth).show()
    }

    private fun setDateValue(dateTime: DateTime) {
        binding.weightEditorDateInput.text = datetimeToString(dateTime)
        binding.weightEditorDateInput.tag = dateTime.millis
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
        result = result.substring(0, 1).uppercase() + result.substring(1)

        return result
    }

    private fun animateViewsEntrance(view: LinearLayout) {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
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
        val dateMillis = binding.weightEditorDateInput.tag
        val value = binding.weightEditorValueInput.tag

        WeightRepository.addIfNotExists(applicationContext, listOf(Weight(value.toString().toDouble(), dateMillis as Long)))
        supportFinishAfterTransition()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.editor_toolbar_menu, menu)
        return true
    }
}