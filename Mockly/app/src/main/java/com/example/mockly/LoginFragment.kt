package com.example.mockly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mockly.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // ✅ "게스트로 로그인" 버튼 클릭 시 메인 화면으로 전환 + 로그인 프래그먼트 제거
        binding.googleLoginButton.setOnClickListener {
            (activity as? MainActivity)?.apply {
                showMainActivity()
                supportFragmentManager.beginTransaction()
                    .remove(this@LoginFragment)
                    .commitAllowingStateLoss()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
