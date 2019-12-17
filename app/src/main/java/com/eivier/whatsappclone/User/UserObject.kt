package com.eivier.whatsappclone.User

import java.io.Serializable

class UserObject : Serializable {

    var uid: String? = null
        private set
    var name: String? = null
    val phone: String
    var notificationKey: String? = null

    var selected: Boolean? = false

    constructor(uid: String) {
        this.uid = uid
    }

    constructor(uid: String, name: String, phone: String) {
        this.uid = uid
        this.name = name
        this.phone = phone
    }
}
