package com.ober.arctic.ui.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.ober.arctic.data.model.Category
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.cell_category.view.*

class CategoryRecyclerAdapter(private val categoryClickedListener: CategoryClickedListener) :
    RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryViewHolder>() {

    var categories: ArrayList<Category> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.cell_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.view.edit_button.visibility = View.VISIBLE
        holder.view.extra_buttons.visibility = View.GONE
        holder.view.category_text_view.text = categories[position].name
        holder.view.card_root.setOnClickListener {
            if (holder.view.extra_buttons.visibility == View.VISIBLE) {
                holder.view.extra_buttons.visibility = View.GONE
            } else {
                categoryClickedListener.onCategoryClicked(categories[position])
            }
        }

        holder.view.delete_button.setOnClickListener {
            categoryClickedListener.onDeleteCategory(categories[position])
        }
        holder.view.edit_button.setOnClickListener {
            categoryClickedListener.onEditCategory(categories[position])
        }
    }

    inner class CategoryViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.card_root.setOnLongClickListener {
                if (view.extra_buttons.visibility == View.GONE) {
                    view.extra_buttons.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.fade_in))
                    view.extra_buttons.visibility = View.VISIBLE
                }
                true
            }
        }
    }

    interface CategoryClickedListener {
        fun onCategoryClicked(category: Category)
        fun onDeleteCategory(category: Category)
        fun onEditCategory(category: Category)
    }
}