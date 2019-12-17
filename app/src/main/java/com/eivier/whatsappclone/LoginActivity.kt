package com.eivier.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.util.HashMap
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private var mPhoneNumber: EditText? = null
    private var mCode: EditText? = null
    private var mSend: Button? = null

    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    internal var mVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        userIsLoggedIn()

        mPhoneNumber = findViewById(R.id.phoneNumber)
        mCode = findViewById(R.id.code)

        mSend = findViewById(R.id.send)

        mSend!!.setOnClickListener {
            if (mVerificationId != null)
                verifyPhoneNumberWithCode()
            else
                startPhoneNumberVerification()
        }

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {}

            override fun onCodeSent(verificationId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken)
                mVerificationId = verificationId
                mSend!!.text = "Verificar código"
            }
        }

    }

    private fun verifyPhoneNumberWithCode() {
        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, mCode!!.text.toString())
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(phoneAuthCredential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {

                val user = FirebaseAuth.getInstance().currentUser

                if (user != null) {
                    val mUserDB = FirebaseDatabase.getInstance().reference.child("user").child(user.uid)
                    mUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                val userMap = HashMap<String, Any>()
                                userMap["phone"] = user.phoneNumber
                                userMap["name"] = user.phoneNumber
                                mUserDB.updateChildren(userMap)
                            }
                            userIsLoggedIn()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                }

            }
        }
    }

    private fun userIsLoggedIn() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(Intent(applicationContext, MainPageActivity::class.java))
            finish()
            return
        }
    }

    private fun startPhoneNumberVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mPhoneNumber!!.text.toString(),
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks!!)
    }
}
