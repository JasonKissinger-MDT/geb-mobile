package geb.mobile.android

import geb.Page
import io.appium.java_client.AppiumDriver
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.AndroidKeyCode
import io.selendroid.SelendroidKeys
import org.openqa.selenium.interactions.Actions

/**
 * Created by gmueksch on 26.06.14.
 *
 */
abstract class AndroidBaseActivity extends Page {

    static at = {
        waitFor {
            getActivityName() ? currentActivity == getActivityName() : true
        }
    }

    void back() {
        if (driver instanceof AndroidDriver) driver.sendKeyEvent(AndroidKeyCode.BACK)
        else new Actions(driver).sendKeys(SelendroidKeys.BACK).perform()
    }
    void menu() {
        if( driver instanceof AndroidDriver ) driver.sendKeyEvent(AndroidKeyCode.MENU)
        else new Actions(driver).sendKeys(SelendroidKeys.MENU).perform()

    }
    void home() {
        if( driver instanceof AndroidDriver ) driver.sendKeyEvent(AndroidKeyCode.HOME)
        else new Actions(driver).sendKeys(SelendroidKeys.ANDROID_HOME).perform()

    }

    /**
     * @return the Simple name of this Class, overwrite if classname is not the activityname, or null if it should not be checked
     */
    String getActivityName() {
        return this.getClass().getSimpleName()
    }

    /**
     * Special behavior for Appium
     * @return
     */
    public String getCurrentActivity() {
        if (driver instanceof AppiumDriver) {
            def currAct = driver.currentActivity()
            return currAct.startsWith(".") ? currAct.substring(1) : currAct
        } else {
            def currUrl = driver.currentUrl
            return currUrl.startsWith("and-") ? currUrl.split(/\/\//)[1] : currUrl
        }
    }


}
