package com.example.mockly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RankingAdapter(private val items: List<RankingItem>) :
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    inner class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankText: TextView = itemView.findViewById(R.id.rankText)
        val rankImage: ImageView = itemView.findViewById(R.id.rankImage)
        val nameText: TextView = itemView.findViewById(R.id.nameText)
        val scoreText: TextView = itemView.findViewById(R.id.scoreText)
        val detailLayout: LinearLayout = itemView.findViewById(R.id.detailLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val item = items[position]
        holder.nameText.text = item.name
        holder.scoreText.text = item.score.toString()

        // 1~3위 이미지 처리
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
        holder.itemView.setOnClickListener {
            val visible = if (holder.detailLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            holder.detailLayout.visibility = visible
        }
    }

    override fun getItemCount() = items.size
}

