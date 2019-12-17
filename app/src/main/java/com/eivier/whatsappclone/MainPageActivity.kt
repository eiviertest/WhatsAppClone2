package com.eivier.whatsappclone

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.onesignal.OneSignal
import com.eivier.whatsappclone.Chat.ChatListAdapter
import com.eivier.whatsappclone.Chat.ChatObject
import com.eivier.whatsappclone.User.UserObject

import java.util.ArrayList

class MainPageActivity : AppCompatActivity() {

    private var mChatList: RecyclerView? = null
    private var mChatListAdapter: RecyclerView.Adapter<*>? = null
    private var mChatListLayoutManager: RecyclerView.LayoutManager? = null

    internal lateinit var chatList: ArrayList<ChatObject>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        OneSignal.startInit(this).init()
        OneSignal.setSubscription(true)
        OneSignal.idsAvailable { userId, registrationId -> FirebaseDatabase.getInstance().reference.child("user").child(FirebaseAuth.getInstance().uid!!).child("notificationKey").setValue(userId) }
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)

        Fresco.initialize(this)

        val mLogout = findViewById<Button>(R.id.logout)
        val mFindUser = findViewById<Button>(R.id.findUser)
        mFindUser.setOnClickListener { startActivity(Intent(applicationContext, FindUserActivity::class.java)) }
        mLogout.setOnClickListener(View.OnClickListener {
            OneSignal.setSubscription(false)
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return@OnClickListener
        })

        getPermissions()
        initializeRecyclerView()
        getUserChatList()
    }

    private fun getUserChatList() {
        val mUserChatDB = FirebaseDatabase.getInstance().reference.child("user").child(FirebaseAuth.getInstance().uid!!).child("chat")

        mUserChatDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val mChat = ChatObject(childSnapshot.key.toString())
                        var exists = false
                        for (mChatIterator in chatList) {
                            if (mChatIterator.chatId == mChat.chatId)
                                exists = true
                        }
                        if (exists)
                            continue
                        chatList.add(mChat)
                        getChatData(mChat.chatId)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun getChatData(chatId: String) {
        val mChatDB = FirebaseDatabase.getInstance().reference.child("chat").child(chatId).child("info")
        mChatDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var chatId = ""

                    if (dataSnapshot.child("id").value != null)
                        chatId = dataSnapshot.child("id").value!!.toString()

                    for (userSnapshot in dataSnapshot.child("users").children) {
                        for (mChat in chatList) {
                            if (mChat.chatId == chatId) {
                                val mUser = UserObject(userSnapshot.key.toString())
                                mChat.addUserToArrayList(mUser)
                                getUserData(mUser)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    private fun getUserData(mUser: UserObject) {
        val mUserDb = FirebaseDatabase.getInstance().reference.child("user").child(mUser.uid!!)
        mUserDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mUser = UserObject(dataSnapshot.key.toString())

                if (dataSnapshot.child("notificationKey").value != null)
                    mUser.notificationKey = dataSnapshot.child("notificationKey").value!!.toString()

                for (mChat in chatList) {
                    for (mUserIt in mChat.userObjectArrayList) {
                        if (mUserIt.uid == mUser.uid) {
                            mUserIt.notificationKey = mUser.notificationKey
                        }
                    }
                }
                mChatListAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun initializeRecyclerView() {
        chatList = ArrayList()
        mChatList = findViewById(R.id.chatList)
        mChatList!!.isNestedScrollingEnabled = false
        mChatList!!.setHasFixedSize(false)
        mChatListLayoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        mChatList!!.layoutManager = mChatListLayoutManager
        mChatListAdapter = ChatListAdapter(chatList)
        mChatList!!.adapter = mChatListAdapter
    }

    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS), 1)
        }
    }

}
