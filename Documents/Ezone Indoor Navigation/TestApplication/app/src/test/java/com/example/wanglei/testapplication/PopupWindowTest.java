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

public class PopupWindowTest {

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
    public void ShowPopupWindow() throws Exception{
        //once the user click on the marker, a pop-up window should show up


    }
    @Test
    public void PopupWindowButton1() throws Exception{
        // check if there's a button to enable the user to view more room information
        // check if there's a button to enable the user to click to navigate to this destination
    }

    @Test
    public void PopupWindowButton2() throws Exception{
        // check if there's a button to enable the user to click to navigate to this destination
    }

    @After
    public void tearDown(){

        driver.quit();
    }

}