package com.example.myapp014asharedtasklist


import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.DocumentId
data class Task(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val completed: Boolean = false
)