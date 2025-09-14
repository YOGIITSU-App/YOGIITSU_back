<p align="center">
  <picture>
    <!-- 다크/라이트 모드 배너를 따로 쓰고 싶으면 source 한 줄을 더 추가하세요 -->
    <img width="1024" height="500" alt="Readme_pic" src="https://github.com/user-attachments/assets/5c781bcb-83db-4495-ac32-ad66e8609741" />
  </picture>
</p>

<h1 align="center">YOGIITSU · 요기있수</h1>

<p align="center">📱 수원대학교 전용 스마트캠퍼스 애플리케이션 </p>
<p align="center">
  수원대학교 학우들의 캠퍼스 생활을 더 편리하게 하기 위해 개발된 통합 플랫폼입니다.<br/>
  지도, 학식, 셔틀버스, 지름길, AI 챗봇 등 교내 생활 필수 기능을 한 곳에서 제공합니다.
</p>

<p align="center">
  <a href="https://www.youtube.com/watch?v=uGwNB6_aN9s">🎬 시연 영상</a>
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.yogiitsuapp&hl=ko">
    <img src="https://img.shields.io/badge/요기있수%20다운로드-3DDC84?style=for-the-badge&logo=android&logoColor=white"
         alt="요기있수 다운로드" />
  </a>
</p>

---

## 📑 목차

