package com.akameiot.app

import android.app.Application
import android.util.Log
import com.amplifyframework.core.Amplify
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.akameiot.di.AppModule

class AkameApp : Application() {

    override fun onCreate() {
        AppModule.init(this)
        super.onCreate()

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)

        } catch (e: Exception) {
            Log.e("AmplifyInit", "Failed", e)
        }
    }
}