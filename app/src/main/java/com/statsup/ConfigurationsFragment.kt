package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.statsup.R.string.settings_delete_activities_complete
import com.statsup.R.string.settings_delete_weights_complete
import kotlinx.android.synthetic.main.configurations_fragment.view.*

class ConfigurationsFragment : NoMenuFragment() {

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.configurations_fragment, container, false)
        val heightEditorValueInput = view.height_editor_value_input

        view.delete_activities_button.setOnClickListener {
            ActivityRepository.clean(context!!)
            Toast.makeText(context!!, settings_delete_activities_complete, LENGTH_SHORT).show()
        }

        view.delete_weights_button.setOnClickListener {
            WeightRepository.clean(context!!)
            Toast.makeText(context!!, settings_delete_weights_complete, LENGTH_SHORT).show()
        }

        UserRepository.listen("ConfigurationsFragment", object : Listener<User> {
            override fun update(subject: User) {
                view.height_editor_value.setOnClickListener { showHeightDialog(subject) }
                heightEditorValueInput.text = heightOrDefault(subject)
            }
        })

        return view
    }

    private fun heightOrDefault(user: User) =
        if (user.height == 0) " - " else user.height.toString()

    private fun showHeightDialog(user: User) {
        IntegerDialog(
            context!!.getString(R.string.integer_dialog_height),
            context!!.getString(R.string.height_unit_cm),
            user.height
        ) { number -> onValueDialogPositiveButton(number, user) }
            .makeDialog(activity!!)
            .show()
    }

    private fun onValueDialogPositiveButton(number: Int, user: User) {
        user.height = number
        UserRepository.update(context!!, user)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        UserRepository.removeListener("ConfigurationsFragment")
    }
}
