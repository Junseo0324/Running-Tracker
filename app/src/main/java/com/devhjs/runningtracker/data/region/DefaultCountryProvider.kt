package com.devhjs.runningtracker.data.region

import android.content.Context
import android.content.res.Resources
import android.telephony.TelephonyManager
import com.devhjs.runningtracker.domain.region.CountryProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * 접속 중인 이동통신망 국가 -> SIM 국가 -> 시스템 지역 설정 순으로 판단한다.
 *
 * 앱 언어(Locale.getDefault())는 앱별 언어 설정으로 바뀔 수 있어 기준으로 쓰지 않는다.
 * Resources.getSystem() 은 앱별 언어와 무관한 기기 설정을 돌려준다.
 */
class DefaultCountryProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : CountryProvider {

    override fun currentCountryCode(): String {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return listOfNotNull(
            telephony?.networkCountryIso,
            telephony?.simCountryIso,
            Resources.getSystem().configuration.locales[0]?.country
        ).firstOrNull { it.isNotBlank() }?.uppercase().orEmpty()
    }
}
