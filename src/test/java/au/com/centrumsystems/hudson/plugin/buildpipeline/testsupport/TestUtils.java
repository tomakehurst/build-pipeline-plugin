package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestUtils {

    public static WebElement waitForElement(final By findBy, WebDriver driver) {
        return new WebDriverWait(driver, 10).until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                return driver.findElement(findBy);
            }
        });
    }

    public static void checkState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
