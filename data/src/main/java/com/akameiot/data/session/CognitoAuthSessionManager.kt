package com.akameiot.data.session


import com.akameiot.domain.exceptions.SessionExpiredException
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


class CognitoAuthSessionManager(
    private val sessionDataStore: SessionDataStore
    ) : AuthSessionManager{


    override suspend fun isUserLoggedIn(): Boolean {
        return suspendCancellableCoroutine { cont ->
            Amplify.Auth.fetchAuthSession(
                { result ->
                    if (cont.isActive) cont.resume(result.isSignedIn)
                },
                { error ->
                    if (cont.isActive) cont.resume(false)
                }
            )
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return suspendCancellableCoroutine { cont ->
            Amplify.Auth.getCurrentUser(
                { user -> if (cont.isActive) cont.resume(user.userId) },
                { if (cont.isActive) cont.resume(null) }
            )
        }
    }

    override suspend fun setLocalSession(active: Boolean) {
        sessionDataStore.setHasSession(active)
    }

    override suspend fun logout() {
        suspendCancellableCoroutine { cont ->
            val options = AuthSignOutOptions.builder().globalSignOut(true).build()
            Amplify.Auth.signOut(options) {
                if (cont.isActive) cont.resume(Unit)
            }
        }
        sessionDataStore.setHasSession(false)
        sessionDataStore.setLimitedSession(false)
    }

    override suspend fun fetchIdToken(): String =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.fetchAuthSession(
                { session ->
                    val cognitoSession = session as? AWSCognitoAuthSession
                    val idToken = cognitoSession
                        ?.userPoolTokensResult
                        ?.value
                        ?.idToken

                    if (!session.isSignedIn || idToken == null) {
                        if (cont.isActive) {
                            cont.resumeWithException(SessionExpiredException())
                        }
                    } else {
                        if (cont.isActive) cont.resume(idToken)
                    }
                },
                { error ->
                    if (cont.isActive) cont.resumeWithException(error)
                }
            )
        }

    override suspend fun isLimitedSession(): Boolean {
        return sessionDataStore.isLimitedSession()
    }

    override suspend fun setLimitedSession(isLimited: Boolean) {
        sessionDataStore.setLimitedSession(isLimited)
    }

    override suspend fun hasLocalSession(): Boolean {
        return sessionDataStore.hasSession()
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
                        if (cont.isActive) cont.resume(Unit)
                    } else if (result.nextStep.signInStep == AuthSignInStep.CONFIRM_SIGN_IN_WITH_CUSTOM_CHALLENGE) {
                        Amplify.Auth.confirmSignIn(
                            token,
                            { confirmResult ->
                                if (confirmResult.isSignedIn) {
                                    if (cont.isActive) cont.resume(Unit)
                                } else {
                                    if (cont.isActive) cont.resumeWithException(Exception("Custom auth falló"))
                                }
                            },
                            { error -> if (cont.isActive) cont.resumeWithException(error) }
                        )
                    } else {
                        if (cont.isActive) cont.resumeWithException(
                            Exception("Paso inesperado: ${result.nextStep.signInStep}")
                        )
                    }
                },
                { error -> if (cont.isActive) cont.resumeWithException(error) }
            )
        }
        sessionDataStore.setHasSession(true)
    }
}


