package com.example.enigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.enigma.util.Constants.Companion.EDGES_TABLE

@Entity(tableName = EDGES_TABLE)
class EdgeEntity (
    val startAddress: String,
    val endAddress: String
)
{
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}
