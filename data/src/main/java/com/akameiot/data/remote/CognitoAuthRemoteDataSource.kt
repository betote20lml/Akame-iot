package com.akameiot.data.remote

import kotlinx.coroutines.suspendCancellableCoroutine
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignUpResult
import com.amplifyframework.core.Amplify
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CognitoAuthRemoteDataSource {

    suspend fun register(
        email: String,
        password: String
    ): AuthSignUpResult = suspendCancellableCoroutine { cont ->

        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.email(), email)
            .build()

        Amplify.Auth.signUp(
            email,
            password,
            options,
            { cont.resume(it) },
            { cont.resumeWithException(it) }
        )
    }
}
