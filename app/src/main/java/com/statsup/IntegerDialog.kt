package com.statsup

import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.NumberPicker
import androidx.fragment.app.FragmentActivity
import com.statsup.databinding.IntegerDialogBinding

class IntegerDialog(
    private val label: String,
    private val unit: String,
    private val actualValue: Int,
    private val listener: (number: Int) -> Unit
) {
    fun makeDialog(activity: FragmentActivity): AlertDialog {
        val binding = IntegerDialogBinding.inflate(activity.layoutInflater)
        val pickerInteger = binding.pickerInteger.apply {
            minValue = 100
            maxValue = 299
            value = actualValue
        }

        binding.unitText.text = unit

        return AlertDialog.Builder(activity)
            .setView(binding.root)
            .setTitle(label)
            .setNegativeButton(R.string.negative_button) { dialog, _ -> onNegativeClick(dialog) }
            .setPositiveButton(R.string.positive_button) { dialog, _ -> onPositiveClick(dialog, pickerInteger) }
            .create()
    }

    private fun onNegativeClick(dialog: DialogInterface) {
        dialog.cancel()
    }

    private fun onPositiveClick(
        dialog: DialogInterface,
        pickerInteger: NumberPicker
    ) {
        listener.invoke(pickerInteger.value)
        dialog.dismiss()
    }
}


