package com.akameiot.app

import android.app.Application
import android.util.Log
import com.amplifyframework.core.Amplify
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.akameiot.di.AppModule
import com.google.firebase.FirebaseApp

class AkameApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppModule.init(this)

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
        } catch (e: Exception) {
            Log.e("AmplifyInit", "Failed", e)
        }
    }
}