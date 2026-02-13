package com.akameiot.data.session

import com.akameiot.domain.session.AuthSessionManager
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.amplifyframework.auth.options.AuthSignOutOptions


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
        suspendCancellableCoroutine<Unit> { cont ->
            val options = AuthSignOutOptions.builder().globalSignOut(true).build()
            Amplify.Auth.signOut(options) { result ->
                cont.resume(Unit)
            }
        }
    }
}
