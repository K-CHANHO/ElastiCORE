# 파일 첨부 설계

이슈 #185. 코드 전에 합의할 설계안이다.

## 1. 문제

행에 파일이 붙는 일은 흔하다: 글의 첨부파일, 신청서의 증빙 서류, 상품 사진. 지금 DSL 은 이것을 말할 수 없다. 엔티티의 필드는 파일이
될 수 없고, port DSL 의 multipart 는 애플리케이션이 **밖으로 보내는** 요청용이다. 그래서 프로젝트마다 같은 것을 손으로 쓴다.

실제 프로젝트 하나(헥사고날, 엔티티 12개, Spring Boot 4)가 손으로 쓴 것:

| 손코드 | 줄 | 하는 일 |
|---|---|---|
| `StoredFile` 엔티티 (모델로 씀) | - | 파일 정보: 올린 사람, 쓰임, 붙은 행, 저장 위치, 원래 이름, 확인한 종류, 크기 |
| `FileController` | 87 | multipart 올리기, 내려받기(안전한 응답 헤더), 글의 첨부 목록 |
| `FileService` | 371 | 크기·종류·하루 한도 검사, 내려받기 권한, 글과 잇기·떼기, 커밋 뒤에 지우기, 쓰지 않은 파일 정리 |
| `FileRules` | 139 | 쓰임별 확장자·크기, 파일 첫 바이트로 종류 확인, 파일 이름 다듬기 |
| `FileStoragePort` + `LocalFileStorage` | 25 + 79 | 저장소 포트와 서버 디스크 구현 (경로 밖으로 못 나가게, 임시 파일 뒤 원자적 이동) |
| `UploadLimitErrors` | 27 | multipart 한도를 넘은 요청을 413 문제 응답으로 |

이 가운데 프로젝트마다 다른 것은 표의 값(크기, 종류, 개수)과 권한 규칙 몇 줄이다. 나머지는 어느 프로젝트에서나 같고, 틀리기 쉽다:
올린 사람이 보낸 Content-Type 을 믿기, 파일 이름으로 경로 만들기, 롤백된 요청이 파일을 지우기, 이미지가 아닌 파일을 화면에 그대로 열기.

## 2. 목표와 비목표

**목표**
1. 필드 한 줄로 "이 행에는 파일이 붙는다" 와 그 규칙을 말한다. 생성기는 올리기·내려받기·잇기·정리를 쓴다.
2. 내려받기는 그 파일이 붙은 **행을 읽을 수 있는 사람**에게만 된다. 행의 `@access` 와 서비스의 읽기 경로를 그대로 따른다.
3. 저장소는 포트 뒤에 둔다. 생성기는 서버 디스크 구현 하나를 처음 한 번 쓰고, 그 뒤로는 프로젝트의 것이다.
4. 파일 필드가 없는 프로젝트는 생성물이 **한 글자도 바뀌지 않는다**.

**비목표** (애플리케이션의 몫으로 남긴다)
- 객체 저장소(S3 등) 구현 - 포트를 구현하면 된다.
- 바이러스 검사, 이미지 줄이기·썸네일, 미리보기.
- 간단한 하루 용량 말고의 한도 (디스크 여유, 요금제별 한도).
- HTML 본문 안의 이미지를 글과 잇기 (`html` 필드와 엮는 것은 다음 단계, 6장).
- 서명된 URL 로 저장소에 바로 올리기.

## 3. 설계

### 3.1 DSL

```yaml
Post:
  meta: entity @root @expose @owner(authorId) @access(read=public, create=user, update=owner)
  fields:
    attachments: files @file(maxCount=5, maxSize=20MB, types="pdf,docx,xlsx,pptx,txt,image")

Verification:
  fields:
    document: file! @file(maxSize=10MB, types="pdf,jpg,png")
```

- `file` 은 파일 하나, `files` 는 여러 개다. `!` 는 "하나 이상" 이다.
- `@file` 의 값은 모두 한 줄에 쓸 수 있는 것만: `maxSize`, `types`, `maxCount` (`files` 만).
  빠지면 `maxSize=10MB`, `maxCount=10`. `types` 는 꼭 쓴다 - 아무 파일이나 받는 것을 기본으로 두지 않는다.
- `types` 는 확장자 목록이다. 생성기가 아는 표(3.4)에 있는 것만 쓸 수 있다. `image` 는 jpg·png·gif·webp 를 묶은 이름이다.
  표에 없는 확장자는 빌드를 멈춘다 - 첫 바이트로 확인할 방법이 없는 종류를 조용히 받지 않으려고.
