package com.eivier.whatsappclone

import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.view.View
import android.widget.Button

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.eivier.whatsappclone.User.UserListAdapter
import com.eivier.whatsappclone.User.UserObject
import com.eivier.whatsappclone.Utils.CountryToPhonePrefix

import java.util.ArrayList
import java.util.HashMap

class FindUserActivity : AppCompatActivity() {

    private var mUserList: RecyclerView? = null
    private var mUserListAdapter: RecyclerView.Adapter<*>? = null
    private var mUserListLayoutManager: RecyclerView.LayoutManager? = null

    internal var userList: ArrayList<UserObject>
    internal var contactList: ArrayList<UserObject>


    private val countryISO: String?
        get() {
            var iso: String? = null

            val telephonyManager = applicationContext.getSystemService(applicationContext.TELEPHONY_SERVICE) as TelephonyManager
            if (telephonyManager.networkCountryIso != null)
                if (telephonyManager.networkCountryIso != "")
                    iso = telephonyManager.networkCountryIso

            return CountryToPhonePrefix.getPhone(iso!!)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_user)

        contactList = ArrayList()
        userList = ArrayList()

        val mCreate = findViewById<Button>(R.id.create)
        mCreate.setOnClickListener { createChat() }

        initializeRecyclerView()
        getContactList()
    }

    private fun createChat() {
        val key = FirebaseDatabase.getInstance().reference.child("chat").push().key

        val chatInfoDb = FirebaseDatabase.getInstance().reference.child("chat").child(key!!).child("info")
        val userDb = FirebaseDatabase.getInstance().reference.child("user")

        val newChatMap = HashMap()
        newChatMap.put("id", key)
        newChatMap.put("users/" + FirebaseAuth.getInstance().uid!!, true)

        var validChat: Boolean? = false
        for (mUser in userList) {
            if (mUser.selected!!) {
                validChat = true
                newChatMap.put("users/" + mUser.uid!!, true)
                userDb.child(mUser.uid!!).child("chat").child(key).setValue(true)
            }
        }

        if (validChat!!) {
            chatInfoDb.updateChildren(newChatMap)
            userDb.child(FirebaseAuth.getInstance().uid!!).child("chat").child(key).setValue(true)
        }

    }

    private fun getContactList() {

        val ISOPrefix = countryISO

        val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            phone = phone.replace(" ", "")
            phone = phone.replace("-", "")
            phone = phone.replace("(", "")
            phone = phone.replace(")", "")

            if (phone[0].toString() != "+")
                phone = ISOPrefix!! + phone

            val mContact = UserObject("", name, phone)
            contactList.add(mContact)
            getUserDetails(mContact)
        }
    }

    private fun getUserDetails(mContact: UserObject) {
        val mUserDB = FirebaseDatabase.getInstance().reference.child("user")
        val query = mUserDB.orderByChild("phone").equalTo(mContact.phone)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var phone = ""
                    var name = ""
                    for (childSnapshot in dataSnapshot.children) {
                        if (childSnapshot.child("phone").value != null)
                            phone = childSnapshot.child("phone").value!!.toString()
                        if (childSnapshot.child("name").value != null)
                            name = childSnapshot.child("name").value!!.toString()


                        val mUser = UserObject(childSnapshot.key.toString(), name, phone)
                        if (name == phone)
                            for (mContactIterator in contactList) {
                                if (mContactIterator.phone == mUser.phone) {
                                    mUser.name = mContactIterator.name
                                }
                            }

                        userList.add(mUser)
                        mUserListAdapter!!.notifyDataSetChanged()
                        return
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun initializeRecyclerView() {
        mUserList = findViewById(R.id.userList)
        mUserList!!.isNestedScrollingEnabled = false
        mUserList!!.setHasFixedSize(false)
        mUserListLayoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        mUserList!!.layoutManager = mUserListLayoutManager
        mUserListAdapter = UserListAdapter(userList)
        mUserList!!.adapter = mUserListAdapter
    }
}
