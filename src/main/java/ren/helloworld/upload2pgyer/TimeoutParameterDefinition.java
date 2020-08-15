package ren.helloworld.upload2pgyer;

import hudson.Extension;
import hudson.model.StringParameterDefinition;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class TimeoutParameterDefinition extends StringParameterDefinition {

    @DataBoundConstructor
    public TimeoutParameterDefinition(String name, String defaultValue, String description) {
        super(name, defaultValue, description);
    }

    @Symbol("uploadPgyerTimeoutParameter")
    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Upload Pgyer Parameter - Timeout";
        }
    }
}
