package com.ober.arctic.ui.landing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ober.arctic.data.model.Domain
import com.ober.arcticpass.R
import kotlinx.android.synthetic.main.cell_domain.view.*

class DomainRecyclerAdapter : RecyclerView.Adapter<DomainRecyclerAdapter.DomainViewHolder>() {

    var domains: List<Domain> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DomainViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.cell_domain, parent, false)
        return DomainViewHolder(view)
    }

    override fun getItemCount(): Int {
        return domains.size
    }

    override fun onBindViewHolder(holder: DomainViewHolder, position: Int) {
        holder.view.domain_text_view.text = domains[position].name
    }

    class DomainViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}