package com.devhjs.runningtracker.fake

import com.devhjs.runningtracker.domain.location.GpsStatusClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * GPS 켜짐/꺼짐 상태를 테스트에서 임의로 흘려보낼 수 있는 [GpsStatusClient] Fake 구현체.
 */
class FakeGpsStatusClient(initialEnabled: Boolean = true) : GpsStatusClient {

    private val statusFlow = MutableStateFlow(initialEnabled)

    override fun getGpsStatusFlow(): Flow<Boolean> = statusFlow

    /** 테스트에서 GPS 상태 변경을 트리거한다. */
    fun emit(isEnabled: Boolean) {
        statusFlow.value = isEnabled
    }
}
