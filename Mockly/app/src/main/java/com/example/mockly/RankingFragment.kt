import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mockly.R
import com.example.mockly.RankingAdapter
import com.example.mockly.RankingItem

class RankingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranking, container, false)
        recyclerView = view.findViewById(R.id.rankingRecyclerView)

        val sampleData = listOf(
            RankingItem(1, "CastleTiger", 95.6),
            RankingItem(2, "GodSilver", 92.6),
            RankingItem(3, "Mr.Song", 90.6),
            RankingItem(4, "PSJ", 13.6),
            RankingItem(5, "Hoon", 7.6)
        )

        adapter = RankingAdapter(sampleData)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.bottom = 16
                }
            }
        )

        return view
    }
}


