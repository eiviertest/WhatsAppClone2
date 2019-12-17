package com.eivier.whatsappclone.User

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.eivier.whatsappclone.R

import java.util.ArrayList

class UserListAdapter(internal var userList: ArrayList<UserObject>) : RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_user, null, false)
        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutView.layoutParams = lp

        return UserListViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        holder.mName.text = userList[position].name
        holder.mPhone.text = userList[position].phone

        holder.mAdd.setOnCheckedChangeListener { buttonView, isChecked -> userList[holder.adapterPosition].selected = isChecked }
    }

    override fun getItemCount(): Int {
        return userList.size
    }


    internal inner class UserListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mName: TextView
        var mPhone: TextView
        var mLayout: LinearLayout
        var mAdd: CheckBox

        init {
            mName = view.findViewById(R.id.name)
            mPhone = view.findViewById(R.id.phone)
            mAdd = view.findViewById(R.id.add)
            mLayout = view.findViewById(R.id.layout)
        }
    }
}
