package com.eivier.whatsappclone.Chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.eivier.whatsappclone.R
import com.stfalcon.frescoimageviewer.ImageViewer

import java.util.ArrayList

class MessageAdapter(internal var messageList: ArrayList<MessageObject>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_message, null, false)
        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutView.layoutParams = lp

        return MessageViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.mMessage.text = messageList[position].message
        holder.mSender.text = messageList[position].senderId

        if (messageList[holder.adapterPosition].mediaUrlList.isEmpty())
            holder.mViewMedia.visibility = View.GONE

        holder.mViewMedia.setOnClickListener { v ->
            ImageViewer.Builder(v.context, messageList[holder.adapterPosition].mediaUrlList)
                    .setStartPosition(0)
                    .show()
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }


    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mMessage: TextView
        var mSender: TextView
        var mViewMedia: Button
        var mLayout: LinearLayout

        init {
            mLayout = view.findViewById(R.id.layout)

            mMessage = view.findViewById(R.id.message)
            mSender = view.findViewById(R.id.sender)

            mViewMedia = view.findViewById(R.id.viewMedia)
        }
    }
}
