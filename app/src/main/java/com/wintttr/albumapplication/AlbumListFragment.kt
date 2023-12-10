package com.wintttr.albumapplication

import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wintttr.albumapplication.databinding.FragmentAlbumListBinding
import com.wintttr.albumapplication.databinding.ViewHolderAlbumBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private class AlbumViewHolder(
    private val binding: ViewHolderAlbumBinding,
    private val onClick: (String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(title: String) {
        binding.albumTitle.text = title
        binding.root.setOnClickListener {
            onClick(title)
        }
    }
}

private class AlbumListAdapter(
    list: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<AlbumViewHolder>() {
    private var innerList = mutableListOf<String>()

    init {
        innerList.addAll(list)
    }

    fun add(title: String) {
        innerList.add(title)
        notifyItemInserted(innerList.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(
            ViewHolderAlbumBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClick
        )
    }

    override fun getItemCount() = innerList.size

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(innerList[position])
    }
}

class AlbumListFragment : Fragment() {
    private var _binding: FragmentAlbumListBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumListBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun addAlbum(title: String, adapter: AlbumListAdapter) {
        adapter.add(title)
        binding.albumList.scrollToPosition(adapter.itemCount - 1)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            AlbumRepository.get().insertAlbum(title)
        }
    }

    // ЭТО ВСЁ НЕПРАВИЛЬНО, Я ЭТО НЕ ПОНИМАЮ
    // ЭТО КОСТЫЛЬ, ЭТО НАДО ИСПРАВИТЬ
    // TODO TODO TODO TODO TODO
    private fun okDialogListener(adapter: AlbumListAdapter?) = { title: String ->
        if(adapter != null) {
            addAlbum(title, adapter)
        }
        else {
            Toast.makeText(requireContext(), resources.getString(R.string.toast_error), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var adapter: AlbumListAdapter? = null

        viewLifecycleOwner.lifecycleScope.launch {
            val albums: List<String>

            withContext(Dispatchers.IO) {
                 albums = AlbumRepository.get().getAlbums().map { it.title }
            }

            adapter = AlbumListAdapter(albums) {
                findNavController().navigate(AlbumListFragmentDirections.navigatePhotoGrid(it))
            }

            binding.albumList.adapter = adapter
        }

        // Лист
        binding.albumList.layoutManager = LinearLayoutManager(requireContext())

        // Тулбар
        binding.albumListToolbar.inflateMenu(R.menu.menu_album_list)
        binding.albumListToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.add_album_item -> {
                    TitlePickerDialogFragment(okDialogListener(adapter)).show(
                        childFragmentManager, TitlePickerDialogFragment.TAG
                    )
                    true
                }
                R.id.change_language_item -> {
                    val languageToLoad = if(Locale.getDefault().language == Locale("ru").language) {
                        "en"
                    }
                    else{
                        "ru"
                    }

                    val locale = Locale(languageToLoad)
                    Locale.setDefault(locale)

                    val config = Configuration().apply {
                        setToDefaults()
                        setLocales(LocaleList(locale))
                    }

                    resources.updateConfiguration(
                        config,
                        resources.displayMetrics
                    )

                    requireActivity().recreate()

                    true
                }
                else -> false
            }
        }


        setFragmentResultListener(TitlePickerDialogFragment.REQUEST_ALBUM_TITLE) { requestKey, bundle ->
            if(bundle.getString(requestKey) != null && adapter != null) {
                addAlbum(bundle.getString(requestKey)!!, adapter!!)
            }
            else {
                Toast.makeText(requireContext(), resources.getString(R.string.toast_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}