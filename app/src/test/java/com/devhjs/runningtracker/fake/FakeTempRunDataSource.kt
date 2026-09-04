package com.devhjs.runningtracker.fake

import com.devhjs.runningtracker.data.local.SerializableLatLng
import com.devhjs.runningtracker.data.local.TempRunState
import com.devhjs.runningtracker.domain.datasource.TempRunDataSource

/**
 * 파일 IO 없이 인메모리로 동작하는 [TempRunDataSource] Fake 구현체.
 *
 * 저장된 상태뿐 아니라 각 메서드의 호출 횟수도 기록해, 위임 여부까지 검증할 수 있다.
 */
class FakeTempRunDataSource : TempRunDataSource {

    /** 테스트에서 미리 주입하거나, persistState 결과를 확인할 때 사용 */
    var savedState: TempRunState? = null

    var persistCallCount: Int = 0
        private set
    var clearCallCount: Int = 0
        private set

    override suspend fun persistState(
        timeInMillis: Long,
        pathPoints: List<List<SerializableLatLng>>
    ) {
        persistCallCount++
        savedState = TempRunState(timeInMillis = timeInMillis, pathPoints = pathPoints)
    }

    override suspend fun restoreState(): TempRunState? = savedState

    override suspend fun clearData() {
        clearCallCount++
        savedState = null
    }
}
