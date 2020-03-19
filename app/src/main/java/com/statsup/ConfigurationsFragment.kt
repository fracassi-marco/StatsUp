package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.configurations_fragment.view.*

class ConfigurationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.configurations_fragment, container, false)
        val heightEditorValueInput = view.height_editor_value_input

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
