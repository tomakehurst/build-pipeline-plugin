package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.TestUtils.waitForElement;

public class BuildCardComponent {

    private final WebDriver webDriver;
    private final int pipelineGroup;
    private final int pipeline;
    private final int card;

    private WebElement cardWebElement;

    public BuildCardComponent(WebDriver webDriver, int pipelineGroup, int pipeline, int card) {
        this.webDriver = webDriver;
        this.pipelineGroup = pipelineGroup;
        this.pipeline = pipeline;
        this.card = card;
    }

    public BuildCardComponent waitFor() {
        cardWebElement = waitForElement(By.xpath(cardXPath(pipelineGroup, pipeline, card)), webDriver);
        return this;
    }

    public boolean hasManualTriggerButton() throws Exception {
        try {
            triggerButtonHtmlElement();
        } catch (NoSuchElementException e) {
            return false;
        }

        return true;
    }

    public BuildCardComponent clickTriggerButton() throws Exception {
        triggerButtonHtmlElement().click();
        return this;
    }

    private WebElement triggerButtonHtmlElement() {
        return cardWebElement.findElement(By.xpath("//span[@class='pointer trigger']"));
    }

    private String cardXPath(int pipelineGroup, int pipeline, int card) {
        return String.format("//table[@id = 'pipelines']/tbody[%d]/tr[@class='build-pipeline'][%d]/td[starts-with(@id,'build-')][%d]",
                pipelineGroup, pipeline, card);
    }
}
