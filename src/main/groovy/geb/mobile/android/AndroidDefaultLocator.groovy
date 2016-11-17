package geb.mobile.android
import geb.navigator.BasicLocator
import geb.navigator.DefaultLocator
import geb.navigator.Navigator
import groovy.util.logging.Slf4j
import io.appium.java_client.MobileBy
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.remote.RemoteWebDriver

@Slf4j
class AndroidDefaultLocator extends DefaultLocator {

    public static final String SKIP_SCROLLING_INDICATOR = "--"
    private final RemoteWebDriver driver
    private final Collection<? extends SearchContext> searchContexts

    private String getAppPackage() {
        driver.capabilities.getCapability("appPackage")
    }

    AndroidDefaultLocator(BasicLocator basicLocator, RemoteWebDriver driver) {
        super(basicLocator)
        this.driver = driver
    }

    @Override
    Navigator find(String selectorString) {
        By by = getByForSelector(selectorString)
        find(by)
    }

    protected By getByForSelector(String selectorString) {
        boolean skipScrolling = selectorString.startsWith(SKIP_SCROLLING_INDICATOR)
        if (skipScrolling) {
            selectorString = selectorString.subSequence(SKIP_SCROLLING_INDICATOR.size(), selectorString.length())
        }

        By by
        if (selectorString.startsWith("//")) {
            by = By.xpath(selectorString)
        } else if (selectorString.startsWith("#")) {
            String value = selectorString.substring(1)
            String resource = value.indexOf(':') != -1 ? "$value" : "$appPackage:id/$value"
            by = new ByAndroidUIAutomatorSkipScrolling("resourceId(\"$resource\")", skipScrolling)
        } else if (selectorString.startsWith(".")) {
            String value = selectorString.substring(1)
            by = MobileBy.className(value)
        } else {
            // replace single quotes with double quotes unless preceded by a slash, just convert those to single quote
            String escapedSelector = selectorString?.replaceAll("(?<![\\\\])[']", "\"").replaceAll("\\\\'", "'")
            by = new ByAndroidUIAutomatorSkipScrolling(escapedSelector, skipScrolling)
        }

        log.debug "Using $by selector"
        by
    }
}
