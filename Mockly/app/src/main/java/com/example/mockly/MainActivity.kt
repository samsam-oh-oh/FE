package com.example.mockly

import MypageFragment
import RankingFragment
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mockly.ChatFragment
import com.example.mockly.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide() // 액션바 숨기기

        if (savedInstanceState == null) {
            showLoginFragment()
            //showMainActivity()

        }
    }


    private fun showLoginFragment() {
        binding.mainBnv.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, LoginFragment())  // ✅ 진짜 로그인 화면 띄움
            .commitAllowingStateLoss()
    }


    fun showMainActivity() {
        binding.mainBnv.visibility = View.VISIBLE
        initBottomNavigation()

        // 백스택에서 LoginFragment 제거 (옵션)
        supportFragmentManager.popBackStack()
    }


    private fun initBottomNavigation() {
        // ✅ 선택 리스너 잠시 제거 (꼼꼼하게)
        binding.mainBnv.setOnItemSelectedListener(null)

        // ✅ 가장 먼저 선택된 메뉴 명시
        binding.mainBnv.selectedItemId = R.id.scheduleFragment

        // ✅ 리스너 다시 설정
        binding.mainBnv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.scheduleFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, ChatFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.goalFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, RankingFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.mypageFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MypageFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

}
