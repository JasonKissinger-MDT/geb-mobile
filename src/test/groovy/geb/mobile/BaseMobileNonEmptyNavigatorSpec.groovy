package geb.mobile

import geb.Browser
import geb.navigator.factory.NavigatorFactory
import io.appium.java_client.MobileElement
import org.openqa.selenium.Capabilities
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import spock.lang.Specification

abstract class BaseMobileNonEmptyNavigatorSpec<T extends AbstractMobileNonEmptyNavigator> extends Specification {
    T navigator

    NavigatorFactory mockNavigatorFactory = Mock(NavigatorFactory)
    RemoteWebDriver mockDriver = Mock(RemoteWebDriver)
    Browser mockBrowser = Mock(Browser)
    MobileElement mockContextElement = Mock(MobileElement)

    abstract T createNavigator()

    protected void setupDefaultMocking() {
        mockBrowser.driver >> mockDriver
        mockBrowser.navigatorFactory >> mockNavigatorFactory

        Capabilities capabilities = new DesiredCapabilities()
        capabilities.setCapability('appPackage', 'com.test.app')
        mockDriver.capabilities >> capabilities

        navigator = createNavigator()
    }
}
