# RunningTracker

## 📱 프로젝트 소개
**RunningTracker**는 사용자의 러닝 활동을 실시간으로 추적하고 기록하는 안드로이드 애플리케이션입니다.
직관적인 UI와 정확한 위치 추적 기능을 통해 사용자에게 향상된 러닝 경험을 제공합니다.

## ✨ 주요 기능
- **실시간 위치 추적**: Google Maps API를 활용하여 러닝 경로를 실시간으로 지도에 표시합니다.
- **러닝 데이터 기록**: 이동 거리, 시간, 평균 속도, 소모 칼로리 등 핵심 운동 데이터를 실시간으로 계산하고 보여줍니다.
- **러닝 히스토리**: 완료된 러닝 기록을 Room Database에 로컬 저장하여 언제든지 과거 기록을 조회할 수 있습니다.
- **통계 및 분석**: 주간/월간 러닝 데이터를 그래프 형태로 시각화하여 사용자의 운동 성과를 분석해줍니다.
- **백그라운드 트래킹**: Foreground Service를 통해 앱이 백그라운드 상태일 때도 안정적으로 위치를 추적합니다.

## 🛠 기술 스택
- **Language**: Kotlin
- **Architecture**: MVVM, Clean Architecture
- **UI Framework**: Jetpack Compose (Material3)
- **Dependency Injection**: Hilt
- **Asynchronous Processing**: Coroutines, Flow
- **Local Database**: Room
- **Network & Location**: Google Maps SDK, Fused Location Provider Client
- **Ads**: Google AdMob

## 📸 스크린샷

| 홈 | 운동 시작 | 종료 | 저장된 런닝 기록 |
|----|-----------|----------|------|
| <img src="https://github.com/user-attachments/assets/d9bf5863-918a-4fd7-b9d4-7605a38b1e05" width="180"/> | <img src="https://github.com/user-attachments/assets/8e8237da-89ac-4fe8-86b0-ac9b831e9b58" width="180"/> |<img src="https://github.com/user-attachments/assets/b56be834-1bb8-438c-889a-44721fd7bf34" width="180"/> | <img src="https://github.com/user-attachments/assets/1e491055-1eb4-49da-a062-0474984a1df3" width="180"/>  |
