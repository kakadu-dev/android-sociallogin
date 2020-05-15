/*
 * Copyright (c) 2019 Kakadu Development
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bz.kakadu.sociallogin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.WorkerThread
import bz.kakadu.sociallogin.SocialLogin.LoginType.FB
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

internal class FacebookModule(
    private val extraPermissions: Array<out String>,
    override val listener: ILoginListener
) : ISocialModule {
    private val fbCallbackManager = CallbackManager.Factory.create()
    private val fbCallback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
            handleFbLoginToken(loginResult.accessToken)
        }

        override fun onCancel() {
            listener.onLoginResult(null)
        }

        override fun onError(error: FacebookException) {
            listener.onLoginResult(
                LoginResult(
                    false,
                    FB,
                    errorMessage = error.localizedMessage
                )
            )
        }
    }


    override fun login(activity: Activity) {
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken != null && !accessToken.isExpired) {
            handleFbLoginToken(accessToken)
        } else {
            LoginManager.getInstance().registerCallback(fbCallbackManager, fbCallback)
            val permissions = mutableSetOf("public_profile", "email")
            permissions += extraPermissions
            LoginManager.getInstance()
                .logInWithReadPermissions(activity, permissions)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return fbCallbackManager.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }

    private fun handleFbLoginToken(token: AccessToken) {
        listener.onLoginResult(
            LoginResult(
                true,
                FB,
                id = token.userId,
                token = token.token
            )
        )
    }

    companion object {
        fun logout() {
            LoginManager.getInstance().logOut()
        }

        @WorkerThread
        fun getWithUserInfo(orig: bz.kakadu.sociallogin.LoginResult): bz.kakadu.sociallogin.LoginResult {
            if (!orig.isSuccess) return orig
            val accessToken = AccessToken.getCurrentAccessToken()!!
            val req = GraphRequest.newMeRequest(accessToken, null)
            val parameters = Bundle()
            parameters.putString("fields", "email,first_name,last_name,picture")
            req.parameters = parameters
            val response = req.executeAndWait().jsonObject
            val email = response.optString("email").takeIf { it.isNotEmpty() }
            val firstName = response.optString("first_name").takeIf { it.isNotEmpty() }
            val lastName = response.optString("last_name").takeIf { it.isNotEmpty() }
            val avatar = response.optJSONObject("picture")
                ?.optJSONObject("data")
                ?.optString("url")?.takeIf { it.isNotEmpty() }
            return orig.copy(
                email = email,
                firstName = firstName,
                lastName = lastName,
                avatar = avatar
            )
        }
    }
}
