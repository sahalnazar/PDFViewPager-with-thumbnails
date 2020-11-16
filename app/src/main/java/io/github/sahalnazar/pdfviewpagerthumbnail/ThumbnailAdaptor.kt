package io.github.sahalnazar.pdfviewpagerthumbnail

import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_thumbnail.view.*


class ThumbnailAdaptor : RecyclerView.Adapter<ThumbnailAdaptor.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    val differCallback = object : DiffUtil.ItemCallback<Bitmap>() {
        override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_thumbnail,
                parent,
                false
            )
        )
    }

    var row_index = 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pdfImage = differ.currentList[position]

        holder.itemView.apply {
            itemViewImage.setImageBitmap(pdfImage)

            setOnClickListener {
                onItemClickListener?.let {
                    it(position)
                }

                row_index = position
                notifyDataSetChanged()

            }

            if (Constants.selectedItem == position) {
                itemViewImage.setBackgroundResource(R.drawable.selected_border)
            } else {
                itemViewImage.setBackgroundColor(Color.parseColor("#ffffff"))
            }

        }
    }

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}