package com.example.zadanie.ui.widget.followerList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.data.db.model.Follower
import com.example.zadanie.helpers.autoNotify
import kotlin.properties.Delegates

class FollowerAdapter(val events: FollowerEvents? = null) :
    RecyclerView.Adapter<FollowerAdapter.FollowerItemViewHolder>() {
    var items: List<Follower> by Delegates.observable(emptyList()) { _, old, new ->
        autoNotify(old, new) { o, n -> o.id.compareTo(n.id) == 0 }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerItemViewHolder {
        return FollowerItemViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: FollowerItemViewHolder, position: Int) {
        holder.bind(items[position], events)
    }

    class FollowerItemViewHolder(
        private val parent: ViewGroup,
        itemView: View = LayoutInflater.from(parent.context).inflate(
            R.layout.follower_item,
            parent,
            false
        )
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Follower, events: FollowerEvents?) {
            itemView.findViewById<TextView>(R.id.name).text = item.name
            itemView.findViewById<TextView>(R.id.bar_name).text = item.bar_name

            itemView.setOnClickListener { events?.onFollowerClick(item) }
        }
    }
}