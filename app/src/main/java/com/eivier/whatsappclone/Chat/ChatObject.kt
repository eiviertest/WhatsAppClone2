package com.eivier.whatsappclone.Chat

import com.eivier.whatsappclone.User.UserObject

import java.io.Serializable
import java.util.ArrayList

class ChatObject(val chatId: String) : Serializable {

    val userObjectArrayList = ArrayList<UserObject>()

    fun addUserToArrayList(mUser: UserObject) {
        userObjectArrayList.add(mUser)
    }
}
