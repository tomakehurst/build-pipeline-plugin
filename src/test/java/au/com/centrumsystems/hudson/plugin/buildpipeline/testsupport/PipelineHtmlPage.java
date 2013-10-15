package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jvnet.hudson.test.JenkinsRule;

public class PipelineHtmlPage {

    private static final String TRIGGER_PIPELINE_BUTTON = "trigger-pipeline-button";
    private final BuildPipelineView pipelineView;
    private final JenkinsRule.WebClient webClient;
    private HtmlPage page;

    public PipelineHtmlPage(BuildPipelineView pipelineView, JenkinsRule.WebClient webClient) {
        this.pipelineView = pipelineView;
        this.webClient = webClient;
    }

    public PipelineHtmlPage viewedAs(String user) throws Exception {
        page = webClient.login(user, user).getPage(pipelineView);
        return this;
    }

    public boolean runButtonIsPresent() throws Exception {
        return page.getElementById(TRIGGER_PIPELINE_BUTTON) != null;
    }

    public PipelineHtmlPage clickRunButton() throws Exception {
        page.getElementById(TRIGGER_PIPELINE_BUTTON).click();
        return this;
    }

    public void reload() throws Exception {
        page.refresh();
    }

    public BuildCardHtmlComponent buildCard(int pipelineGroup, int pipeline, int card) {
        BuildCardHtmlComponent buildCard = new BuildCardHtmlComponent(page, pipelineGroup, pipeline, card);
        buildCard.waitToAppear();
        return buildCard;
    }

}
