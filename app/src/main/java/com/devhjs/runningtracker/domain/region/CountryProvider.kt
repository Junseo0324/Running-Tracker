package com.devhjs.runningtracker.domain.region

/**
 * 사용자가 현재 있는 나라. 지역 한정 기능(쿠팡 파트너스 등)의 노출 여부를 정하는 데 쓴다.
 * 앱 표시 언어와는 별개다. (한국에서 앱 언어만 영어로 쓰는 사용자도 한국으로 본다)
 */
interface CountryProvider {
    /** ISO 3166-1 alpha-2 국가 코드(대문자). 알 수 없으면 빈 문자열. */
    fun currentCountryCode(): String
}