- 도메인 단위 설정은 `env.yml` 에:

```yaml
elasticore:
  config:
    files:
      dir: ${FILES_DIR:uploads}   # 생성된 LocalFileStorage 가 쓰는 폴더
      dailyLimit: 300MB           # 한 사람이 하루에 올리는 양 (운영자 제외). 빠지면 한도 없음
      unattachedHours: 24         # 올리고 어디에도 붙이지 않은 파일을 지우기까지
```

### 3.2 행이 파일을 가리키는 법

파일 정보는 도메인마다 한 테이블 `stored_file` 에 둔다. 파일 행이 **자기가 붙은 자리**를 안다:

| 컬럼 | 뜻 |
|---|---|
| `slot` | 어느 엔티티의 어느 필드인가. 생성된 enum `FileSlot` (`POST_ATTACHMENTS`, `VERIFICATION_DOCUMENT`) |
| `owner_id` | 붙은 행의 키. 올렸지만 아직 붙지 않았으면 비어 있다 |
| `position` | `files` 의 순서 |
| `uploader_id`, `storage_key`, `original_name`, `content_type`, `size_bytes`, `created_at` | 올린 사람, 저장소 안의 위치(서버가 만든 임의 값), 원래 이름(보여 주기만), 확인한 종류, 크기 |

여러 엔티티의 행을 가리키므로 외래 키는 없다 - `(slot, owner_id)` 에 색인을 둔다. 엔티티의 테이블은 바뀌지 않는다.

DTO 에서는 지금 관계를 쓰는 방식과 같게, 번호와 내용 두 갈래로 나온다:

| 필드 | DTO | 요청 | 응답 |
|---|---|---|---|
| `attachments: files` | `attachmentIds: List<Long>`, `attachments: List<StoredFileDTO>` (읽기 전용) | 붙일 파일 번호, 순서대로 | 둘 다 |
| `document: file` | `documentId: Long`, `document: StoredFileDTO` (읽기 전용) | 파일 번호 | 둘 다 |

`StoredFileDTO` 는 `fileId`, `name`, `sizeBytes`, `contentType`, `url` 이다. 저장 위치는 나가지 않는다.

**올리기와 잇기는 두 걸음이다.** 화면은 먼저 파일을 올려 번호를 받고, 행을 저장할 때 그 번호를 보낸다. 행의 생성·수정 요청은
지금처럼 JSON 이고, TypeScript 클라이언트와 생성된 검증도 그대로 쓴다.

행을 저장할 때 생성된 서비스가 하는 일 (행의 쓰기와 같은 트랜잭션):
- 요청의 번호가 `null` 이면 붙은 파일을 그대로 둔다 (파일을 다루지 않는 수정). 빈 목록이면 모두 뗀다.
- 새 번호는 **어디에도 붙지 않았고, 같은 slot 으로 올렸고, 지금 사람이 올린** 파일이어야 한다 (운영자는 누구 것이든). 아니면 400.
- 빠진 번호의 파일은 지운다. `maxCount` 를 넘으면 400.
- 행을 지우면 (`@onDelete(cascade)` 로 함께 지워지는 행 포함) 그 행의 파일도 지운다.
- 저장소의 파일은 **커밋된 뒤에** 지운다. 거절되어 되돌아간 요청이 파일을 없애지 않게.

### 3.3 생성되는 것

헥사고날 기준의 자리. layered·ddd 는 다른 산출물과 같은 규칙으로 놓인다.

| 무엇 | 자리 | 다시 생성 |
|---|---|---|
| `StoredFile` 엔티티, `FileSlot` enum, 마이그레이션 | `domain.model`, `db/migration` | 모델을 따른다 |
| `StoredFileDTO` | `application.dto` | 모델을 따른다 |
| `FileCoreService`: 올리기, 내려받기, 잇기·떼기, 정리 | `application` | 모델을 따른다 |
| `FileService extends FileCoreService` (다른 `XxxService` 와 같은 짝) | `application` | **처음 한 번** - 권한을 좁히는 훅을 여기서 |
| `FileStoragePort`: `store`, `load`, `delete` | `application.port.out` | 모델을 따른다 |
| `LocalFileStorage`: 서버 디스크 | `infrastructure.files` | **처음 한 번** - 그 뒤로 프로젝트의 것 |
| `<Domain>FileController`: `POST /api/<domain>/files`, `GET /api/<domain>/files/{fileId}` | `adapter.in.web` | 모델을 따른다 |
| 413·415 문제 응답 | `<Domain>ApiErrors` | 모델을 따른다 |
| TypeScript: `uploadFile(file, slot)`, `fileUrl(fileId)` | 생성된 클라이언트 | 모델을 따른다 |

