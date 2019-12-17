package com.eivier.whatsappclone.Utils

import com.onesignal.OneSignal

import org.json.JSONException
import org.json.JSONObject

class SendNotification(message: String, heading: String, notificationKey: String) {

    init {

        try {
            val notificationContent = JSONObject(
                    "{'contents':{'en':'" + message + "'}," +
                            "'include_player_ids':['" + notificationKey + "']," +
                            "'headings':{'en': '" + heading + "'}}")
            OneSignal.postNotification(notificationContent, null)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }
}
