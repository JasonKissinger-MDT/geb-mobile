package geb.mobile.android
import io.appium.java_client.MobileBy

class ByAndroidUIAutomatorSkipScrolling extends MobileBy.ByAndroidUIAutomator {

    final boolean skipScrolling

    public ByAndroidUIAutomatorSkipScrolling(String uiautomatorText, boolean skipScrolling) {
        super(uiautomatorText)
        this.skipScrolling = skipScrolling
    }

    @Override public String toString() {
        return "By.ByAndroidUIAutomatorSkipScrolling: ${getLocatorString()}, skipScrolling: ${skipScrolling}"
    }
}
