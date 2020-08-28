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
import android.os.Bundle
import android.util.Log
import androidx.annotation.WorkerThread

class SocialLogin : Activity(), ILoginListener {
    private lateinit var loginType: LoginType
    private lateinit var socialModule: ISocialModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginType = intent.loginType!!
        val permissions = intent.permissions!!
        socialModule = when (loginType) {
            LoginType.GOOGLE -> GoogleModule(this)
            LoginType.FB -> FacebookModule(permissions, this)
            LoginType.VK -> VkModule(permissions, this)
            LoginType.HUAWEI -> HuaweiModule(this)
        }
        if (savedInstanceState == null) {
            socialModule.login(this)
        }
    }


    override fun onLoginResult(result: LoginResult?) {
        val intent = Intent()
        intent.loginResult = result
        setResult(RESULT_OK, intent)
        finish()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!socialModule.onActivityResult(requestCode, resultCode, data)) finish()
    }

    enum class LoginType {
        GOOGLE,
        FB,
        VK,
        HUAWEI
    }

    companion object {
        /**
         * Get date from result intent: val result: SocialLogin.LoginResult? = data.loginResult
         */
        fun loginIntent(context: Context, loginType: LoginType, vararg extraPermissions: String) =
            Intent(context, SocialLogin::class.java)
                .also {
                    it.loginType = loginType
                    it.permissions = extraPermissions
                }

        /**
         * Work only for Facebook and Google
         */
        fun logout(context: Context, loginType: LoginType) {
            try {
                when (loginType) {
                    LoginType.FB -> FacebookModule.logout()
                    LoginType.GOOGLE -> GoogleModule.logout(context)
                    LoginType.HUAWEI -> HuaweiModule.logout(context)
                    else -> Log.w("SocialLogin", "Unsupported logout for $loginType")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @WorkerThread
        fun getResultWithUserInfo(orig: LoginResult): LoginResult {
            return when (orig.loginType) {
                LoginType.GOOGLE -> orig
                LoginType.FB -> FacebookModule.getWithUserInfo(orig)
                LoginType.VK -> VkModule.getWithUserInfo(orig)
                else -> {
                    Log.w("SocialLogin", "Unsupported user info for ${orig.loginType}")
                    orig
                }
            }
        }

    }
}

private var Intent.loginType: SocialLogin.LoginType?
    set(value) {
        putExtra("loginType", value?.ordinal ?: -1)
    }
    get() = getIntExtra("loginType", -1)
        .takeIf { it != -1 }?.let { SocialLogin.LoginType.values()[it] }

private var Intent.permissions: Array<out String>?
    set(value) {
        putExtra("permissions", value)
    }
    get() = getStringArrayExtra("permissions")

var Intent.loginResult: LoginResult?
    set(value) {
        putExtra("loginResult", value)
    }
    get() = getParcelableExtra("loginResult")
