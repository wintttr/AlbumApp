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

class TitlePickerFragment : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogTitlePickerBinding.inflate(layoutInflater)

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton("Ok") { dialog, _ ->
                val enteredText = binding.albumTitle.text.toString()

                if(enteredText.isEmpty()) {
                    Toast.makeText(requireContext(), "Title can't be empty.", Toast.LENGTH_SHORT).show()
                }
                else {
                    setFragmentResult(REQUEST_ALBUM_TITLE, bundleOf(REQUEST_ALBUM_TITLE to enteredText))
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    companion object {
        const val REQUEST_ALBUM_TITLE = "REQUEST_ALBUM_TITLE"
    }

}