package com.eivier.whatsappclone.Chat

import java.util.ArrayList

class MessageObject(messageId: String, senderId: String, message: String, mediaUrlList: ArrayList<String>) {

    var messageId: String
        internal set
    var senderId: String
        internal set
    var message: String
        internal set

    var mediaUrlList: ArrayList<String>
        internal set

    init {
        this.messageId = messageId
        this.senderId = senderId
        this.message = message
        this.mediaUrlList = mediaUrlList
    }
}
