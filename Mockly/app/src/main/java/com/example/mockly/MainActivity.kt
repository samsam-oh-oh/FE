package com.example.mockly

import MypageFragment
import RankingFragment
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mockly.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // ✅ 앱 실행 시 가장 먼저 로그인 화면 표시
        if (savedInstanceState == null) {
            showLoginFragment()
        }
    }

    // ✅ 로그인 화면 띄우기
    private fun showLoginFragment() {
        binding.mainBnv.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, LoginFragment())
            .commitAllowingStateLoss()
    }

    // ✅ 로그인 완료 후 호출: 인트로 화면 띄우기
    fun showIntroFragment() {
        binding.mainBnv.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, IntroFragment())
            .commitAllowingStateLoss()
    }

    // ✅ 인트로에서 "면접 시작" 클릭 시 메인 바텀 네비게이션 화면 띄우기
    fun showMainActivity() {
        binding.mainBnv.visibility = View.VISIBLE
        initBottomNavigation()

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, ChatFragment()) // 기본 탭은 ChatFragment
            .commitAllowingStateLoss()
    }

    // ✅ 바텀 네비게이션 탭 설정
    private fun initBottomNavigation() {
        binding.mainBnv.setOnItemSelectedListener(null)
        binding.mainBnv.selectedItemId = R.id.scheduleFragment

        binding.mainBnv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.scheduleFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, ChatFragment())
                        .commitAllowingStateLoss()
                    true
                }

                R.id.goalFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, RankingFragment())
                        .commitAllowingStateLoss()
                    true
                }

                R.id.mypageFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MypageFragment())
                        .commitAllowingStateLoss()
                    true
                }

                else -> false
            }
        }
    }
}
