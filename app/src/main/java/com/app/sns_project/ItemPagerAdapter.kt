package com.app.sns_project

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ItemPagerAdapter(private val list: ArrayList<Uri>) :
    RecyclerView.Adapter<ItemPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_image_item, parent, false)
        )
    }

    override fun getItemCount(): Int = list.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.selectedImage.setImageURI(data)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val selectedImage : ImageView = view.findViewById(R.id.select_image)
    }

}