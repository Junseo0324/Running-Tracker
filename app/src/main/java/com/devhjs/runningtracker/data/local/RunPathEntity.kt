package com.devhjs.runningtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 러닝 기록의 이동 경로.
 *
 * 경로는 기록 하나에 수천 개의 좌표가 들어갈 수 있어 [RunEntity] 와 테이블을 분리했다.
 * 목록 조회(SELECT * FROM running_table)는 이 테이블을 읽지 않으므로,
 * 기록이 아무리 쌓여도 경로는 상세 화면에서 한 건씩만 메모리에 올라간다.
 *
 * @property encodedPath 구간(일시정지로 끊긴 선)마다 Encoded Polyline 으로 압축해 공백으로 이은 문자열.
 */
@Entity(tableName = "run_path")
data class RunPathEntity(
    @PrimaryKey
    val runId: Int,
    val encodedPath: String
)
