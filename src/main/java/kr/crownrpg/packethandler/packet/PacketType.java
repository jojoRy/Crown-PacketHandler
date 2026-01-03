package kr.crownrpg.packethandler.packet;

public enum PacketType {
    HOTKEY,
    TEXT_INPUT_PREVIEW,
    TEXT_INPUT,
    UI_ACTION,
    OPEN_CONFIRM_UI,
    OPEN_TEXT_INPUT,
    UI_VALIDATE_RESULT,
    CLOSE_UI;

    public boolean requiresRequestId() {
        return switch (this) {
            case HOTKEY -> false;
            default -> true;
        };
    }
}
