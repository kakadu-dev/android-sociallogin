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
import androidx.annotation.WorkerThread
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject


internal class VkModule(
    private val extraPermissions: Array<out String>,
    override val listener: ILoginListener
) : ISocialModule {
    private val vkCallback = object : VKAuthCallback {
        override fun onLogin(token: VKAccessToken) {
            listener.onLoginResult(
                LoginResult(
                    true,
                    SocialLogin.LoginType.VK,
                    id = token.userId.toString(),
                    token = token.accessToken,
                    email = token.email
                )
            )
        }

        override fun onLoginFailed(authException: VKAuthException) {
            if (authException.isCanceled) {
                listener.onLoginResult(null)
            } else {
                listener.onLoginResult(
                    LoginResult(
                        false,
                        SocialLogin.LoginType.VK,
                        errorMessage = "Error"
                    )
                )
            }
        }
    }

    override fun login(activity: Activity) {
        val permissions = mutableSetOf(VKScope.OFFLINE, VKScope.EMAIL)
        permissions += extraPermissions.map { VKScope.valueOf(it) }
        VK.login(activity, permissions)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return VK.onActivityResult(
            requestCode,
            resultCode,
            data,
            vkCallback
        )
    }

    companion object {
        @WorkerThread
        fun getWithUserInfo(orig: LoginResult): LoginResult {
            if (!orig.isSuccess) return orig
            val request = object : VKRequest<JSONObject>("users.get") {
                init {
                    addParam("fields", "photo_200")
                }

                override fun parse(r: JSONObject): JSONObject {
                    val users = r.getJSONArray("response")
                    return users.getJSONObject(0)
                }
            }
            val response = VK.executeSync(request)
            val firstName = response.optString("first_name").takeIf { it.isNotEmpty() }
            val lastName = response.optString("last_name").takeIf { it.isNotEmpty() }
            val avatar = response.optString("photo_200").takeIf { it.isNotEmpty() }
            return orig.copy(
                firstName = firstName,
                lastName = lastName,
                avatar = avatar
            )
        }
    }
}
