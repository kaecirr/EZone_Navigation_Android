package com.example.wanglei.testapplication;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
/**
 * Created by wanglei on 9/09/2017.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CurrentLocationTest.class,
        ChangeFloorTest.class,
        SelectDestinationTest.class,
        PopupWindowTest.class,
        PathUpdateTest.class,
        ResponseTimeTest.class
})
public class TestSuite {

}
