package com.example.koracards.data.model

/** Supported UI languages. Default is Arabic per PRD §4.11. */
enum class Language(val code: String) {
    Arabic("ar"),
    English("en");

    val isArabic: Boolean get() = this == Arabic

    companion object {
        fun fromCode(code: String): Language =
            entries.firstOrNull { it.code == code } ?: Arabic
    }
}
