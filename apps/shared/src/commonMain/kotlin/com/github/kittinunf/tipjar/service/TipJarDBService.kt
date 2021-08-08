package com.github.kittinunf.tipjar.service

import com.github.kittinunf.tipjar.TipJarQueries
import com.github.kittinunf.tipjar.db.TipJarDB
import com.squareup.sqldelight.db.SqlDriver

expect class DBContext

const val filename = "TipJar.db"
expect fun createSqlDriver(context: DBContext, databaseFilename: String): SqlDriver

class TipJarDBService(private val db: TipJarDB) : TipJarQueries by db.tipJarQueries
