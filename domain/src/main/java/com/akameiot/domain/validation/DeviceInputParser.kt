package com.akameiot.domain.validation


sealed class DeviceInput {

    data class ActivationCode(val value: String) : DeviceInput()

    data class ThingName(val value: String) : DeviceInput()

    object Invalid : DeviceInput()
}

object DeviceInputParser {

    private val activationRegex =
        Regex("^[A-Z0-9]{3}-[A-Z0-9]{10}$")

    private val thingRegex =
        Regex("gw_[0-9a-f]{32}", RegexOption.IGNORE_CASE)

    fun parse(input: String): DeviceInput {

        val normalized =
            input
                .trim()
                .replace("\\s".toRegex(), "")
                .replace("\u200B", "")

        val activationCandidate = normalized.uppercase()
        if (activationRegex.matches(activationCandidate)) {
            return DeviceInput.ActivationCode(activationCandidate)
        }

        if (thingRegex.matches(normalized)) {
            return DeviceInput.ThingName(normalized.lowercase())
        }

        return DeviceInput.Invalid
    }
}