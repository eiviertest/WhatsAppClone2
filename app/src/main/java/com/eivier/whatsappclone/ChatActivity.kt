package com.eivier.whatsappclone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.eivier.whatsappclone.Chat.ChatObject
import com.eivier.whatsappclone.Chat.MediaAdapter
import com.eivier.whatsappclone.Chat.MessageAdapter
import com.eivier.whatsappclone.Chat.MessageObject
import com.eivier.whatsappclone.User.UserObject
import com.eivier.whatsappclone.Utils.SendNotification

import java.util.ArrayList
import java.util.HashMap

class ChatActivity : AppCompatActivity() {

    private var mChat: RecyclerView? = null
    private var mMedia: RecyclerView? = null
    private var mChatAdapter: RecyclerView.Adapter<*>? = null
    private var mMediaAdapter: RecyclerView.Adapter<*>? = null
    private var mChatLayoutManager: RecyclerView.LayoutManager? = null
    private var mMediaLayoutManager: RecyclerView.LayoutManager? = null

    internal var messageList: ArrayList<MessageObject>

    internal var mChatObject: ChatObject

    internal var mChatMessagesDb: DatabaseReference

    internal var totalMediaUploaded = 0
    internal var mediaIdList = ArrayList<String>()
    internal var mMessage: EditText

    internal var PICK_IMAGE_INTENT = 1
    internal var mediaUriList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mChatObject = intent.getSerializableExtra("chatObject") as ChatObject

        mChatMessagesDb = FirebaseDatabase.getInstance().reference.child("chat").child(mChatObject.chatId).child("messages")

        val mSend = findViewById<Button>(R.id.send)
        val mAddMedia = findViewById<Button>(R.id.addMedia)

        mSend.setOnClickListener { sendMessage() }
        mAddMedia.setOnClickListener { openGallery() }

        initializeMessage()
        initializeMedia()
        getChatMessages()
    }

    private fun getChatMessages() {
        mChatMessagesDb.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists()) {
                    var text = ""
                    var creatorID = ""
                    val mediaUrlList = ArrayList<String>()

                    if (dataSnapshot.child("text").value != null)
                        text = dataSnapshot.child("text").value!!.toString()
                    if (dataSnapshot.child("creator").value != null)
                        creatorID = dataSnapshot.child("creator").value!!.toString()

                    if (dataSnapshot.child("media").childrenCount > 0)
                        for (mediaSnapshot in dataSnapshot.child("media").children)
                            mediaUrlList.add(mediaSnapshot.value!!.toString())

                    val mMessage = MessageObject(dataSnapshot.key, creatorID, text, mediaUrlList)
                    messageList.add(mMessage)
                    mChatLayoutManager!!.scrollToPosition(messageList.size - 1)
                    mChatAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    private fun sendMessage() {
        mMessage = findViewById(R.id.messageInput)

        val messageId = mChatMessagesDb.push().key
        val newMessageDb = mChatMessagesDb.child(messageId!!)

        val newMessageMap = HashMap()

        newMessageMap.put("creator", FirebaseAuth.getInstance().uid)

        if (!mMessage.text.toString().isEmpty())
            newMessageMap.put("text", mMessage.text.toString())


        if (!mediaUriList.isEmpty()) {
            for (mediaUri in mediaUriList) {
                val mediaId = newMessageDb.child("media").push().key
                mediaIdList.add(mediaId)
                val filePath = FirebaseStorage.getInstance().reference.child("chat").child(mChatObject.chatId).child(messageId).child(mediaId!!)

                val uploadTask = filePath.putFile(Uri.parse(mediaUri))

                uploadTask.addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { uri ->
                        newMessageMap.put("/media/" + mediaIdList[totalMediaUploaded] + "/", uri.toString())

                        totalMediaUploaded++
                        if (totalMediaUploaded == mediaUriList.size)
                            updateDatabaseWithNewMessage(newMessageDb, newMessageMap)
                    }
                }
            }
        } else {
            if (!mMessage.text.toString().isEmpty())
                updateDatabaseWithNewMessage(newMessageDb, newMessageMap)
        }


    }


    private fun updateDatabaseWithNewMessage(newMessageDb: DatabaseReference, newMessageMap: Map<*, *>) {
        newMessageDb.updateChildren(newMessageMap)
        mMessage.setText(null)
        mediaUriList.clear()
        mediaIdList.clear()
        totalMediaUploaded = 0
        mMediaAdapter!!.notifyDataSetChanged()

        val message: String

        if (newMessageMap.get("text") != null)
            message = newMessageMap.get("text")!!.toString()
        else
            message = "Enviar imagen"

        for (mUser in mChatObject.userObjectArrayList) {
            if (mUser.uid != FirebaseAuth.getInstance().uid) {
                SendNotification(message, "Nuevo mensaje", mUser.notificationKey)
            }
        }
    }

    private fun initializeMessage() {
        messageList = ArrayList()
        mChat = findViewById(R.id.messageList)
        mChat!!.isNestedScrollingEnabled = false
        mChat!!.setHasFixedSize(false)
        mChatLayoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        mChat!!.layoutManager = mChatLayoutManager
        mChatAdapter = MessageAdapter(messageList)
        mChat!!.adapter = mChatAdapter
    }

    private fun initializeMedia() {
        mediaUriList = ArrayList()
        mMedia = findViewById(R.id.mediaList)
        mMedia!!.isNestedScrollingEnabled = false
        mMedia!!.setHasFixedSize(false)
        mMediaLayoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
        mMedia!!.layoutManager = mMediaLayoutManager
        mMediaAdapter = MediaAdapter(applicationContext, mediaUriList)
        mMedia!!.adapter = mMediaAdapter
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_INTENT) {
                if (data!!.clipData == null) {
                    mediaUriList.add(data.data!!.toString())
                } else {
                    for (i in 0 until data.clipData!!.itemCount) {
                        mediaUriList.add(data.clipData!!.getItemAt(i).uri.toString())
                    }
                }

                mMediaAdapter!!.notifyDataSetChanged()
            }
        }
    }
}
