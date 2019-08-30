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

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * upload to jenkins
 *
 * @author myroid
 */
public class UploadPublisher extends Recorder {

    private Secret uKey;
    private Secret apiKey;
    private String scanDir;
    private String wildcard;
    private String installType;
    private Secret password;
    private String updateDescription;

    private String qrcodePath;
    private String envVarsPath;

    @DataBoundConstructor
    public UploadPublisher(String uKey, String apiKey, String scanDir, String wildcard, String installType, String password, String updateDescription, String qrcodePath, String envVarsPath) {
        this.uKey = Secret.fromString(uKey);
        this.apiKey = Secret.fromString(apiKey);
        this.scanDir = scanDir;
        this.wildcard = wildcard;
        this.installType = installType;
        this.password = Secret.fromString(password);
        this.updateDescription = updateDescription;
        this.qrcodePath = qrcodePath;
        this.envVarsPath = envVarsPath;
    }

    public String getuKey() {
        return uKey.getEncryptedValue();
    }

    public String getApiKey() {
        return apiKey.getEncryptedValue();
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

    public String getPassword() {
        return password.getEncryptedValue();
    }

    public String getUpdateDescription() {
        return updateDescription;
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
        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckUKey(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a uKey");
            if (!value.matches("[A-Za-z0-9]{32}"))
                return FormValidation.warning("Is this correct?");
            return FormValidation.ok();
        }

        public FormValidation doCheckApiKey(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a api_key");
            if (!value.matches("[A-Za-z0-9]{32}"))
                return FormValidation.warning("Is this correct?");
            return FormValidation.ok();
        }

        public FormValidation doCheckScanDir(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set upload ipa or apk file base dir name");
            return FormValidation.ok();
        }

        public FormValidation doCheckWildcard(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set upload ipa or apk file wildcard");
            return FormValidation.ok();
        }

        public FormValidation doCheckInstallType(@QueryParameter int value)
                throws IOException, ServletException {
            if (value < 1 || value > 3)
                return FormValidation.error("application installation, the value is (1,2,3).");
            return FormValidation.ok();
        }

        public FormValidation doCheckPassword(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a password");
            if (value.length() < 6)
                return FormValidation.warning("Isn't the password too short?");
            return FormValidation.ok();
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
