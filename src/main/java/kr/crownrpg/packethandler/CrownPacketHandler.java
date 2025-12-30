package kr.crownrpg.packethandler;

import kr.crownrpg.packethandler.channel.CrownPluginMessageListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Crown-PacketHandler 메인 플러그인 클래스.
 *
 * 역할:
 * - 클라이언트(Fabric Mod)에서 전송된 CustomPayload 패킷을 수신한다.
 * - JSON 파싱 및 최소 검증을 수행한다.
 * - 입력 패킷을 Bukkit Event로 변환하여 서버에 전달한다.
 *
 * ❌ 하지 않는 것:
 * - 게임 로직 처리
 * - DB / Redis / Netty 접근
 * - 입력의 의미 해석
 *
 * 이 플러그인은 "입력 어댑터"이며,
 * 실제 동작은 Feature Plugin의 책임이다.
 */
public final class CrownPacketHandler extends JavaPlugin {

    /**
     * 클라이언트 ↔ 서버 간 통신에 사용하는 고정 채널.
     * CrownClient와 계약된 값이므로 절대 변경 금지.
     */
    public static final String CHANNEL = "crown:packet";

    @Override
    public void onEnable() {
        // 클라이언트 → 서버 패킷 수신 등록
        Bukkit.getMessenger().registerIncomingPluginChannel(
                this,
                CHANNEL,
                new CrownPluginMessageListener(this)
        );

        // 서버 → 클라이언트 패킷 송신 채널 등록
        Bukkit.getMessenger().registerOutgoingPluginChannel(
                this,
                CHANNEL
        );

        getLogger().info("Crown-PacketHandler enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Crown-PacketHandler disabled");
    }
}
