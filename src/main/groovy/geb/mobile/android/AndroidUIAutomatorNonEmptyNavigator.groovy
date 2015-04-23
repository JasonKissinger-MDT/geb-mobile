package geb.mobile.android

import geb.Browser
import geb.mobile.AbstractMobileNonEmptyNavigator
import geb.navigator.Navigator
import groovy.util.logging.Slf4j
import io.appium.java_client.MobileBy
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

/**
 * Created by gmueksch on 23.06.14.
 */
@Slf4j
class AndroidUIAutomatorNonEmptyNavigator extends AbstractMobileNonEmptyNavigator<AndroidDriver> {

    AndroidUIAutomatorNonEmptyNavigator(Browser browser, Collection<? extends MobileElement> contextElements) {
        super(browser,contextElements)
    }

    private String getAppPackage() {
        driver.capabilities.getCapability("appPackage")
    }

    @Override
    Navigator find(String selectorString) {
        By by = getByForSelector(selectorString)

        List<WebElement> list = []

        if (!contextElements || (by instanceof By.ByXPath)) {
            list = driver.findElements(by)
        } else {
            contextElements?.each { WebElement element ->
                List<WebElement> found = element.findElements(by)

                if (!found && by instanceof MobileBy.ByAndroidUIAutomator) {
                    By scrolledBy = MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(${by.automatorText})")
                    log.debug "Not found with selector $by attempting to scroll into view using $scrolledBy"

                    found = element.findElements(scrolledBy)
                }

                list.addAll(found)
            }
        }

        log.debug "Found $list.size() elements"

        navigatorFor(list)
    }

    private By getByForSelector(String selectorString) {
        By by
        if (selectorString.startsWith("//")) {
            by = By.xpath(selectorString)
        } else if (selectorString.startsWith("#")) {
            String value = selectorString.substring(1)
            String resource = value.indexOf(':') != -1 ? "$value" : "$appPackage:id/$value"
            by = MobileBy.AndroidUIAutomator("resourceId(\"$resource\")")
        } else if (selectorString.startsWith(".")) {
            String value = selectorString.substring(1)
            by = MobileBy.className(value);
        } else {
            by = MobileBy.AndroidUIAutomator(selectorString?.replaceAll("'", '\"'))
        }

        log.debug "Using $by selector"
        by
    }

    @Override
    Navigator unique() {
        new AndroidUIAutomatorNonEmptyNavigator(browser, contextElements.unique(false))
    }

    @Override
    protected getInputValue(WebElement input) {
        def value
        def tagName = tag()

        if (tagName == "android.widget.Spinner") {
            if( AndroidHelper.isOnListView(driver) )
                value = input.findElementByAndroidUIAutomator("fromParent(new UiSelector().checked(true))").getText()
            else
                value = input.findElementByAndroidUIAutomator("fromParent(new UiSelector())").getText()
        } else if (tagName in ['android.widget.CheckBox','android.widget.Switch']) {
            value = input.getAttribute("checked")
        } else {
            value = input.getText()
        }
        log.debug("inputValue for $tagName : $value ")
        value
    }

    @Override
    void setInputValue(WebElement input, Object value) {

        def tagName = tag()
        log.debug("setInputValue: $input, $tagName")
        if (tagName == "android.widget.Spinner") {
            if (getInputValue(input) == value) return
            setSpinnerValue(input,value)
            AndroidHelper.closeListView(driver)

        } else if (tagName in ['android.widget.CheckBox', 'android.widget.RadioButton' ,'android.widget.Switch']) {
            def checked = input.getAttribute("checked")?.toBoolean()
            if ( !checked && value) {
                input.click()
            } else if (checked && !value ) {
                input.click()
            }
        } else {
            //TODO: hideKeyboard after sendKeys
            //TODO: clear Copy/Paste
//            input.clear()
            //input.sendKeys(Keys.HOME,Keys.chord(Keys.SHIFT,Keys.END),value);
            input.sendKeys value as String

            try{
                driver.hideKeyboard()
            }catch(e){
                log.warn("Hiding keyboard propably worked, but has thrown exc: $e.message")
            }
        }
    }

    private void setSpinnerValueWithScrollTo(MobileElement input, value) {
        try {
            input.click()
            driver.scrollTo(value?.toString())?.click()
        } catch (e) {
            log.warn("Could not set $value to $input.tagName : $e.message")
        }
    }

    private void setSpinnerValueWithScrollToExact(MobileElement input, value) {
        try {
            input.click()
            driver.scrollTo(value?.toString())?.click()
        } catch (e) {
            log.warn("Could not set $value to $input.tagName : $e.message")
        }
    }

    private void setSpinnerValue(MobileElement input, value) {
        try {
            def currVal = getInputValue(input)
            log.debug("Setting $value to Spinner: currentVal: ${currVal}")
            input.click()
            //input.properties
            driver.findElementByAndroidUIAutomator("text(\"$value\")").click()
            //input.findElementByAndroidUIAutomator("fromParent(new UiSelector().text(\"$value\"))")?.click()
            if (getInputValue(input) == value) return
            if( AndroidHelper.isOnListView(driver) ) {
                log.debug("Value not set and on ListView: Scrolling to $value")
                browser.driver.findElementByAndroidUIAutomator("new UiScrollable(new UiSelector().className(\"android.widget.ListView\")).flingBackward();")
                driver.findElementByAndroidUIAutomator("text(\"$value\")").click()
            }
            //input.findElementByAndroidUIAutomator("fromParent(new UiSelector().text(\"$value\"))")?.click()
        } catch (e) {
            log.warn("Error selecting with UiAutomator: $e.message")
        }

    }

    private void flingBack(){
        driver.ex ("new UiScrollable(new UiSelector().className('android.widget.ListView')).flingBackward();")
    }


}