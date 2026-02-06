package uk.co.imoqen.nourish.user

data class User(
    val uid: String, //uid from firebase
    val username: String,
    val email: String
)