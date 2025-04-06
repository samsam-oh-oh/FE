

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.mockly.R
import com.example.mockly.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {

    private lateinit var binding: FragmentMypageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentMypageBinding.inflate(inflater, container, false)

        // SharedPreferences에서 닉네임 불러오기
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val nickname = sharedPref?.getString("user_nickname", "사용자")

        // 닉네임을 UI에 설정
        binding.mypageNinkname.text = nickname


        // 로그아웃 버튼 클릭 시 팝업 띄우기
        binding.logout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        binding.accountDelete.setOnClickListener {
            showAccountDeleteConfirmationDialog()
        }



        return binding.root
    }
    private fun showLogoutConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_confirmation, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        //로그아웃 버튼 클릭 시
//        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
//            dialog.dismiss()
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.main_frm, LoginFragment())
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
//                .addToBackStack(null)
//                .commit()
//        }

        dialog.show()
    }
    private fun showAccountDeleteConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_account_delete_confirmation, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.cancelDeleteButton).setOnClickListener {
            dialog.dismiss()
        }

//        dialogView.findViewById<Button>(R.id.confirmDeleteButton).setOnClickListener {
//            dialog.dismiss()
//            // 계정 삭제 로직을 여기에 추가
//            // 예: 서버에 계정 삭제 요청, 로컬 데이터 삭제 등
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.main_frm, LoginFragment())
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
//                .addToBackStack(null)
//                .commit()
//        }

        dialog.show()
    }
}
