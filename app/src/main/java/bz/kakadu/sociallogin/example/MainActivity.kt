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

package bz.kakadu.sociallogin.example

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import bz.kakadu.sociallogin.LoginResult
import bz.kakadu.sociallogin.SocialLogin
import bz.kakadu.sociallogin.loginResult

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun loginWithGoogle(v: View) {
        startActivityForResult(
            SocialLogin.loginIntent(v.context, SocialLogin.LoginType.GOOGLE),
            REQUEST_LOGIN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: LoginResult? = data?.loginResult
        when (requestCode) {
            REQUEST_LOGIN -> {
                if (data != null) {
                    val result = data.loginResult
                    AlertDialog.Builder(this)
                        .setMessage(result.toString())
                        .setTitle("Login result")
                        .setPositiveButton(android.R.string.ok, null)
                        .apply {
                            if (result?.isSuccess == true) {
                                setNeutralButton("Logout") { _, _ ->
                                    SocialLogin.logout(
                                        this@MainActivity,
                                        SocialLogin.LoginType.GOOGLE
                                    )
                                }
                            }
                        }
                        .show()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}

private const val REQUEST_LOGIN = 433
