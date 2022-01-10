package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.statsup.R.string.settings_delete_activities_complete
import com.statsup.R.string.settings_delete_weights_complete
import com.statsup.databinding.ConfigurationsFragmentBinding

class ConfigurationsFragment : NoMenuFragment() {

    private var _binding: ConfigurationsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = ConfigurationsFragmentBinding.inflate(inflater, container, false)

        binding.deleteActivitiesButton.setOnClickListener {
            ActivityRepository.clean(context!!)
            Toast.makeText(context!!, settings_delete_activities_complete, LENGTH_SHORT).show()
        }

        binding.deleteWeightsButton.setOnClickListener {
            WeightRepository.clean(context!!)
            Toast.makeText(context!!, settings_delete_weights_complete, LENGTH_SHORT).show()
        }

        UserRepository.listen("ConfigurationsFragment", object : Listener<User> {
            override fun update(subject: User) {
                binding.heightEditorValue.setOnClickListener { showHeightDialog(subject) }
                binding.heightEditorValueInput.text = heightOrDefault(subject)
            }
        })

        return binding.root
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
        _binding = null
        UserRepository.removeListener("ConfigurationsFragment")
    }
}
