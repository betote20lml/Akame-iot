package com.akameiot.data.network

import com.akameiot.data.fcm.FcmTokenProvider
import com.akameiot.data.session.DeviceNetworkStore
import com.akameiot.domain.usecase.SubscribeToDeviceTopicUseCase
import com.akameiot.domain.model.Network

class NetworkManager(
    private val networkStore: DeviceNetworkStore,
    private val subscribeToDeviceTopicUseCase: SubscribeToDeviceTopicUseCase,
    private val fcmTokenProvider: FcmTokenProvider
) {

    suspend fun subscribeNetwork(
        authToken: String,
        thingName: String,
        displayName: String
    ) {

        val fcmToken = fcmTokenProvider.getToken()

        subscribeToDeviceTopicUseCase(
            authToken,
            thingName,
            fcmToken
        )

        networkStore.addNetwork(
            Network(
                thingName = thingName,
                displayName = displayName
            )
        )
    }

    suspend fun getNetworks(): List<Network> {
        return networkStore.getNetworks()
    }

    suspend fun resubscribeAll(authToken: String) {

        val networks = networkStore.getNetworks()
        val fcmToken = fcmTokenProvider.getToken()


        networks.forEach { network ->

            subscribeToDeviceTopicUseCase(
                authToken,
                network.thingName,
                fcmToken
            )
        }
    }

    suspend fun removeNetwork(thingName: String) {
        networkStore.removeNetwork(thingName)
    }
}