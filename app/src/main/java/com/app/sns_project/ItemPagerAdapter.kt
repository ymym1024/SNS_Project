package com.app.sns_project

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ItemPagerAdapter(private val context: Context, private val list: ArrayList<String>,val count:Int) :
    RecyclerView.Adapter<ItemPagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {

        val width = context.getResources().getDisplayMetrics().widthPixels / count

        val imageView = ImageView(parent.context)
        imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)

        return ImageViewHolder(imageView)
    }

    override fun getItemCount(): Int = list.count()

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        var imageview = (holder as ImageViewHolder).imageView
        val data = list[position]
        Glide.with(context).load(data).apply(RequestOptions().centerCrop()).into(imageview)
    }

    inner class ImageViewHolder(var imageView: ImageView): RecyclerView.ViewHolder(imageView)

}