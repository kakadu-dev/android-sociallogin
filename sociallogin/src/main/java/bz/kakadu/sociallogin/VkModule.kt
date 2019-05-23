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
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope

internal class VkModule(override val listener: ILoginListener) : ISocialModule {
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

        override fun onLoginFailed(errorCode: Int) {
            if (errorCode == VKAuthCallback.AUTH_CANCELED) {
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
        VK.login(activity, arrayListOf(VKScope.OFFLINE, VKScope.EMAIL))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return VK.onActivityResult(
            requestCode,
            resultCode,
            data,
            vkCallback
        )
    }
}
