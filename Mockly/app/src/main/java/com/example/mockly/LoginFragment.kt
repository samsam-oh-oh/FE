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

        val bodyJson = JSONObject().apply {
            put("adminCode", adminCode)  // ✅ 정확한 key 이름 사용
        }

        Log.d("GuestLogin", "요청 JSON: $bodyJson")

        val requestBody = bodyJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://13.209.230.38/auth/login/admin")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d("GuestLogin", "서버 응답: $responseBody")

                try {
                    val json = JSONObject(responseBody)
                    val data = json.optJSONObject("data")
                    val token = data?.optString("accessToken") ?: ""

                    activity?.runOnUiThread {
                        if (token.isNotEmpty()) {
                            val prefs = requireActivity().getSharedPreferences("mockly_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putString("token", token).apply()
                            Toast.makeText(requireContext(), "✅ 로그인 성공", Toast.LENGTH_SHORT).show()
                            goToIntro()
                        } else {
                            Toast.makeText(requireContext(), "❌ 로그인 실패 (토큰 없음)", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GuestLogin", "JSON 파싱 오류", e)
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "❌ 응답 파싱 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("GuestLogin", "❌ 서버 연결 실패", e)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "❌ 서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
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