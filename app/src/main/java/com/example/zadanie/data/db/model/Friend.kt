package com.example.zadanie.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
class Friend (
    @PrimaryKey val id: String,
    val name: String,
    val bar_id: String?,
    val bar_name: String?,
    val bar_lat: Double?,
    val bar_lon: Double?,
)

