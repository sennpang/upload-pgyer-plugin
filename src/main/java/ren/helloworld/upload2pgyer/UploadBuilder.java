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
import ren.helloworld.upload2pgyer.apiv1.ParamsBeanV1;
import ren.helloworld.upload2pgyer.helper.PgyerV1Helper;

import java.io.IOException;

/**
 * upload to jenkins
 *
 * @author myroid
 */
public class UploadBuilder extends Builder {

    private final Secret uKey;
    private final Secret apiKey;
    private final String scanDir;
    private final String wildcard;
    private final String installType;
    private final Secret password;
    private final String updateDescription;
    private final String channelShortcut;

    private final String qrcodePath;
    private final String envVarsPath;

    @DataBoundConstructor
    public UploadBuilder(String uKey, String apiKey, String scanDir, String wildcard, String installType, String password, String updateDescription, String channelShortcut, String qrcodePath, String envVarsPath) {
        this.uKey = Secret.fromString(uKey);
        this.apiKey = Secret.fromString(apiKey);
        this.scanDir = scanDir;
        this.wildcard = wildcard;
        this.installType = installType;
        this.password = Secret.fromString(password);
        this.updateDescription = updateDescription;
        this.channelShortcut = channelShortcut;
        this.qrcodePath = qrcodePath;
        this.envVarsPath = envVarsPath;
    }

    public Secret getuKey() {
        return uKey;
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

    public String getInstallType() {
        return installType;
    }

    public Secret getPassword() {
        return password;
    }

    public String getUpdateDescription() {
        return updateDescription;
    }

    public String getChannelShortcut() {
        return channelShortcut;
    }

    public String getQrcodePath() {
        return qrcodePath;
    }

    public String getEnvVarsPath() {
        return envVarsPath;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        ParamsBeanV1 paramsBeanV1 = new ParamsBeanV1();
        paramsBeanV1.setUkey(uKey.getPlainText());
        paramsBeanV1.setApiKey(apiKey.getPlainText());
        paramsBeanV1.setScandir(scanDir);
        paramsBeanV1.setWildcard(wildcard);
        paramsBeanV1.setInstallType(installType);
        paramsBeanV1.setPassword(password.getPlainText());
        paramsBeanV1.setUpdateDescription(updateDescription);
        paramsBeanV1.setChannelShortcut(channelShortcut);
        paramsBeanV1.setQrcodePath(qrcodePath);
        paramsBeanV1.setEnvVarsPath(envVarsPath);
        return PgyerV1Helper.upload(build, listener, paramsBeanV1);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Symbol("upload-pgyer")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String installType = "1";

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckUKey(@QueryParameter String value) {
            return ValidationParameters.doCheckUKey(value);
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

        public FormValidation doCheckInstallType(@QueryParameter String value) {
            installType = value;
            return ValidationParameters.doCheckInstallType(value);
        }

        public FormValidation doCheckPassword(@QueryParameter String value) {
            return ValidationParameters.doCheckPassword(installType, value);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return false;
        }

        public String getDisplayName() {
            return "Upload to pgyer with apiV1";
        }
    }
}