1. [프로젝트 소개](#-프로젝트-소개)  
2. [주요 기능](#-주요-기능)  
3. [기술 스택](#-기술-스택)  
4. [설치 및 실행 방법](#-설치-및-실행-방법)  
5. [사용법 (Usage)](#-사용법-usage)  
6. [기여 방법 (Contributing)](#-기여-방법-contributing)  
7. [테스트](#-테스트)  
8. [저자 및 기여자](#-저자-및-기여자)  
9. [프로젝트 상태 및 로드맵](#-프로젝트-상태-및-로드맵)  
10. [라이선스](#-라이선스)  

---

## 📖 프로젝트 소개

- 프로젝트명: **요기있수 (YOGIITSU)**  
- 카테고리: **스마트 캠퍼스 앱**  
- 목적: 수원대학교 학우들의 교내 생활 필수 정보를 **한눈에, 한 손에** 제공하는 서비스  
- 플랫폼: Android, iOS
  

### 📅 개발 기간
- **2025.01 ~ 현재 (졸업 프로젝트)**  


### 🔔 서비스 배경
> “처음 가는 건물은 위치가 헷갈리고, 프린터기나 셔틀버스 위치를 물어보는 글이 늘 올라오고,  
> 지름길이 있는 줄 모르고 멀리 돌아간 경험… 저희도 새내기 때 이런 경험을 많이 했습니다.”

수원대학교 학생들은 학교 시스템이나 지도만으로는 필요한 정보를 충분히 얻기 어려워  
여전히 커뮤니티나 익명 게시판에 의존할 수밖에 없는 상황입니다.  

이에 따라 학생들에게 꼭 필요한 정보를 한눈에 확인할 수 있는 스마트캠퍼스 앱 ‘요기있수’를 기획하게 되었습니다.  
지도 기반으로 학내 시설, 셔틀버스, 학식, 지름길 경로 등 캠퍼스 생활 필수 기능을 제공하여  
학생들의 불편을 해소하고 더 편리한 학교 생활을 지원합니다.  


### 👩‍🎓 서비스 대상
- **신입생**: 건물 위치, 시설 이용, 셔틀버스 정보가 필요한 학생  
- **편의시설을 빠르게 찾고 싶은 학우**: 프린터기, 카페, 편의점, 식당 등  
- **효율적으로 이동하고 싶은 학우**: 지름길 안내를 통해 불필요한 이동을 줄이고 싶은 학생  
- **통합 정보가 필요한 학우**: 학식, 셔틀버스 등 생활 정보를 한 곳에서 확인하고 싶은 학생  
  
---

## 🚀 주요 기능

- 🏫 **캠퍼스 지도 & 길찾기**
  - 카카오맵 + Tmap API 기반 도보 길찾기
  - 건물별·층별 편의시설 안내 (프린터, 식당, 카페, 편의점 등)
  - 지름길 경로 및 건물 별칭 검색

- 🍽️ **학식 조회**
  - 크롤링 + DB 저장으로 자동 업데이트
  - 오늘/이번 주 학식 메뉴 확인

- 🚌 **셔틀버스**
  - 수원대학교 셔틀버스 시간표 및 노선
  - 정류장별 버스 도착 예상 시간

- 📢 **공지사항**
  - 관리자 전용 공지 등록/수정/삭제

- 🤖 **AI 챗봇**
  - RAG 기반 자유 질의응답
  - 학내 시설·공지 실시간 조회
  - 선택형 챗봇 제공

- 👤 **회원 관리**
  - 이메일 인증 회원가입
  - JWT 로그인/로그아웃
  - 이메일 변경, 비밀번호 찾기/변경

---

## 🛠️ 기술 스택 (Tech Stack)

###  📱 Frontend

- **프레임워크:** React Native
- **API:** Kakao Map API, TMap 보행자 경로 탐색 API

###  🖥️ Backend

- **언어 및 프레임워크:** Java 21, Spring Boot
- **보안:** Spring Security, JWT (JSON Web Token)
- **데이터베이스 및 ORM:** MySQL, JPA (Hibernate)
- **빌드 도구:** Gradle
- **문서화:** Swagger (OpenAPI)
- **API:** TMap API, Kakao Map API


###  🚀 배포 및 인프라 (Deployment & Infrastructure)

- **클라우드 서비스:** AWS (Amazon Web Services)
    - **컴퓨팅:** EC2 (Elastic Compute Cloud)

    - **데이터베이스:** RDS (Relational Database Service)
    - **스토리지:** S3 (Simple Storage Service)
    - **콘텐츠 전송 네트워크 (CDN):** CloudFront
    - **모니터링:** CloudWatch

- **컨테이너:** Docker, Docker Compose
- **웹 서버:** Nginx
- **도메인 관리:** Route 53
- **SSL/TLS:** Certbot
- **CI/CD:** GitHub Actions
- **버전 관리:** Git, GitHub

---

## ⚡ 설치 및 실행 방법

### Backend (Spring Boot)
```bash
cd backend
./gradlew build
./gradlew bootRun
```
---

## 📖 사용법 (Usage)
- 앱 설치 후 회원가입
- 지도에서 원하는 건물/편의시설 검색
- 오늘 학식 메뉴와 셔틀버스 시간 확인
- 공지사항과 AI 챗봇으로 필요한 정보 탐색

---

## 🤝 기여 방법 (Contributing)
1. 이슈 등록: 버그/개선 제안은 Issues 활용
2. 브랜치 전략:
  - main: 안정화 배포 브랜치
  - develop: 개발 통합 브랜치
  - feature/*: 기능별 브랜치
3. Pull Request:
  - 코드 스타일 가이드 준수 (Java: Google Style)
  - 테스트 코드 포함 필수
  - 리뷰어 1명 이상 승인 후 병합 가능
### Commit Convention  
| Commit Type        | Description                              |
|--------------------|------------------------------------------|
| `feat`             | 새로운 기능 추가                                |
| `fix`              | 버그 수정                                    |
| `docs`             | 문서 수정                                    |
| `style`            | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우 |
| `refactor`         | 코드 리팩토링                                  |
| `test`             | 테스트 코드, 리팩토링 테스트 코드 추가                   |
| `chore`            | 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore     |
| `design`           | CSS 등 사용자 UI 디자인 변경                      |
| `comment`          | 필요한 주석 추가 및 변경                           |
| `rename`           | 파일 또는 폴더 명을 수정하거나 옮기는 작업만인 경우            |
| `remove`           | 파일을 삭제하는 작업만 수행한 경우                      |
| `!breaking change` | 커다란 API 변경의 경우                           |
| `!hotfix`          | 급하게 치명적인 버그를 고쳐야 하는 경우                   |

---

## ✅ 테스트
```bash
cd backend
./gradlew test
```

---


## 🧑‍🤝‍🧑 프로젝트 팀원
### **Frontend Team**
|                    김종민                     |
|:------------------------------------------:|
| ![](https://github.com/jongmink0.png?size=120) | 
|      [@jongmink0](https://github.com/parksomii)      |  

### **Backend Team**

|                    박소미                     |                     이가영                      |                      조예성                       |
|:------------------------------------------:|:--------------------------------------------:|:----------------------------------------------:|
| ![](https://github.com/parksomii.png?size=120) | ![](https://github.com/gayoung228.png?size=120) | ![](https://github.com/joyes0ng.png?size=120) |
|      [@parksomii](https://github.com/parksomii)      |    [@gayoung228](https://github.com/gayoung228)    |   [@joyes0ng](https://github.com/joyes0ng)   |


---

## 📌 프로젝트 상태 및 로드맵
- 현재 상태:
  - 안드로이드 배포 완료 (Google Play Store)
- 향후 계획:
  - iOS 버전 정식 배포 (App Store)
  - 다국어 지원 (영어, 중국어)
