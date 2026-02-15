package com.akameiot.data.remote

import com.akameiot.domain.model.RegisterResult
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.step.AuthSignInStep
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

    suspend fun confirmSignUp(
        email: String,
        code: String
    ) = suspendCancellableCoroutine<Unit> { cont ->

        Amplify.Auth.confirmSignUp(
            email,
            code,
            { result ->
                if (cont.isActive) {
                    if (result.isSignUpComplete) {
                        cont.resume(Unit)
                    } else {
                        cont.resumeWithException(
                            Exception("No se pudo confirmar la cuenta")
                        )
                    }
                }
            },
            { error ->
                if (cont.isActive) {
                    cont.resumeWithException(error)
                }
            }
        )
    }

    suspend fun login(
        email: String,
        password: String
    ) = suspendCancellableCoroutine<Unit> { cont ->

        Amplify.Auth.signIn(
            email,
            password,
            { result ->
                if (cont.isActive) {

                    when (result.nextStep.signInStep) {

                        AuthSignInStep.DONE -> {
                            cont.resume(Unit)
                        }

                        else -> {
                            cont.resumeWithException(
                                Exception("Se requiere paso adicional: ${result.nextStep.signInStep}")
                            )
                        }
                    }
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