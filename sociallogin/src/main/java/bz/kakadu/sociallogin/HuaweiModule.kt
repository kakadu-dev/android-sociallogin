package bz.kakadu.sociallogin

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper

class HuaweiModule(override val listener: ILoginListener) : ISocialModule {
    override fun login(activity: Activity) {
        val authParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setIdToken()
            .setEmail()
            .createParams()
        val service = HuaweiIdAuthManager.getService(activity, authParams)
        activity.startActivityForResult(service.signInIntent, REQUEST_HUAWEI_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_HUAWEI_LOGIN && data != null) {
            HuaweiIdAuthManager.parseAuthResultFromIntent(data)
                .addOnSuccessListener { huaweiId ->
                    listener.onLoginResult(
                        LoginResult(
                            isSuccess = true,
                            loginType = SocialLogin.LoginType.HUAWEI,
                            id = huaweiId.unionId,
                            token = huaweiId.idToken,
                            firstName = huaweiId.givenName
                                ?.takeIf { it.isNotBlank() }
                                ?: huaweiId.displayName,
                            lastName = huaweiId.familyName,
                            avatar = huaweiId.avatarUriString,
                            email = huaweiId.email
                        )
                    )
                }
                .addOnFailureListener {
                    listener.onLoginResult(
                        LoginResult(
                            isSuccess = false,
                            loginType = SocialLogin.LoginType.HUAWEI,
                            errorMessage = it.message
                        )
                    )
                }
            return true
        }
        return false
    }

    companion object {
        fun logout(context: Context) {
            val authParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
                .setEmail()
                .createParams()
            val service = HuaweiIdAuthManager.getService(context, authParams)
            service.signOut()
        }
    }
}

private const val REQUEST_HUAWEI_LOGIN = 215