**올리기** (`POST /api/<domain>/files`, multipart `file` + `slot`):
1. 누가 올릴 수 있나: 그 slot 의 엔티티를 **만들거나 고칠 수 있는** 사람 (`@access` 의 `create` 또는 `update`). 붙일 때 다시 본다.
2. 크기: `@file(maxSize)`. 넘으면 413. Spring 의 multipart 한도는 생성기가 가장 큰 `maxSize` 보다 조금 크게 안내한다 - 그 한도를
   넘은 요청도 같은 413 문제 응답으로 답한다.
3. 종류: 확장자가 `types` 에 있고, **파일 첫 바이트가 그 확장자와 맞아야** 한다. 응답에 쓰는 Content-Type 은 올린 사람이 보낸 값이
   아니라 표의 값이다. 아니면 415 와 받는 확장자 목록.
4. 하루 한도: `dailyLimit` 이 있으면 지난 24시간 동안 그 사람이 올린 양 + 이번 파일. 넘으면 429.
5. 저장소 위치는 서버가 만든다 (`2026/11/<uuid>.pdf`). 파일 이름으로 경로를 만들지 않는다.
6. 응답은 `StoredFileDTO`. 파일은 아직 어디에도 붙지 않았다.

**내려받기** (`GET /api/<domain>/files/{fileId}`):
- 붙지 않은 파일: 올린 사람과 운영자만.
- 붙은 파일: 그 행을 **그 행의 서비스로 읽어 본다** (`GET /{id}` 와 같은 `findById` - `@access`·`@owner` 와 서비스에 손으로 더한 읽기 검사가
  모두 지나는 길). 읽히면 내려준다. 못 읽으면 404 - 파일이 있다는 것도 알리지 않는다.
- 행은 읽혀도 파일은 더 좁혀야 하는 프로젝트(예: "목록은 누구나, 본문과 첨부는 회원만")를 위해 생성된 서비스에 한 군데 훅을 둔다
  (`mayDownload(file, row)`, 기본은 `true`, 처음 한 번 생성되는 `FileService` 에서 재정의).
- 응답 헤더는 늘 같다: 이미지만 `inline`, 나머지는 `attachment`, `X-Content-Type-Options: nosniff`,
  `Content-Security-Policy: default-src 'none'; sandbox`, `Cache-Control: private`.

**정리**: `purgeUnattached(Instant)` 가 `unattachedHours` 가 지나도록 붙지 않은 파일과, 붙은 행이 없어진 파일(손코드가 행을 직접 지운
경우)을 지운다. 생성된 `@Scheduled` 가 하루 한 번 부른다 - 애플리케이션이 `@EnableScheduling` 을 켜야 한다는 것은 빌드가 알린다.

### 3.4 종류 표

생성기가 아는 확장자와 첫 바이트. 이 표에 없는 것은 `types` 에 쓸 수 없다.

| 확장자 | Content-Type | 첫 바이트 |
|---|---|---|
| jpg, jpeg | image/jpeg | `FF D8 FF` |
| png | image/png | `89 50 4E 47 0D 0A 1A 0A` |
| gif | image/gif | `GIF87a`, `GIF89a` |
| webp | image/webp | `RIFF` ... `WEBP` |
| pdf | application/pdf | `%PDF-` |
| docx, xlsx, pptx, hwpx | Office Open XML, `application/hwp+zip` | zip (`50 4B 03 04`) |
| doc, xls, ppt, hwp | 예전 Office, `application/x-hwp` | OLE (`D0 CF 11 E0 A1 B1 1A E1`), hwp 는 `HWP Document File` 도 |
| txt | text/plain | 앞 8KB 에 NUL 이 없다 |

**SVG 는 넣지 않는다** - 안에 스크립트를 넣을 수 있고, 같은 사이트에서 열리면 그 사이트의 권한으로 돈다. 실행 파일과 압축 파일도 넣지 않는다.
zip 기반 문서끼리(docx 와 xlsx)는 첫 바이트로 가르지 못한다. 확장자를 믿되, 응답은 `attachment` 와 `nosniff` 로 보내 브라우저가 열지 않게 한다.

### 3.5 오류 응답

다른 생성된 거절과 같은 문제 응답(`ProblemDetail`, `code`)이다.

