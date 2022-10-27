package ren.helloworld.upload2pgyer;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import ren.helloworld.upload2pgyer.apiv2.ParamsBeanV2;
import ren.helloworld.upload2pgyer.helper.PgyerV2Helper;

import java.io.IOException;

/**
 * upload to jenkins
 *
 * @author myroid
 */
public class UploadBuilderV2 extends Builder {

    private final Secret apiKey;
    private final String scanDir;
    private final String wildcard;
    private final String buildInstallType;
    private final Secret buildPassword;
    private final String buildUpdateDescription;
    private final String buildType;
    private final String buildChannelShortcut;


    @DataBoundConstructor
    public UploadBuilderV2(String apiKey, String scanDir, String wildcard, String buildType, String buildInstallType, String buildPassword, String buildUpdateDescription, String buildChannelShortcut, String qrcodePath, String envVarsPath) {
        this.apiKey = Secret.fromString(apiKey);
        this.scanDir = scanDir;
        this.wildcard = wildcard;
        this.buildType = buildType;
        this.buildPassword = Secret.fromString(buildPassword);
        this.buildInstallType = buildInstallType;
        this.buildUpdateDescription = buildUpdateDescription;
        this.buildChannelShortcut = buildChannelShortcut;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    public String getScanDir() {
        return scanDir;
    }

    public String getWildcard() {
        return wildcard;
    }

    public String getBuildInstallType() {
        return buildInstallType;
    }

    public Secret getBuildPassword() {
        return buildPassword;
    }

    public String getBuildUpdateDescription() {
        return buildUpdateDescription;
    }

    public String getBuildType() {
        return buildType;
    }

    public String getBuildChannelShortcut() {
        return buildChannelShortcut;
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        ParamsBeanV2 paramsBeanV2 = new ParamsBeanV2();
        paramsBeanV2.setApiKey(apiKey.getPlainText());
        paramsBeanV2.setScandir(scanDir);
        paramsBeanV2.setWildcard(wildcard);
        paramsBeanV2.setBuildPassword(buildPassword.getPlainText());
        paramsBeanV2.setBuildInstallType(buildInstallType);
        paramsBeanV2.setBuildUpdateDescription(buildUpdateDescription);
        paramsBeanV2.setBuildType(buildType);
        paramsBeanV2.setBuildChannelShortcut(buildChannelShortcut);
        return PgyerV2Helper.upload(build, listener, paramsBeanV2);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Symbol("upload-pgyer-v2")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String installType = "1";

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckApiKey(@QueryParameter String value) {
            return ValidationParameters.doCheckApiKey(value);
        }

        public FormValidation doCheckScanDir(@QueryParameter String value) {
            return ValidationParameters.doCheckScanDir(value);
        }

        public FormValidation doCheckWildcard(@QueryParameter String value) {
            return ValidationParameters.doCheckWildcard(value);
        }

        public FormValidation doCheckBuildType(@QueryParameter String value) {
            return ValidationParameters.doCheckBuildType(value);
        }

        public FormValidation doCheckBuildInstallType(@QueryParameter String value) {
            installType = value;
            return ValidationParameters.doCheckInstallType(value);
        }

        public FormValidation doCheckBuildPassword(@QueryParameter String value) {
            return ValidationParameters.doCheckPassword(installType, value);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Upload to pgyer with apiV2";
        }
    }
}

