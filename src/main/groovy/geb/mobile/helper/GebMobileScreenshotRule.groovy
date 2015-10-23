package geb.mobile.helper

import geb.mobile.GebMobileBaseSpec
import groovy.util.logging.Slf4j
import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriver

import javax.imageio.ImageIO

/**
 * Add the screenshots dir to the jenkins archive artifacts post build plugin
 */
@Slf4j
class GebMobileScreenshotRule implements MethodRule {

    public File getSnapshotDir() {
        def dir = new File(getJenkinsWorkspace() ?: '.', 'screenshots')

        if (!dir.exists()) {
            dir.mkdirs()
        }

        return dir
    }

    public static String getJenkinsWorkspace() {
        return System.getenv('WORKSPACE')
    }

    public static String getJobName() {
        return System.getenv("JOB_NAME")
    }

    public static String getBuildNumber() {
        return System.getenv("BUILD_NUMBER")
    }

    public static String getBuildURL() {
        return System.getenv("BUILD_URL")
    }

    public static String getArtifactURL() {
        return getBuildURL() + "artifact/"
    }

    public static boolean isJenkinsExecution() {
        return getJenkinsWorkspace() != null
    }

    /**
     * Method to get an URL to a file
     * @param file , to generate the URL for
     * @return an URL, when on Jenkins to the archived artifact, when local the URL to file is returned
     */
    public static String getRefUrlToArchivedFileInBuild(File file) {

        if (file == null) return null
        if (!isJenkinsExecution()) return file.toURI().toURL()

        File ws = new File(getJenkinsWorkspace())
        if (!file.absolutePath.startsWith(ws.absolutePath)) {
            log.warn("File $file.absolutePath is not part of the Workspace")
            return ""
        }

        String url = getArtifactURL() + (file.absolutePath - ws.absolutePath)
        url = url.replace('\\', '/')

        log.debug("URL in build ${getBuildNumber()} to archived file: $file --> $url")
        return url

    }

    public GebMobileBaseSpec baseSpec

    @Override
    Statement apply(Statement base, FrameworkMethod method, Object target) {

        return new Statement() {
            @Override
            void evaluate() throws Throwable {
                try {
                    base.evaluate()
                } catch (Throwable ex) {

                    if (baseSpec) {
                        WebDriver driver = baseSpec.driver
                        def snapDir = snapshotDir
                        def fName = method.name.replaceAll(/[ ,\._\-:]/, "_")

                        if (driver instanceof TakesScreenshot) {
                            log.warn("Caught $ex.message --> take screenshot")
                            def img = baseSpec.screenShotAsImage
                            try {
                                def pngFile = new File(snapDir, fName + '.png')
                                ImageIO.write(img, "png", pngFile)
                                log.warn("Saved screen shot: ${getRefUrlToArchivedFileInBuild(pngFile)}")
                            } catch (e) {
                                log.warn "error writing image: $e.message"
                            }
                        }

                        def xmlFile = new File(snapDir, fName + '_pageSource.xml')
                        try {
                            xmlFile.withWriter { wr ->
                                wr.write(driver.pageSource)
                            }
                            log.warn("Saved page source: ${getRefUrlToArchivedFileInBuild(xmlFile)}")
                        } catch (e) {
                            log.warn("problem creating pageSource: $e.message")
                        }
                    }

                    throw ex
                }
            }
        }

    }
}
