package geb.mobile.android
import geb.navigator.BasicLocator
import io.appium.java_client.MobileBy
import org.openqa.selenium.By
import org.openqa.selenium.Capabilities
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class AndroidDefaultLocatorSpec extends Specification {
    BasicLocator mockBasicLocator = Mock(BasicLocator)
    RemoteWebDriver mockRemoteWebDriver = Mock(RemoteWebDriver)
    AndroidDefaultLocator locator = new AndroidDefaultLocator(mockBasicLocator, mockRemoteWebDriver)

    def '// selector'() {
        given:
        String selector = '//'

        when:
        By by = locator.getByForSelector(selector)

        then:
        by == By.xpath(selector)
    }

    def '# selector - without id'() {
        given:
        String selector = '#id'
        Capabilities capabilities = new DesiredCapabilities()
        capabilities.setCapability('appPackage', 'com.test.app')
        mockRemoteWebDriver.capabilities >> capabilities

        when:
        By by = locator.getByForSelector(selector)

        then:
        by == new ByAndroidUIAutomatorSkipScrolling("resourceId(\"com.test.app:id/id\")", false)
    }

    def '# selector -#desc'() {
        given:
        Capabilities capabilities = new DesiredCapabilities()
        capabilities.setCapability('appPackage', 'com.test.app')
        mockRemoteWebDriver.capabilities >> capabilities

        when:
        By by = locator.getByForSelector(selector)

        then:
        by == new ByAndroidUIAutomatorSkipScrolling('resourceId("com.test.different.app:id/id")', skipScroll)

        where:
        desc                  | selector                          | skipScroll
        'with app package id' | '#com.test.different.app:id/id'   | false
        'skip scrollable'     | '--#com.test.different.app:id/id' | true
    }

    def '. selector - with app package id'() {
        given:
        String selector = '.android.widget.TextView'

        when:
        By by = locator.getByForSelector(selector)

        then:
        by == MobileBy.className('android.widget.TextView')
    }

    def 'defaults to ByAndroidUIAutomator'() {
        given:
        String selector = 'new UiSelector().clickable(true)'

        when:
        By by = locator.getByForSelector(selector)

        then:
        by == new ByAndroidUIAutomatorSkipScrolling(selector, false)
    }

    def 'ByAndroidUIAutomator with single quote'() {
        given:
        String selector = "text('It\\'s working!')"
        String doubleQuote = '"'

        when:
        By by = locator.getByForSelector(selector)

        then:
        by == new ByAndroidUIAutomatorSkipScrolling("text(${doubleQuote}It's working!${doubleQuote})", false)
    }
}
