package ro.aenigma.util

import com.google.gson.FieldNamingStrategy
import java.lang.reflect.Field

class CapitalizedFieldNamingStrategy : FieldNamingStrategy {
    override fun translateName(field: Field): String {
        return field.name.replaceFirstChar { it.uppercaseChar() }
    }
}

class CaseInsensitiveFieldNamingStrategy : FieldNamingStrategy {
    override fun translateName(f: Field): String {
        return f.name.replaceFirstChar { it.uppercaseChar() }
    }
}
