package com.nfcat.nktool;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.nfcat.nktool.adapter.ChromeAdapter;
import com.nfcat.nktool.proto.DouYinPackWSS;
import com.nfcat.nktool.proto.DouYinWSS;
import com.nfcat.nktool.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v107.network.Network;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DouYinWebSocket {

    public static void main(String[] args) {
//        https://registry.npmmirror.com/binary.html?path=chromedriver/
//        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        new DouYinWebSocket().start();
    }


    public void start() {
        ChromeAdapter chromeAdapter = new ChromeAdapter(ChromeAdapter.getSimpleOptions(true));
        ChromeDriver driver = chromeAdapter.getDriver();

        DevTools devTools = chromeAdapter.getInitDevTools();
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
                case "WebcastRoomUserSeqMessage" -> {
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
