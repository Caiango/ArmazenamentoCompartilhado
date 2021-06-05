package com.josecmj.armazenamentocompartilhado

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ImageAdapter : ListAdapter<MyImage, ImageAdapter.FileViewHolder>(FileDiffCallback) {

    class FileViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)  {
        private val fileTextView: TextView = itemView.findViewById(R.id.image_name)
        private val fileImageView: ImageView = itemView.findViewById(R.id.image)
        private var currentFile: MyImage? = null

        fun bind(file: MyImage) {
            currentFile = file
            fileTextView.text = file.displayName
            fileImageView.setImageURI(file.contentUri)

        }

        init {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = getItem(position)
        holder.bind(file)
    }
}
object FileDiffCallback : DiffUtil.ItemCallback<MyImage>() {
    override fun areItemsTheSame(oldItem: MyImage, newItem: MyImage): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MyImage, newItem: MyImage): Boolean {
        return oldItem.id == newItem.id
    }
}