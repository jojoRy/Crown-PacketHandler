package kr.crownrpg.packethandler.channel;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import kr.crownrpg.packethandler.event.*;
import kr.crownrpg.packethandler.packet.Envelope;
import kr.crownrpg.packethandler.packet.PacketType;
import kr.crownrpg.packethandler.util.JsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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

    private final Plugin plugin;

    public CrownPluginMessageListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        // 계약된 채널 외 패킷 무시
        if (!channel.equals("crown:packet")) return;

        try {
            // byte[] → UTF-8 JSON 문자열
            String json = new String(message, StandardCharsets.UTF_8);

            // 악성 패킷 방지 (너무 큰 패킷 무시)
            if (json.length() > 8192) return;

            JsonObject root = JsonUtils.parse(json);
            Envelope envelope = Envelope.from(root);

            // 패킷 타입에 따라 입력 이벤트 발행
            PacketType type = envelope.type();
            JsonObject payload = envelope.payload();

            switch (type) {

                case HOTKEY -> Bukkit.getPluginManager().callEvent(
                        new CrownPlayerHotkeyEvent(
                                player,
                                extractString(payload, "action"),
                                extractBoolean(payload, "pressed"),
                                extractNullableString(payload, "context")
                        )
                );

                case TEXT_INPUT_PREVIEW -> Bukkit.getPluginManager().callEvent(
                        new CrownPlayerTextInputPreviewEvent(
                                player,
                                requireRequestId(envelope.requestId()),
                                extractString(payload, "context"),
                                extractString(payload, "text")
                        )
                );

                case TEXT_INPUT -> Bukkit.getPluginManager().callEvent(
                        new CrownPlayerTextInputEvent(
                                player,
                                requireRequestId(envelope.requestId()),
                                extractString(payload, "context"),
                                extractString(payload, "text"),
                                extractBoolean(payload, "confirmed")
                        )
                );

                case UI_ACTION -> Bukkit.getPluginManager().callEvent(
                        new CrownPlayerUiActionEvent(
                                player,
                                requireRequestId(envelope.requestId()),
                                extractString(payload, "ui"),
                                extractString(payload, "action")
                        )
                );

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
            throw new IllegalArgumentException("Missing required string field: " + key);
        }
        return payload.get(key).getAsString();
    }

    private static boolean extractBoolean(JsonObject payload, String key) {
        if (!hasBoolean(payload, key)) {
            throw new IllegalArgumentException("Missing required boolean field: " + key);
        }
        return payload.get(key).getAsBoolean();
    }

    private static String extractNullableString(JsonObject payload, String key) {
        return hasString(payload, key) ? payload.get(key).getAsString() : null;
    }

    private static String requireRequestId(String requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId is required for this packet type");
        }
        return requestId;
    }
}
