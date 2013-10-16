package au.com.centrumsystems.hudson.plugin.buildpipeline;

import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.LoginLogoutPage;
import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.PipelinePage;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import com.google.common.base.Function;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.Permission;
import hudson.tasks.BuildTrigger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static hudson.model.Result.SUCCESS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BuildSecurityTest {

    @Rule
    public JenkinsRule jr = new JenkinsRule();

    static final String INITIAL_JOB = "secured-job";
    static final String SECOND_JOB = "second-job";
    static final String UNPRIVILEGED_USER = "unprivilegeduser";
    static final String PRIVILEGED_USER = "privilegeduser";

    JenkinsRule.DummySecurityRealm realm;
    FreeStyleProject initialJob;
    BuildPipelineView pipelineView;

    LoginLogoutPage loginLogoutPage;
    PipelinePage pipelinePage;
    private WebDriver webDriver;

    @Before
    public void init() throws Exception {
        realm = jr.createDummySecurityRealm();
        jr.jenkins.setSecurityRealm(realm);
        GlobalMatrixAuthorizationStrategy authorizationStrategy = new GlobalMatrixAuthorizationStrategy();
        jr.jenkins.setAuthorizationStrategy(authorizationStrategy);
        authorizationStrategy.add(Permission.READ, UNPRIVILEGED_USER);
        authorizationStrategy.add(Permission.READ, PRIVILEGED_USER);
        authorizationStrategy.add(Item.BUILD, PRIVILEGED_USER);
        authorizationStrategy.add(Item.CONFIGURE, PRIVILEGED_USER);

        initialJob = jr.createFreeStyleProject(INITIAL_JOB);
//        BuildTrigger secondJobTrigger = new BuildTrigger(SECOND_JOB, true);
        BuildPipelineTrigger secondJobTrigger = new BuildPipelineTrigger(SECOND_JOB, Collections.<AbstractBuildParameters>emptyList());
        jr.createFreeStyleProject(SECOND_JOB);
        initialJob.getPublishersList().add(secondJobTrigger);

        pipelineView = new BuildPipelineView("pipeline", "Pipeline", new DownstreamProjectGridBuilder(INITIAL_JOB), "5", false, null);
        jr.jenkins.addView(pipelineView);

        webDriver = new FirefoxDriver();
        loginLogoutPage = new LoginLogoutPage(webDriver, jr.getURL());
        pipelinePage = new PipelinePage(webDriver, pipelineView.getViewName(), jr.getURL());
    }

    @After
    public void cleanUp() {
        webDriver.close();
        webDriver.quit();
    }

    @Test
    public void pipelineShouldNotShowRunButtonIfUserNotPermittedToTriggerBuild() throws Exception {
        loginLogoutPage.login(UNPRIVILEGED_USER);
        pipelinePage.open();

        assertFalse("The Run button should not be present",
                pipelinePage.runButtonIsPresent());
    }

    @Test
    public void pipelineShouldShowRunButtonIfUserPermittedToTriggerBuild() throws Exception {
        loginLogoutPage.login(PRIVILEGED_USER);
        pipelinePage.open();

        assertTrue("The Run button should be present",
                pipelinePage.runButtonIsPresent());
    }

    @Test
    public void shouldNotBeAbleToTriggerDownstreamJobIfNotPermitted() throws Exception {
        initialJob.scheduleBuild2(0);
        waitForInitialBuildToSucceed();

        loginLogoutPage.login(UNPRIVILEGED_USER);
        pipelinePage.open();

        assertFalse("Second card in pipeline should not have a trigger button",
                pipelinePage.buildCard(1, 1, 2).hasManualTriggerButton());
    }

    @Test
    public void shouldBeAbleToTriggerDownstreamJobIfPermitted() throws Exception {
        initialJob.scheduleBuild2(0);
        waitForInitialBuildToSucceed();

        loginLogoutPage.login(PRIVILEGED_USER);
        pipelinePage.open();

        assertTrue("Second card in pipeline should have a trigger button",
                pipelinePage.buildCard(1, 1, 2).hasManualTriggerButton());
    }

    private void waitForInitialBuildToSucceed() {
        new FluentWait<FreeStyleProject>(initialJob)
                .withTimeout(5, TimeUnit.SECONDS)
                .pollingEvery(100, TimeUnit.MILLISECONDS)
                .until(new Function<FreeStyleProject, Boolean>() {
            public Boolean apply(FreeStyleProject job) {
                return job.getBuilds() != null &&
                        job.getBuilds().getFirstBuild() != null &&
                        job.getBuilds().getFirstBuild().getResult() != null &&
                        job.getBuilds().getFirstBuild().getResult().isBetterOrEqualTo(SUCCESS);
            }
        });
    }
}
