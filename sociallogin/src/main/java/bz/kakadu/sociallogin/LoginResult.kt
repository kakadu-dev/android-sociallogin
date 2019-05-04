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

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LoginResult(
    val isSuccess: Boolean,
    val loginType: SocialLogin.LoginType,
    val id: String? = null,
    val token: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatar: String? = null,
    val email: String? = null,
    var errorMessage: String? = null
) : Parcelable