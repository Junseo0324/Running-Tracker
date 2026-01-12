package com.devhjs.runningtracker.domain.repository

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * 위치 정보에 접근하기 위한 도메인 계층(Domain Layer)의 레포지토리 인터페이스입니다.
 *
 * Clean Architecture 원칙에 따라, 앱의 비즈니스 로직(UseCase 등)은 구체적인 위치 추적 구현
 */
interface LocationRepository {

    /**
     * 현재 위치 정보를 실시간 데이터 스트림(Flow) 형태로 제공합니다.
     * 사용자의 이동에 따라 지속적으로 업데이트되는 위치 데이터를 방출합니다.
     */
    fun getLocationFlow(): Flow<Location>
}
