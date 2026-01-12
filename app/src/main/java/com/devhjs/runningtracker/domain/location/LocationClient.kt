package com.devhjs.runningtracker.domain.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * 위치 정보에 접근하기 위한 인터페이스입니다.
 * Android System Location 서비스에 대한 클라이언트 역할을 합니다.
 */
interface LocationClient {

    /**
     * 현재 위치 정보를 실시간 데이터 스트림(Flow) 형태로 제공합니다.
     * 사용자의 이동에 따라 지속적으로 업데이트되는 위치 데이터를 방출합니다.
     */
    fun getLocationFlow(): Flow<Location>
}
