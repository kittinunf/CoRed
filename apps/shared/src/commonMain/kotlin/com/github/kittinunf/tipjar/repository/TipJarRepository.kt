package com.github.kittinunf.tipjar.repository

import com.github.kittinunf.tipjar.service.TipJarDBService
import com.squareup.sqldelight.Query

interface TipJarRepository {
    suspend fun createTip(amount: Int, peopleCount: Int, tipAmount: Int, image: String? = null, timestamp: Long)
    suspend fun <T : Any> getTips(mapper: (id: Long, amount: Long, peopleCount: Long, tipAmount: Long, imageLocation: String?, time: Long) -> T): Query<T>
    suspend fun <T : Any> getTipsDesc(mapper: (id: Long, amount: Long, peopleCount: Long, tipAmount: Long, imageLocation: String?, time: Long) -> T): Query<T>
    suspend fun deleteTip(id: Int)
    suspend fun deleteAllTips()
}

class TipJarRepositoryImpl(private val service: TipJarDBService) : TipJarRepository {

    override suspend fun createTip(amount: Int, peopleCount: Int, tipAmount: Int, image: String?, timestamp: Long) {
        service.insertTip(amount.toLong(), peopleCount.toLong(), tipAmount.toLong(), image, timestamp)
    }

    override suspend fun <T : Any> getTips(mapper: (
        id: Long,
        amount: Long,
        peopleCount: Long,
        tipAmount: Long,
        imageLocation: String?,
        time: Long
    ) -> T): Query<T> = service.selectTips(mapper)

    override suspend fun <T : Any> getTipsDesc(mapper: (id: Long, amount: Long, peopleCount: Long, tipAmount: Long, imageLocation: String?, time: Long) -> T): Query<T> = service.selectTipsDesc(mapper)

    override suspend fun deleteTip(id: Int) {
        service.deleteTip(id.toLong())
    }

    override suspend fun deleteAllTips() {
        service.deleteAllTips()
    }
}
