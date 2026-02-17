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
    ) = suspendCancellableCoroutine { cont ->

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
    ) = suspendCancellableCoroutine { cont ->

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

    suspend fun isUserLoggedIn(): Boolean =
        suspendCancellableCoroutine { cont ->

            Amplify.Auth.fetchAuthSession(
                { session ->
                    if (cont.isActive) {
                        cont.resume(session.isSignedIn)
                    }
                },
                {
                    if (cont.isActive) {
                        cont.resume(false)
                    }
                }
            )
        }

    suspend fun resendConfirmationCode(email: String) =
        suspendCancellableCoroutine { cont ->

            Amplify.Auth.resendSignUpCode(
                email,
                {
                    if (cont.isActive) {
                        cont.resume(Unit)
                    }
                },
                { error ->
                    if (cont.isActive) {
                        cont.resumeWithException(error)
                    }
                }
            )
        }

    suspend fun startResetPassword(
        email: String
    ) = suspendCancellableCoroutine { cont ->

        Amplify.Auth.resetPassword(
            email,
            { result ->
                if (cont.isActive) {
                    cont.resume(Unit)
                }
            },
            { error ->
                if (cont.isActive) {
                    cont.resumeWithException(error)
                }
            }
        )
    }

    suspend fun confirmResetPassword(
        email: String,
        code: String,
        newPassword: String
    ) = suspendCancellableCoroutine { cont ->

        Amplify.Auth.confirmResetPassword(
            email,
            newPassword,
            code,
            {
                if (cont.isActive) {
                    cont.resume(Unit)
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