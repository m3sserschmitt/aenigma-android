package com.example.enigma.util

import org.json.JSONObject

class ExportedContactData constructor(
    private val guard: String,
    private val publicKey: String) {

    override fun toString(): String {
        val data = JSONObject()

        try {
            data.put("guardAddress", guard)
            data.put("publicKey", publicKey)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }

        return data.toString()
    }
}
