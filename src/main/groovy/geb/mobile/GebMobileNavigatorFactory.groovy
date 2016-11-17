package geb.mobile
import geb.Browser
import geb.mobile.android.AndroidBasicLocator
import geb.mobile.android.AndroidDefaultLocator
import geb.navigator.Locator
import geb.navigator.Navigator
import geb.navigator.factory.NavigatorBackedNavigatorFactory
import geb.navigator.factory.NavigatorFactory
import groovy.util.logging.Slf4j
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
/**
 * Created by gmueksch on 23.06.14.
 */
@Slf4j
class GebMobileNavigatorFactory implements NavigatorFactory {

    private final Browser browser
    private final GebMobileInnerNavigatorFactory innerNavigatorFactory
    final Locator locator

    String context

    GebMobileNavigatorFactory(Browser browser) {
        this(browser, new GebMobileInnerNavigatorFactory() )
    }

    GebMobileNavigatorFactory(Browser browser, GebMobileInnerNavigatorFactory innerNavigatorFactory) {
        this.browser = browser
        this.innerNavigatorFactory = innerNavigatorFactory
        this.innerNavigatorFactory.navigatorFactory = this
        locator = new AndroidDefaultLocator(new AndroidBasicLocator([browser.driver], this, browser.driver), browser.driver)
    }

    @Override
    Navigator getBase() {
        def baseNavigatorWaiting = browser.config.baseNavigatorWaiting
        baseNavigatorWaiting ? baseNavigatorWaiting.waitFor { createBase() } : createBase()
    }

    protected Navigator createBase() {
        createFromWebElements(Collections.singletonList(browser.driver.findElement(By.xpath("//*"))))
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
}
