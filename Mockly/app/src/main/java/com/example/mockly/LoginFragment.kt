package com.example.mockly

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mockly.databinding.FragmentLoginBinding
import com.kakao.sdk.user.UserApiClient
import okhttp3.OkHttpClient
import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // ✅ 게스트 로그인 → IntroFragment
        binding.googleLoginButton.setOnClickListener {
            Toast.makeText(requireContext(), "게스트 로그인 시도 중...", Toast.LENGTH_SHORT).show()
            guestLogin()
        }

        // ✅ 카카오 로그인 버튼
        binding.kakaoLoginButton.setOnClickListener {
            kakaoLogin()
        }

        return binding.root
    }

    private fun kakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
            UserApiClient.instance.loginWithKakaoTalk(requireContext()) { token, error ->
                if (error != null) {
                    Log.e("KakaoLogin", "카카오톡 로그인 실패: ${error.message}")
                    loginWithKakaoAccount()
                } else if (token != null) {
                    Log.i("KakaoLogin", "카카오톡 로그인 성공: ${token.accessToken}")
                    goToIntro()
                }
            }
        } else {
            loginWithKakaoAccount()
        }
    }

    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(requireContext()) { token, error ->
            if (error != null) {
                Toast.makeText(requireContext(), "카카오 계정 로그인 실패", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                goToIntro()
            }
        }
    }

    private fun guestLogin() {
        val adminCode = "b91c260571b524bf1e433c81cdc05d9d68bcbc2bd32b63124ab11ebf1cf8cf4d"
        val bodyJson = JSONObject().apply { put("adminCode", adminCode) }

        val requestBody = bodyJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://13.209.230.38/auth/login/admin")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val data = json.optJSONObject("data")
                val token = data?.optString("accessToken") ?: ""

                if (token.isNotEmpty()) {
                    // 🔹 1단계: 토큰 저장
                    val prefs = requireActivity().getSharedPreferences("mockly_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("token", token).commit()

                    // 🔹 2단계: 포인트 가져오기 API 호출
                    getUserPoint(token) {
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "✅ 로그인 성공", Toast.LENGTH_SHORT).show()
                            goToIntro()
                        }
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "❌ 로그인 실패 (토큰 없음)", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("GuestLogin", "❌ 서버 연결 실패", e)
            }
        })
    }
    private fun getUserPoint(token: String, onComplete: () -> Unit) {
        val pointRequest = Request.Builder()
            .url("http://13.209.230.38/points/me")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        client.newCall(pointRequest).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()?.trim() ?: ""
                Log.d("PointFetch", "✅ 원시 응답: $responseBody")

                val point = try {
                    val json = JSONObject(responseBody)
                    json.optInt("data", 0)  // ✅ "data" 필드에 들어있는 포인트만 파싱
                } catch (e: Exception) {
                    Log.e("PointFetch", "❌ 정수 추출 실패", e)
                    0
                }

                Log.d("PointFetch", "✅ 최종 포인트: $point")

                val prefs = requireActivity().getSharedPreferences("mockly_prefs", Context.MODE_PRIVATE)
                prefs.edit().putInt("point", point).apply()

                onComplete()
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("PointFetch", "❌ 포인트 불러오기 실패", e)
                onComplete()
            }
        })
    }


    private fun goToIntro() {
        if (!isAdded || activity == null) return
        (activity as? MainActivity)?.showIntroFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}