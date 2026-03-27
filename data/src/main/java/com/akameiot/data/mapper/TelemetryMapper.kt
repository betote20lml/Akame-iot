package com.akameiot.data.mapper

import com.akameiot.data.local.entity.TelemetryEntity
import com.akameiot.data.remote.dto.TelemetryDto

fun TelemetryDto.toEntities(): List<TelemetryEntity> {

    val base = "${meshid}_${nodeId}_${timestamp}"

    val metrics = listOf(
        "humidity"          to humidity,
        "raw"               to raw?.toDouble(),
        "soil_moisture"     to soil_moisture,
        "soil_temperature"  to soil_temperature,
        "soil_ph"           to soil_ph,
        "soil_ec"           to soil_ec,
        "soil_nitrogen"     to soil_nitrogen,
        "soil_phosphorus"   to soil_phosphorus,
        "soil_potassium"    to soil_potassium,
        "soil_salinity"     to soil_salinity,
        "air_temperature"   to air_temperature,
        "air_humidity"      to air_humidity,
        "air_pressure"      to air_pressure,
        "wind_speed"        to wind_speed,
        "rainfall"          to rainfall,
        "solar_radiation"   to solar_radiation,
        "co2_level"         to co2_level,
        "leaf_wetness"      to leaf_wetness,
        "pm1"               to pm1,
        "pm2_5"             to pm2_5,
        "pm10"              to pm10,
        "voc"               to voc,
        "o3_level"          to o3_level,
        "no2_level"         to no2_level,
        "so2_level"         to so2_level,
        "geiger_counter"    to geiger_counter,
        "battery_voltage"   to battery_voltage,
        "battery_level"     to battery_level,
        "battery_health"    to battery_health,
        "signal_strength"   to signal_strength?.toDouble(),
        "device_temperature" to device_temperature,
        "uptime"            to uptime?.toDouble(),
        "latitude"          to latitude,
        "longitude"         to longitude,
        "altitude"          to altitude,
        "speed"             to speed,
        "course"            to course,
        "x_acceleration"    to x_acceleration,
        "y_acceleration"    to y_acceleration,
        "z_acceleration"    to z_acceleration,
    )


    return metrics.mapNotNull { (name, value) ->
        value?.let {
            TelemetryEntity(
                meshid = meshid,
                nodeId = nodeId,
                timestamp = timestamp,
                metric = name,
                value = it,
            )
        }
    }
}