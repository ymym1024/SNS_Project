package com.app.sns_project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.sns_project.R
import com.bumptech.glide.Glide

class PostImageViewPager(private var list: List<String>?): RecyclerView.Adapter<PostImageViewPager.ImageViewHolder>() {

    inner class ImageViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val Image : ImageView = view.findViewById(R.id.pagerImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ImageViewHolder = ImageViewHolder(
        LayoutInflater.from(parent.context).inflate(
    R.layout.layout_pager_item, parent, false))

    override fun onBindViewHolder(holder: PostImageViewPager.ImageViewHolder, position: Int) {
        Glide.with(holder.Image.context).load(list?.get(position)!!).into(holder.Image)
    }

    override fun getItemCount(): Int = list!!.size
}