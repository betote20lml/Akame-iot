package com.akameiot.data.session


import com.akameiot.domain.session.AuthSessionManager
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.cognito.options.AWSCognitoAuthSignInOptions
import com.amplifyframework.auth.result.step.AuthSignInStep
import kotlin.coroutines.resumeWithException
import com.amplifyframework.auth.cognito.options.AuthFlowType


class CognitoAuthSessionManager : AuthSessionManager {

    override suspend fun isUserLoggedIn(): Boolean {
        return suspendCancellableCoroutine { cont ->
            Amplify.Auth.fetchAuthSession(
                { result ->
                    cont.resume(result.isSignedIn)
                },
                { error ->
                    cont.resume(false)
                }
            )
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return suspendCancellableCoroutine { cont ->
            Amplify.Auth.getCurrentUser(
                { user -> cont.resume(user.userId) },
                { cont.resume(null) }
            )
        }
    }

    override suspend fun logout() {
        suspendCancellableCoroutine { cont ->
            val options = AuthSignOutOptions.builder().globalSignOut(true).build()
            Amplify.Auth.signOut(options) { result ->
                cont.resume(Unit)
            }
        }
    }

    override suspend fun fetchIdToken(): String =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.fetchAuthSession(
                { session ->
                    val cognitoSession = session as? AWSCognitoAuthSession
                    val idToken = cognitoSession
                        ?.userPoolTokensResult   // ← nombre correcto
                        ?.value
                        ?.idToken

                    if (idToken != null) {
                        cont.resume(idToken)
                    } else {
                        cont.resumeWithException(
                            Exception("No se pudo obtener el ID Token — sesión no activa")
                        )
                    }
                },
                { error -> cont.resumeWithException(error) }
            )
        }

    override suspend fun signInWithCustomAuth(username: String, token: String) {
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.signIn(
                username,
                null,
                AWSCognitoAuthSignInOptions.builder()
                    .authFlowType(AuthFlowType.CUSTOM_AUTH)
                    .build(),
                { result ->
                    if (result.isSignedIn) {
                        cont.resume(Unit)
                    } else if (result.nextStep.signInStep == AuthSignInStep.CONFIRM_SIGN_IN_WITH_CUSTOM_CHALLENGE) {
                        Amplify.Auth.confirmSignIn(
                            token,
                            { confirmResult ->
                                if (confirmResult.isSignedIn) {
                                    cont.resume(Unit)
                                } else {
                                    cont.resumeWithException(Exception("Custom auth falló"))
                                }
                            },
                            { error -> cont.resumeWithException(error) }
                        )
                    } else {
                        cont.resumeWithException(Exception("Paso inesperado: ${result.nextStep.signInStep}"))
                    }
                },
                { error -> cont.resumeWithException(error) }
            )
        }
    }
}


