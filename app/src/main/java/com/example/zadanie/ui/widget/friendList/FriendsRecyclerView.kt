package com.example.zadanie.ui.widget.friendList

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.data.db.model.Friend
import com.example.zadanie.helpers.Injection.provideDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendsRecyclerView : RecyclerView {
    private lateinit var friendsAdapter: FriendsAdapter

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
        friendsAdapter = FriendsAdapter(object : FriendsEvents {
            override fun onDeleteButtonClick(friend: Friend) {
                Toast.makeText(
                    context,
                    "Používateľ bol úspešne vymazaný.",
                    Toast.LENGTH_SHORT
                ).show()
                CoroutineScope(Dispatchers.IO).launch {
                    provideDataRepository(context).apiDeleteFriend(friend)
                }
            }
        })
        adapter = friendsAdapter
    }
}

@BindingAdapter(value = ["friendItems"])
fun FriendsRecyclerView.applyItems(
    friends: List<Friend>?
) {
    (adapter as FriendsAdapter).items = friends ?: emptyList()
}