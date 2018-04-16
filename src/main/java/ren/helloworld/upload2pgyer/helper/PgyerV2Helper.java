package ren.helloworld.upload2pgyer.helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import ren.helloworld.upload2pgyer.apiv2.ParamsBeanV2;
import ren.helloworld.upload2pgyer.apiv2.PgyerBeanV2;
import ren.helloworld.upload2pgyer.apiv2.PgyerUploadV2;
import ren.helloworld.upload2pgyer.impl.Message;

import java.io.IOException;
import java.util.Map;

public class PgyerV2Helper {
    private static final String TAG = "[UPLOAD TO PGYER] - ";

    /**
     * @param build        build
     * @param listener     listener
     * @param paramsBeanV2 uploadBean
     * @return success or failure
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    public static boolean upload(AbstractBuild<?, ?> build, final BuildListener listener, ParamsBeanV2 paramsBeanV2) throws IOException, InterruptedException {
        Message message = new Message() {
            @Override
            public void message(boolean needTag, String mesage) {
                listener.getLogger().println((needTag ? TAG : "") + mesage);
            }
        };

        // expand params
        paramsBeanV2.setApiKey(build.getEnvironment(listener).expand(paramsBeanV2.getApiKey()));
        paramsBeanV2.setScandir(build.getEnvironment(listener).expand(paramsBeanV2.getScandir()));
        paramsBeanV2.setWildcard(build.getEnvironment(listener).expand(paramsBeanV2.getWildcard()));
        paramsBeanV2.setBuildPassword(build.getEnvironment(listener).expand(paramsBeanV2.getBuildPassword()));
        paramsBeanV2.setBuildInstallType(build.getEnvironment(listener).expand(paramsBeanV2.getBuildInstallType()));
        paramsBeanV2.setBuildUpdateDescription(build.getEnvironment(listener).expand(paramsBeanV2.getBuildUpdateDescription()));
        paramsBeanV2.setBuildName(build.getEnvironment(listener).expand(paramsBeanV2.getBuildName()));
        paramsBeanV2.setQrcodePath(build.getEnvironment(listener).expand(paramsBeanV2.getQrcodePath()));
        paramsBeanV2.setEnvVarsPath(build.getEnvironment(listener).expand(paramsBeanV2.getEnvVarsPath()));

        // check build result
        Result result = build.getResult();
        boolean unStable = result != null && result.isWorseThan(Result.UNSTABLE);
        if (unStable) {
            message.message(true, "Build was " + result.toString() + ", so the file was not uploaded.");
            return true;
        }

        // upload
        PgyerBeanV2 pgyerBeanV2 = PgyerUploadV2.upload2Pgyer(true, paramsBeanV2, message);
        if (pgyerBeanV2 == null) return false;

        // http://jenkins-ci.361315.n4.nabble.com/Setting-an-env-var-from-a-build-step-td4657347.html
        message.message(true, "now setting the envs……");
        String data = new Gson().toJson(pgyerBeanV2.getData());
        Map<String, String> maps = new Gson().fromJson(data, new TypeToken<Map<String, String>>() {
        }.getType());
        for (Map.Entry<String, String> entry : maps.entrySet()) {
            String key = entry.getKey();
            build.addAction(new PublishEnvVarAction(key, entry.getValue()));
            message.message(true, "The ${" + key + "} set up successfully! now you can use it anywhere!");

            if (key.equals("buildQRCodeURL")) {
                build.addAction(new PublishEnvVarAction("appQRCodeURL", entry.getValue()));
                message.message(true, "The ${appQRCodeURL} set up successfully! now you can use it anywhere!");
            }
        }
        message.message(true, "congratulations!\n");
        return true;
    }
}
