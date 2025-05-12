package com.example.mockly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mockly.databinding.FragmentLoginBinding
import com.kakao.sdk.user.UserApiClient
import android.util.Log

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // ✅ "게스트로 로그인" 버튼
        binding.googleLoginButton.setOnClickListener {
            (activity as? MainActivity)?.apply {
                showMainActivity()
                supportFragmentManager.beginTransaction()
                    .remove(this@LoginFragment)
                    .commitAllowingStateLoss()
            }
        }

        // ✅ "카카오 로그인" 버튼
        binding.kakaoLoginButton.setOnClickListener {
            // 1. 카카오톡 앱 로그인 시도
            UserApiClient.instance.loginWithKakaoTalk(requireContext()) { token, error ->
                if (error != null) {
                    Log.w("KakaoLogin", "카카오톡 로그인 실패, 웹으로 대체: ${error.message}")

                    // 2. 카카오 계정(웹뷰) 로그인 fallback
                    UserApiClient.instance.loginWithKakaoAccount(requireContext()) { tokenWeb, errorWeb ->
                        if (errorWeb != null) {
                            Log.e("KakaoLogin", "카카오 계정 로그인 실패", errorWeb)
                            Toast.makeText(requireContext(), "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                        } else if (tokenWeb != null) {
                            Log.i("KakaoLogin", "카카오 계정 로그인 성공: ${tokenWeb.accessToken}")
                            (activity as? MainActivity)?.apply {
                                showMainActivity()
                                supportFragmentManager.beginTransaction()
                                    .remove(this@LoginFragment)
                                    .commitAllowingStateLoss()
                            }
                        }
                    }
                } else if (token != null) {
                    Log.i("KakaoLogin", "카카오톡 로그인 성공: ${token.accessToken}")
                    (activity as? MainActivity)?.apply {
                        showMainActivity()
                        supportFragmentManager.beginTransaction()
                            .remove(this@LoginFragment)
                            .commitAllowingStateLoss()
                    }
                }
            }
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
