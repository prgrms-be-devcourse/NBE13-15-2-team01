# 티켓 예매 시스템 (Ticketing)

공연 정보를 조회하고, 회차와 좌석을 선택해 예매·취소할 수 있는 REST API 서버입니다.
NBE13-15 2차 팀 프로젝트 (team01)
자리있조

## 프로젝트 소개

- 공연 목록과 상세 정보를 조회합니다.
- 회차(날짜·시간)별 좌석을 확인하고 예매합니다.
- 같은 좌석의 중복 예매를 DB 제약으로 막습니다.
- 한 회원은 한 공연의 한 회차당 최대 3매까지 예매할 수 있습니다.
- 예매 내역 조회와 취소를 지원합니다.

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build | Gradle |
| DB | MySQL 8 (Docker) |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security (BCrypt) |
| API 문서 | springdoc-openapi 3.2.0 (Swagger UI) |

## 시작하기

### 1. 사전 준비

- JDK 21
- Docker

### 2. MySQL 실행

```
docker run -d --name ticketing-db -p 3306:3306 -e MYSQL_ROOT_PASSWORD=<root 비밀번호> -e MYSQL_DATABASE=ticketing mysql:8
```

이미 컨테이너가 있다면 아래 명령으로 시작합니다.

```
docker start ticketing-db
```

### 3. DB 계정 생성

컨테이너에 접속합니다.

```
docker exec -it ticketing-db mysql -uroot -p
```

아래 SQL을 실행합니다. (`docs/sql/mysql-setup.sql`과 동일)

```sql
CREATE DATABASE IF NOT EXISTS ticketing;
CREATE USER IF NOT EXISTS 'ticketing'@'%' IDENTIFIED BY 'ticketing1234';
GRANT ALL PRIVILEGES ON ticketing.* TO 'ticketing'@'%';
FLUSH PRIVILEGES;
```

계정 호스트는 반드시 `%`여야 합니다. `localhost`로 만들면 Docker 환경에서 접속이 거부됩니다.

### 4. 환경 변수 (선택)

기본값은 위에서 만든 `ticketing` / `ticketing1234` 계정입니다.
다른 계정을 쓰려면 프로젝트 루트에 `.env`를 만들어 값을 덮어씁니다.
변수 이름은 `.env.example`을 복사해서 사용하세요. `.env`는 Git에 올리지 않습니다.

### 5. 애플리케이션 실행

```
./gradlew bootRun
```

Windows에서는 `gradlew.bat bootRun` 또는 IntelliJ에서 `TicketingApplication`을 실행합니다.

테이블은 앱 실행 시 `schema.sql`로 자동 생성됩니다.

### 6. 확인

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs/v1

## 데이터베이스

| 테이블 | 설명 |
|---|---|
| member | 회원 (email 유니크, role: USER / ADMIN) |
| performance | 공연 정보 (KOPIS 공연 ID 기준) |
| schedule | 공연 회차 (OPEN / CLOSED) |
| seat | 회차별 좌석 (회차 내 좌석명 유니크) |
| reservation | 예매 내역 (1행 = 1좌석, RESERVED / CANCELED) |

### 중복 예매 방지

`reservation.active_seat_id`에 유니크 제약을 둡니다.
예매 중에는 `seat_id` 값을, 취소되면 `NULL`을 저장하므로 같은 좌석에 활성 예매가 둘 이상 생길 수 없습니다.

### schema.sql 규칙

- 앱 실행 시마다 실행되므로 `IF NOT EXISTS`를 사용합니다.
- 엔티티를 추가하거나 수정하면 `schema.sql`도 같은 PR에서 수정합니다.
- 이미 생성된 테이블의 컬럼 변경은 반영되지 않습니다. 로컬에서 해당 테이블을 삭제한 뒤 다시 실행하세요.
- 외래키 때문에 삭제 순서는 `reservation`, `seat`, `schedule`, `performance`, `member`입니다.

## 에러 응답 형식

모든 예외는 아래 형식으로 응답합니다.

```json
{
  "code": "SEAT_ALREADY_RESERVED",
  "message": "이미 예매된 좌석입니다.",
  "fieldErrors": {}
}
```

| 상태 코드 | 의미 | 예시 |
|---|---|---|
| 400 | 잘못된 요청 | VALIDATION_ERROR |
| 401 | 인증 실패 | LOGIN_FAILED |
| 403 | 권한 없음 | NOT_RESERVATION_OWNER |
| 404 | 대상 없음 | PERFORMANCE_NOT_FOUND |
| 409 | 상태 충돌 | SEAT_ALREADY_RESERVED, TICKET_LIMIT_EXCEEDED |
| 500 | 서버 오류 | INTERNAL_ERROR |

### 새 예외 추가 방법

1. `error` 패키지에 `RuntimeException`을 상속한 클래스를 만들고 고정 메시지를 지정합니다.
2. `ApiExceptionAdvice`에서 해당 상태 코드의 `@ExceptionHandler` 목록에 클래스를 추가합니다. 추가하지 않으면 500으로 응답됩니다.

## 프로젝트 구조

```
src/main/java/com/team01/ticketing
├── config        Swagger, Security 등 설정
├── error         예외 클래스와 exceptionhandler
├── member        회원
├── performance   공연
├── schedule      회차
├── seat          좌석
└── reservation   예매
```

패키지 구성은 개발 진행에 따라 달라질 수 있습니다.

## 협업 규칙

### 브랜치

- `main`: 최종본
- `dev`: 개발 통합 브랜치
- `<<작업명>- main>`: 기능 개발 (예: `infra-main`)

`dev` 브랜치가 존재하므로 `dev/...` 형태의 브랜치 이름은 사용할 수 없습니다.

### PR

- `feature/*` 브랜치에서 `dev`로 PR을 보냅니다.
- 최소 1명의 리뷰를 받은 뒤 머지합니다.

### API 규칙

- 경로는 `/api/v1/...` 형태로 통일합니다.
- 컨트롤러에 `@Tag`, `@Operation`을 붙여 Swagger 문서를 작성합니다.
- Pageable 파라미터에는 `@ParameterObject`를 붙입니다.

### 공통 규칙

공유 설정 파일(build.gradle, application.yml, schema.sql)은 직접 수정하지 말고 요청
엔티티 추가·수정 시 schema.sql도 같은 PR에서 수정
이미 있는 테이블의 구조 변경은 반영되지 않음 → 로컬에서 해당 테이블 삭제 후 재실행

## 팀원

| 이름 | 담당 |
|---|---|
| 김지호 | 회원/인증 |
| 이지원 | 공연/회차 |
| 이제섭 | 좌석/예매 |
| 맹주석 | 공통/인프라, 기획서·발표 총괄 |
