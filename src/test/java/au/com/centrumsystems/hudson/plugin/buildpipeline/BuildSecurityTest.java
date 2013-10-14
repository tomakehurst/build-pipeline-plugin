package au.com.centrumsystems.hudson.plugin.buildpipeline;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.Permission;
import hudson.tasks.BuildTrigger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.*;

public class BuildSecurityTest {

    @Rule
    public JenkinsRule jr = new JenkinsRule();

    static final String INITIAL_JOB = "secured-job";
    static final String SECOND_JOB = "second-job";
    static final String UNPRIVILEGED_USER = "unprivilageduser";
    static final String PRIVILEGED_USER = "privilageduser";

    JenkinsRule.DummySecurityRealm realm;
    JenkinsRule.WebClient webClient;
    FreeStyleProject initialJob;
    BuildPipelineView pipelineView;

    @Before
    public void init() throws Exception {
        realm = jr.createDummySecurityRealm();
        jr.jenkins.setSecurityRealm(realm);
        GlobalMatrixAuthorizationStrategy authorizationStrategy = new GlobalMatrixAuthorizationStrategy();
        jr.jenkins.setAuthorizationStrategy(authorizationStrategy);
        authorizationStrategy.add(Permission.READ, UNPRIVILEGED_USER);
        authorizationStrategy.add(Permission.READ, PRIVILEGED_USER);
        authorizationStrategy.add(Item.BUILD, PRIVILEGED_USER);

        webClient = jr.createWebClient();
        initialJob = jr.createFreeStyleProject(INITIAL_JOB);
        BuildTrigger secondJobTrigger = new BuildTrigger(SECOND_JOB, true);
        jr.createFreeStyleProject(SECOND_JOB);
        initialJob.getPublishersList().add(secondJobTrigger);

        pipelineView = new BuildPipelineView("pipeline", "Pipeline", new DownstreamProjectGridBuilder(INITIAL_JOB), "5", false, null);
        jr.jenkins.addView(pipelineView);
    }

    @Test
    public void pipelineShouldNotShowRunButtonIfUserNotPermittedToTriggerBuild() throws Exception {
        HtmlPage pipelineViewPage = loggedInAs(UNPRIVILEGED_USER).getPage(pipelineView);
        HtmlElement runButton = runButtonOn(pipelineViewPage);
        assertNull("The Run button should not be present", runButton);
    }

    @Test
    public void pipelineShouldShowRunButtonIfUserPermittedToTriggerBuild() throws Exception {
        HtmlPage pipelineViewPage = loggedInAs(PRIVILEGED_USER).getPage(pipelineView);
        HtmlElement runButton = runButtonOn(pipelineViewPage);
        assertNotNull("The Run button should be present", runButton);
    }

    @Test
    public void shouldNotBeAbleToTriggerDownstreamJobIfNotPermitted() throws Exception {
        HtmlPage pipelineViewPage = loggedInAs(PRIVILEGED_USER).getPage(pipelineView);
        runButtonOn(pipelineViewPage).click();


    }

    private HtmlElement runButtonOn(HtmlPage pipelineViewPage) {
        return pipelineViewPage.getElementById("trigger-pipeline-button");
    }

    private JenkinsRule.WebClient loggedInAs(String user) throws Exception {
        return webClient.login(user, user);
    }

    
}
