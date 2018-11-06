package com.ober.arctic.ui.entries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.ober.arctic.data.model.Credentials
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.cell_category.view.*

class CredentialsRecyclerAdapter(private val credentialsClickedListener: CredentialsClickedListener) :
    RecyclerView.Adapter<CredentialsRecyclerAdapter.CredentialsViewHolder>() {

    var credentials: List<Credentials> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CredentialsViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.cell_category, parent, false)
        return CredentialsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return credentials.size
    }

    override fun onBindViewHolder(holder: CredentialsViewHolder, position: Int) {
        holder.view.delete_button.visibility = View.GONE
        holder.view.category_text_view.text = credentials[position].description
        holder.view.card_root.setOnClickListener {
            if (holder.view.delete_button.visibility == View.VISIBLE) {
                holder.view.delete_button.visibility = View.GONE
            } else {
                credentialsClickedListener.onCredentialClicked(credentials[position])
            }
        }

        holder.view.delete_button.setOnClickListener {
            credentialsClickedListener.onDeleteCredential(credentials[position])
        }
    }

    inner class CredentialsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.card_root.setOnLongClickListener {
                if (view.delete_button.visibility == View.GONE) {
                    view.delete_button.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.fade_in))
                    view.delete_button.visibility = View.VISIBLE
                }
                true
            }
        }
    }

    interface CredentialsClickedListener {
        fun onCredentialClicked(credentials: Credentials)
        fun onDeleteCredential(credentials: Credentials)
    }
}