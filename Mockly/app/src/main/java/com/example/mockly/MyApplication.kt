package com.example.mockly

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "ca6d1e6569b7ffe1d115bf8e49633d5d")  // ← 콘솔에서 복사한 앱 키
    }
}
