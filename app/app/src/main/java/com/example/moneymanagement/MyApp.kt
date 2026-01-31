package com.example.moneymanagement

import android.app.Application
import com.example.moneymanagement.data.model.TransactionDatabase
import com.example.moneymanagement.data.repository.TransactionRepository

class MyApp : Application() {
    val db by lazy { TransactionDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(db.transactionDao(), db.categoryDao()) }
}