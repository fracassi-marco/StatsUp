package com.statsup

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import kotlinx.android.synthetic.main.decimal_dialog.view.*

class DecimalDialog(
    private val label: String,
    private val unit: String,
    private val actualValue: Double,
    private val listener: (wholeNum: Int, fractionalNum: Int) -> Unit
) {
    fun makeDialog(activity: Activity): AlertDialog {
        val view = activity.layoutInflater.inflate(R.layout.decimal_dialog, null)

        val doubleAsString = actualValue.toString()
        val indexOfDecimal = doubleAsString.indexOf(".")

        val pickerInteger = view.picker_integer.apply {
            maxValue = 199
            value = doubleAsString.substring(0, indexOfDecimal).toInt()
        }
        val pickerDecimal = view.picker_decimal.apply {
            maxValue = 9
            value = doubleAsString.substring(indexOfDecimal + 1).toInt()
        }

        initUnitText(view)

        return AlertDialog.Builder(activity)
            .setView(view)
            .setTitle(label)
            .setNegativeButton(R.string.negative_button) { dialog, _ -> onNegativeClick(dialog) }
            .setPositiveButton(R.string.positive_button) { dialog, _ -> onPositiveClick(dialog, pickerInteger, pickerDecimal) }
            .create()
    }

    private fun initUnitText(view: View) {
        val unitText = view.findViewById<TextView>(R.id.unit_text)
        unitText.text = unit
    }

    private fun onNegativeClick(dialog: DialogInterface) {
        dialog.cancel()
    }

    private fun onPositiveClick(
        dialog: DialogInterface,
        pickerInteger: NumberPicker,
        pickerDecimal: NumberPicker
    ) {
        listener.invoke(pickerInteger.value, pickerDecimal.value)
        dialog.dismiss()
    }
}


