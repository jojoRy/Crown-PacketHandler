package kr.crownrpg.packethandler.event;

import kr.crownrpg.lib.event.input.CrownPlayerInputEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * 클라이언트 UI에서 발생한 "선택 / 버튼 클릭" 이벤트.
 *
 * 사용 예:
 * - 파티 초대 수락 / 거절
 * - 던전 입장 확인 / 취소
 * - 우편 보상 수령 버튼
 *
 * 이 이벤트의 의미:
 * - 특정 UI(ui)에서
 * - 특정 액션(action)이 선택되었음을 알린다.
 *
 * ❗ 주의:
 * - 이 이벤트는 "선택 의사"만 전달한다.
 * - 실제 처리 결과(성공/실패)는 Feature Plugin 책임이다.
 *
 * ❌ 이 이벤트에서 하면 안 되는 것:
 * - UI 상태 관리
 * - 클라이언트 화면 제어
 * - 공용 Infra 접근
 */
public final class CrownPlayerUiActionEvent extends CrownPlayerInputEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 서버 → 클라이언트 UI 요청과 매칭되는 식별자.
     * (예: 특정 파티 초대, 특정 던전 요청)
     */
    private final String requestId;

    /**
     * UI 식별자.
     * 예: party_invite, dungeon_entry, mail_box
     */
    private final String ui;

    /**
     * 플레이어가 선택한 액션.
     * 예: accept, decline, confirm, cancel
     */
    private final String action;

    public CrownPlayerUiActionEvent(
            Player player,
            String requestId,
            String ui,
            String action
    ) {
        super(player);
        this.requestId = requestId;
        this.ui = ui;
        this.action = action;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUi() {
        return ui;
    }

    public String getAction() {
        return action;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
