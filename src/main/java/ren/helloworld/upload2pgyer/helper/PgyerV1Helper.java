package ren.helloworld.upload2pgyer.helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import ren.helloworld.upload2pgyer.apiv1.ParamsBeanV1;
import ren.helloworld.upload2pgyer.apiv1.PgyerBeanV1;
import ren.helloworld.upload2pgyer.apiv1.PgyerUploadV1;
import ren.helloworld.upload2pgyer.impl.Message;

import java.io.IOException;
import java.util.Map;

public class PgyerV1Helper {
    private static final String TAG = "[UPLOAD TO PGYER] - ";

    /**
     * @param build        build
     * @param listener     listener
     * @param paramsBeanV1 uploadBean
     * @return success or failure
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    public static boolean upload(AbstractBuild<?, ?> build, final BuildListener listener, ParamsBeanV1 paramsBeanV1) throws IOException, InterruptedException {
        Message message = new Message() {
            @Override
            public void message(boolean needTag, String mesage) {
                listener.getLogger().println((needTag ? TAG : "") + mesage);
            }
        };

        // expand params
        paramsBeanV1.setUkey(build.getEnvironment(listener).expand(paramsBeanV1.getUkey()));
        paramsBeanV1.setApiKey(build.getEnvironment(listener).expand(paramsBeanV1.getApiKey()));
        paramsBeanV1.setScandir(build.getEnvironment(listener).expand(paramsBeanV1.getScandir()));
        paramsBeanV1.setWildcard(build.getEnvironment(listener).expand(paramsBeanV1.getWildcard()));
        paramsBeanV1.setInstallType(build.getEnvironment(listener).expand(paramsBeanV1.getInstallType()));
        paramsBeanV1.setPassword(build.getEnvironment(listener).expand(paramsBeanV1.getPassword()));
        paramsBeanV1.setUpdateDescription(build.getEnvironment(listener).expand(paramsBeanV1.getUpdateDescription()));
        paramsBeanV1.setQrcodePath(build.getEnvironment(listener).expand(paramsBeanV1.getQrcodePath()));
        paramsBeanV1.setEnvVarsPath(build.getEnvironment(listener).expand(paramsBeanV1.getEnvVarsPath()));

        // check build result
        Result result = build.getResult();
        boolean unStable = result != null && result.isWorseThan(Result.UNSTABLE);
        if (unStable) {
            message.message(true, "Build was " + result.toString() + ", so the file was not uploaded.");
            return true;
        }

        // upload
        PgyerBeanV1 pgyerBeanV1 = PgyerUploadV1.upload2Pgyer(true, paramsBeanV1, message);
        if (pgyerBeanV1 == null) return false;

        // http://jenkins-ci.361315.n4.nabble.com/Setting-an-env-var-from-a-build-step-td4657347.html
        message.message(true, "now setting the envs……");
        String data = new Gson().toJson(pgyerBeanV1.getData());
        Map<String, String> maps = new Gson().fromJson(data, new TypeToken<Map<String, String>>() {
        }.getType());
        for (Map.Entry<String, String> entry : maps.entrySet()) {
            String key = entry.getKey();
            if (key.equals("userKey")) continue;
            build.addAction(new PublishEnvVarAction(key, entry.getValue()));
            message.message(true, "The ${" + key + "} set up successfully! now you can use it anywhere!");
        }
        message.message(true, "congratulations!\n");
        return true;
    }
}
