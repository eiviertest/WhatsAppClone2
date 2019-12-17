package com.eivier.whatsappclone.Chat

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.eivier.whatsappclone.R

import java.util.ArrayList

class MediaAdapter(internal var context: Context, internal var mediaList: ArrayList<String>) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_media, null, false)

        return MediaViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        Glide.with(context).load(Uri.parse(mediaList[position])).into(holder.mMedia)
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }


    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var mMedia: ImageView

        init {
            mMedia = itemView.findViewById(R.id.media)
        }
    }
}
