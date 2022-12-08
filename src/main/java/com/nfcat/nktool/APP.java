package com.nfcat.nktool;


import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class APP {

    static {
//        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
    }

    public static APP app = new APP();

    public static void main(String[] args) {
//        while (true) {
        if (app == null) app = new APP();
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
//        options.addArguments("headless");

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

//        driver.get("https://live.douyin.com/76955319392");
        driver.get("https://live.douyin.com/265366259141");
        waitS20.until(ExpectedConditions.presenceOfElementLocated(By.className("webcast-chatroom___bottom-message")));//等待聊天框加载
        driver.executeScript("""
                window.global_item = {}
                window.g_put = function (item,value){
                    if (typeof window.global_item[item] === 'object'){
                        if (window.global_item[item].length > 200){
                            window.global_item[item].shift();
                        }
                        window.global_item[item].push(value);
                    }else{
                        window.global_item[item] = [value];
                    }
                }
                window.g_get = function (item){
                    if (window.global_item[item].length === 0){
                        return;
                    }
                    const re = window.global_item[item][0];
                    window.global_item[item].shift();
                    return re;
                }
                window.g_del = function (item){
                    window.global_item[item] = []
                }
                                
                var chat_div = document.querySelector(".webcast-chatroom___items div");
                chat_div.addEventListener("DOMNodeInserted",function(){
                    window.g_put("chat",chat_div.lastChild.innerText);
                },false);
                                
                var join_div = document.getElementsByClassName("webcast-chatroom___bottom-message")[0];
                join_div.addEventListener("DOMNodeInserted",function(){
                    window.g_put("join",join_div.innerText);
                },false);
                """);
        waitS20.until(ExpectedConditions.presenceOfElementLocated(By.className("xgplayer-danmu")));//等待视频加载

        driver.executeScript("""
                var dm_div = document.querySelector(".xgplayer-danmu");
                dm_div.addEventListener("DOMNodeInserted",function(){
                    window.g_put("dm",dm_div.lastChild.innerText);
                },false);
                                
                var gift_div1 = document.querySelector(".living_player > div > div").childNodes[0];
                var gift_div2 = document.querySelector(".living_player > div > div").childNodes[1];
                gift_div1.addEventListener("DOMSubtreeModified",function(evt){
                    if(gift_div1.innerText.indexOf("送") === -1) return;
//                    let text = gift_div1.childNodes[0].childNodes[0].childNodes[1].innerText;
//                    if (!gift_div1.innerText.endsWith("1"))
                     window.g_put("gift",gift_div1.innerText);
                },false);
                gift_div2.addEventListener("DOMSubtreeModified",function(evt){
                    if(gift_div2.innerText.indexOf("送") === -1) return;
//                    let text = gift_div2.childNodes[0].childNodes[0].childNodes[1].innerText;
//                    if (!gift_div2.innerText.endsWith("1"))
                     window.g_put("gift",gift_div2.innerText);
                },false);
                """);
        while (true) {
            try {
                Object join = driver.executeScript("return window.g_get(\"join\")");
                Object chat = driver.executeScript("return window.g_get(\"chat\")");
                Object dm = driver.executeScript("return window.g_get(\"dm\")");
                Object gift = driver.executeScript("return window.g_get(\"gift\")");
                if (join != null && !join.toString().equals("")) {
//                    log.info("进房间:" + join.toString().replace("\n", "  "));
                }
                if (chat != null && !chat.toString().equals("")) {
//                    log.info("聊天:" + chat.toString().replace("\n", "  "));
                }
                if (dm != null && !dm.toString().equals("")) {
//                    log.info("弹幕:" + dm.toString().replace("\n", "  "));
                }
                if (gift != null && !gift.toString().equals("")) {
                    log.info("礼物:" + gift.toString().replace("\n", "  "));
                }
            } catch (Exception ignored) {
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (Exception ignored) {
            }
        }
    }

    public void destroy() {
        if (driver != null) driver.quit();
        app = null;
    }
}
