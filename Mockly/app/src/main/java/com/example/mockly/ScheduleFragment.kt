

import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mockly.databinding.FragmentMypageBinding

import com.example.mockly.databinding.FragmentScheduleBinding

import java.util.*

class ScheduleFragment : Fragment() {

        private lateinit var binding: FragmentMypageBinding

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {


            binding = FragmentMypageBinding.inflate(inflater, container, false)
            return binding.root
        }
    }

