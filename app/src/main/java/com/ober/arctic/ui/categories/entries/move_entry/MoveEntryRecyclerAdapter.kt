package com.ober.arctic.ui.categories.entries.move_entry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ober.arctic.data.model.Category
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.cell_move_entry.view.*

class MoveEntryRecyclerAdapter(private var categoryClickedListener: CategoryClickedListener) :
    RecyclerView.Adapter<MoveEntryRecyclerAdapter.MoveEntryViewHolder>() {

    var categories: List<Category>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveEntryViewHolder {
        return MoveEntryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_move_entry, parent, false))
    }

    override fun getItemCount(): Int {
        return categories?.size ?: 0
    }

    override fun onBindViewHolder(holder: MoveEntryViewHolder, position: Int) {
        holder.view.category_text_view.text = categories?.get(position)?.name

        holder.view.setOnClickListener {
            categoryClickedListener.onCategoryClicked(categories?.get(position)?.name)
        }
    }

    inner class MoveEntryViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    interface CategoryClickedListener {
        fun onCategoryClicked(categoryName: String?)
    }
}