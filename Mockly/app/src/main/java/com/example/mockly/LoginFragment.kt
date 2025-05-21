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

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // ✅ 게스트 로그인 → IntroFragment
        binding.googleLoginButton.setOnClickListener {
            (activity as? MainActivity)?.showIntroFragment()
        }

        // ✅ 카카오 로그인 버튼
        binding.kakaoLoginButton.setOnClickListener {
            kakaoLogin()
        }

        return binding.root
    }

    private fun kakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
            // 카카오톡으로 로그인
            UserApiClient.instance.loginWithKakaoTalk(requireContext()) { token, error ->
                if (error != null) {
                    Log.e("KakaoLogin", "카카오톡 로그인 실패 → 계정으로 재시도: ${error.message}")
                    loginWithKakaoAccount()
                } else if (token != null) {
                    Log.i("KakaoLogin", "카카오톡 로그인 성공: ${token.accessToken}")
                    goToIntro()
                }
            }
        } else {
            // 카카오 계정으로 로그인 (웹뷰)
            loginWithKakaoAccount()
        }
    }

    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(requireContext()) { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "카카오 계정 로그인 실패: ${error.message}", error)
                Toast.makeText(requireContext(), "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                Log.i("KakaoLogin", "카카오 계정 로그인 성공: ${token.accessToken}")
                goToIntro()
            } else {
                Log.e("KakaoLogin", "로그인 실패: 토큰도 에러도 없음 (이상한 상태)")
            }
        }
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
