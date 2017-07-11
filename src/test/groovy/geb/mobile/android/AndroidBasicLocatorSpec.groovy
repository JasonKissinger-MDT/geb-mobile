package geb.mobile.android
import geb.navigator.Navigator
import geb.navigator.factory.NavigatorFactory
import io.appium.java_client.MobileBy
import io.appium.java_client.MobileElement
import org.openqa.selenium.By
import org.openqa.selenium.remote.RemoteWebDriver
import spock.lang.Specification

class AndroidBasicLocatorSpec extends Specification {
    MobileElement mockFoundElement = Mock(MobileElement)
    MobileElement mockContextElement = Mock(MobileElement)
    NavigatorFactory mockNavigatorFactory = Mock(NavigatorFactory)
    RemoteWebDriver mockDriver = Mock(RemoteWebDriver)
    AndroidBasicLocator locator = new AndroidBasicLocator([mockContextElement], mockNavigatorFactory, mockDriver)

    def 'NOT ByAndroidUIAutomatorSkipScrolling - found'() {
        given:
        By by = By.xpath('//')

        when:
        locator.find(by)

        then:
        1 * mockContextElement.findElements(by) >> [mockFoundElement]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement])
        0 * _
    }

    def 'NOT ByAndroidUIAutomatorSkipScrolling - not found'() {
        given:
        By by = By.xpath('//')

        when:
        locator.find(by)

        then:
        1 * mockContextElement.findElements(by) >> []
        1 * mockNavigatorFactory.createFromWebElements([])
        0 * _
    }

    def 'multiple elements in context'() {
        given:
        MobileElement mockContextElement2 = Mock(MobileElement)
        MobileElement mockFoundElement2 = Mock(MobileElement)
        AndroidBasicLocator locatorMultiple = new AndroidBasicLocator([mockContextElement, mockContextElement2], mockNavigatorFactory, mockDriver)
        By by = By.xpath('//')

        when:
        locatorMultiple.find(by)

        then:
        1 * mockContextElement.findElements(by) >> [mockFoundElement]
        1 * mockContextElement2.findElements(by) >> [mockFoundElement2]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement, mockFoundElement2])
        0 * _
    }


    // ------------------------------- ByAndroidUIAutomatorSkipScrolling tests (has more logic to go through) -------------------------------

    def 'ByAndroidUIAutomatorSkipScrolling - skip scroll - found'() {
        given:
        By by = new ByAndroidUIAutomatorSkipScrolling('resourceId("com.test.different.app:id/id")', true)

        when:
        locator.find(by)

        then:
        1 * mockContextElement.findElements(by) >> [mockFoundElement]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement])
        0 * _
    }

    def 'ByAndroidUIAutomatorSkipScrolling - skip scroll - not found'() {
        given:
        By by = new ByAndroidUIAutomatorSkipScrolling('resourceId("com.test.different.app:id/id")', true)

        when:
        locator.find(by)

        then:
        1 * mockContextElement.findElements(by) >> []
        1 * mockNavigatorFactory.createFromWebElements([])
        0 * _
    }

    def 'ByAndroidUIAutomatorSkipScrolling - scroll - found'() {
        given:
        By by = new ByAndroidUIAutomatorSkipScrolling('resourceId("com.test.different.app:id/id")', false)

        when:
        locator.find(by)

        then:
        1 * mockContextElement.findElements(by) >> [mockFoundElement]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement])
        0 * _
    }

    def 'ByAndroidUIAutomatorSkipScrolling - scroll - not found - scrollable not found'() {
        given:
        By by = new ByAndroidUIAutomatorSkipScrolling('resourceId("com.test.different.app:id/id")', false)

        when:
        locator.find(by)

        then:
        1 * mockContextElement.findElements(by) >> []
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator("new UiSelector().scrollable(true)"))
        1 * mockDriver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().scrollable(true)"))
        2 * mockNavigatorFactory.createFromWebElements([])
        0 * _
    }

    def 'ByAndroidUIAutomatorSkipScrolling - scroll - not found - scrollable found - not found'() {
        given:
        MobileElement mockMobileScrollable = Mock(MobileElement)
        Navigator mockScrollableNavigator = Mock(Navigator)
        String selector = 'resourceId("com.test.different.app:id/id")'
        By by = new ByAndroidUIAutomatorSkipScrolling(selector, false)

        when:
        locator.find(by)

        then:
        1 * mockContextElement.findElements(by) >> []
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator("new UiSelector().scrollable(true)")) >> [mockMobileScrollable]
        1 * mockNavigatorFactory.createFromWebElements([mockMobileScrollable]) >> mockScrollableNavigator
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(${selector})"))
        1 * mockNavigatorFactory.createFromWebElements([])
        0 * _
    }

    def 'ByAndroidUIAutomatorSkipScrolling - scroll - not found - scrollable found - found'() {
        given:
        MobileElement mockMobileScrollable = Mock(MobileElement)
        Navigator mockScrollableNavigator = Mock(Navigator)
        String selector = 'resourceId("com.test.different.app:id/id")'
        By by = new ByAndroidUIAutomatorSkipScrolling(selector, false)

        when:
        locator.find(by)

        then:
        1 * mockContextElement.findElements(by) >> []
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator("new UiSelector().scrollable(true)")) >> [mockMobileScrollable]
        1 * mockNavigatorFactory.createFromWebElements([mockMobileScrollable]) >> mockScrollableNavigator
        1 * mockContextElement.findElements(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(${selector})")) >> [mockFoundElement]
        1 * mockNavigatorFactory.createFromWebElements([mockFoundElement])
        0 * _
    }
}
