package com.example.wanglei.testapplication;

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

public class CurrentLocationTest {

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
    public void UserPosition() throws Exception{
        WebElement button=driver.findElement(By.id("fab"));
        button.click();
        // check the longtitude and latitude

    }
    @Test
    public void FloorUpdate() throws Exception{
        WebElement button=driver.findElement(By.id("fab"));
        button.click();
        // check the floor plan updated correctly
        // check if the user is in the correct floor
    }

    @Test
    public void ErrorRadius() throws Exception{
        WebElement button=driver.findElement(By.id("fab"));
        button.click();
        // check if the radius of the error is within 3 meters

    }

    @After
    public void tearDown(){

        driver.quit();
    }

}
