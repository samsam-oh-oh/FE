
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import com.example.mockly.R
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
        }
    }


    private fun showLoginFragment() {
        binding.mainBnv.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, ScheduleFragment())  // 여기서 main_frm 사용
            .commitAllowingStateLoss()
    }

    fun showMainActivity() {
        binding.mainBnv.visibility = View.VISIBLE
        initBottomNavigation()
    }

    private fun initBottomNavigation() {
        // Set default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, ScheduleFragment())  // 여기서 main_frm 사용
            .commitAllowingStateLoss()

        // Handle bottom navigation item selection
        binding.mainBnv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.scheduleFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, ScheduleFragment())  // 여기서 main_frm 사용
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.goalFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, GoalFragment())  // 여기서 main_frm 사용
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }


                R.id.mypageFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MypageFragment())  // 여기서 main_frm 사용
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }
}
