package kr.crownrpg.packethandler.channel;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import kr.crownrpg.packethandler.CrownPacketHandler;
import kr.crownrpg.packethandler.event.*;
import kr.crownrpg.packethandler.packet.Envelope;
import kr.crownrpg.packethandler.packet.PacketType;
import kr.crownrpg.packethandler.util.JsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;

/**
 * Plugin Message 기반 패킷 수신 리스너.
 *
 * 이 클래스의 책임:
 * - Raw byte[] → UTF-8 JSON 문자열 변환
 * - JSON 파싱 및 기본 검증
 * - PacketType에 따라 Bukkit Event 발행
 *
 * ❗ 주의:
 * - 이 클래스는 절대 게임 로직을 포함하지 않는다.
 * - 모든 예외는 서버 안정성을 위해 무시된다.
 */
public final class CrownPluginMessageListener implements PluginMessageListener {

    public CrownPluginMessageListener() {
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        // 계약된 채널 외 패킷 무시
        if (!channel.equals(CrownPacketHandler.CHANNEL)) return;
        if (player == null || message == null) return;

        try {
            // 악성 패킷 방지 (너무 큰 패킷 무시)
            if (message.length > 8192) return;

            // byte[] → UTF-8 JSON 문자열
            String json = new String(message, StandardCharsets.UTF_8);

            JsonObject root = JsonUtils.parse(json);
            Envelope envelope = Envelope.from(root);

            // 패킷 타입에 따라 입력 이벤트 발행
            PacketType type = envelope.type();
            JsonObject payload = envelope.payload();

            switch (type) {

                case HOTKEY -> {
                    String action = extractString(payload, "action");
                    Boolean pressed = extractBoolean(payload, "pressed");
                    if (action == null || pressed == null) return;
                    Bukkit.getPluginManager().callEvent(
                            new CrownPlayerHotkeyEvent(
                                    player,
                                    action,
                                    pressed,
                                    extractNullableString(payload, "context")
                            )
                    );
                }

                case TEXT_INPUT_PREVIEW -> {
                    if (!hasRequestId(envelope.requestId())) return;
                    String context = extractString(payload, "context");
                    String text = extractString(payload, "text");
                    if (context == null || text == null) return;
                    Bukkit.getPluginManager().callEvent(
                            new CrownPlayerTextInputPreviewEvent(
                                    player,
                                    envelope.requestId(),
                                    context,
                                    text
                            )
                    );
                }

                case TEXT_INPUT -> {
                    if (!hasRequestId(envelope.requestId())) return;
                    String context = extractString(payload, "context");
                    String text = extractString(payload, "text");
                    Boolean confirmed = extractBoolean(payload, "confirmed");
                    if (context == null || text == null || confirmed == null) return;
                    Bukkit.getPluginManager().callEvent(
                            new CrownPlayerTextInputEvent(
                                    player,
                                    envelope.requestId(),
                                    context,
                                    text,
                                    confirmed
                            )
                    );
                }

                case UI_ACTION -> {
                    if (!hasRequestId(envelope.requestId())) return;
                    String ui = extractString(payload, "ui");
                    String action = extractString(payload, "action");
                    if (ui == null || action == null) return;
                    Bukkit.getPluginManager().callEvent(
                            new CrownPlayerUiActionEvent(
                                    player,
                                    envelope.requestId(),
                                    ui,
                                    action
                            )
                    );
                }

                // 서버→클라이언트 전송 전용 패킷 타입은 수신 시 무시한다.
                default -> {
                }
            }

        } catch (Exception ignored) {
            // ❗ 어떤 경우에도 서버 크래시를 유발하지 않는다.
        }
    }

    private static boolean hasString(JsonObject payload, String key) {
        if (payload == null || !payload.has(key)) return false;
        JsonPrimitive primitive = payload.getAsJsonPrimitive(key);
        return primitive != null && primitive.isString();
    }

    private static boolean hasBoolean(JsonObject payload, String key) {
        if (payload == null || !payload.has(key)) return false;
        JsonPrimitive primitive = payload.getAsJsonPrimitive(key);
        return primitive != null && primitive.isBoolean();
    }

    private static String extractString(JsonObject payload, String key) {
        if (!hasString(payload, key)) {
            return null;
        }
        return payload.get(key).getAsString();
    }

    private static Boolean extractBoolean(JsonObject payload, String key) {
        if (!hasBoolean(payload, key)) {
            return null;
        }
        return payload.get(key).getAsBoolean();
    }

    private static String extractNullableString(JsonObject payload, String key) {
        return hasString(payload, key) ? payload.get(key).getAsString() : null;
    }

    private static boolean hasRequestId(String requestId) {
        return requestId != null && !requestId.isEmpty();
    }
}
