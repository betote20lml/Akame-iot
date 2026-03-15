package com.akameiot.data.remote

import com.akameiot.data.remote.api.SnsApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.akameiot.data.remote.api.SessionApi

object NetworkModule {

    private const val BASE_URL =
        "https://k1erdfmr11.execute-api.us-east-2.amazonaws.com/"

    private val logging by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }


    private val okHttp by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val deviceApi: DeviceApi by lazy {
        retrofit.create(DeviceApi::class.java)
    }

    val pairingPrivateApi: PairingApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://g1xb8zgddi.execute-api.us-east-2.amazonaws.com/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PairingApiService::class.java)
    }

    val pairingPublicApi: PairingPublicApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://y4r8gxvkkb.execute-api.us-east-2.amazonaws.com/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PairingPublicApiService::class.java)
    }

    val snsApi: SnsApi by lazy {
        retrofit.create(SnsApi::class.java)
    }

    val sessionApi: SessionApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://ikgq0r2yhl.execute-api.us-east-2.amazonaws.com/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SessionApi::class.java)
    }

}
