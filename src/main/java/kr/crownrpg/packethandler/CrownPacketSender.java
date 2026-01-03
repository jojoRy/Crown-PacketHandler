package kr.crownrpg.packethandler;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import kr.crownrpg.packethandler.packet.PacketType;
import kr.crownrpg.packethandler.util.JsonUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;

/**
 * Crown-PacketHandler가 제공하는 서버→클라이언트 송신 API.
 *
 * Feature Plugin은 JSON/직렬화/채널명에 대해 알 필요 없이,
 * 이 클래스의 공개 메서드를 호출하기만 하면 된다.
 */
public final class CrownPacketSender {

    private final Plugin plugin;

    CrownPacketSender(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 텍스트 입력 UI를 요청한다.
     */
    public void openTextInput(
            Player player,
            String requestId,
            String context,
            String title,
            String placeholder,
            int maxLength,
            int timeoutMillis
    ) {
        if (player == null || requestId == null || context == null || title == null) {
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("context", context);
        payload.addProperty("title", title);
        addNullableString(payload, "placeholder", placeholder);
        payload.addProperty("maxLength", maxLength);
        addPositiveTimeout(payload, timeoutMillis);

        send(player, PacketType.OPEN_TEXT_INPUT, requestId, payload);
    }

    /**
     * 확인/취소 UI를 연다.
     */
    public void openConfirmUi(
            Player player,
            String requestId,
            String ui,
            String title,
            String message,
            String acceptAction,
            String cancelAction,
            int timeoutMillis
    ) {
        if (player == null
                || requestId == null
                || ui == null
                || title == null
                || message == null
                || acceptAction == null
                || cancelAction == null) {
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("ui", ui);
        payload.addProperty("title", title);
        payload.addProperty("message", message);
        payload.addProperty("acceptAction", acceptAction);
        payload.addProperty("cancelAction", cancelAction);
        addPositiveTimeout(payload, timeoutMillis);

        send(player, PacketType.OPEN_CONFIRM_UI, requestId, payload);
    }

    /**
     * 입력 검증 결과를 실시간으로 전달한다.
     */
    public void sendValidateResult(Player player, String requestId, boolean valid, String message) {
        if (player == null || requestId == null) {
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("valid", valid);
        payload.add("message", message == null ? JsonNull.INSTANCE : new JsonPrimitive(message));

        send(player, PacketType.UI_VALIDATE_RESULT, requestId, payload);
    }

    /**
     * 특정 UI 세션을 강제로 닫는다.
     */
    public void closeUi(Player player, String requestId) {
        if (player == null || requestId == null) {
            return;
        }

        send(player, PacketType.CLOSE_UI, requestId, new JsonObject());
    }

    private void send(Player player, PacketType type, String requestId, JsonObject payload) {
        if (player == null || type == null || payload == null) {
            return;
        }

        try {
            if (type.requiresRequestId() && requestId == null) {
                return;
            }

            JsonObject envelope = new JsonObject();
            envelope.addProperty("type", type.name());
            envelope.add("requestId", requestId == null ? JsonNull.INSTANCE : new JsonPrimitive(requestId));
            envelope.addProperty("clientTime", System.currentTimeMillis());
            envelope.add("payload", payload);

            byte[] bytes = JsonUtils.gson().toJson(envelope).getBytes(StandardCharsets.UTF_8);
            if (bytes.length > 8192) {
                // payload 최대 크기 초과 시 전송하지 않는다.
                return;
            }

            player.sendPluginMessage(plugin, CrownPacketHandler.CHANNEL, bytes);
        } catch (Exception ignored) {
            // 어떤 이유로든 직렬화/전송 실패 시 서버 크래시를 방지한다.
        }
    }

    private static void addNullableString(JsonObject payload, String key, String value) {
        payload.add(key, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    private static void addPositiveTimeout(JsonObject payload, int timeoutMillis) {
        if (timeoutMillis > 0) {
            payload.addProperty("timeout", timeoutMillis);
        }
    }
}
