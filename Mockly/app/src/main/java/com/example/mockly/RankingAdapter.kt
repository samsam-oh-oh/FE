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
import org.json.JSONObject
import java.io.IOException

class RankingAdapter(private val items: List<RankingItem>) :
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    inner class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankText: TextView = itemView.findViewById(R.id.rankText)
        val rankImage: ImageView = itemView.findViewById(R.id.rankImage)
        val nameText: TextView = itemView.findViewById(R.id.nameText)
        val scoreText: TextView = itemView.findViewById(R.id.scoreText)
        val detailLayout: LinearLayout = itemView.findViewById(R.id.detailLayout)
        val btnPoint: Button = itemView.findViewById(R.id.btnPoint)
        val tvPointLabel: TextView = itemView.findViewById(R.id.tvPointLabel) // ✅ 포인트 라벨 참조
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val item = items[position]

        holder.nameText.text = item.nickname
        holder.scoreText.text = item.maxScore.toString()

        // ✅ rank에 따른 이미지 처리
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

        // ✅ tvPointLabel에는 SharedPreferences에서 가져온 포인트 출력
        holder.tvPointLabel.text = "보유 포인트: ${item.userPoint}pt"

        // ✅ 버튼 텍스트는 고정 10pt
        holder.btnPoint.text = "10pt"

        holder.itemView.setOnClickListener {
            val visible = if (holder.detailLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            holder.detailLayout.visibility = visible
        }

        holder.btnPoint.setOnClickListener {
            showFeedbackDialog(holder.itemView.context, item.feedback, item.maxScore)
        }
    }

    override fun getItemCount() = items.size

    private fun showFeedbackDialog(context: Context, feedbackText: String, score: Double) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_feedback_popup, null)
        val dialog = AlertDialog.Builder(context).setView(dialogView).create()

        val tvContent = dialogView.findViewById<TextView>(R.id.tvFeedbackContent)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)

        btnClose.setOnClickListener { dialog.dismiss() }

        val formatted = formatFeedbackText(feedbackText)
        val displayText = "📊 종합 점수: ${score}점\n\n$formatted"
        tvContent.text = displayText

        dialog.show()
    }

    fun formatFeedbackText(raw: String): String {
        return raw
            .replace(Regex("\\.(?=\\d+\\.)"), ".\n")      // .숫자. 줄바꿈
            .replace(Regex("(?=\\s*-\\s*)"), "\n")        // - 앞 줄바꿈
            .replace(Regex("(?=\\[)"), "\n\n")            // [ 앞 줄바꿈
    }
}

