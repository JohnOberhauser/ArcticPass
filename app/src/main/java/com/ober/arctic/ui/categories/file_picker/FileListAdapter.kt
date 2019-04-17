package com.ober.arctic.ui.categories.file_picker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.api.services.drive.model.File
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.cell_file.view.*

class FileListAdapter: RecyclerView.Adapter<FileListAdapter.FileViewHolder>() {

    var files: List<File> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedFile: File? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.cell_file, parent, false)
        return FileViewHolder(view)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.view.file_name.text = files[position].name

        holder.view.check.visibility = if (selectedFile != files[position]) {
            View.GONE
        } else {
            View.VISIBLE
        }

        holder.view.setOnClickListener {
            selectedFile = files[position]
            notifyDataSetChanged()
        }
    }

    inner class FileViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}