package com.akameiot.data.remote

import com.akameiot.domain.model.RegisterResult
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CognitoAuthRemoteDataSource {

    suspend fun register(
        email: String,
        password: String
    ): RegisterResult = suspendCancellableCoroutine { cont ->

        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.email(), email)
            .build()

        Amplify.Auth.signUp(
            email,
            password,
            options,
            { result ->
                if (cont.isActive) {
                    cont.resume(
                        RegisterResult(
                            requiresConfirmation = !result.isSignUpComplete
                        )
                    )
                }
            },
            { error ->
                if (cont.isActive) {
                    cont.resumeWithException(error)
                }
            }
        )
    }
}