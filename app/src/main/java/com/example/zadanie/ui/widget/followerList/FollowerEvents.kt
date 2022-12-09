package com.example.zadanie.ui.widget.followerList

import com.example.zadanie.data.db.model.Follower

interface FollowerEvents {
    fun onFollowerClick(follower: Follower)
}