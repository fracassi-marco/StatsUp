package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.configurations_fragment.view.*

class ConfigurationsFragment : Fragment() {
    private lateinit var user: User
    private lateinit var heightEditorValueInput: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.configurations_fragment, container, false)
        heightEditorValueInput = view.height_editor_value_input

        view.height_editor_value.setOnClickListener { showHeightDialog() }

        UserRepository.listen(object : Listener<User> {
            override fun update(subject: User) {
                user = subject
                heightEditorValueInput.text = heightOrDefault()
            }
        })

        return view
    }

    private fun heightOrDefault() = if(user.height == 0) " - " else user.height.toString()

    private fun showHeightDialog() {
        IntegerDialog(
            context!!.getString(R.string.integer_dialog_height),
            context!!.getString(R.string.height_unit_cm),
            user.height
        ) { number -> onValueDialogPositiveButton(number) }
            .makeDialog(activity!!)
            .show()
    }

    private fun onValueDialogPositiveButton(number: Int) {
        user.height = number
        UserRepository.update(context!!, user)
    }
}
