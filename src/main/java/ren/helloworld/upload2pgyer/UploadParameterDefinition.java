package ren.helloworld.upload2pgyer;

import hudson.Extension;
import hudson.model.BooleanParameterDefinition;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class UploadParameterDefinition extends BooleanParameterDefinition {

    @DataBoundConstructor
    public UploadParameterDefinition(String name, boolean defaultValue, String description) {
        super(name, defaultValue, description);
    }

    @Symbol("uploadPgyerUploadParameter")
    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Upload Pgyer Parameter - Upload";
        }
    }
}
