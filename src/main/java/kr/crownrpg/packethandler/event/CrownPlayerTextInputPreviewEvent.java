package kr.crownrpg.packethandler.event;

import kr.crownrpg.lib.event.input.CrownPlayerInputEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * 클라이언트 UI에서 발생한 실시간 텍스트 입력(preview) 이벤트.
 *
 * 서버가 열어둔 입력 UI(requestId) 맥락에서 플레이어가 입력 중인
 * 텍스트를 실시간으로 전달한다. 유효성 검증, 버튼 활성화 여부 등은
 * Feature Plugin이 판단한다.
 */
public final class CrownPlayerTextInputPreviewEvent extends CrownPlayerInputEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String requestId;
    private final String context;
    private final String text;

    public CrownPlayerTextInputPreviewEvent(
            Player player,
            String requestId,
            String context,
            String text
    ) {
        super(player);
        this.requestId = requestId;
        this.context = context;
        this.text = text;
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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
