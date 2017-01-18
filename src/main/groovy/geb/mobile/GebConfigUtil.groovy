package geb.mobile

import io.appium.java_client.service.local.AppiumDriverLocalService
import io.appium.java_client.service.local.AppiumServiceBuilder
import io.appium.java_client.service.local.flags.GeneralServerFlag
import org.apache.commons.lang3.SystemUtils

/**
 * A common place to put some methods multiple projects will need in their GebConfig.groovy files.
 */
class GebConfigUtil {

    /**
     * Starts appium on a random open port.  You can retrieve the URL (with the port) by running the .getUrl()
     * method on the returned object.
     * @return
     */
    static AppiumDriverLocalService startAppium(){
        println "Starting Appium..."
        AppiumServiceBuilder appiumServiceBuilder = new AppiumServiceBuilder().
                withArgument(GeneralServerFlag.LOG_TIMESTAMP).
                withArgument(GeneralServerFlag.LOG_LEVEL, "info")
                .usingAnyFreePort()
    
        if (!SystemUtils.IS_OS_WINDOWS) {
            // this code assumes that it is being run in the docker container created by this docker file: CC_Docker\androidnode\Dockerfile
            File node = new File('/home/appium/apps/bin/node')
            File appiumLocation = new File('/home/appium/apps/lib/node_modules/appium')
            appiumServiceBuilder = appiumServiceBuilder.
                    usingDriverExecutable(node).
                    withAppiumJS(appiumLocation)
        }
    
        AppiumDriverLocalService appium = AppiumDriverLocalService.buildService(appiumServiceBuilder)
        appium.start()
        println "Appium has started."
        appium
    }

/**
 * Attempts to unlock a mobile device screen by first swiping and then entering in a password if it's still locked.
 * Password is set/read from the <b>'device.password'</b> system environment variable.
 *
 * Code is needed because Appium will unlock a screen without a password, but not one with.
 */
    static void unlockDevice(){
        // appium enhancement issue to unlock with password: https://github.com/appium/appium/issues/5755

        if(isInteractive()){
            println("Screen is not interactive.  Attempting to swipe to unlock.")
            executeAdbCommand("shell input keyevent KEYCODE_WAKEUP")
            Thread.sleep(1 * 1000)
            executeAdbCommand("shell input swipe 200 400 200 0")
            Thread.sleep(1 * 1000)

            if(isInteractive()){
                println("Screen is still not interactive.  Attempting to enter password to unlock.")
                // turn off screen first as some will automatically turn off in the middle of the code below due to the timeout from the above wake up
                executeAdbCommand("shell input keyevent 26")
                Thread.sleep(1 * 1000)

                // turn on screen
                executeAdbCommand("shell input keyevent KEYCODE_WAKEUP")
                Thread.sleep(1 * 1000)

                // enter password
                String prop = 'device.password'
                String devicePassword = System.properties.get(prop, 'mdt2222')
                String command = "shell input text '${devicePassword}'"
                executeAdbCommand(command)
                Thread.sleep(1 * 1000)

                // hit enter
                executeAdbCommand("shell input keyevent 66")
                if(isInteractive()){
                    println("Screen is still not interactive!?  Maybe the password '${devicePassword}' tried is wrong?\n" +
                            "You can set the password used with the '${prop}' system property.\n" +
                            "Moving on to try and let Appium unlock the screen.")
                }
            } else {
                println("Screen is interactive.  Moving on.")
            }
        } else {
            println("Screen is interactive.  Moving on.")
        }
    }

/**
 * Detects if the screen of a mobile device is interactive (i.e. on and unlocked).
 * @return true if interactive, false otherwise
 */
    static private boolean isInteractive(){
        executeAdbCommand("shell service call trust 7").contains("Result: Parcel(00000000 00000001   '........')")
    }

    /**
     * Executes the given adb command.
     * If the  <b>'device.id'</b> system environment variable is set, it will execute the command for that specific device.
     * @param command
     * @return the output stream result of the command
     */
    static String executeAdbCommand(String command){
        String deviceId = System.properties.get('device.id')

        if(command.startsWith('adb ')){
            command = command.substring(4)
        }

        if(deviceId == null){
            command = "adb ${command}"
        } else {
            command = "adb -s ${deviceId} ${command}"
        }

        println("Running adb command: ${command}")
        Process process = command.execute()
        def (output, error) = new StringWriter().with { o -> // For the output
            new StringWriter().with { e ->                     // For the error stream
                process.waitForProcessOutput( o, e )
                [ o, e ]*.toString()                             // Return them both
            }
        }

        if(error.contains('error: more than one device/emulator')){
            throw new Exception("More than one device/emulator is attached.  You need to set the 'device.id' system property to issue adb commands.")
        }
        output
    }
}
