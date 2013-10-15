package au.com.centrumsystems.hudson.plugin.buildpipeline;

import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.Condition;
import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.PipelineHtmlPage;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.Permission;
import hudson.tasks.BuildTrigger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.TestUtils.waitFor;
import static hudson.model.Result.SUCCESS;
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

    PipelineHtmlPage pipelinePage;

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

        pipelinePage = new PipelineHtmlPage(pipelineView, jr.createWebClient());
    }

    @Test
    public void pipelineShouldNotShowRunButtonIfUserNotPermittedToTriggerBuild() throws Exception {
        assertFalse("The Run button should not be present",
                pipelinePage.viewedAs(UNPRIVILEGED_USER).runButtonIsPresent());
    }

    @Test
    public void pipelineShouldShowRunButtonIfUserPermittedToTriggerBuild() throws Exception {
        assertTrue("The Run button should be present",
                pipelinePage.viewedAs(PRIVILEGED_USER).runButtonIsPresent());
    }

    @Test
    public void shouldNotBeAbleToTriggerDownstreamJobIfNotPermitted() throws Exception {
        pipelinePage.viewedAs(PRIVILEGED_USER).clickRunButton();
        waitFor(initialBuildToSucceed());
        pipelinePage.reload();

        assertFalse("Second card in pipeline should not have a trigger button",
                pipelinePage.buildCard(1, 1, 2).hasManualTriggerButton());
    }

    private Condition initialBuildToSucceed() {
        return new Condition() {
            @Override
            public boolean isSatisfied() throws Exception {
                return initialJob.getBuilds().getFirstBuild().getResult().isBetterOrEqualTo(SUCCESS);
            }

            @Override
            public String describe() {
                return "Initial build has succeeded";
            }
        };
    }
}
