package geb.mobile.android

import geb.Browser
import geb.mobile.AbstractMobileNonEmptyNavigator
import geb.navigator.Navigator
import groovy.util.logging.Slf4j
import io.appium.java_client.MobileBy
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.By
import org.openqa.selenium.UnsupportedCommandException
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

                    try {
                        found = element.findElements(scrolledBy)
                    } catch (UnsupportedCommandException e) {
                        // this exception will be thrown if the selector is invalid or can not be found on a scrollIntoView, either way we just want
                        // to return an empty list vs. bubbling up an exception
                        log.debug "Scroll into view failed, returning empty set.  The message was $e"
                        found = []
                    }
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
            by = MobileBy.AndroidUIAutomator("resourceId(\"$appPackage:id/$value\")")
        } else {
            by = MobileBy.AndroidUIAutomator(selectorString?.replaceAll("'", '\"'))
        }

        log.debug "Using $by selector"
        by
    }

    @Override
    String tag() {
        firstElement().getAttribute("className")
    }

    @Override
    String text() {
        firstElement().text
    }

    @Override
    boolean isDisabled() {
        !isEnabled()
    }

    @Override
    boolean isEnabled() {
        firstElement().getAttribute("enabled")?.toBoolean()
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
            value = input?.findElementByAndroidUIAutomator("new UiSelector().enabled(true)").getText()
        } else if (tagName == "android.widget.CheckBox") {
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
            setSpinnerValueWithScrollToExact(input,value)
            if( getInputValue(input) != value ) {
                setSpinnerValueWithScrollTo(input, value)
            }
        } else if (tagName in ["android.widget.CheckBox", "android.widget.RadioButton"]) {
            boolean checked = input.getAttribute("checked")
            if ( !checked && value) {
                input.click()
            } else if (checked && !value ) {
                input.click()
            }
        } else {
            input.sendKeys value as String
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
            // scroll to exact was failing if the accessibility text didn't match the desired text https://github.com/appium/java-client/issues/196
            // driver.scrollToExact(value?.toString())?.click()

            // we really only cared about the text anyways
            (MobileElement) driver.findElementByAndroidUIAutomator(driver.UiScrollable("new UiSelector().text(\"" + value + "\")")).click()
        } catch (e) {
            log.warn("Could not set $value to $input : $e.message")
        }
    }

    private void setSpinnerValueWithUISelector(MobileElement input, value) {
        try {
            input.click()
            input.findElementByAndroidUIAutomator("new UiSelector().text(\"$value\")")?.click()
            if (getInputValue(input) == value) return
            input.findElementByAndroidUIAutomator("new UiScrollable(new UiSelector().className(\"${input.tagName}\")).getChildByText(new UiSelector().enabled(true), \"${value}\")")
        } catch (e) {
            log.warn("Error selecting with UiAutomator: $e.message")
        }

    }



}