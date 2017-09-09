package com.example.wanglei.testapplication;

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

public class PathUpdateTest {

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
    public void VisiblePathAvailable() throws Exception{
        // check if there is a visible path available on the screen


    }
    @Test
    public void ShortestPathTest() throws Exception{
        // check if the path is an optimal or a shortest path
    }

    @Test
    public void PathMoveUpdate() throws Exception{
        // check if the path updated accordingly based on the user movement
    }

    @After
    public void tearDown(){

        driver.quit();
    }

}
