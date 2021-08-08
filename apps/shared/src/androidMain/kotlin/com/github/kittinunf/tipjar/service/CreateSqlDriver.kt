package com.github.kittinunf.tipjar.service

import android.app.Application
import com.github.kittinunf.tipjar.db.TipJarDB
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual typealias DBContext = Application

actual fun createSqlDriver(context: DBContext, databaseFilename: String): SqlDriver = AndroidSqliteDriver(TipJarDB.Schema, context, databaseFilename)
