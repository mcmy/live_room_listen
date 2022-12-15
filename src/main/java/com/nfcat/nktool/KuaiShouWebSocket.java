package com.nfcat.nktool;

import com.alibaba.fastjson.JSONObject;
import com.nfcat.nktool.adapter.ChromeAdapter;
import com.nfcat.nktool.proto.KSPackWSS;
import com.nfcat.nktool.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v107.network.Network;

import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class KuaiShouWebSocket {

    public static void main(String[] args) {
//        https://registry.npmmirror.com/binary.html?path=chromedriver/
//        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        new KuaiShouWebSocket().start();
    }

    public void start() {
        ChromeAdapter chromeAdapter = new ChromeAdapter(ChromeAdapter.getSimpleOptions(true));
        ChromeDriver driver = chromeAdapter.getDriver();

        DevTools devTools = chromeAdapter.getInitDevTools();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        devTools.addListener(Network.webSocketFrameReceived(), webSocketCreated -> {
            String payloadData = webSocketCreated.getResponse().getPayloadData();
            final byte[] data = Base64.getDecoder().decode(payloadData);
            try {
                KSPackWSS.SocketMessage message = KSPackWSS.SocketMessage.parseFrom(data);
                log.info("message type: {},compression type：{}", message.getPayloadType(), message.getCompressionType());
                byte[] payload = new byte[0];
                switch (message.getCompressionType()) {
                    case GZIP -> {
                        byte[] uncompress = Utils.uncompress(message.getPayload());
                        if (uncompress != null) payload = uncompress;
                    }
                    case AES -> log.info("aes encrypt");
                    case NONE, COMPRESSION_TYPE_UNKNOWN, UNRECOGNIZED -> payload = message.getPayload().toByteArray();
                }
                if (payload.length == 0) return;
                switch (message.getPayloadType()) {
                    case CS_ENTER_ROOM -> {
                        KSPackWSS.CSWebEnterRoom csWebEnterRoom = KSPackWSS.CSWebEnterRoom.parseFrom(payload);
                        System.out.println(JSONObject.toJSONString(csWebEnterRoom));
                    }
                    case SC_FEED_PUSH -> {
                        KSPackWSS.SCWebFeedPush scWebFeedPush = KSPackWSS.SCWebFeedPush.parseFrom(payload);
                        System.out.println(JSONObject.toJSONString(scWebFeedPush));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(payloadData);
            }
        });

        driver.get("https://live.kuaishou.com/u/limimi666");

        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (Exception ignored) {

            }
        }
    }
}
