package kr.crownrpg.packethandler.packet;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * 클라이언트에서 전송되는 모든 패킷의 공통 Envelope.
 *
 * 이 클래스는 JSON 구조를 객체로 변환하는 역할만 수행하며,
 * payload의 의미 해석은 하지 않는다.
 */
public record Envelope(
        PacketType type,
        String requestId,
        Long clientTime,
        JsonObject payload
) {

    /**
     * JSON 객체로부터 Envelope를 생성한다.
     *
     * @throws IllegalArgumentException 필수 필드 누락 시
     */
    public static Envelope from(JsonObject root) {

        if (!root.has("type") || !root.has("payload")) {
            throw new IllegalArgumentException("Invalid packet envelope");
        }

        PacketType type = PacketType.valueOf(root.get("type").getAsString());
        String requestId = extractNullableString(root, "requestId");
        Long clientTime = root.has("clientTime") && !root.get("clientTime").isJsonNull()
                ? root.get("clientTime").getAsLong()
                : null;

        return new Envelope(type, requestId, clientTime, root.getAsJsonObject("payload"));
    }

    private static String extractNullableString(JsonObject root, String key) {
        if (!root.has(key)) return null;

        JsonPrimitive primitive = root.getAsJsonPrimitive(key);
        if (primitive == null || primitive.isJsonNull()) return null;

        return primitive.getAsString();
    }
}
