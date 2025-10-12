# 🚀 TREEtment Backend 인증 시스템

## 📋 구현된 기능

### ✅ 완료된 기본 토대
1. **프로젝트 의존성 추가**
   - Spring Security
   - JWT (jjwt)
   - OAuth2 Client
   - Spring Mail

2. **패키지 구조 생성**
   ```
   src/main/java/com/treetment/backend/
   ├── auth/                    # 인증 관련
   │   ├── controller/
   │   ├── service/
   │   ├── repository/
   │   ├── entity/
   │   ├── dto/
   │   ├── domain/
   │   ├── oauth2/
   │   └── exception/
   ├── security/                # 보안 관련
   │   ├── filter/
   │   ├── handler/
   │   ├── principle/
   │   ├── util/
   │   └── logger/
   ├── global/                  # 전역 설정
   │   ├── config/
   │   ├── dto/
   │   └── exception/
   └── email/                   # 이메일 서비스
   ```

3. **핵심 컴포넌트**
   - **도메인**: ROLE, PROVIDER 열거형
   - **엔티티**: User, RefreshToken, PasswordResetToken
   - **Repository**: UserRepository, RefreshTokenRepository, PasswordResetTokenRepository
   - **보안**: JwtUtil, CustomPrincipal, JwtAuthenticationFilter
   - **응답 처리**: ApiResponse, GlobalResponseHandler, ResponseUtil
   - **예외 처리**: AuthException, AuthErrorCode, GlobalExceptionHandler

## 🔧 설정 파일

### application.yml
- JWT 설정 (시크릿 키, 만료 시간)
- OAuth2 설정 (Google, Kakao, Naver)
- 이메일 설정 (Gmail SMTP)
- CORS 설정
- 로깅 설정

### 환경 변수 (.env)
```bash
# OAuth2 클라이언트 정보
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret

# 이메일 설정
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

## 🚀 다음 단계

### 1. 인증 서비스 구현
- AuthService 클래스 생성
- 회원가입, 로그인, 로그아웃 로직
- 비밀번호 재설정 기능

### 2. OAuth2 서비스 구현
- CustomOAuth2UserService
- OAuth2UserInfo 팩토리
- 소셜 로그인 처리

### 3. 이메일 서비스 구현
- 이메일 인증 기능
- 비밀번호 재설정 이메일

### 4. 보안 강화
- Rate Limiting
- 보안 헤더 설정
- 로그인 시도 제한

### 5. 테스트 작성
- 단위 테스트
- 통합 테스트
- 보안 테스트

## 📚 API 엔드포인트

### 인증 관련
- `GET /api/auth/health` - 서비스 상태 확인
- `GET /api/auth/me` - 현재 사용자 정보 (인증 필요)

### 보안 설정
- 모든 `/api/auth/**` 엔드포인트는 인증 없이 접근 가능
- 나머지 엔드포인트는 JWT 토큰 인증 필요
- CORS 설정으로 모든 도메인에서 접근 가능

## 🔒 보안 기능

1. **JWT 토큰 기반 인증**
   - Access Token (30분)
   - Refresh Token (7일)
   - HttpOnly 쿠키 사용

2. **비밀번호 암호화**
   - BCrypt 사용

3. **예외 처리**
   - 전역 예외 핸들러
   - 표준화된 에러 응답

4. **보안 로깅**
   - 로그인 성공/실패 로그
   - IP 주소 및 User-Agent 추적

## 🛠️ 개발 환경 실행

```bash
# 의존성 설치
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 테스트 실행
./gradlew test
```

## 📝 참고사항

- JWT 시크릿 키는 프로덕션에서 반드시 변경해야 합니다
- OAuth2 클라이언트 정보는 각 플랫폼에서 발급받아야 합니다
- 이메일 설정은 Gmail 앱 비밀번호를 사용해야 합니다
- 데이터베이스 설정은 application-prod.yml에서 관리됩니다
