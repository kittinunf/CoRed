package com.github.kittinunf.tipjar.service

import com.github.kittinunf.tipjar.db.TipJarDB
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

actual typealias DBContext = Any

actual fun createSqlDriver(context: DBContext, databaseFilename: String): SqlDriver = NativeSqliteDriver(schema = TipJarDB.Schema, databaseFilename)
