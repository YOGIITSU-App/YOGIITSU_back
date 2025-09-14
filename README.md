# 요기있수 (YOGIITSU)

> 📱 **수원대학교 전용 스마트캠퍼스 애플리케이션**  
수원대학교 학우들의 캠퍼스 생활을 더 편리하게 하기 위해 개발된 통합 플랫폼입니다.  
지도, 학식, 셔틀버스, 지름길, AI 챗봇 등 교내 생활 필수 기능을 한 곳에서 제공합니다.  

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

---

## 📅 개발 기간
- **2024.09 ~ 현재 (졸업 프로젝트)**  

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

## 🛠 기술 스택

### Backend
- Java 17, Spring Boot
- Spring Security + JWT
- JPA (Hibernate) + MySQL
- Gradle, Swagger(OpenAPI)

### Frontend
- React Native
- Kakao Map API, Naver Map API
- Tmap 도보 길찾기 API

### AI / Data
- Python FastAPI
- OpenAI GPT + LangChain
- FAISS(Vector DB), ElasticSearch
- Jsoup / BeautifulSoup 크롤링

### Infra / DevOps
- GitHub Actions (CI/CD)
- Docker / Docker Compose
- AWS (EC2, RDS, S3)

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
- 앱 설치 후 회원가입 (학교 이메일 인증 필수)
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

## 👨‍💻 저자 및 기여자
- 컴퓨터SW학과 졸업 프로젝트 팀 (2025)
- Frontend: [@jongmink0](https://github.com/jongmink0)
- Backend: [@parksomii](https://github.com/parksomii)
- Backend: [@gayoung228](https://github.com/gayoung228)
- Backend: [@joyes0ng](https://github.com/joyes0ng)
- Design: @design-lead

---

## 📌 프로젝트 상태 및 로드맵
- 현재 상태:
  - 안드로이드 배포 완료 (Google Play Store)
- 향후 계획:
  - iOS 버전 정식 배포 (App Store)
  - 다국어 지원 (영어, 중국어)

