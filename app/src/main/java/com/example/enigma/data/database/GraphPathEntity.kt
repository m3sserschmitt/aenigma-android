package com.example.enigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.enigma.util.Constants.Companion.GRAPH_PATHS_TABLE

@Entity(tableName = GRAPH_PATHS_TABLE)
class GraphPathEntity (val destination: String, val path: List<String>)
{
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}
