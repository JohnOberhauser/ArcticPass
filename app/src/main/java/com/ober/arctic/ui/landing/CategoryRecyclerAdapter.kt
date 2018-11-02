package com.ober.arctic.ui.landing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ober.arctic.data.model.Category
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.cell_domain.view.*

class CategoryRecyclerAdapter : RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryViewHolder>() {

    var categories: List<Category> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.cell_domain, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.view.category_text_view.text = categories[position].name
    }

    class CategoryViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}