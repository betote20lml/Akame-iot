package com.akameiot.domain.validation

import java.util.Locale

fun String.isIndexMetric(): Boolean = endsWith("_index")

fun String.baseMetric(): String =
    removeSuffix("_index")

fun String.formatIndexName(
    locale: Locale,
    baseFormatter: (String, Locale) -> String
): String {
    val base = baseFormatter(baseMetric(), locale)

    return if (locale.language == "es") {
        "Índice de $base"
    } else {
        "$base Index"
    }
}