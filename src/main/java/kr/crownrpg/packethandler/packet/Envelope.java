package kr.crownrpg.packethandler.packet;

import com.google.gson.JsonObject;

/**
 * 클라이언트에서 전송되는 모든 패킷의 공통 Envelope.
 *
 * 이 클래스는 JSON 구조를 객체로 변환하는 역할만 수행하며,
 * payload의 의미 해석은 하지 않는다.
 */
public record Envelope(
        PacketType type,
        String requestId,
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
        String requestId = root.has("requestId")
                ? root.get("requestId").getAsString()
                : "";

        return new Envelope(type, requestId, root.getAsJsonObject("payload"));
    }
}
