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
import android.content.Context
import android.content.Intent
import android.util.Log
import bz.kakadu.sociallogin.SocialLogin.LoginType.GOOGLE
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException

internal class GoogleModule(override val listener: ILoginListener) : ISocialModule {
    override fun login(activity: Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .apply {
                val clientId = activity.getString(R.string.google_default_web_client_id)
                if (clientId.isNotEmpty()) {
                    requestIdToken(clientId)
                } else {
                    Log.w(
                        "SocialLogin",
                        "Can't find R.string.default_web_client_id for request id_token"
                    )
                }
            }
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, REQUEST_GOOGLE_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        when (requestCode) {
            REQUEST_GOOGLE_LOGIN -> {
                var result: LoginResult? = null
                if (data != null) try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)!!
                    result = LoginResult(
                        true,
                        GOOGLE,
                        id = account.id,
                        email = account.email,
                        firstName = account.givenName,
                        lastName = account.familyName,
                        token = account.idToken
                    )
                } catch (e: Exception) {
                    Log.w("SocialLogin", "Google sign in failed", e)
                    result = if (e is ApiException) {
                        val message = GoogleSignInStatusCodes.getStatusCodeString(e.statusCode)
                        Log.w("SocialLogin", "${e.statusCode} - $message")
                        if (e.statusCode != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                            LoginResult(
                                false,
                                GOOGLE,
                                errorMessage = message
                            )
                        } else null
                    } else LoginResult(
                        false,
                        GOOGLE,
                        errorMessage = e.message
                    )

                }
                listener.onLoginResult(result)
                true
            }
            else -> false
        }

    companion object {
        fun logout(context: Context) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut()
        }
    }
}

private const val REQUEST_GOOGLE_LOGIN = 214