package com.wintttr.albumapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.wintttr.albumapplication.databinding.DialogTitlePickerBinding

class TitlePickerDialogFragment(
    private val okListener: (String) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogTitlePickerBinding.inflate(layoutInflater)

        return AlertDialog.Builder(requireContext())
            .setTitle("Album title")
            .setView(binding.root)
            .setPositiveButton("Ok") { _, _ ->
                val enteredText = binding.albumTitle.text.toString()
                okListener(enteredText)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    companion object {
        const val TAG = "TitlePickerDialogFragmentTag"
        const val REQUEST_ALBUM_TITLE = "REQUEST_ALBUM_TITLE"
    }

}