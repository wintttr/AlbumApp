package com.wintttr.albumapplication

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wintttr.albumapplication.databinding.FragmentPhotoGridBinding
import com.wintttr.albumapplication.databinding.ViewHolderPhotoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class PhotoViewHolder(
    private val binding: ViewHolderPhotoBinding,
    private val viewModel: PhotoGridViewModel,
    private val onClick: (Long) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(photo: Photo) {
        GlobalScope.launch(Dispatchers.Main) {
            val bitmap: Bitmap

            withContext(Dispatchers.IO) {
                bitmap = viewModel.getBitmap(binding.root.context, photo)
            }

            binding.root.setImageBitmap(bitmap)

            binding.root.setOnClickListener {
                onClick(photo.id)
            }
        }
    }
}

class PhotoGridAdapter(
    private val fragment: PhotoGridFragment,
    private val onClick: (Long) -> Unit
): RecyclerView.Adapter<PhotoViewHolder>() {
    private val photoList = mutableListOf<Photo>()

    @SuppressLint("NotifyDataSetChanged")
    fun changeAndNotify(list: List<Photo>) {
        photoList.clear()
        photoList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            ViewHolderPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            fragment.viewModel,
            onClick
        )
    }

    override fun getItemCount() = photoList.size

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photoList[position])
    }
}

class PhotoGridFragment : Fragment() {
    private var _binding: FragmentPhotoGridBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val args by navArgs<PhotoGridFragmentArgs>()

    private var adapter = PhotoGridAdapter(this) {
        findNavController().navigate(PhotoGridFragmentDirections.navigatePhotoDescription(it))
    }

    private var photoName: String? = null

    val viewModel: PhotoGridViewModel by viewModels {
        PhotoGridViewModelFactory(args.albumTitle)
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {
        if(it && photoName != null) {
            val photo = Photo(photoName!!, args.albumTitle)
            viewModel.insertPhoto(photo)
        } else {
            Toast.makeText(requireContext(), "Что-то пошло не так((", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickPhotosLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) {
        for(uri in it) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val filename = UUID.randomUUID().toString()

                val photoFile = File(
                    requireContext().applicationContext.filesDir,
                    filename
                )

                val photo = Photo(filename, args.albumTitle)

                photoFile.createNewFile()
                photoFile.outputStream().use { outputStream ->
                    requireContext().contentResolver.openInputStream(uri)?.let { inputStream ->
                        val buffer = ByteArray(1024)
                        var len = inputStream.read(buffer)
                        while (len != -1) {
                            outputStream.write(buffer, 0, len)
                            len = inputStream.read(buffer)
                        }
                    }
                }

                viewModel.insertPhoto(photo)
            }
        }
    }

    private fun takePhoto() {
        val id = UUID.randomUUID()

        photoName = "${id}.jpg"

        val photoFile = File(
            requireContext().applicationContext.filesDir,
            photoName!!
        )

        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "com.wintttr.albumapplication.fileprovider",
            photoFile
        )

        takePhotoLauncher.launch(photoUri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoGridBinding.inflate(inflater, container, false)
        binding.photoGrid.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.photoList.collect {
                    adapter.changeAndNotify(it)
                }
            }
        }

        // Настраиваем грид
        binding.photoGrid.layoutManager = GridLayoutManager(requireContext(), 2)

        // Настраиваем тулбар
        binding.photoGridToolbar.title = args.albumTitle
        binding.photoGridToolbar.inflateMenu(R.menu.menu_photo_grid)
        binding.photoGridToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.take_photo_item -> {
                    takePhoto()
                    true
                }
                R.id.add_photo_item -> {
                    pickPhotosLauncher.launch("image/*")
                    true
                }
                else -> {
                    Toast.makeText(requireContext(), "ЕЩЁ НЕ ГОТОВО", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}