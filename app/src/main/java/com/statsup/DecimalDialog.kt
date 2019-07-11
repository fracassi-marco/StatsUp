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
    private lateinit var pickerInteger: NumberPicker
    private lateinit var pickerDecimal: NumberPicker

    fun makeDialog(activity: Activity): AlertDialog {
        val view = activity.layoutInflater.inflate(R.layout.decimal_dialog, null)

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
        val doubleAsString = actualValue.toString()
        val indexOfDecimal = doubleAsString.indexOf(".")

        pickerInteger = view.picker_integer.apply {
            maxValue = 199
            value = doubleAsString.substring(0, indexOfDecimal).toInt()
        }

        pickerDecimal = view.picker_decimal.apply {
            maxValue = 9
            value = doubleAsString.substring(indexOfDecimal + 1).toInt()
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
        listener.invoke(pickerInteger.value, pickerDecimal.value)
        dialog.dismiss()
    }
}


