package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebDriverTest {

    @Test
    public void firefox() throws Exception {
        FirefoxDriver driver = new FirefoxDriver();
        driver.get("http://www.google.co.uk");
        Thread.sleep(2000);
        driver.close();
    }

    @Test
    public void chrome() throws Exception {
        System.setProperty("webdriver.chrome.driver", "/Users/thomas.akehurst/dev/workspace/build-pipeline-plugin/webdriver/chromedriver");

        ChromeDriver driver = new ChromeDriver();
        driver.get("www.google.co.uk");
        Thread.sleep(2000);
        driver.close();
    }
}
