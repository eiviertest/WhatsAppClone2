package com.eivier.whatsappclone.Chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.eivier.whatsappclone.ChatActivity
import com.eivier.whatsappclone.R

import java.util.ArrayList

class ChatListAdapter(internal var chatList: ArrayList<ChatObject>) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, null, false)
        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutView.layoutParams = lp

        return ChatListViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.mTitle.text = chatList[position].chatId

        holder.mLayout.setOnClickListener { v ->
            val intent = Intent(v.context, ChatActivity::class.java)
            intent.putExtra("chatObject", chatList[holder.adapterPosition])
            v.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }


    inner class ChatListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mTitle: TextView
        var mLayout: LinearLayout

        init {
            mTitle = view.findViewById(R.id.title)
            mLayout = view.findViewById(R.id.layout)
        }
    }
}
