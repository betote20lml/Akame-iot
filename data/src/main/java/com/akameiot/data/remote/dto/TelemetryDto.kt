package com.akameiot.data.remote.dto

data class TelemetryDto(
    val meshid: String,
    val timestamp: Long,
    val nodeId: Int,


    // Ambiente
    val humidity: Double?,
    val raw: Int?,
    val soil_moisture: Double?,
    val soil_temperature: Double?,
    val soil_ph: Double?,
    val soil_ec: Double?,
    val soil_nitrogen: Double?,
    val soil_phosphorus: Double?,
    val soil_potassium: Double?,
    val soil_salinity: Double?,
    val air_temperature: Double?,
    val air_humidity: Double?,
    val air_pressure: Double?,
    val wind_speed: Double?,
    val rainfall: Double?,
    val solar_radiation: Double?,
    val co2_level: Double?,
    val leaf_wetness: Double?,

    // Calidad del aire
    val pm1: Double?,
    val pm2_5: Double?,
    val pm10: Double?,
    val voc: Double?,
    val o3_level: Double?,
    val no2_level: Double?,
    val so2_level: Double?,
    val geiger_counter: Double?,

    // Dispositivo
    val battery_voltage: Double?,
    val battery_level: Double?,
    val battery_health: Double?,
    val signal_strength: Int?,
    val device_temperature: Double?,
    val uptime: Long?,

    // GPS
    val latitude: Double?,
    val longitude: Double?,
    val altitude: Double?,
    val speed: Double?,
    val course: Double?,

    // Acelerómetro
    val x_acceleration: Double?,
    val y_acceleration: Double?,
    val z_acceleration: Double?,
)