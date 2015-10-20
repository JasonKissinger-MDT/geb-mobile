package geb.mobile

import geb.Browser
import geb.navigator.Navigator
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.WebElement


public class TestMobileNonEmptyNavigator extends AbstractMobileNonEmptyNavigator<AndroidDriver> {
    TestMobileNonEmptyNavigator(Browser browser, Collection<? extends MobileElement> contextElements) {
        super(browser, contextElements)
    }

    @Override
    Navigator find(String selector) {
        return null
    }

    @Override
    Navigator unique() {
        return null
    }

    @Override
    void setInputValue(WebElement input, Object value) {

    }
}