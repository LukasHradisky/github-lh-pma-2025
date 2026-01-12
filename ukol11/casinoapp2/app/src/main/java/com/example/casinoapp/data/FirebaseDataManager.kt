package com.example.casinoapp.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseDataManager {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    // Používáme pevné ID uživatele pro jednoduchost (v reálu by bylo userID z Firebase Auth)
    private val userId = "test_user_id"
    private val userRef: DatabaseReference = database.getReference("users").child(userId)

    // Funkce pro nastavení počátečního kreditu (jen pokud ještě není v databázi)
    fun initializeBalance() {
        // Kontroluje, jestli data existují, a pokud ne, nastaví počáteční hodnotu
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    setBalance(1000) // Počáteční kredit 1000 Kč
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Zde můžete implementovat logiku pro chybu
            }
        })
    }

    // Nastaví novou hodnotu kreditu v databázi
    fun setBalance(value: Int) {
        userRef.child("balance").setValue(value)
    }

    // Přečte kredit z databáze a zavolá callback
    fun getBalance(onComplete: (Int) -> Unit) {
        userRef.child("balance").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Převádí hodnotu z databáze na Int, pokud neexistuje, použije 0
                val balance = snapshot.getValue(Int::class.java) ?: 0
                onComplete(balance)
            }

            override fun onCancelled(error: DatabaseError) {
                // V případě chyby vrátí 0 a může zalogovat chybu
                onComplete(0)
            }
        })
    }

    // Metoda pro poslech změn kreditu v reálném čase
    fun listenToBalanceChanges(listener: ValueEventListener) {
        userRef.child("balance").addValueEventListener(listener)
    }

    // Metoda pro odebrání posluchače, abychom předešli memory leakům
    fun removeBalanceListener(listener: ValueEventListener) {
        userRef.child("balance").removeEventListener(listener)
    }
}