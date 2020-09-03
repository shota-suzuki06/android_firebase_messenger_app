package shota.suzuki.android_firebase_messenger_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import shota.suzuki.android_firebase_messenger_app.entity.ChatFromItem
import shota.suzuki.android_firebase_messenger_app.entity.ChatMessage
import shota.suzuki.android_firebase_messenger_app.entity.ChatToItem
import shota.suzuki.android_firebase_messenger_app.entity.User

class ChatLogActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var toUser: User

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser.username

        listenForMessages()

        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "Attempt to send message....")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = Firebase.auth.uid
        val toId = toUser?.uid
        val ref = Firebase.database.getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage?.text)

                    if (chatMessage.fromId == Firebase.auth.uid) {
                        val currentUser = LatestMassagesActivity.currentUser ?: return
                        adapter.add(ChatToItem(chatMessage?.text, currentUser!!))
                    } else {
                        adapter.add(ChatFromItem(chatMessage?.text, toUser!!))
                    }
                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {
        val text = edittext_chat_log.text.toString()

        val fromId = Firebase.auth.uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if (fromId == null) return
        database = Firebase.database.getReference("/user-messages/$fromId/$toId").push()

        val toReference = Firebase.database.getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(database.key!!, text, fromId!!, toId, System.currentTimeMillis() / 1000)
        database.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "saved our chat message: ${database.key}")
                    edittext_chat_log.text.clear()
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
                }

        toReference.setValue(chatMessage)

        val latestMessageRef = Firebase.database.getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = Firebase.database.getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }

}
