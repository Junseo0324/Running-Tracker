package com.devhjs.runningtracker.data.location

import com.devhjs.runningtracker.domain.location.GpsStatusClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * 테스트 및 개발을 위한 [GpsStatusClient]의 Mock 구현체입니다.
 * 항상 GPS가 켜져 있는 것으로 간주하거나 시뮬레이션된 상태를 제공합니다.
 */
class MockGpsStatusClient @Inject constructor() : GpsStatusClient {
    override fun getGpsStatusFlow(): Flow<Boolean> = flow {
        // 항상 true 반환 (또는 필요시 토글 로직 추가)
        while (true) {
            emit(true)
            delay(1000L)
        }
    }
}
