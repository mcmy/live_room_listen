package com.nfcat.nktool;

import com.google.protobuf.InvalidProtocolBufferException;
import com.nfcat.nktool.proto.DouYinWSS;
import com.nfcat.nktool.proto.DouYinPackWSS;
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
public class DouYinWebSocket {

    static {
        //https://registry.npmmirror.com/binary.html?path=chromedriver/
//        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
    }

    public static DouYinWebSocket app = new DouYinWebSocket();

    public static void main(String[] args) {
//        while (true) {
        if (app == null) app = new DouYinWebSocket();
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

        DevTools devTools = driver.getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        devTools.addListener(Network.webSocketFrameReceived(), received -> {
            String payloadData = received.getResponse().getPayloadData();
            final byte[] data = Base64.getDecoder().decode(payloadData);
            try {
                //proto解析外层
                DouYinPackWSS.WssResponse wss = DouYinPackWSS.WssResponse.parseFrom(data);
                //GZIP解压data数据
                final byte[] uncompress = Utils.uncompress(wss.getData());
                //解析data
                DouYinWSS.Response response = DouYinWSS.Response.parseFrom(uncompress);
                final List<DouYinWSS.Message> messagesList = response.getMessagesList();
                //处理具体数据
                decodeMessage(messagesList);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(payloadData);
            }

        });

        driver.get("https://live.douyin.com/120352258508");

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


    private void decodeMessage(List<DouYinWSS.Message> messagesList) {
        if (messagesList == null) return;
        messagesList.forEach(item -> {
            switch (item.getMethod()) {
                case "WebcastMemberMessage" -> {
                    try {
                        DouYinWSS.MemberMessage message = DouYinWSS.MemberMessage.parseFrom(item.getPayload());
                        log.info("[有人加入] " + message.getUser().getNickname() + "(" + message.getUser().getId() + ")");
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }

                }
                case "WebcastSocialMessage" -> {
                    try {
                        DouYinWSS.SocialMessage message = DouYinWSS.SocialMessage.parseFrom(item.getPayload());
                        log.info("[关注消息] " + message.getUser().getNickname() + "(" + message.getUser().getId() + ")");
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }

                }
                case "WebcastChatMessage" -> {
                    try {
                        DouYinWSS.ChatMessage message = DouYinWSS.ChatMessage.parseFrom(item.getPayload());
                        log.info("[评论消息] " + message.getUser().getNickname() + "(" + message.getUser().getId() + ")" + "说：" + message.getContent());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
                case "WebcastLikeMessage" -> {
                    try {
                        DouYinWSS.LikeMessage message = DouYinWSS.LikeMessage.parseFrom(item.getPayload());
//                        log.info("[点赞消息] " + message.getUser().getNickname() + "(" + message.getUser().getId() + ")" + " 点赞数：" + message.getCount());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
                case "WebcastGiftMessage" -> {
                    try {
                        DouYinWSS.GiftMessage message = DouYinWSS.GiftMessage.parseFrom(item.getPayload());
                        if (message.getGift().getId() == 0) return;
                        log.info("[礼物消息] 用户：{}({}) 送出：{}(id:{})*{}",
                                message.getUser().getNickname(),
                                message.getUser().getId(),
                                message.getGift().getName(),
                                message.getGift().getId(),
                                //礼物数量修正，不确定
                                message.getComboCount() - message.getRepeatCount());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
                case "WebcastRoomUserSeqMessage"->{
                    try {
                        DouYinWSS.RoomUserSeqMessage message = DouYinWSS.RoomUserSeqMessage.parseFrom(item.getPayload());
                        log.info("[人数更新] 总观看人数：" + message.getTotalUser() + " 正在观看人数：" + message.getTotal());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
