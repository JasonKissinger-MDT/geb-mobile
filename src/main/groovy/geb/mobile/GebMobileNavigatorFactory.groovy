package geb.mobile

import geb.Browser
import geb.navigator.Navigator
import geb.navigator.factory.NavigatorBackedNavigatorFactory
import geb.navigator.factory.NavigatorFactory
import groovy.util.logging.Slf4j
import io.appium.java_client.FindsByAndroidUIAutomator
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.WebElement

/**
 * Created by gmueksch on 23.06.14.
 */
@Slf4j
class GebMobileNavigatorFactory implements NavigatorFactory {

    private final Browser browser
    private final GebMobileInnerNavigatorFactory innerNavigatorFactory

    String context

    GebMobileNavigatorFactory(Browser browser) {
        this(browser, new GebMobileInnerNavigatorFactory() )
    }

    GebMobileNavigatorFactory(Browser browser, GebMobileInnerNavigatorFactory innerNavigatorFactory) {
        this.browser = browser
        this.innerNavigatorFactory = innerNavigatorFactory
        this.innerNavigatorFactory.navigatorFactory = this
    }

    @Override
    Navigator getBase() {
        List<WebElement> list

        if (browser.driver instanceof AndroidDriver) {
            // The base element should be the top element in the hierarchy
            list = Arrays.asList(new AndroidRootElement(browser.driver))
        } else {
            list = browser.driver.findElementsByXPath("//*") as List
        }

        createFromWebElements(list)
    }

    protected Browser getBrowser() {
        return browser
    }

    Navigator createFromWebElements(Iterable<WebElement> elements) {
        List<WebElement> filtered = []
        elements.each {
            if (it != null) {
                filtered << it
            }
        }
        innerNavigatorFactory.createNavigator(browser, filtered)
    }

    Navigator createFromNavigators(Iterable<Navigator> navigators) {
        List<WebElement> filtered = []
        navigators.each {
            if (it != null) {
                filtered.addAll(it.allElements())
            }
        }
        innerNavigatorFactory.createNavigator(browser, filtered)
    }

    NavigatorFactory relativeTo(Navigator newBase) {
        new NavigatorBackedNavigatorFactory(newBase, innerNavigatorFactory)
    }

    private class AndroidRootElement extends MobileElement implements FindsByAndroidUIAutomator {
        AndroidDriver driver

        AndroidRootElement(AndroidDriver driver) {
            this.driver = driver
        }

        @Override
        public WebElement findElementByAndroidUIAutomator(String using) {
            driver.findElementByAndroidUIAutomator(using);
        }

        @Override
        public List<WebElement> findElementsByAndroidUIAutomator(String using) {
            driver.findElementsByAndroidUIAutomator(using);
        }
    }

}
