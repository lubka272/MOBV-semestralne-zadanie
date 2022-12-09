package com.example.zadanie.ui.widget.followerList

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.data.db.model.Follower
import com.example.zadanie.ui.fragments.FriendsFragmentDirections

class FollowersRecyclerView : RecyclerView {
    private lateinit var followersAdapter: FollowerAdapter

    /**
     * Default constructor
     *
     * @param context context for the activity
     */
    constructor(context: Context) : super(context) {
        init(context)
    }

    /**
     * Constructor for XML layout
     *
     * @param context activity context
     * @param attrs   xml attributes
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        followersAdapter = FollowerAdapter(object : FollowerEvents {
            override fun onFollowerClick(follower: Follower) {
                if (follower.bar_id != null) {
                    this@FollowersRecyclerView.findNavController().navigate(
                        FriendsFragmentDirections.actionToDetail(follower.bar_id, "1")
                    )
                } else {
                    Toast.makeText(
                        context,
                        "Používateľ nieje označený v žiadnom podniku.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        })
        adapter = followersAdapter
    }
}

@BindingAdapter(value = ["followerItems"])
fun FollowersRecyclerView.applyItems(
    followers: List<Follower>?
) {
    (adapter as FollowerAdapter).items = followers ?: emptyList()
}