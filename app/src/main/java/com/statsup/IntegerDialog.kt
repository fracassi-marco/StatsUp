package com.statsup

import android.app.AlertDialog
import android.content.DialogInterface
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import kotlinx.android.synthetic.main.decimal_dialog.view.*

class IntegerDialog(
    private val label: String,
    private val unit: String,
    private val actualValue: Int,
    private val listener: (number: Int) -> Unit
) {
    private lateinit var pickerInteger: NumberPicker

    fun makeDialog(activity: FragmentActivity): AlertDialog {
        val view = activity.layoutInflater.inflate(R.layout.integer_dialog, null)

        initNumberPickers(view)
        initUnitText(view)

        return AlertDialog.Builder(activity)
            .setView(view)
            .setTitle(label)
            .setNegativeButton(R.string.negative_button) { dialog, _ -> onNegativeClick(dialog) }
            .setPositiveButton(R.string.positive_button) { dialog, _ -> onPositiveClick(dialog) }
            .create()
    }

    private fun initNumberPickers(view: View) {
        pickerInteger = view.picker_integer.apply {
            minValue = 100
            maxValue = 299
            value = actualValue
        }
    }

    private fun initUnitText(view: View) {
        val unitText = view.findViewById<TextView>(R.id.unit_text)
        unitText.text = unit
    }

    private fun onNegativeClick(dialog: DialogInterface) {
        dialog.cancel()
    }

    private fun onPositiveClick(dialog: DialogInterface) {
        listener.invoke(pickerInteger.value)
        dialog.dismiss()
    }
}

