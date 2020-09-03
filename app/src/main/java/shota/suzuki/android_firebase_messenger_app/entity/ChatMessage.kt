package shota.suzuki.android_firebase_messenger_app.entity

data class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long) {
    constructor(): this("", "", "", "", -1)
}