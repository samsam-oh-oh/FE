package com.example.mockly

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mockly.databinding.FragmentNicknameBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class NicknameFragment : Fragment() {

    private var _binding: FragmentNicknameBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNicknameBinding.inflate(inflater, container, false)

        binding.startButton.setOnClickListener {
            val nickname = binding.nicknameEditText.text.toString().trim()

            // 빈값 검사
            if (nickname.isEmpty()) {
                binding.nullText.visibility = View.VISIBLE
                binding.duplicateWarningText.visibility = View.GONE
                binding.duplicateSuccessText.visibility = View.GONE
                return@setOnClickListener
            }

            // PATCH 요청
            updateNickname(nickname)
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }

    private fun updateNickname(nickname: String) {
        val prefs = requireActivity().getSharedPreferences("mockly_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            Toast.makeText(requireContext(), "❌ 토큰이 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("nickname", nickname)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://13.209.230.38/members/me/nickname")
            .patch(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d("NicknameUpdate", "서버 응답: $responseBody")

                if (response.isSuccessful) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "✅ 닉네임 설정 완료", Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.showIntroFragment()
                    }
                } else {
                    activity?.runOnUiThread {
                        binding.duplicateWarningText.visibility = View.VISIBLE
                        binding.duplicateSuccessText.visibility = View.GONE
                        binding.nullText.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("NicknameUpdate", "❌ 닉네임 설정 실패", e)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "❌ 서버 요청 실패", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
