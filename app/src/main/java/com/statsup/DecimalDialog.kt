package com.statsup

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.NumberPicker
import com.statsup.databinding.DecimalDialogBinding

class DecimalDialog(
    private val label: String,
    private val unit: String,
    private val actualValue: Double,
    private val listener: (wholeNum: Int, fractionalNum: Int) -> Unit
) {
    fun makeDialog(activity: Activity): AlertDialog {
        val binding = DecimalDialogBinding.inflate(activity.layoutInflater)

        val doubleAsString = actualValue.toString()
        val indexOfDecimal = doubleAsString.indexOf(".")

        val pickerInteger = binding.pickerInteger.apply {
            maxValue = 199
            value = doubleAsString.substring(0, indexOfDecimal).toInt()
        }
        val pickerDecimal = binding.pickerDecimal.apply {
            maxValue = 9
            value = doubleAsString.substring(indexOfDecimal + 1).toInt()
        }

        binding.unitText.text = unit

        return AlertDialog.Builder(activity)
            .setView(binding.root)
            .setTitle(label)
            .setNegativeButton(R.string.negative_button) { dialog, _ -> onNegativeClick(dialog) }
            .setPositiveButton(R.string.positive_button) { dialog, _ -> onPositiveClick(dialog, pickerInteger, pickerDecimal) }
            .create()
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


