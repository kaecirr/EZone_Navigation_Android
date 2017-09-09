package com.example.wanglei.testapplication;

/**
 * Created by wanglei on 9/09/2017.
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by wanglei on 9/09/2017.
 */

public class ResponseTimeTest {

    WebDriver driver;

    @Before
    public void setUp() throws MalformedURLException {
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
    public void GetCurrentLocationResponseTime() throws Exception{
        // check the response time when using current location button


    }
    @Test
    public void GetRoomInformationResponseTime() throws Exception{
        // check the response time when get room information
    }

    @Test
    public void GetVisiblePathResponseTime() throws Exception{
        // check the response time when get a visible path shown on the screen/start tracking
    }



    @After
    public void tearDown(){

        driver.quit();
    }

}
