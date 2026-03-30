package com.akameiot.app.ui.home.formatter

import java.util.Locale

object TelemetryFormatter {

    private data class MetricSpec(
        val nameEs: String,
        val nameEn: String,
        val unit: String? = null,
        val decimals: Int = 2
    )

    private val specs = mapOf(

        //  Ambientales
        "humidity" to MetricSpec("Humedad", "Humidity", "%"),
        "air_humidity" to MetricSpec("Humedad Aire", "Air Humidity", "%"),
        "temperature" to MetricSpec("Temperatura", "Temperature", "°C"),
        "air_temperature" to MetricSpec("Temp. Aire", "Air Temperature", "°C"),
        "device_temperature" to MetricSpec("Temp. Dispositivo", "Device Temperature", "°C"),

        //  Suelo
        "soil_moisture" to MetricSpec("Humedad Suelo", "Soil Moisture", "%"),
        "soil_temperature" to MetricSpec("Temp. Suelo", "Soil Temperature", "°C"),
        "soil_ph" to MetricSpec("pH Suelo", "Soil pH"),
        "soil_ec" to MetricSpec("Conductividad", "Soil EC", "dS/m"),
        "soil_nitrogen" to MetricSpec("Nitrógeno", "Nitrogen", "mg/kg"),
        "soil_phosphorus" to MetricSpec("Fósforo", "Phosphorus", "mg/kg"),
        "soil_potassium" to MetricSpec("Potasio", "Potassium", "mg/kg"),
        "soil_salinity" to MetricSpec("Salinidad", "Salinity", "dS/m"),

        //  Clima
        "air_pressure" to MetricSpec("Presión", "Pressure", "hPa"),
        "wind_speed" to MetricSpec("Viento", "Wind Speed", "m/s"),
        "rainfall" to MetricSpec("Lluvia", "Rainfall", "mm"),
        "solar_radiation" to MetricSpec("Radiación Solar", "Solar Radiation", "W/m²"),

        //  Calidad del aire
        "co2_level" to MetricSpec("CO2", "CO2", "ppm"),
        "voc" to MetricSpec("VOC", "VOC", "ppb"),
        "o3_level" to MetricSpec("Ozono", "Ozone", "ppb"),
        "no2_level" to MetricSpec("NO2", "NO2", "ppb"),
        "so2_level" to MetricSpec("SO2", "SO2", "ppb"),
        "pm1" to MetricSpec("PM1", "PM1", "µg/m³"),
        "pm2_5" to MetricSpec("PM2.5", "PM2.5", "µg/m³"),
        "pm10" to MetricSpec("PM10", "PM10", "µg/m³"),

        //  Otros sensores
        "geiger_counter" to MetricSpec("Radiación", "Radiation", "µSv/h"),
        "leaf_wetness" to MetricSpec("Humedad Hoja", "Leaf Wetness", "%"),

        // Energía
        "battery_voltage" to MetricSpec("Voltaje", "Voltage", "V"),
        "battery_level" to MetricSpec("Batería", "Battery", "%"),
        "battery_health" to MetricSpec("Salud Batería", "Battery Health", "%"),

        //  Dispositivo
        "signal_strength" to MetricSpec("Señal", "Signal", "dBm"),
        "uptime" to MetricSpec("Tiempo Activo", "Uptime", "s"),

        //  GPS
        "latitude" to MetricSpec("Latitud", "Latitude", decimals = 6),
        "longitude" to MetricSpec("Longitud", "Longitude", decimals = 6),
        "altitude" to MetricSpec("Altitud", "Altitude", "m"),
        "speed" to MetricSpec("Velocidad", "Speed", "m/s"),
        "course" to MetricSpec("Dirección", "Course", "°"),

        //  Movimiento
        "x_acceleration" to MetricSpec("Aceleración X", "Accel X", "m/s²"),
        "y_acceleration" to MetricSpec("Aceleración Y", "Accel Y", "m/s²"),
        "z_acceleration" to MetricSpec("Aceleración Z", "Accel Z", "m/s²"),

        //  Raw
        "raw" to MetricSpec("Dato Crudo", "Raw", decimals = 0)
    )

    // -------- NAME --------
    fun formatName(metric: String, locale: Locale): String {
        val spec = specs[metric.lowercase()]
        return when {
            spec != null && isSpanish(locale) -> spec.nameEs
            spec != null -> spec.nameEn
            else -> metric.replaceFirstChar { it.uppercase() }
        }
    }

    // -------- VALUE --------
    fun formatValue(metric: String, value: Double, locale: Locale): String {
        val spec = specs[metric.lowercase()]

        val decimals = spec?.decimals ?: 2
        val formatted = String.format(locale, "%.${decimals}f", value)

        return if (spec?.unit != null) {
            "$formatted ${spec.unit}"
        } else {
            formatted
        }
    }

    private fun isSpanish(locale: Locale): Boolean {
        return locale.language == "es"
    }
}