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

    // 쿠팡 파트너스 카테고리별 고정 링크입니다. (파트너스 센터 > 링크 생성에서 만든 단축 URL)
    // 비어있는 카테고리는 추천 카드가 표시되지 않습니다.
    const val COUPANG_LINK_RUNNING_SHOES = "https://link.coupang.com/a/hxZAi6HA96"
    const val COUPANG_LINK_NUTRITION = "https://link.coupang.com/a/hxZCAHY7bx"
    const val COUPANG_LINK_GEAR = "https://link.coupang.com/a/hxZF92GTAa"
    const val COUPANG_LINK_ELECTROLYTE = "https://link.coupang.com/a/hxZyXroK3E"
    const val COUPANG_LINK_SOCKS = "https://link.coupang.com/a/hxZDZdTayi"
    const val COUPANG_LINK_APPAREL = "https://link.coupang.com/a/hxZBl0Bc4G"
    const val COUPANG_LINK_WATCH = "https://link.coupang.com/a/hxZwUlfU72"
    // 카테고리별 대표 상품 이미지 주소입니다. (파트너스 센터 > 상품 링크 > HTML 의 img src)
    // 비어있으면 이미지 대신 카테고리 아이콘이 표시됩니다.
    const val COUPANG_IMAGE_RUNNING_SHOES = "https://img1c.coupangcdn.com/image/affiliate/banner/07a3ae5cdb111e4b1062bb121d25acb3@2x.jpg"
    const val COUPANG_IMAGE_GEAR = "https://image13.coupangcdn.com/image/affiliate/banner/a837e36c1c8a08f15dd7968700c130f1@2x.jpg"
    const val COUPANG_IMAGE_NUTRITION = "https://image12.coupangcdn.com/image/affiliate/banner/d7327a7eda61cef396988f7194181c4a@2x.jpg"
    const val COUPANG_IMAGE_ELECTROLYTE = "https://image6.coupangcdn.com/image/affiliate/banner/0ab2d4934b7ffdb79719f0a9cc06ec53@2x.jpg"
    const val COUPANG_IMAGE_SOCKS = "https://image15.coupangcdn.com/image/affiliate/banner/b92cdc18110e9cb3610690c9ee8eb620@2x.jpg"
    const val COUPANG_IMAGE_APPAREL = "https://img2a.coupangcdn.com/image/affiliate/banner/401d54136b234f46d723b70a7af97d00@2x.jpg"
    const val COUPANG_IMAGE_WATCH = "https://img3c.coupangcdn.com/image/affiliate/banner/64a4cad92c79f80ac284c62e13bbd1d4@2x.jpg"
    // 공정위 지침상 파트너스 링크 근처에 반드시 표시해야 하는 대가성 문구입니다.
    const val COUPANG_PARTNERS_DISCLOSURE =
        "이 게시물은 쿠팡 파트너스 활동의 일환으로, 이에 따른 일정액의 수수료를 제공받습니다."
}
