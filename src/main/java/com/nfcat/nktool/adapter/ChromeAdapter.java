package com.nfcat.nktool.adapter;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChromeAdapter {
    private ChromeDriver driver;

    public WebDriverWait getWebDriverWait(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    public DevTools getInitDevTools(){
        DevTools devTools = driver.getDevTools();
        devTools.createSession();
        return devTools;
    }

    public static @NotNull ChromeOptions getSimpleOptions(boolean showChromeWindow) {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(!showChromeWindow);

        List<Object> list = new ArrayList<>();
        list.add("enable-automation");
        options.setExperimentalOption("excludeSwitches", list);

        options.addArguments("--disable-blink-features");
        options.addArguments("--disable-blink-features=AutomationControlled");

//        options.addArguments("--start-maximized");

//        options.addArguments("window-size=1920x1080");
//        options.addArguments("kiosk");
//        options.addArguments("disable-gpu");

//        Chrome设置代理
//        options.addArguments("--proxy-server=http://" + proxyIP);

        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        return options;
    }

    public ChromeAdapter(ChromeOptions options) {
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
    }
}
