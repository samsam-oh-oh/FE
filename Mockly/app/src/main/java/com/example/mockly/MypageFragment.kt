package com.example.mockly

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mockly.databinding.FragmentMypageBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MypageFragment : Fragment() {

    private lateinit var binding: FragmentMypageBinding
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMypageBinding.inflate(inflater, container, false)

        loadUserInfo()

        binding.logout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        return binding.root
    }

    private fun loadUserInfo() {
        val prefs = requireActivity().getSharedPreferences("mockly_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "⚠️ 로그인 토큰이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = Request.Builder()
            .url("http://13.209.230.38/members/me")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("Mypage", "서버 응답: $body")

                try {
                    val json = JSONObject(body ?: "{}")
                    val data = json.optJSONObject("data")
                    val nickname = data?.optString("nickname") ?: "Unknown"
                    val pointAmount = data?.optInt("pointAmount", 0) ?: 0
                    val maxScore = data?.optDouble("maxScore", 0.0) ?: 0.0

                    activity?.runOnUiThread {
                        binding.mypageNinkname.text = nickname
                        binding.nameText.text = nickname
                        binding.mypagePoints.text = "${pointAmount} P"
                        binding.scoreText.text = String.format("%.1f", maxScore)
                    }
                } catch (e: Exception) {
                    Log.e("Mypage", "응답 파싱 실패", e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("Mypage", "⚠️ 사용자 정보 로딩 실패", e)
            }
        })
    }
    private fun showLogoutConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_confirmation, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        confirmButton.setOnClickListener {
            // 🔄 토큰 초기화 (선택사항)
            val prefs = requireActivity().getSharedPreferences("mockly_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            dialog.dismiss()

            // 🔁 로그인 화면으로 전환
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, LoginFragment())
                .commit()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}
