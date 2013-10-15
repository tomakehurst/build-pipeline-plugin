package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import static au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.TestUtils.checkState;
import static au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.TestUtils.waitFor;

public class BuildCardHtmlComponent {

    private final HtmlPage page;
    private final int pipelineGroup;
    private final int pipeline;
    private final int card;

    public BuildCardHtmlComponent(HtmlPage page, int pipelineGroup, int pipeline, int card) {
        this.page = page;
        this.pipelineGroup = pipelineGroup;
        this.pipeline = pipeline;
        this.card = card;
    }

    public boolean hasManualTriggerButton() throws Exception {
        return triggerButtonHtmlElement() != null;
    }

    public BuildCardHtmlComponent clickTriggerButton() throws Exception {
        triggerButtonHtmlElement().click();
        return this;
    }

    private HtmlElement triggerButtonHtmlElement() {
        HtmlElement buildCardElement = buildCardHtmlElement();
        checkState(buildCardElement != null, String.format("Build card (%d, %d, %d) is not present", pipelineGroup, pipeline, card));
        assert buildCardElement != null;
        return buildCardElement.getFirstByXPath("//span[@class='pointer trigger']");
    }

    private HtmlElement buildCardHtmlElement() {
        return page.getFirstByXPath(cardXPath(pipelineGroup, pipeline, card));
    }

    private String cardXPath(int pipelineGroup, int pipeline, int card) {
        return String.format("//table[@id = 'pipelines']/tbody[%d]/tr[@class='build-pipeline'][%d]/td[starts-with(@id,'build-')][%d]",
                pipelineGroup, pipeline, card);
    }

    public void waitToAppear() {
        waitFor(new Condition() {
            public boolean isSatisfied() throws Exception {
                return buildCardHtmlElement() != null;
            }

            public String describe() {
                return "build card to appear";
            }
        });
    }
}
