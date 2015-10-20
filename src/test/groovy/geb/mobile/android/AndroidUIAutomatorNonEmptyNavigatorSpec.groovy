package geb.mobile.android

import geb.Browser
import geb.navigator.factory.NavigatorFactory
import io.appium.java_client.MobileBy
import io.appium.java_client.MobileElement
import org.openqa.selenium.By
import org.openqa.selenium.Capabilities
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import spock.lang.Specification

class AndroidUIAutomatorNonEmptyNavigatorSpec extends Specification {
    AndroidUIAutomatorNonEmptyNavigator navigator

    NavigatorFactory mockNavigatorFactory = Mock(NavigatorFactory)
    RemoteWebDriver mockDriver = Mock(RemoteWebDriver)
    Browser mockBrowser = Mock(Browser)
    MobileElement mockContextElement = Mock(MobileElement)
    MobileElement mockFoundElement = Mock(MobileElement)

    def setup() {
    }

    def '// selector'() {
        given:
        String selector = '//'

        when:
        navigator.find(selector)

        then:
        interaction { setupDefaultMocking() }
        1 * mockDriver.findElements(By.xpath(selector)) >> [mockFoundElement]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement])
        0 * _
    }

    def '# selector - without id'() {
        given:
        String selector = '#id'

        when:
        navigator.find(selector)

        then:
        interaction { setupDefaultMocking() }
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator('resourceId("com.test.app:id/id")')) >> [mockFoundElement]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement])
        0 * _
    }

    def '# selector - with app package id'() {
        given:
        String selector = '#com.test.different.app:id/id'

        when:
        navigator.find(selector)

        then:
        interaction { setupDefaultMocking() }
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator('resourceId("com.test.different.app:id/id")')) >> [mockFoundElement]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement])
        0 * _
    }

    def '. selector - with app package id'() {
        given:
        String selector = '.android.widget.TextView'

        when:
        navigator.find(selector)

        then:
        interaction { setupDefaultMocking() }
        1 * mockContextElement.findElements(By.className('android.widget.TextView')) >> [mockFoundElement]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement])
        0 * _
    }

    def 'defaults to ByAndroidUIAutomator'() {
        given:
        String selector = 'new UiSelector().clickable(true)'

        when:
        navigator.find(selector)

        then:
        interaction { setupDefaultMocking() }
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator(selector)) >> [mockFoundElement]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement])
        0 * _
    }

    def 'not found - not ByAndroidUIAutomator' () {
        given:
        String selector = '.android.widget.TextView'

        when:
        navigator.find(selector)

        then:
        interaction { setupDefaultMocking() }
        1 * mockContextElement.findElements(By.className('android.widget.TextView')) >> []
        1 * mockNavigatorFactory.createFromWebElements([])
        0 * _
    }

    def 'not found - ByAndroidUIAutomator - no scrollable' () {
        given:
        String selector = 'new UiSelector().clickable(true)'

        when:
        navigator.find(selector)

        then:
        interaction { setupDefaultMocking() }
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator(selector)) >> []
        1 * mockDriver.findElements(MobileBy.AndroidUIAutomator('new UiSelector().scrollable(true)')) >> []
        1 * mockNavigatorFactory.createFromWebElements([])
        0 * _
    }

    def 'not found - ByAndroidUIAutomator - has scrollable' () {
        given:
        MobileElement mockMobileScrollable = Mock(MobileElement)
        String selector = 'new UiSelector().clickable(true)'

        when:
        navigator.find(selector)

        then:
        interaction { setupDefaultMocking() }
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator(selector)) >> []
        1 * mockDriver.findElements(MobileBy.AndroidUIAutomator('new UiSelector().scrollable(true)')) >> [mockMobileScrollable]
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView($selector)"))
        1 * mockNavigatorFactory.createFromWebElements([])
        0 * _
    }

    def 'multiple elements in context' () {
        given:
        MobileElement mockContextElement2 = Mock(MobileElement)
        MobileElement mockFoundElement2 = Mock(MobileElement)
        String selector = 'new UiSelector().clickable(true)'

        when:
        navigator.find(selector)

        then:
        interaction { setupDefaultMocking() }
        interaction {
            navigator = new AndroidUIAutomatorNonEmptyNavigator(mockBrowser, [mockContextElement, mockContextElement2])
        }
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator(selector)) >> [mockFoundElement]
        1 * mockContextElement2.findElements(MobileBy.AndroidUIAutomator(selector)) >> [mockFoundElement2]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement, mockFoundElement2])
        0 * _
    }

    private void setupDefaultMocking() {
        mockBrowser.driver >> mockDriver
        mockBrowser.navigatorFactory >> mockNavigatorFactory

        Capabilities capabilities = new DesiredCapabilities()
        capabilities.setCapability('appPackage', 'com.test.app')
        mockDriver.capabilities >> capabilities

        navigator = new AndroidUIAutomatorNonEmptyNavigator(mockBrowser, [mockContextElement])
    }
}
