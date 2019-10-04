
# android-sociallogin
[![Release](https://jitpack.io/v/kakadu-dev/android-sociallogin.svg)](https://jitpack.io/#kakadu-dev/android-sociallogin)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

Easy login with Google, Facebook, VK for android apps

## Use:

```kotlin
    fun loginWithGoogle() {
        startActivityForResult(SocialLogin.loginIntent(this, LoginType.GOOGLE), 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: LoginResult? = data?.loginResult
        // do something
    }
```

## Setup

#### First you need to register your app:
* [Google](https://developers.google.com/identity/sign-in/android/start-integrating)
* [Facebook](https://developers.facebook.com/docs/android/getting-started/)
* [VK](https://vk.com/dev/android_sdk)

#### Then add the received resources to your app:
```xml
<resources>

  <!--For Google (optional)-->
  <string name="default_web_client_id">...</string>

  <!--For Facebook (required)-->
  <string name="facebook_app_id">...</string>
  <string name="fb_login_protocol_scheme">...</string>

  <!--For VK (required)-->
  <integer name="com_vk_sdk_AppId">...</integer>

</resources>
```

#### Add the dependency in your `build.gradle` file:
  
```gradle  
repositories {  
    maven { url "https://jitpack.io" }  
}  
  
dependencies { 
	// For all social networks 
	implementation 'com.github.kakadu-dev:android-sociallogin:1.0.3'
	
	// or exclude some
    implementation('com.github.kakadu-dev:android-sociallogin:1.0.3') {
        exclude group: 'com.vk' //without VK  
        exclude group: 'com.facebook.android' //without Facebook  
        exclude group: 'com.google.android.gms' //without Google  
    }  
}  
```