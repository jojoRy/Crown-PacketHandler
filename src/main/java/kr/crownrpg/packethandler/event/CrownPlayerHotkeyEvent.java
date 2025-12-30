package kr.crownrpg.packethandler.event;

import kr.crownrpg.lib.event.input.CrownPlayerInputEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * 클라이언트에서 발생한 HOTKEY 입력 이벤트.
 *
 * 이 이벤트는 "입력 발생"만을 의미하며,
 * 실제 스킬 실행 / UI 동작 여부는 Feature Plugin의 책임이다.
 */
public final class CrownPlayerHotkeyEvent extends CrownPlayerInputEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String action;
    private final boolean pressed;
    private final String context;

    public CrownPlayerHotkeyEvent(
            Player player,
            String action,
            boolean pressed,
            String context
    ) {
        super(player);
        this.action = action;
        this.pressed = pressed;
        this.context = context;
    }

    public String getAction() {
        return action;
    }

    public boolean isPressed() {
        return pressed;
    }

    public String getContext() {
        return context;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
