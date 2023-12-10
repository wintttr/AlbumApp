package com.wintttr.albumapplication

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.wintttr.albumapplication.databinding.DialogTitlePickerBinding

class TitlePickerDialogFragment(
    private val okListener: (String) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogTitlePickerBinding.inflate(layoutInflater)

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title))
            .setView(binding.root)
            .setPositiveButton(getString(R.string.dialog_positive_answer)) { _, _ ->
                val enteredText = binding.albumTitle.text.toString()
                okListener(enteredText)
            }
            .setNegativeButton(getString(R.string.dialog_negative_answer)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    companion object {
        const val TAG = "TitlePickerDialogFragmentTag"
        const val REQUEST_ALBUM_TITLE = "REQUEST_ALBUM_TITLE"
    }

}