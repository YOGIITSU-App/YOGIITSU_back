# 예외처리 가이드라인

## 개요
이 프로젝트는 일관된 예외처리를 위해 계층화된 예외 구조를 사용합니다.

## 예외 구조

### 1. 기본 예외 클래스
- `BaseException`: 모든 커스텀 예외의 기본 클래스
- `ErrorCode`: 애플리케이션 전체에서 사용되는 에러 코드 정의

### 2. 예외 분류
- **인증/인가 예외** (`com.YOGIITSU.exception.auth`)
  - `UnauthorizedException`: 인증되지 않은 사용자
  - `InvalidTokenException`: 유효하지 않은 토큰
  - `MissingTokenException`: 토큰 누락
  - `InvalidLoginException`: 잘못된 로그인 정보
  - `EmailVerificationNotApprovedException`: 이메일 인증 미완료
  - `AccessDeniedException`: 접근 권한 없음
  - `AdminAccessDeniedException`: 관리자 권한 없음

- **사용자 관련 예외** (`com.YOGIITSU.exception.user`)
  - `MemberNotFoundException`: 회원을 찾을 수 없음
  - `MemberAlreadyExistsException`: 이미 존재하는 회원
  - `PasswordMismatchException`: 비밀번호 불일치
  - `PasswordNotEqualsException`: 새 비밀번호와 확인 비밀번호 불일치
  - `SamePasswordException`: 기존 비밀번호와 동일한 비밀번호 변경 시도
  - `SameEmailException`: 기존 이메일과 동일한 이메일 변경 시도

- **리소스 관련 예외** (`com.YOGIITSU.exception.resource`)
  - `BuildingNotFoundException`: 건물을 찾을 수 없음
  - `CafeteriaNotFoundForBuildingException`: 건물에 식당이 없음
  - `NoticeNotFoundException`: 공지사항을 찾을 수 없음
  - `InquiryNotFoundException`: 문의를 찾을 수 없음
  - `FavoriteNotFoundException`: 즐겨찾기를 찾을 수 없음
  - `FavoriteAlreadyExistsException`: 이미 즐겨찾기에 추가된 리소스
  - `ShuttleStopNotFoundException`: 셔틀 정류장을 찾을 수 없음
  - `ResourceNotFoundException`: 기타 리소스를 찾을 수 없음
  - `ResourceException`: 기타 리소스 관련 예외

- **외부 서비스 관련 예외** (`com.YOGIITSU.exception.external`)
  - `AppleExchangeException`: Apple 토큰 교환 실패
  - `AppleTokenInvalidException`: Apple 토큰이 유효하지 않음
  - `AppleVerificationException`: Apple 토큰 검증 실패
  - `ApplePublicKeyNotFoundException`: Apple 공개키를 찾을 수 없음
  - `GoogleAuthException`: Google 인증 실패
  - `KakaoAuthException`: Kakao 인증 실패
  - `EmailSendException`: 이메일 전송 실패
  - `ExternalServiceException`: 기타 외부 서비스 관련 예외

- **유효성 검사 관련 예외** (`com.YOGIITSU.exception.validation`)
  - `ValidationException`: 유효성 검사 관련 기본 예외
  - `InvalidArgumentException`: 잘못된 인수
  - `MissingRequiredFieldException`: 필수 필드 누락

- **시스템 관련 예외** (`com.YOGIITSU.exception.system`)
  - `SystemException`: 시스템 관련 기본 예외
  - `DatabaseException`: 데이터베이스 오류
  - `ExternalServiceException`: 외부 서비스 오류

## 사용 방법

### 1. 예외 발생시키기
```java
// 기본 사용법
throw new MemberNotFoundException();

// 상세 메시지와 함께
throw new MemberNotFoundException("memberId=123");

// ErrorCode를 직접 사용
throw new BuildingNotFoundException(buildingId);

// 즐겨찾기 관련 예외
throw new FavoriteAlreadyExistsException();
throw new FavoriteNotFoundException();
```

### 2. 예외 처리하기
```java
try {
    // 비즈니스 로직
} catch (MemberNotFoundException e) {
    // 특정 예외 처리
    log.warn("Member not found: {}", e.getDetailMessage());
    throw e; // GlobalExceptionHandler가 처리
}
```

### 3. 새로운 예외 추가하기
1. 적절한 패키지에 예외 클래스 생성
2. `BaseException`을 상속받아 구현
3. `ErrorCode`에 새로운 에러 코드 추가
4. `GlobalExceptionHandler`에 처리 로직 추가 (필요시)

## 에러 응답 형식

모든 예외는 다음과 같은 일관된 형식으로 응답됩니다:

```json
{
  "code": "USER_001",
  "message": "존재하지 않는 계정입니다.",
  "detail": "memberId=123",
  "timestamp": "2024-01-01T12:00:00",
  "status": 404
}
```

## 에러 코드 체계

- **AUTH_XXX**: 인증/인가 관련 (1000번대)
- **USER_XXX**: 사용자 관련 (2000번대)
- **RESOURCE_XXX**: 리소스 관련 (3000번대)
- **EXTERNAL_XXX**: 외부 서비스 관련 (4000번대)
- **VALIDATION_XXX**: 유효성 검사 관련 (5000번대)
- **SYSTEM_XXX**: 시스템 관련 (9000번대)

## 주의사항

1. **예외는 가능한 한 구체적으로** 발생시키세요
2. **예외 메시지는 사용자에게 친화적**이어야 합니다
3. **로깅은 적절한 레벨**로 설정하세요 (WARN, ERROR)
4. **민감한 정보는 로그에 포함하지 마세요**
5. **예외 체인을 유지**하여 원인을 추적할 수 있도록 하세요