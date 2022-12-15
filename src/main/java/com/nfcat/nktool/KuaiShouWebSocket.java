package com.nfcat.nktool;

import com.alibaba.fastjson.JSONObject;
import com.nfcat.nktool.proto.KSPackWSS;
import com.nfcat.nktool.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v107.network.Network;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class KuaiShouWebSocket {

    static {
//        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
    }

    public static KuaiShouWebSocket app = new KuaiShouWebSocket();

    public static void main(String[] args) {
//        while (true) {
        if (app == null) app = new KuaiShouWebSocket();
        try {
            app.loop();
        } catch (Exception e) {
            app.destroy();
            log.error("loop error:", e);
        }
//        }
    }

    ChromeDriver driver;

    public void loop() {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(false);

        List<Object> list = new ArrayList<>();
        list.add("enable-automation");
        options.setExperimentalOption("excludeSwitches", list);

        options.addArguments("--disable-blink-features");
        options.addArguments("--disable-blink-features=AutomationControlled");
//        options.addArguments("--start-maximized");

//        options.addArguments("window-size=1920x1080");
//        options.addArguments("kiosk");
//        options.addArguments("disable-gpu");

        //Chrome设置代理
        // if (zsyzbConfig.proxyHost != null && !zsyzbConfig.proxyHost.equals("") && zsyzbConfig.proxyPort != null) {
        //    options.addArguments("--proxy-server=http://" + proxyIP);
        //}

        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebDriverWait waitS20 = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get("https://live.kuaishou.com/u/limimi666");
        DevTools devTools = driver.getDevTools();
        devTools.createSession();
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

        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (Exception ignored) {

            }
        }
    }

    public void destroy() {
        if (driver != null) driver.quit();
        app = null;
    }

}
