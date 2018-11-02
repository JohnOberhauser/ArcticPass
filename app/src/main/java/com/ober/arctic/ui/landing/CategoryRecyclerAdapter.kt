package com.ober.arctic.ui.landing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.ober.arctic.data.model.Category
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.cell_domain.view.*

class CategoryRecyclerAdapter(private val categoryClickedListener: CategoryClickedListener) :
    RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryViewHolder>() {

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
        holder.view.delete_button.visibility = View.GONE
        holder.view.category_text_view.text = categories[position].name
        holder.view.setOnClickListener {
            if (holder.view.delete_button.visibility == View.VISIBLE) {
                holder.view.delete_button.visibility = View.GONE
            } else {
                categoryClickedListener.onCategoryClicked(categories[position])
            }
        }

        holder.view.delete_button.setOnClickListener {
            categoryClickedListener.onDeleteCategory(categories[position])
        }
    }

    inner class CategoryViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.setOnLongClickListener {
                if (view.delete_button.visibility == View.GONE) {
                    view.delete_button.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.fade_in))
                    view.delete_button.visibility = View.VISIBLE
                }
                true
            }
        }
    }

    interface CategoryClickedListener {
        fun onCategoryClicked(category: Category)
        fun onDeleteCategory(category: Category)
    }
}