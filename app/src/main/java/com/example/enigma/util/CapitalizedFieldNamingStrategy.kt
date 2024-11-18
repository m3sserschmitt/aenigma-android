package com.example.enigma.util

import com.google.gson.FieldNamingStrategy
import java.lang.reflect.Field

class CapitalizedFieldNamingStrategy : FieldNamingStrategy {
    override fun translateName(field: Field): String {
        return field.name.replaceFirstChar { it.uppercaseChar() }
    }
}
