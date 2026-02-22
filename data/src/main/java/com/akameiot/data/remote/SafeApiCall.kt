package com.akameiot.data.remote

import com.akameiot.domain.exceptions.SessionExpiredException
import com.akameiot.domain.exceptions.ActivationCodeInvalidException
import retrofit2.HttpException
import org.json.JSONObject

suspend inline fun <T> safeApiCall(
    crossinline apiCall: suspend () -> T
): T {
    try {
        return apiCall()
    } catch (e: HttpException) {

        when (e.code()) {

            401 -> {
                throw SessionExpiredException()
            }

            404 -> {
                val errorBody = e.response()?.errorBody()?.string()
                val message = errorBody?.let {
                    JSONObject(it).optString("message")
                }

                if (message == "activation code invalid") {
                    throw ActivationCodeInvalidException()
                }

                throw e
            }

            else -> throw e
        }
    }
}