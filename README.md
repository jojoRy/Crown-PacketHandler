# 🧩 Crown-PacketHandler

**Crown-PacketHandler**는 CrownClient(Fabric Mod) ↔ Paper Server 간
JSON 기반 통신을 담당하는 **양방향 어댑터 플러그인**입니다.

> ❗ 이 플러그인은 **게임 로직을 처리하지 않습니다**
> ❗ 입력의 의미 해석은 **Feature Plugin의 책임**입니다

---

## 📌 목적 (Why this exists)

Minecraft 클라이언트 모드와 서버 간의 통신을
**안정적이고 확장 가능한 구조**로 분리하기 위함입니다.

```
Client Mod (Fabric)
   ⇄ JSON PluginMessage
Crown-PacketHandler
   ⇄ Java API
Feature Plugins
```

* 클라이언트 UI / 키 입력 변경 시 서버 코드 수정 ❌
* 서버 게임 로직 변경 시 클라이언트 수정 ❌
* 입력 계약(Input Contract)만 고정

---

## 🧠 설계 철학

### ✔ 책임 분리

* PacketHandler는 **입력 수신 + 이벤트 발행 + UI 요청 송신**만 담당
* Feature Plugin이 **의미 해석 + 게임 로직** 담당

### ✔ 서버 안정성 최우선

* JSON 파싱 실패 → 무시
* 알 수 없는 패킷 → 무시
* 예외 발생 → 서버 크래시 ❌

### ✔ Infra 독립

* Redis / DB / Netty 의존 ❌
* Crown-Infra와 완전 분리

---

## 🖥 지원 환경

| 항목        | 값                        |
| --------- |--------------------------|
| 서버        | Paper                    |
| Minecraft | 1.21.8                   |
| Java      | 21                       |
| 클라이언트     | Fabric Mod (CrownClient) |

---

## 📡 통신 채널

* **Plugin Message Channel**

```
crown:packet
```

* 인코딩

    * UTF-8
    * JSON 문자열

---

## 📦 지원 패킷 타입

### 🔹 서버 → 클라이언트

* `OPEN_TEXT_INPUT`
* `OPEN_CONFIRM_UI`
* `UI_VALIDATE_RESULT`
* `CLOSE_UI` (확장 대비)

### 🔹 HOTKEY

클라이언트 커스텀 키 입력

```json
{
  "type": "HOTKEY",
  "payload": {
    "action": "skill.cast.4",
    "pressed": true,
    "context": "gameplay"
  }
}
```

➡ `CrownPlayerHotkeyEvent`

---

### 🔹 TEXT_INPUT_PREVIEW

텍스트 입력 UI에서 입력 중인 내용(확정 전)을 실시간으로 전달

```json
{
  "type": "TEXT_INPUT_PREVIEW",
  "requestId": "uuid",
  "payload": {
    "context": "nickname_change",
    "text": "입력중 텍스트"
  }
}
```

➡ `CrownPlayerTextInputPreviewEvent`

---


### 🔹 TEXT_INPUT

텍스트 입력 UI 결과

```json
{
  "type": "TEXT_INPUT",
  "requestId": "uuid",
  "payload": {
    "context": "nickname_change",
    "text": "새닉네임",
    "confirmed": true
  }
}
```

➡ `CrownPlayerTextInputEvent`

---

### 🔹 UI_ACTION

UI 선택 / 버튼 클릭

```json
{
  "type": "UI_ACTION",
  "requestId": "uuid",
  "payload": {
    "ui": "party_invite",
    "action": "accept"
  }
}
```

➡ `CrownPlayerUiActionEvent`

---

## 🔹 서버 → 클라이언트 UI 패킷

### OPEN_TEXT_INPUT

텍스트 입력 UI를 연다. 타임아웃에 도달하면 클라이언트가 `confirmed=false`인 `TEXT_INPUT`을 보내며 UI를 닫는다.

```json
{
  "type": "OPEN_TEXT_INPUT",
  "requestId": "uuid",
  "payload": {
    "context": "nickname_change",
    "title": "닉네임 변경",
    "placeholder": "닉네임을 입력하세요",
    "maxLength": 16,
    "timeout": 15000
  }
}
```

### OPEN_CONFIRM_UI

두 버튼(확인/취소) UI를 연다. 타임아웃 또는 사용자가 닫았을 때 `cancelAction`으로 `UI_ACTION`이 전송된다.

```json
{
  "type": "OPEN_CONFIRM_UI",
  "requestId": "uuid",
  "payload": {
    "ui": "party_invite",
    "title": "파티 초대",
    "message": "수락하시겠습니까?",
    "acceptAction": "accept",
    "cancelAction": "cancel",
    "timeout": 10000
  }
}
```

