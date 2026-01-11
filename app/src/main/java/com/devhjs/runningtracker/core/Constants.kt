package com.devhjs.runningtracker.core

/**
 * 앱 전체에서 광범위하게 사용되는 상수들을 정의한 객체입니다.
 * 데이터베이스 설정, 서비스 인텐트 액션, 위치 및 타이머 업데이트 간격, 지도 UI 설정, 알림 채널 설정 등을 포함합니다.
 */
object Constants {
    // Room 데이터베이스의 파일 이름을 정의합니다.
    const val RUNNING_DATABASE_NAME = "running_db"

    // 포그라운드 서비스 시작 또는 재개(Resume)를 위한 인텐트 액션입니다.
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    // 포그라운드 서비스를 일시 정지(Pause)하기 위한 인텐트 액션입니다.
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    // 포그라운드 서비스를 완전히 중지(Stop)하기 위한 인텐트 액션입니다.
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    // 알림 클릭 시 트래킹 화면으로 이동하기 위해 사용되는 인텐트 액션입니다.
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    // 타이머가 갱신되는 주기(밀리초)입니다. UI 업데이트 빈도에 영향을 줍니다.
    const val TIMER_UPDATE_INTERVAL = 50L
    // 일반적인 위치 업데이트 요청 간격(밀리초)입니다.
    const val LOCATION_UPDATE_INTERVAL = 5000L
    // 위치 업데이트를 처리할 수 있는 가장 빠른 간격(밀리초)입니다.
    const val FASTEST_LOCATION_INTERVAL = 2000L

    // 지도 경로(Polyline)의 색상입니다. (빨간색)
    const val POLYLINE_COLOR = 0xFFFF0000.toInt() // Red
    // 지도 경로(Polyline)의 선 두께.
    const val POLYLINE_WIDTH = 10f
    // 지도가 처음 로드되거나 이동할 때의 기본 줌 레벨입니다.
    const val MAP_ZOOM = 15f

    // 알림 채널을 식별하는 고유 ID입니다.
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    // 사용자에게 표시되는 알림 채널의 이름입니다. 시스템 설정에서 확인 가능합니다.
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    // 트래킹 서비스 알림의 고유 ID입니다.
    const val NOTIFICATION_ID = 1
}
