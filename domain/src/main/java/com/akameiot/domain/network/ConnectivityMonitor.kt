package com.akameiot.domain.network


interface ConnectivityMonitor {
    fun isOnline(): Boolean
}