### UI_VALIDATE_RESULT

입력 검증 결과를 전달한다. `valid=true`일 때만 확인 버튼이 활성화된다.

```json
{
  "type": "UI_VALIDATE_RESULT",
  "requestId": "uuid",
  "payload": {
    "valid": true,
    "message": "사용 가능한 닉네임입니다"
  }
}
```

---

## 📢 발생하는 Bukkit Events

모든 이벤트는 **Crown-Lib**의 입력 이벤트 베이스를 상속합니다.

```
CrownPlayerInputEvent
 ├─ CrownPlayerHotkeyEvent
 ├─ CrownPlayerTextInputPreviewEvent
 ├─ CrownPlayerTextInputEvent
 └─ CrownPlayerUiActionEvent
```

### 공통 특징

* Player는 항상 non-null
* cancellable ❌
* async ❌ (메인 스레드)

---

## 🚀 서버 → 클라이언트 송신 API

Feature Plugin은 **PacketHandler가 제공하는 API만 호출**하면 된다.
JSON 직렬화, 채널명, PacketType 참조가 필요 없다.

```java
public void openTextInput(
    Player player,
    String requestId,
    String context,
    String title,
    String placeholder,
    int maxLength,
    int timeoutMillis
);

public void openConfirmUi(
    Player player,
    String requestId,
    String ui,
    String title,
    String message,
    String acceptAction,
    String cancelAction,
    int timeoutMillis
);

public void sendValidateResult(
    Player player,
    String requestId,
    boolean valid,
    String message
);

public void closeUi(Player player, String requestId);
```

> UI 요청은 반드시 PacketHandler를 통해 전송한다. Feature Plugin은 PluginMessage를 직접 다루지 않는다.

모든 송신 패킷은 다음 Envelope 규칙을 따른다.

```json
{
  "type": "PACKET_TYPE",
  "requestId": "string | null",
  "clientTime": 123456789,
  "payload": {}
}
```

* UI 흐름이 있는 패킷(`OPEN_TEXT_INPUT`, `OPEN_CONFIRM_UI`, `UI_VALIDATE_RESULT`, `CLOSE_UI`)은 항상 `requestId`를 사용한다.
* `timeout`과 `placeholder` 같은 UI 필드는 선택적이다. `timeout`은 밀리초 단위이며 생략 시 클라이언트 기본값을 사용한다.

---

## 🧪 Feature Plugin 사용 예제

```java
@EventHandler
public void onHotkey(CrownPlayerHotkeyEvent event) {
    if (!event.isPressed()) return;

    if (event.getAction().equals("skill.cast.4")) {
        event.getPlayer().sendMessage("스킬 4 발동!");
    }
}
```

```java
@EventHandler
public void onTextInput(CrownPlayerTextInputEvent event) {
    if (!event.isConfirmed()) return;

    if (event.getContext().equals("nickname_change")) {
        // 닉네임 유효성 검사 및 처리
    }
}
```

---

## ❌ 이 플러그인에서 하지 않는 것

* 게임 로직 처리
* DB 저장
* Redis Pub/Sub
* UI 상태 관리
* 클라이언트 화면 제어

> ⚠️ 위 작업은 **Feature Plugin 또는 Crown-Infra 책임**입니다

---

## 📁 프로젝트 구조

```
kr.crownrpg.packethandler
 ├─ CrownPacketHandler.java
 ├─ channel/
 │   └─ CrownPluginMessageListener.java
 ├─ packet/
 │   ├─ Envelope.java
 │   └─ PacketType.java
 ├─ event/
 │   ├─ CrownPlayerHotkeyEvent.java
 │   ├─ CrownPlayerTextInputPreviewEvent.java
 │   ├─ CrownPlayerTextInputEvent.java
 │   └─ CrownPlayerUiActionEvent.java
 └─ util/
     └─ JsonUtils.java
```

---

## 🔐 안정성 정책

* 패킷 최대 길이 제한 (8KB)
* try/catch 전면 적용
* 서버 크래시 0% 설계
* 잘못된 패킷은 모두 무시

---

## 🧩 의존성

```yaml
depend:
  - CrownLib
```

---

## 📄 라이선스

```
All Rights Reserved
© CrownRPG
```

---

## 🏁 요약

> **Crown-PacketHandler는 게임 플러그인이 아닙니다.**
> **입력 계약을 서버 이벤트로 번역하는 플랫폼입니다.**

이 플러그인을 기반으로:

* 스킬 시스템
* UI 시스템
* 파티 / 길드 / 던전
* 커스텀 HUD

을 **안전하게 확장**할 수 있습니다.

---