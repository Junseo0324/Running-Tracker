package com.devhjs.runningtracker.domain.datasource

import com.devhjs.runningtracker.data.local.SerializableLatLng
import com.devhjs.runningtracker.data.local.TempRunState

/**
 * 임시 러닝 상태(TempRunState)를 저장하고 복구하는 데이터 소스 인터페이스입니다.
 * 파일 시스템, DataStore 등 구체적인 저장 방식으로부터 도메인/레포지토리 계층을 분리합니다.
 */
interface TempRunDataSource {
    /**
     * 현재 러닝 상태를 저장소에 영구 보존합니다.
     */
    suspend fun persistState(timeInMillis: Long, pathPoints: List<List<SerializableLatLng>>)

    /**
     * 저장된 러닝 상태를 복구합니다.
     * @return 저장된 상태가 없으면 null 반환
     */
    suspend fun restoreState(): TempRunState?

    /**
     * 저장된 데이터를 삭제합니다.
     */
    suspend fun clearData()
}