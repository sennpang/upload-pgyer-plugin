package ren.helloworld.upload2pgyer;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
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
public class UploadPublisher extends Recorder {

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
    public UploadPublisher(String uKey, String apiKey, String scanDir, String wildcard, String installType, String password, String updateDescription, String channelShortcut, String qrcodePath, String envVarsPath) {
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
        ParamsBeanV1 bean = new ParamsBeanV1();
        bean.setApiKey(apiKey.getPlainText());
        bean.setUkey(uKey.getPlainText());
        bean.setScandir(scanDir);
        bean.setWildcard(wildcard);
        bean.setInstallType(installType);
        bean.setPassword(password.getPlainText());
        bean.setUpdateDescription(updateDescription);
        bean.setChannelShortcut(channelShortcut);
        bean.setQrcodePath(qrcodePath);
        bean.setEnvVarsPath(envVarsPath);
        return PgyerV1Helper.upload(build, listener, bean);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Symbol("upload-pgyer")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
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
            return true;
        }

        public String getDisplayName() {
            return "Upload to pgyer with apiV1";
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}
