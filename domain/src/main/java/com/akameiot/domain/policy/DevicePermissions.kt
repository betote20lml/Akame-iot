package com.akameiot.domain.policy

import com.akameiot.domain.model.AppUser

object DevicePermissions {

    fun canActivateDevice(user: AppUser): Boolean {
        return user is AppUser.Owner
    }

    fun canLinkDevice(user: AppUser): Boolean {
        return user is AppUser.Owner || user is AppUser.Limited
    }

    fun canRecoverHistoricalData(user: AppUser): Boolean {
        return user is AppUser.Owner
    }

}