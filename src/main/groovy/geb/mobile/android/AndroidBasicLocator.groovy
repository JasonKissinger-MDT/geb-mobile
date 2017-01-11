package geb.mobile.android

import geb.navigator.Navigator
import geb.navigator.SearchContextBasedBasicLocator
import geb.navigator.factory.NavigatorFactory
import groovy.util.logging.Slf4j
import io.appium.java_client.MobileBy
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.UnsupportedCommandException
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver

@Slf4j
class AndroidBasicLocator extends SearchContextBasedBasicLocator {

    private final RemoteWebDriver driver
    private final Collection<? extends SearchContext> searchContexts

    AndroidBasicLocator(Collection<? extends SearchContext> searchContexts, NavigatorFactory navigatorFactory, RemoteWebDriver driver) {
        super(searchContexts, navigatorFactory)
        this.searchContexts = searchContexts
        this.driver = driver
    }

    @Override
    Navigator find(By by) {
        List<WebElement> list = []

        this.searchContexts?.each { SearchContext searchContext ->
            List<WebElement> found = searchContext.findElements(by)

            if (!found && by instanceof ByAndroidUIAutomatorSkipScrolling && !by.skipScrolling) {
                // check the current context to see if it's scrollable
                By scrollableBy = MobileBy.AndroidUIAutomator("new UiSelector().scrollable(true)")
                def scrollable = find(scrollableBy)

                if (!scrollable) {
                    // current context isn't scrollable, let's try the whole page/screen
                    scrollable = this.driver.findElements(scrollableBy)
                }

                if (scrollable) {
                    By scrolledBy = MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(${by.locatorString})")
                    log.debug "Not found with selector $by attempting to scroll into view using $scrolledBy"

                    try {
                        found = searchContext.findElements(scrolledBy)
                    } catch (UnsupportedCommandException e) {
                        // this exception will be thrown if the selector is invalid or can not be found on a scrollIntoView, either way we just want
                        // to return an empty list vs. bubbling up an exception
                        log.debug "Scroll into view failed, returning empty set.  The message was $e"
                        found = []
                    }
                } else {
                    log.debug "No scrollable found, will not attempt to scroll for element"
                }
            }

            found && list.addAll(found)
        }

        log.debug "Found $list.size() elements"

        navigatorFor(list)
    }
}
