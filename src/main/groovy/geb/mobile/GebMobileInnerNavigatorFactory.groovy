package geb.mobile

import geb.Browser
import geb.mobile.android.AndroidInstrumentationNonEmptyNavigator
import geb.mobile.android.AndroidUIAutomatorNonEmptyNavigator
import geb.mobile.ios.AppiumIosInstrumentationNonEmptyNavigator
import geb.mobile.ios.IosInstrumentationNonEmptyNavigator
import geb.navigator.EmptyNavigator
import geb.navigator.Navigator
import geb.navigator.factory.InnerNavigatorFactory
import groovy.util.logging.Slf4j
import io.appium.java_client.AppiumDriver
import org.openqa.selenium.Platform
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.CapabilityType

/**
 * This Factory decides, which NonEmptyNavigator will be created for the WebElements from the Driver
 * This is not always clear, cause
 *
 * AppiumDriver could case 3 different implementations:
 * Android:
 *  Native-Apps:
 *  APILevel > 16 --> AndroidUIAutomator
 *  APILevel < 16 --> InstrumentationFramework
 *
 *  Mobile Site:
 *      Instrumentation Framework
 *
 *  Hybrid Apps or Apps where you call the Camera or other stuff:
 *      both   AndroidUIAutomator and InstrumentationFramework
 *
 * IOS:
 *   IosInstrumentation
 *
 * Selendroid:
 *    InstrumentationFramework
 *
 * IosDriver:
 *   IosInstrumentation
 *
 *
 *
 * Created by gmueksch on 23.06.14.
 *
 */
@Slf4j
class GebMobileInnerNavigatorFactory implements InnerNavigatorFactory {

    private static String NAV_PREFIX = 'appium'

    static Map<String, Class> _defaultNavigators = [selendroid         : AndroidInstrumentationNonEmptyNavigator,
                                                    android            : AndroidUIAutomatorNonEmptyNavigator,
                                                    firefox            : AndroidInstrumentationNonEmptyNavigator,
                                                    ios                : AppiumIosInstrumentationNonEmptyNavigator,
                                                    'appium:NATIVE_APP': AndroidUIAutomatorNonEmptyNavigator,
                                                    'appium:WEBVIEW_1': AndroidInstrumentationNonEmptyNavigator
    ]


    Map<Browser, Class> _innerNavigators = [:]

    GebMobileNavigatorFactory navigatorFactory

    private Class figureCorrectInnerNavigator(browser) {
        def driver = browser.driver

        def clazz
        //Set class null, when a context change was done
        if (driver instanceof AppiumDriver && navigatorFactory.context) {
            String ctx = driver.getContext()
            if (navigatorFactory.context != ctx) {
                clazz = _defaultNavigators["$NAV_PREFIX:$ctx"]
            }
        }

        if (!clazz)
            clazz = _innerNavigators[browser]

        if (!clazz) {
            synchronized (_innerNavigators) {
                String browserName = driver.capabilities.getCapability(CapabilityType.BROWSER_NAME)
                String platformName = driver.capabilities.getCapability("platformName")
                Platform platform = driver.capabilities.getCapability(CapabilityType.PLATFORM)
                log.debug("trying to figure out correct Navigator for $browserName, $platformName, ${platform.name()}")

                String driverName = driver.class.simpleName
                if (driverName ==  "SelendroidDriver") {
                    clazz = AndroidInstrumentationNonEmptyNavigator
                } else if (driverName == "RemoteIOSDriver") {
                    clazz = IosInstrumentationNonEmptyNavigator
                } else if (driverName == "IOSDriver"){
                    clazz = AppiumIosInstrumentationNonEmptyNavigator
                    navigatorFactory.context = driver.getContext()
                } else if (driverName == "AndroidDriver") {
                    navigatorFactory.context = driver.getContext()
                      clazz = _defaultNavigators["$NAV_PREFIX:$navigatorFactory.context"]
                } else {
                    clazz = _defaultNavigators[browserName.toLowerCase()]
                    if (!clazz) clazz = _defaultNavigators[platformName.toLowerCase()]
                }

                if (clazz) {
                    log.info("Using $clazz.name for $browserName, $platformName, ${platform.name()}")
                    _innerNavigators[browser] = clazz
                } else {
                    throw new RuntimeException("Could not determine correct InnerNavigator for $browserName, $platformName, ${platform.name()}")
                }

            }
        }
        return clazz
    }

    /**
     * If {@code elements != null && elements.size() > 0} a {@link geb.navigator.NonEmptyNavigator is returned, otherwise {@link EmptyNavigator}.
     * @param browser The browse to associate with the navigator
     * @param elements The elements to back the navigator
     * @return The newly created navigator
     */
    Navigator createNavigator(Browser browser, List<WebElement> elements) {
        if (!elements) return new EmptyNavigator(browser)
        return figureCorrectInnerNavigator(browser).newInstance(browser, elements)
    }

}
