package com.example.mockly

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
class RankingAdapter(private val items: List<RankingItem>) :
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    private val client = OkHttpClient()

    inner class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankText: TextView = itemView.findViewById(R.id.rankText)
        val rankImage: ImageView = itemView.findViewById(R.id.rankImage)
        val nameText: TextView = itemView.findViewById(R.id.nameText)
        val scoreText: TextView = itemView.findViewById(R.id.scoreText)
        val detailLayout: LinearLayout = itemView.findViewById(R.id.detailLayout)
        val btnPoint: Button = itemView.findViewById(R.id.btnPoint)
        val tvPointLabel: TextView = itemView.findViewById(R.id.tvPointLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val item = items[position]

        holder.nameText.text = item.nickname
        holder.scoreText.text = item.totalScore.toString()

        when (item.rank) {
            1 -> {
                holder.rankImage.setImageResource(R.drawable.first_ranking)
                holder.rankImage.visibility = View.VISIBLE
                holder.rankText.visibility = View.GONE
            }
            2 -> {
                holder.rankImage.setImageResource(R.drawable.second_ranking)
                holder.rankImage.visibility = View.VISIBLE
                holder.rankText.visibility = View.GONE
            }
            3 -> {
                holder.rankImage.setImageResource(R.drawable.third_ranking)
                holder.rankImage.visibility = View.VISIBLE
                holder.rankText.visibility = View.GONE
            }
            else -> {
                holder.rankText.text = item.rank.toString()
                holder.rankImage.visibility = View.GONE
                holder.rankText.visibility = View.VISIBLE
            }
        }

        // 현재 보유 포인트 불러오기
        val prefs = holder.itemView.context.getSharedPreferences("mockly_prefs", Context.MODE_PRIVATE)
        var currentPoint = prefs.getInt("point", 0)
        holder.tvPointLabel.text = "보유 포인트: ${currentPoint}pt"
        holder.btnPoint.text = "10pt"

        holder.itemView.setOnClickListener {
            val visible = if (holder.detailLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            holder.detailLayout.visibility = visible
        }

        holder.btnPoint.setOnClickListener {
            if (currentPoint < 10) {
                Toast.makeText(holder.itemView.context, "포인트가 부족합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            deductPoint(holder.itemView.context) { newPoint ->
                // ✅ SharedPreferences 값 갱신 후 UI도 즉시 반영
                (holder.itemView.context as? Activity)?.runOnUiThread {
                    holder.tvPointLabel.text = "보유 포인트: ${newPoint}pt"
                    showFeedbackDialog(
                        holder.itemView.context,
                        item.feedback,
                        item.totalScore,
                        item.techScore,
                        item.communicateScore
                    )                }
            }
        }
    }

    override fun getItemCount() = items.size

    private fun deductPoint(context: Context, onSuccess: (Int) -> Unit) {
        val prefs = context.getSharedPreferences("mockly_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: return
        val currentPoint = prefs.getInt("point", 0)

        val jsonBody = JSONObject().apply {
            put("pointAmount", 10)
            put("reason", "피드백 열람")
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://13.209.230.38/points/deduct")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("PointDeduct", "✅ 차감 응답: $responseBody")

                // ✅ SharedPreferences에 차감 반영
                val newPoint = currentPoint - 10
                prefs.edit().putInt("point", newPoint).apply()
                onSuccess(newPoint)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("PointDeduct", "❌ 차감 실패", e)
            }
        })
    }

    private fun showFeedbackDialog(
        context: Context,
        feedbackText: String,
        totalScore: Double,
        techScore: Double,
        communicateScore: Double
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_feedback_popup, null)
        val dialog = AlertDialog.Builder(context).setView(dialogView).create()

        val tvContent = dialogView.findViewById<TextView>(R.id.tvFeedbackContent)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)
        val barChart = dialogView.findViewById<BarChart>(R.id.barChart)

        btnClose.setOnClickListener { dialog.dismiss() }

        // ✅ 막대그래프 데이터
        val entries = listOf(
            BarEntry(0f, techScore.toFloat()),           // index 0 → 아래쪽 막대
            BarEntry(1f, communicateScore.toFloat())     // index 1 → 위쪽 막대
        )
        val labels = listOf("기술부분", "소통부분")


        val dataSet = BarDataSet(entries, "").apply {
            valueTextSize = 14f
            setDrawValues(true)
            colors = listOf(
                Color.parseColor("#E91E63"), // 기술: 핑크
                Color.parseColor("#2196F3")  // 소통: 파랑
            )
        }

        barChart.data = BarData(dataSet).apply {
            barWidth = 0.6f
        }

        // X축 = 점수 축
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("기술부분", "소통부분"))
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textSize = 14f
        xAxis.labelCount = 2

// Y축 = 점수값 (세로)
        barChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 100f
            granularity = 10f
            textSize = 12f
            setDrawGridLines(true)
        }
        barChart.axisRight.isEnabled = false

        barChart.setExtraOffsets(10f, 10f, 10f, 24f)

        // 기타 스타일
        barChart.setFitBars(true)
        barChart.setScaleEnabled(false)
        barChart.setTouchEnabled(false)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.animateX(800)
        barChart.invalidate()

        // 텍스트 구성
        val formatted = formatFeedbackText(feedbackText)
        val displayText = """
        🧠 전문성 및 문제 해결력: ${"%.1f".format(techScore)}점
        🗣️ 표현력 및 커뮤니케이션 역량: ${"%.1f".format(communicateScore)}점
        
        📊 종합 점수: ${"%.1f".format(totalScore)}점

$formatted
    """.trimIndent()

        tvContent.text = displayText
        dialog.show()
    }


    private fun formatFeedbackText(raw: String): String {
        return raw
            .replace(Regex("\\.(?=\\d+\\.)"), ".\n")      // .숫자. 줄바꿈
            .replace(Regex("(?=\\s*-\\s*)"), "\n")        // - 앞 줄바꿈
            .replace(Regex("(?=\\[)"), "\n\n")            // [ 앞 줄바꿈
    }
}
