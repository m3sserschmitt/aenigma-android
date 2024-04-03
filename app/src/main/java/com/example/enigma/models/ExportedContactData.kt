package com.example.enigma.models

import com.example.enigma.util.AddressHelper
import org.json.JSONObject

class ExportedContactData (
    val guardHostname: String,
    val publicKey: String) {

    override fun toString(): String {
        val data = JSONObject()

        try {
            data.put("guardHostname", guardHostname)
            data.put("publicKey", publicKey)
        } catch (e: Exception) {
            return ""
        }

        return data.toString()
    }
}
