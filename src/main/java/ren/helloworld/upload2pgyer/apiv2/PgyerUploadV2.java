package ren.helloworld.upload2pgyer.apiv2;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hudson.EnvVars;
import okhttp3.*;
import ren.helloworld.upload2pgyer.helper.CommonUtil;
import ren.helloworld.upload2pgyer.helper.ProgressRequestBody;
import ren.helloworld.upload2pgyer.impl.Message;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PgyerUploadV2 {
    private static final String UPLOAD_URL = CommonUtil.PGYER_HOST + "/apiv2/app/getCOSToken";
    public static void main(String[] args) {

        Message listener = new Message() {
            @Override
            public void message(boolean needTag, String mesage) {
                System.out.println((needTag ? CommonUtil.LOG_PREFIX : "") + mesage);
            }
        };

        CommonUtil.printHeaderInfo(listener);
        ParamsBeanV2 paramsBeanV2 = parseArgs(args, listener);
        if (paramsBeanV2 == null) {
            return;
        }
        upload2Pgyer(null, false, paramsBeanV2, listener);
    }

    /**
     * parse args
     *
     * @param args
     * @param listener
     * @return
     */
    private static ParamsBeanV2 parseArgs(String[] args, Message listener) {
        // check args length
        int length = args.length;

        if (length == 0 || length % 2 != 0) {
            CommonUtil.printMessage(listener, true, "args length is error!\n");
            return null;
        }

        // args to map
        Map<String, String> maps = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            maps.put(args[i], args[i + 1]);
        }

        // check apiKey
        if (!maps.containsKey("-apiKey")) {
            CommonUtil.printMessage(listener, true, "apiKey not found!\n");
            return null;
        }
        // check scanDir
        if (!maps.containsKey("-scanDir")) {
            CommonUtil.printMessage(listener, true, "scanDir not found!\n");
            return null;
        }
        // check wildcard
        if (!maps.containsKey("-wildcard")) {
            CommonUtil.printMessage(listener, true, "wildcard not found!\n");
            return null;
        }
        if (!maps.containsKey("-buildType")) {
            CommonUtil.printMessage(listener, true, "buildType not found!\n");
            return null;
        }

        // params to uploadBean
        ParamsBeanV2 paramsBeanV2 = new ParamsBeanV2();
        paramsBeanV2.setApiKey(maps.get("-apiKey"));
        paramsBeanV2.setScandir(maps.get("-scanDir"));
        paramsBeanV2.setWildcard(maps.get("-wildcard"));
        paramsBeanV2.setBuildType(maps.containsKey("-buildType") ? maps.get("-buildType") : "");
        paramsBeanV2.setQrcodePath(maps.containsKey("-qrcodePath") ? maps.get("-qrcodePath") : null);
        paramsBeanV2.setEnvVarsPath(maps.containsKey("-envVarsPath") ? maps.get("-envVarsPath") : null);
        paramsBeanV2.setBuildPassword(maps.containsKey("-buildPassword") ? maps.get("-buildPassword") : "");
        paramsBeanV2.setBuildInstallType(maps.containsKey("-buildInstallType") ? maps.get("-buildInstallType") : "1");
        paramsBeanV2.setBuildUpdateDescription(maps.containsKey("-buildUpdateDescription") ? maps.get("-buildUpdateDescription") : "");
        paramsBeanV2.setBuildChannelShortcut(maps.containsKey("-buildChannelShortcut") ? maps.get("-buildChannelShortcut") : "");
        return paramsBeanV2;
    }

    /**
     * upload 2 pgyer
     *
     * @param envVars      envVars
     * @param printHeader  printHeader
     * @param paramsBeanV2 uploadBean
     * @param listener     listener
     * @return pgyer bean
     */
    public static PgyerBeanV2 upload2Pgyer(EnvVars envVars, boolean printHeader, ParamsBeanV2 paramsBeanV2, Message listener) {
        // print header info
        if (printHeader) {
            CommonUtil.printHeaderInfo(listener);
        }

        // find upload file
        paramsBeanV2.setUploadFile(CommonUtil.findFile(paramsBeanV2.getScandir(), paramsBeanV2.getWildcard(), listener));

        // check upload file
        if (paramsBeanV2.getUploadFile() == null) {
            CommonUtil.printMessage(listener, true, "The uploaded file was not found，plase check scandir or wildcard!\n");
            return null;
        }

        File uploadFile = new File(paramsBeanV2.getUploadFile());
        if (!uploadFile.exists() || !uploadFile.isFile()) {
            CommonUtil.printMessage(listener, true, "The uploaded file was not found，plase check scandir or wildcard!\n");
            return null;
        }

        String result = "";
        try {
            CommonUtil.printMessage(listener, true, "upload：getToken to " + UPLOAD_URL);
//            CommonUtil.printMessage(listener, true, "upload file size: " + CommonUtil.convertFileSize(uploadFile.length()));

            // optimization upload description
            if (CommonUtil.isBlank(paramsBeanV2.getBuildUpdateDescription())
                    || "${SCM_CHANGELOG}".equals(paramsBeanV2.getBuildUpdateDescription())) {
                paramsBeanV2.setBuildUpdateDescription("");
            }

//            MediaType type = MediaType.parse("application/octet-stream");
//            RequestBody fileBody = RequestBody.create(type, uploadFile);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MediaType.parse("multipart/form-data"))
                    .addFormDataPart("_api_key", paramsBeanV2.getApiKey())
                    .addFormDataPart("buildInstallType", paramsBeanV2.getBuildInstallType())
                    .addFormDataPart("buildPassword", paramsBeanV2.getBuildPassword())
                    .addFormDataPart("buildUpdateDescription", paramsBeanV2.getBuildUpdateDescription())
                    .addFormDataPart("buildChannelShortcut", paramsBeanV2.getBuildChannelShortcut())
                    .addFormDataPart("buildType", paramsBeanV2.getBuildType())
                    .build();
            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(new ProgressRequestBody(requestBody, null))
                    .build();
            int timeout = CommonUtil.getUploadTimeout(envVars);
            Response execute = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(true)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .build()
                    .newCall(request).execute();

            if (execute.body() == null) {
                CommonUtil.printMessage(listener, true, "Upload failed with pgyer api v2!");
                CommonUtil.printMessage(listener, true, "upload token result is null.");
                return null;
            }
            result = execute.body().string();
            if (result != null && result.contains("\"data\":[]")) {
                result = result.replace("\"data\":[]", "\"data\":{}");
            }

            PgyerTokenBeanV2 tokenBean = null;
            try {
                tokenBean = new Gson().fromJson(result, new TypeToken<PgyerTokenBeanV2>() {
                }.getType());

            } catch (Exception e) {
                e.printStackTrace();
                CommonUtil.printMessage(listener, true, e.getMessage());
                return null;
            }
            if (tokenBean.getCode() != 0) {
                CommonUtil.printMessage(listener, true, "Upload failed with pgyer api v2!");
                CommonUtil.printMessage(listener, true, "error code：" + tokenBean.getCode());
                CommonUtil.printMessage(listener, true, "error message：" + tokenBean.getMessage() + "\n");
                return null;
            }
            return upload2PgyerFile(envVars,paramsBeanV2,tokenBean,listener);
        } catch (IOException e) {
            listener.message(true, "pgyer result: " + result);
            listener.message(true, "ERROR: " + e.getMessage() + "\n");
            return null;
        }
    }

    /**
     * upload 2 pgyer
     * @param envVars      envVars
     * @param paramsBeanV2 uploadBean
     * @param listener     listener
     * @return pgyer bean
     */
    public static PgyerBeanV2 upload2PgyerFile(EnvVars envVars,  ParamsBeanV2 paramsBeanV2, PgyerTokenBeanV2 tokenBean, Message listener) {

        // find upload file
        paramsBeanV2.setUploadFile(CommonUtil.findFile(paramsBeanV2.getScandir(), paramsBeanV2.getWildcard(), listener));

        // check upload file
        if (paramsBeanV2.getUploadFile() == null) {
            CommonUtil.printMessage(listener, true, "The uploaded file was not found，plase check scandir or wildcard!\n");
            return null;
        }

        File uploadFile = new File(paramsBeanV2.getUploadFile());
        if (!uploadFile.exists() || !uploadFile.isFile()) {
            CommonUtil.printMessage(listener, true, "The uploaded file was not found，plase check scandir or wildcard!\n");
            return null;
        }

        String result = "";
        try {
            CommonUtil.printMessage(listener, true, "upload：" + uploadFile.getName() + " to Pgyer");
            CommonUtil.printMessage(listener, true, "upload file size: " + CommonUtil.convertFileSize(uploadFile.length()));

            MediaType type = MediaType.parse("application/octet-stream");
            RequestBody fileBody = RequestBody.create(type, uploadFile);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MediaType.parse("multipart/form-data"))
                    .addFormDataPart("key", tokenBean.getData().getKey())
                    .addFormDataPart("signature", tokenBean.getData().getParams().getSignature())
                    .addFormDataPart("x-cos-security-token", tokenBean.getData().getParams().getX_cos_security_token())
                    .addFormDataPart("file", uploadFile.getName(), fileBody)
                    .build();
            Request request = new Request.Builder()
                    .url(tokenBean.getData().getEndpoint())
                    .post(new ProgressRequestBody(requestBody, new CommonUtil.FileUploadProgressListener(listener)))
                    .build();
            int timeout = CommonUtil.getUploadTimeout(envVars);
            Response execute = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(true)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .build()
                    .newCall(request).execute();

            if (execute.body() == null) {
                CommonUtil.printMessage(listener, true, "Upload file failed with oss");
                CommonUtil.printMessage(listener, true, "upload file result is null.");
                return null;
            }
            if(execute.code() == 204){
                String url = "https://www.pgyer.com/apiv2/app/buildInfo?_api_key="+paramsBeanV2.getApiKey()+"&buildKey="+tokenBean.getData().getKey();
                times = 0;
                return uploadResult(url,paramsBeanV2,listener);
            } else {
                CommonUtil.printMessage(listener, true, "Upload failed with pgyer api v2!");
                return null;
            }
        } catch (IOException e) {
            listener.message(true, "pgyer result: " + result);
            listener.message(true, "ERROR: " + e.getMessage() + "\n");
            return null;
        }
    }

    static boolean bGo = true;
    static Timer timers = null;
    static int delay = 5000;
    static int times = 0;
    /**
     * Obtain the result of PGYER synchronizing data upload（获取pgyer 同步上传数据结果）
     * @param url
     * @param paramsBeanV2
     * @param listener
     * @return
     */
    public static PgyerBeanV2 uploadResult(String url, ParamsBeanV2 paramsBeanV2, Message listener){
        String result = "";
        CommonUtil.printMessage(listener, true, "upload：Wait for the PGYER synchronization result");
        try {
            //同步数据需要3~5秒延迟4秒获取最终同步数据
            timers = null;
            bGo = true;
            timers = new Timer(delay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(timers != null){
                        bGo = false;
                        timers.stop();
                        timers = null;

                    }
                }
            });
            timers.start();
            int i=0;
            while (bGo){
                i++;
                if(i % 2000000000 == 0){
                    CommonUtil.printMessage(listener, true, "upload：Pgyer is synchronizing data……");
                }
            }
            Request request = new Request.Builder().url(url).get().build();
            Response execute = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(true)
                    .readTimeout(300, TimeUnit.SECONDS)
                    .writeTimeout(300, TimeUnit.SECONDS)
                    .connectTimeout(300, TimeUnit.SECONDS)
                    .build()
                    .newCall(request).execute();

            if (execute.body() == null) {
                CommonUtil.printMessage(listener, true, "Upload file result failed with uploadResult");
                CommonUtil.printMessage(listener, true, "upload file result is null.");
                return null;
            }
            result = execute.body().string();
            if (result != null && result.contains("\"data\":[]")) {
                result = result.replace("\"data\":[]", "\"data\":{}");
            }

            PgyerBeanV2 pgyerBeanV2 = null;
            try {
                pgyerBeanV2 = new Gson().fromJson(result, new TypeToken<PgyerBeanV2>() {
                }.getType());
            } catch (Exception e) {
                e.printStackTrace();
                CommonUtil.printMessage(listener, true, e.getMessage());
                return null;
            }

            if (pgyerBeanV2.getCode() != 0) {
                if(pgyerBeanV2.getCode() == 1246 || pgyerBeanV2.getCode() == 1247){
                    if(times < 5) {
                        times++;
                        CommonUtil.printMessage(listener, true, "upload：Pgyer has not synchronized the results");
                        bGo = true;
                        delay = 2000;
                        return uploadResult(url, paramsBeanV2, listener);
                    }
                } else {
                    CommonUtil.printMessage(listener, true, "Upload failed with pgyer api v2!");
                    CommonUtil.printMessage(listener, true, "error code：" + pgyerBeanV2.getCode());
                    CommonUtil.printMessage(listener, true, "error message：" + pgyerBeanV2.getMessage() + "\n");
                    return null;
                }
            }

            pgyerBeanV2.getData().setAppPgyerURL(CommonUtil.PGYER_HOST + "/" + pgyerBeanV2.getData().getBuildShortcutUrl());
            pgyerBeanV2.getData().setAppBuildURL(CommonUtil.PGYER_HOST + "/" + pgyerBeanV2.getData().getBuildKey());
            pgyerBeanV2.getData().setBuildIcon(CommonUtil.PGYER_HOST + "/image/view/app_icons/" + pgyerBeanV2.getData().getBuildIcon());

            CommonUtil.printMessage(listener, true, "Uploaded successfully!\n");
            printResultInfo(pgyerBeanV2, listener);
            writeEnvVars(paramsBeanV2, pgyerBeanV2, listener);
            downloadQrcode(paramsBeanV2, pgyerBeanV2, listener);
            return pgyerBeanV2;
        } catch (IOException e) {
            e.printStackTrace();
            listener.message(true, "pgyer result: " + result);
            listener.message(true, "ERROR: " + e.getMessage() + "\n");
            return null;
        }
    }

    /**
     * Download the qr code
     *
     * @param paramsBeanV2 paramsBeanV2
     * @param pgyerBeanV2  pgyerBeanV2
     * @param listener     listener
     */
    private static void downloadQrcode(ParamsBeanV2 paramsBeanV2, PgyerBeanV2 pgyerBeanV2, Message listener) {
        if (paramsBeanV2.getQrcodePath() == null) {
            return;
        }
        if (CommonUtil.replaceBlank(paramsBeanV2.getQrcodePath()).length() == 0) {
            return;
        }
        CommonUtil.printMessage(listener, true, "Downloading the qr code……");
        File qrcode = new File(paramsBeanV2.getQrcodePath());
        if (!qrcode.getParentFile().exists() && !qrcode.getParentFile().mkdirs()) {
            CommonUtil.printMessage(listener, true, "Oh, my god, download the qr code failed……" + "\n");
            return;
        }
        File file = CommonUtil.download(pgyerBeanV2.getData().getBuildQRCodeURL(), qrcode.getParentFile().getAbsolutePath(), qrcode.getName());
        if (file != null) {
            CommonUtil.printMessage(listener, true, "Download the qr code successfully! " + file + "\n");
        } else {
            CommonUtil.printMessage(listener, true, "Oh, my god, download the qr code failed……" + "\n");
        }
    }

    /**
     * Writing the environment variable to the file.
     *
     * @param paramsBeanV2 paramsBeanV2
     * @param pgyerBeanV2  pgyerBeanV2
     * @param listener     listener
     */
    private static void writeEnvVars(ParamsBeanV2 paramsBeanV2, PgyerBeanV2 pgyerBeanV2, Message listener) {
        if (paramsBeanV2.getEnvVarsPath() == null) {
            return;
        }
        if (CommonUtil.replaceBlank(paramsBeanV2.getEnvVarsPath()).length() == 0) {
            return;
        }
        CommonUtil.printMessage(listener, true, "Writing the environment variable to the file……");
        File envVars = new File(paramsBeanV2.getEnvVarsPath());
        if (!envVars.getParentFile().exists() && !envVars.getParentFile().mkdirs()) {
            CommonUtil.printMessage(listener, true, "Oh my god, the environment variable writes failed……" + "\n");
            return;
        }
        File file = CommonUtil.write(envVars.getAbsolutePath(), getEnvVarsInfo(pgyerBeanV2), "utf-8");
        if (file != null) {
            CommonUtil.printMessage(listener, true, "The environment variable is written successfully! " + file + "\n");
        } else {
            CommonUtil.printMessage(listener, true, "Oh my god, the environment variable writes failed……" + "\n");
        }
    }

    /**
     * Print return log
     *
     * @param pgyerBeanV2 pgyerBeanV2
     * @param listener    listener
     */
    private static void printResultInfo(PgyerBeanV2 pgyerBeanV2, Message listener) {
        PgyerBeanV2.DataBean data = pgyerBeanV2.getData();
        CommonUtil.printMessage(listener, true, "应用名称：" + data.getBuildName());
        CommonUtil.printMessage(listener, true, "应用类型：" + data.getBuildType());
        CommonUtil.printMessage(listener, true, "版本号：" + data.getBuildVersion());
        CommonUtil.printMessage(listener, true, "build号：" + data.getBuildBuildVersion());
        CommonUtil.printMessage(listener, true, "Build Key：" + data.getBuildKey());
        CommonUtil.printMessage(listener, true, "版本编号：" + data.getBuildVersionNo());
        CommonUtil.printMessage(listener, true, "文件大小：" + data.getBuildFileSize());
        CommonUtil.printMessage(listener, true, "应用介绍：" + data.getBuildDescription());
        CommonUtil.printMessage(listener, true, "应用主页：" + data.getAppPgyerURL());
        CommonUtil.printMessage(listener, true, "应用短链接：" + data.getBuildShortcutUrl());
        CommonUtil.printMessage(listener, true, "应用上传时间：" + data.getBuildCreated());
        CommonUtil.printMessage(listener, true, "应用更新时间：" + data.getBuildUpdated());
        CommonUtil.printMessage(listener, true, "应用构建主页：" + data.getAppBuildURL());
        CommonUtil.printMessage(listener, true, "应用更新说明：" + data.getBuildUpdateDescription());
        CommonUtil.printMessage(listener, true, "是否是最新版：" + data.getBuildIsLastest());
        CommonUtil.printMessage(listener, true, "应用程序包名：" + data.getBuildIdentifier());
        CommonUtil.printMessage(listener, true, "应用截图的key：" + data.getBuildScreenshots());
        CommonUtil.printMessage(listener, true, "应用二维码地址：" + data.getBuildQRCodeURL());
        CommonUtil.printMessage(listener, true, "是否是第一个App：" + data.getBuildType());
        CommonUtil.printMessage(listener, true, "应用的Icon图标key：" + data.getBuildIcon());
        CommonUtil.printMessage(listener, false, "");
    }

    /**
     * Format the return information.
     *
     * @param pgyerBeanV2 pgyerBeanV2
     * @return Formatted log
     */
    private static String getEnvVarsInfo(PgyerBeanV2 pgyerBeanV2) {
        StringBuffer sb = new StringBuffer();
        sb.append("buildKey").append("=").append(pgyerBeanV2.getData().getBuildKey()).append("\n");
        sb.append("buildName").append("=").append(pgyerBeanV2.getData().getBuildName()).append("\n");
        sb.append("buildIcon").append("=").append(pgyerBeanV2.getData().getBuildIcon()).append("\n");
        sb.append("buildType").append("=").append(pgyerBeanV2.getData().getBuildType()).append("\n");
        sb.append("appBuildURL").append("=").append(pgyerBeanV2.getData().getAppBuildURL()).append("\n");
        sb.append("appPgyerURL").append("=").append(pgyerBeanV2.getData().getAppPgyerURL()).append("\n");
        sb.append("buildCreated").append("=").append(pgyerBeanV2.getData().getBuildCreated()).append("\n");
        sb.append("buildIsFirst").append("=").append(pgyerBeanV2.getData().getBuildIsFirst()).append("\n");
        sb.append("buildUpdated").append("=").append(pgyerBeanV2.getData().getBuildUpdated()).append("\n");
        sb.append("buildVersion").append("=").append(pgyerBeanV2.getData().getBuildVersion()).append("\n");
        sb.append("buildFileName").append("=").append(pgyerBeanV2.getData().getBuildFileName()).append("\n");
        sb.append("buildFileSize").append("=").append(pgyerBeanV2.getData().getBuildFileSize()).append("\n");
        sb.append("buildIsLastest").append("=").append(pgyerBeanV2.getData().getBuildIsLastest()).append("\n");
        sb.append("buildQRCodeURL").append("=").append(pgyerBeanV2.getData().getBuildQRCodeURL()).append("\n");
        sb.append("buildVersionNo").append("=").append(pgyerBeanV2.getData().getBuildVersionNo()).append("\n");
        sb.append("buildIdentifier").append("=").append(pgyerBeanV2.getData().getBuildIdentifier()).append("\n");
        sb.append("buildDescription").append("=").append(pgyerBeanV2.getData().getBuildDescription()).append("\n");
        sb.append("buildScreenshots").append("=").append(pgyerBeanV2.getData().getBuildScreenshots()).append("\n");
        sb.append("buildShortcutUrl").append("=").append(pgyerBeanV2.getData().getBuildShortcutUrl()).append("\n");
        sb.append("buildBuildVersion").append("=").append(pgyerBeanV2.getData().getBuildBuildVersion()).append("\n");
        sb.append("buildUpdateDescription").append("=").append(pgyerBeanV2.getData().getBuildUpdateDescription()).append("\n");
        return sb.toString();
    }
}
