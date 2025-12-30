package kr.crownrpg.packethandler.event;

import kr.crownrpg.lib.event.input.CrownPlayerInputEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * 클라이언트 UI에서 발생한 "텍스트 입력" 이벤트.
 *
 * 사용 예:
 * - 닉네임 변경
 * - 길드명 / 길드 설명 입력
 *
 * 이 이벤트의 의미:
 * - 플레이어가 어떤 UI 맥락(context)에서
 * - 텍스트를 입력했고
 * - 확인(confirmed) 또는 취소했는지를 전달한다.
 *
 * ❗ 주의:
 * - 이 이벤트는 입력 "결과"만 전달한다.
 * - 입력값의 유효성 검사, 성공/실패 판정,
 *   UI 재요청 여부는 Feature Plugin의 책임이다.
 *
 * ❌ 이 이벤트에서 하면 안 되는 것:
 * - DB 저장
 * - 닉네임 변경 로직 직접 처리
 * - 클라이언트 UI 제어
 */
public final class CrownPlayerTextInputEvent extends CrownPlayerInputEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 서버 → 클라이언트 UI 요청과 매칭하기 위한 식별자.
     * 동일 requestId를 통해 응답을 추적할 수 있다.
     */
    private final String requestId;

    /**
     * 입력이 발생한 맥락.
     * 예: nickname_change, guild_name, mail_title
     */
    private final String context;

    /**
     * 플레이어가 입력한 텍스트 원문.
     * (정제/검증은 Feature Plugin 책임)
     */
    private final String text;

    /**
     * true  = 확인 버튼 클릭
     * false = 취소 버튼 클릭
     */
    private final boolean confirmed;

    public CrownPlayerTextInputEvent(
            Player player,
            String requestId,
            String context,
            String text,
            boolean confirmed
    ) {
        super(player);
        this.requestId = requestId;
        this.context = context;
        this.text = text;
        this.confirmed = confirmed;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getContext() {
        return context;
    }

    public String getText() {
        return text;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