| 상황 | 상태 | `code` |
|---|---|---|
| 로그인하지 않고 올리기 (올리기가 `user` 이상) | 401 | `not-signed-in` |
| 그 slot 의 행을 만들 수도 고칠 수도 없음 | 403 | `forbidden` |
| `maxSize` 또는 multipart 한도를 넘음 | 413 | `file.too-large` |
| `types` 에 없거나 첫 바이트가 맞지 않음 | 415 | `file.type` |
| 하루 한도를 넘음 | 429 | `file.daily-limit` |
| 붙일 수 없는 파일 번호 (남의 것, 이미 붙은 것, 다른 slot), `maxCount` 를 넘음 | 400 | `file.unavailable`, `file.count` |
| 없거나 읽을 수 없는 파일 | 404 | `not-found` |

## 4. 검토한 대안

| 대안 | 판단 |
|---|---|
| 붙는 엔티티마다 파일 테이블에 컬럼 (`post_id`, `verification_id`) - 위 프로젝트가 지금 하는 방식 | 외래 키를 걸 수 있지만, 엔티티가 파일을 하나 더 가질 때마다 파일 테이블이 바뀌고 파일이 모든 주인을 알아야 한다. 생성기에는 맞지 않는다 |
| 필드마다 잇는 테이블 (`post_attachments(post_id, file_id, position)`) | 양쪽 외래 키와 순서가 깔끔하다. 테이블이 필드 수만큼 늘고, 붙지 않은 파일을 따로 찾아야 한다. **3.2 와 견줄 만하다 - 정할 것 1** |
| 행의 컬럼에 저장소 위치 문자열 | 파일 정보(크기, 종류, 올린 사람)가 없고, 저장 전에 올릴 수 없다 |
| 파일을 DB 에 (BLOB) | DB 와 백업이 커지고, 내려받을 때 메모리를 쓴다 |
| 행의 생성 요청에 파일을 함께 (multipart 한 번) | 행의 요청이 JSON 이 아니게 되어 TypeScript 클라이언트·검증·명령이 모두 갈라진다. 두 걸음이 낫다 |

## 5. 기존 프로젝트

`file`·`files` 필드가 없으면 생성물은 그대로다 - `stored_file` 도, 컨트롤러도, `env.yml` 의 `files` 도 읽지 않는다.
2.x 호환 출력(`output = 'v2'`)과 2.x 생성기는 파일 필드에서 빌드를 멈춘다 (`html` 과 같다). 1단계는 v3 · JPA · Java 이고,
Kotlin·MyBatis·MongoDB 는 같은 모양으로 뒤에 온다 - 그 전까지는 빌드가 멈추고 이유를 말한다.

## 6. 단계

1. **1단계**: 3장 전부 - 필드, `StoredFile`, 올리기·내려받기, 잇기·떼기·정리, `LocalFileStorage`, 오류 응답, TypeScript `uploadFile`.
2. **2단계**: Kotlin, `html` 필드 안의 이미지 (`<img src="/api/<domain>/files/{id}">` 를 찾아 행과 잇고, 빠진 이미지를 지우기).
3. **그 뒤**: MyBatis·MongoDB, 서명된 URL (포트에 `uploadUrl` 을 더하는 것).

## 7. 정할 것

1. 파일이 자리를 아는 한 테이블(3.2) 인가, 필드마다 잇는 테이블(4장) 인가.
2. 종류가 틀리면 415, 하루 한도는 429 - 아니면 다른 생성된 거절처럼 400·409 로 맞출까.
3. 종류 표(3.4)에 hwp·hwpx 를 넣을까 (한국에서는 흔하다).
4. 정리를 생성된 `@Scheduled` 로 돌릴까, 메서드만 두고 애플리케이션이 부르게 할까.

## 8. 검증 계획

- 생성기 테스트: `file`·`files` 필드가 있는 모델의 생성물, 그리고 없는 모델이 그대로인 것 (`elcoreRulesCheck -Pstrict` 0 DIFF,
  `elcoreTreeCheck` 0 differ).
- HTTP 테스트 (샘플 모듈 하나에 `files` 필드를 더해서): 올리기·내려받기, 413·415·429, 남의 파일을 붙이기 400, 붙은 파일은 행을 읽는
  사람만, 읽지 못하는 행의 파일은 404, 떼면 지우기, 롤백된 요청은 파일을 지우지 않기, 정리.
- 위 프로젝트에 적용: 손코드 중 `StoredFile`·`FileController`·`FileRules`·`FileStoragePort`·`UploadLimitErrors` 와
  `FileService` 의 대부분이 생성으로 바뀌고 (`LocalFileStorage` 는 생성된 것으로 바꿔 본다), 남는 것은 "본문은 회원만" 훅, 본문 이미지(2단계까지), 인증 서류의 보관 기간이다.
