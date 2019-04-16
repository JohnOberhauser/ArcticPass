package com.ober.arctic.ui.categories.file_picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.api.services.drive.model.File
import com.ober.arctic.ui.BaseDialogFragment
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.fragment_file_list.*

class BackupGoogleFileListDialogFragment: BaseDialogFragment() {

    lateinit var fileList: List<File>
    private var adapter: FileListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return setAndBindContentView(inflater, container, R.layout.fragment_file_list)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = FileListAdapter()
        files_recycler_view.adapter = adapter
        files_recycler_view.layoutManager = LinearLayoutManager(context)
        adapter?.files = fileList
    }

    companion object {
        fun newInstance(fileList: List<File>): BackupGoogleFileListDialogFragment {
            val fragment = BackupGoogleFileListDialogFragment()
            fragment.fileList = fileList
            return fragment
        }
    }
}