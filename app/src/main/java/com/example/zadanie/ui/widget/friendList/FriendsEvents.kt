package com.example.zadanie.ui.widget.friendList

import com.example.zadanie.data.db.model.Friend

interface FriendsEvents {
    fun onDeleteButtonClick(friend: Friend)
}