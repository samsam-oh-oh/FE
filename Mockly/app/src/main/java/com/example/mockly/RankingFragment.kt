import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mockly.R
import com.example.mockly.RankingAdapter
import com.example.mockly.RankingItem
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RankingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankingAdapter
    private val rankingList = mutableListOf<RankingItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ranking, container, false)
        recyclerView = view.findViewById(R.id.rankingRecyclerView)

        adapter = RankingAdapter(rankingList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // 여기에 서버 통신 추가
        loadRankingFromServer()

        return view
    }

    private fun loadRankingFromServer() {
        val prefs = requireContext().getSharedPreferences("mockly_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: return
        val pointAmount = prefs.getInt("point", -999)
        Log.d("PointDebug", "✅ 불러온 포인트: $pointAmount")
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://13.209.230.38/scores/rank")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string() ?: return
                Log.d("Ranking", "📦 응답 JSON: $jsonString")

                val json = JSONObject(jsonString)
                val dataArray = json.optJSONArray("data") ?: return

                // ✅ SharedPreferences 포인트는 여기서 가져와야 정확함
                val pointAmount = requireContext()
                    .getSharedPreferences("mockly_prefs", Context.MODE_PRIVATE)
                    .getInt("point", 0)

                val result = mutableListOf<RankingItem>()
                for (i in 0 until dataArray.length()) {
                    val obj = dataArray.getJSONObject(i)

                    val nickname = obj.optString("nickname")
                    val maxScore = obj.optDouble("score", 0.0)
                    val feedback = obj.optString("feedback", "피드백 없음")

                    result.add(
                        RankingItem(
                            rank = i + 1,
                            nickname = nickname,
                            maxScore = maxScore,
                            feedback = feedback,
                            userPoint = pointAmount  // ✅ 정확히 30으로 나올 것
                        )
                    )
                }

                activity?.runOnUiThread {
                    rankingList.clear()
                    rankingList.addAll(result)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("Ranking", "불러오기 실패: ${e.message}")
            }
        })
    }

}
