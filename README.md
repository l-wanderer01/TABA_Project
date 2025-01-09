<h1>눈맞춤</h1>

시각장애인을 위한 AI 기반 실시간 안전 및 커뮤니케이션 보조 시스템

<h2>기획 배경</h2>
1. 한국 내 시각장애인 규모

2022년 통계청 자료에 따르면, 점점 더 장애를 가진 사람들이 늘어나고 있는 추세임.
![image](https://github.com/user-attachments/assets/e58f1fcb-5bb1-4d4a-9311-209f29cfa1a3)

2. 장애인 실생활 상태
장애인들의 실생활에서 행복감을 느끼는 수치가 10점 만점 중 5.79점으로, 전체 인구 6.56점보다 낮은 행복감을 느낌.
![image](https://github.com/user-attachments/assets/7571e48f-0df7-4f1c-81eb-005692769264)

3. 장애인 의사소통 문제
의사소통은 크게 “언어적 표현”과 “비언어적 표현”으로 나뉘는데, 시각장애인 분들은 비언어적 표현 (특히 표정) 을 읽지 못함.
![image](https://github.com/user-attachments/assets/0b860955-a55e-4fa8-ad34-110beac7f233)

4. 시각장애인이 외부 활동을 할 때, 주의해야 할 것들이 너무 많음.
![image](https://github.com/user-attachments/assets/1f4a664f-cf7c-4a89-b00b-c152ec1b8944)


이동 모드의 필요성<br> 
[http://www.kbuwel.or.kr/Board/Release/Detail?page=1&contentSeq=1214901
](https://kbuwel.or.kr/Board/Release/Detail?page=1&contentSeq=1214901)


- 현재 기본적으로 우리 어플을 사용하는 방법
[https://theindigo.co.kr/archives/11540]

아이폰 : ‘보이스 오버’    안드로이드 : ’보이스 어시스턴트(구)톡백)  ← 시각장애인 용 어플 사용 도구<br>

사용 시 화면을 손으로 터치, 드래그 중이면 해당 어플이 무슨 어플인지 이름을 음성으로 알려 이를 통해 우리 어플 클릭까지는 가능<br>

 → 이후 어플 클릭 뒤 부터는 우리 어플 자체에서 페이지, 기능 실행 시 마다 음성 출력

서비스 소개(주요기능)

- 대화 모드
대화 상대방의 성별과 나이를 자동으로 인식하여, 시각장애인이 보다 효과적으로 커뮤니케이션을 할 수 있도록 지원하는 기능<br>
- 이동 모드<br>
카메라를 통해 시각장애인에게 다가오는 가장 가까운 객체(사람, 사물)를 판별하고, 사용자에게 다가온다는 음성을 들려줌<br>
- 관리자 페이지
글씨 폰트 크기 및 보호자에게 제공되는 기능<br>
    

화면 정의서(주요 UI)

- 메인 페이지<br>
- 대화 모드 사진<br>
- 이동 모드 사진<br>
- 보호자 페이지<br>

시스템 아키텍쳐

![image](https://github.com/user-attachments/assets/685dc6cf-f6b5-4cf9-a07e-c9fff1861535)

서비스 플로우

<img width="1252" alt="image" src="https://github.com/user-attachments/assets/eb194aba-d4a3-4d6d-996a-d1965c82cc8b" />

유저 시나리오 

- 아들의 결혼 상대를 만나러 간 80대 A씨 첫 소개 받는 자리에서 예비 며느리와 대화할 때 며느리의 단순 목소리와 대화 내용으로는 부족 → 대화 모드 사용<br>
- 보호자의 급한 일로 잠시 혼자 길가에서 기다리게 된 10대 a 양 혼자는 위험 함 → 이동 모드 사용<br>

향후계획, 비전<br>

1. 점자 스마트워치 닷워치 가정 (시각장애인)<br>
  
[[한국이 만든 세계 최초 시각장애인을 위한 스마트워치 / 스브스뉴스](https://www.youtube.com/watch?v=6xoDkY59HWE)
](https://www.youtube.com/watch?v=6xoDkY59HWE)
닷워치 → 스마트폰과 블루투스 연결로 알림 등 기존 스마트워치가 가지고 있는 대부분의 기능을 구현함.<br>

그래서, 우리 앱을 통해 현재는 소리로 인식할 수 있게끔 하지만, Develop 하여 닷워치와의 연동을 통해 점자로 인식할 수 있게끔 할 예정이다.<br>

<h2>Front End</h2>
1. Flutter
    
<h2>Back End</h2>
1. Spring Boot<br>
2. MySQL<br>
3. Nginx<br>

<h2>AI</h2>
1. Docker<br>
2. FastAPI<br>
3. Google Colab<br> 
