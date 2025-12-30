package kr.crownrpg.packethandler.channel;

import com.google.gson.JsonObject;
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

            switch (type) {

                case HOTKEY -> Bukkit.getPluginManager().callEvent(
                        new CrownPlayerHotkeyEvent(
                                player,
                                envelope.payload().get("action").getAsString(),
                                envelope.payload().get("pressed").getAsBoolean(),
                                envelope.payload().has("context")
                                        ? envelope.payload().get("context").getAsString()
                                        : "unknown"
                        )
                );

                case TEXT_INPUT -> Bukkit.getPluginManager().callEvent(
                        new CrownPlayerTextInputEvent(
                                player,
                                envelope.requestId(),
                                envelope.payload().get("context").getAsString(),
                                envelope.payload().get("text").getAsString(),
                                envelope.payload().get("confirmed").getAsBoolean()
                        )
                );

                case UI_ACTION -> Bukkit.getPluginManager().callEvent(
                        new CrownPlayerUiActionEvent(
                                player,
                                envelope.requestId(),
                                envelope.payload().get("ui").getAsString(),
                                envelope.payload().get("action").getAsString()
                        )
                );
            }

        } catch (Exception ignored) {
            // ❗ 어떤 경우에도 서버 크래시를 유발하지 않는다.
        }
    }
}
