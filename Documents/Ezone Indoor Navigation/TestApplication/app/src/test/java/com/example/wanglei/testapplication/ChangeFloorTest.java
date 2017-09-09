package com.example.wanglei.testapplication;

/**
 * Created by wanglei on 9/09/2017.
 */

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
/**
 * Created by wanglei on 9/09/2017.
 */

public class ChangeFloorTest {

    WebDriver driver;

    @Before
    public void setUp() throws MalformedURLException{
        DesiredCapabilities capabilities=new DesiredCapabilities();
        capabilities.setCapability("BROWSER_NAME", "Android");
        capabilities.setCapability("VERSION", "6.0");
        capabilities.setCapability("deviceName", "Emulator");
        capabilities.setCapability("platformName", "Android");

        capabilities.setCapability("appPackage", "com.example.kaelansinclair.ezone_navigation_android");
        capabilities.setCapability("appActivity", "com.example.kaelansinclair.ezone_navigation_android.MainActivity");

        driver=new RemoteWebDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
    }

    @Test
    public void FloorChangeButton() throws Exception{
        // check the floor number button works properly
        // the floor plan should update accordingly

    }

    @After
    public void tearDown(){

        driver.quit();
    }

}
