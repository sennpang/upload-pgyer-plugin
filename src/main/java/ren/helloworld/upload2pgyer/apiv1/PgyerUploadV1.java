package ren.helloworld.upload2pgyer.apiv1;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import ren.helloworld.upload2pgyer.helper.CommonUtil;
import ren.helloworld.upload2pgyer.helper.ProgressRequestBody;
import ren.helloworld.upload2pgyer.impl.Message;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dafan on 2017/5/4 0004.
 */
public class PgyerUploadV1 {
    private static final String TAG = "[UPLOAD TO PGYER] - ";
    private static final String UPLOAD_URL = "https://qiniu-storage.pgyer.com/apiv1/app/upload";

    public static void main(String[] args) {

        Message listener = new Message() {
            @Override
            public void message(boolean needTag, String mesage) {
                System.out.println((needTag ? TAG : "") + mesage);
            }
        };

        CommonUtil.printHeaderInfo(listener);
        ParamsBeanV1 paramsBeanV1 = parseArgs(args, listener);
        if (paramsBeanV1 == null) return;
        upload2Pgyer(false, paramsBeanV1, listener);
    }

    /**
     * parse args
     *
     * @param args
     * @param listener
     * @return
     */
    private static ParamsBeanV1 parseArgs(String[] args, Message listener) {
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

        // check uKey
        if (!maps.containsKey("-uKey")) {
            CommonUtil.printMessage(listener, true, "uKey not found!\n");
            return null;
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

        // params to uploadBean
        ParamsBeanV1 paramsBeanV1 = new ParamsBeanV1();
        paramsBeanV1.setUkey(maps.get("-uKey"));
        paramsBeanV1.setApiKey(maps.get("-apiKey"));
        paramsBeanV1.setScandir(maps.get("-scanDir"));
        paramsBeanV1.setWildcard(maps.get("-wildcard"));
        paramsBeanV1.setPassword(maps.containsKey("-password") ? maps.get("-password") : "");
        paramsBeanV1.setQrcodePath(maps.containsKey("-qrcodePath") ? maps.get("-qrcodePath") : null);
        paramsBeanV1.setEnvVarsPath(maps.containsKey("-envVarsPath") ? maps.get("-envVarsPath") : null);
        paramsBeanV1.setInstallType(maps.containsKey("-installType") ? maps.get("-installType") : "1");
        paramsBeanV1.setUpdateDescription(maps.containsKey("-updateDescription") ? maps.get("-updateDescription") : "");
        return paramsBeanV1;
    }

    /**
     * upload 2 pgyer
     *
     * @param printHeader  printHeader
     * @param paramsBeanV1 uploadBean
     * @param listener     listener
     * @return pgyer bean
     */
    public static PgyerBeanV1 upload2Pgyer(final boolean printHeader, ParamsBeanV1 paramsBeanV1, final Message listener) {
        // print header info
        if (printHeader) CommonUtil.printHeaderInfo(listener);

        // find upload file
        paramsBeanV1.setUploadFile(CommonUtil.findFile(paramsBeanV1.getScandir(), paramsBeanV1.getWildcard(), listener));

        // check upload file
        if (paramsBeanV1.getUploadFile() == null) {
            CommonUtil.printMessage(listener, true, "The uploaded file was not found，plase check scandir or wildcard!\n");
            return null;
        }

        File uploadFile = new File(paramsBeanV1.getUploadFile());
        if (!uploadFile.exists() || !uploadFile.isFile()) {
            CommonUtil.printMessage(listener, true, "The uploaded file was not found，plase check scandir or wildcard!\n");
            return null;
        }

        String result = "";
        try {
            CommonUtil.printMessage(listener, true, "upload：" + uploadFile.getName() + " to " + UPLOAD_URL);
            CommonUtil.printMessage(listener, true, "upload file size: " + CommonUtil.convertFileSize(uploadFile.length()));

            // optimization upload description
            if (CommonUtil.isBlank(paramsBeanV1.getUpdateDescription())
                    || "${SCM_CHANGELOG}".equals(paramsBeanV1.getUpdateDescription())) {
                paramsBeanV1.setUpdateDescription("");
            }

            MediaType type = MediaType.parse("application/octet-stream");
            RequestBody fileBody = RequestBody.create(type, uploadFile);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MediaType.parse("multipart/form-data"))
                    .addFormDataPart("uKey", paramsBeanV1.getUkey())
                    .addFormDataPart("_api_key", paramsBeanV1.getApiKey())
                    .addFormDataPart("installType", paramsBeanV1.getInstallType())
                    .addFormDataPart("password", paramsBeanV1.getPassword())
                    .addFormDataPart("updateDescription", paramsBeanV1.getUpdateDescription())
                    .addFormDataPart("file", uploadFile.getName(), fileBody)
                    .build();
            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(new ProgressRequestBody(requestBody, new CommonUtil.FileUploadProgressListener(listener)))
                    .build();
            Response execute = new OkHttpClient().newCall(request).execute();
            result = execute.body().string();

            if (result != null && result.contains("\"data\":[]")) {
                result = result.replace("\"data\":[]", "\"data\":{}");
            }

            PgyerBeanV1 pgyerBeanV1 = new Gson().fromJson(result, new TypeToken<PgyerBeanV1>() {
            }.getType());

            if (pgyerBeanV1.getCode() != 0) {
                CommonUtil.printMessage(listener, true, "Upload failed!");
                CommonUtil.printMessage(listener, true, "error code：" + pgyerBeanV1.getCode());
                CommonUtil.printMessage(listener, true, "error message：" + pgyerBeanV1.getMessage() + "\n");
                return null;
            }

            pgyerBeanV1.getData().setAppPgyerURL("https://www.pgyer.com/" + pgyerBeanV1.getData().getAppShortcutUrl());
            pgyerBeanV1.getData().setAppBuildURL("https://www.pgyer.com/" + pgyerBeanV1.getData().getAppKey());
            pgyerBeanV1.getData().setAppIcon("https://www.pgyer.com/image/view/app_icons/" + pgyerBeanV1.getData().getAppIcon());

            CommonUtil.printMessage(listener, true, "Uploaded successfully!\n");
            printResultInfo(pgyerBeanV1, listener);
            writeEnvVars(paramsBeanV1, pgyerBeanV1, listener);
            downloadQrcode(paramsBeanV1, pgyerBeanV1, listener);

            return pgyerBeanV1;
        } catch (IOException e) {
            listener.message(true, "pgyer result: " + result);
            listener.message(true, "ERROR: " + e.getMessage() + "\n");
            return null;
        }
    }

    /**
     * Download the qr code.
     *
     * @param paramsBeanV1 paramsBeanV1
     * @param pgyerBeanV1  pgyerBeanV1
     * @param listener     listener
     */
    private static void downloadQrcode(ParamsBeanV1 paramsBeanV1, PgyerBeanV1 pgyerBeanV1, Message listener) {
        if (paramsBeanV1.getQrcodePath() == null) return;
        if (CommonUtil.replaceBlank(paramsBeanV1.getQrcodePath()).length() == 0) return;
        CommonUtil.printMessage(listener, true, "Downloading the qr code……");
        File qrcode = new File(paramsBeanV1.getQrcodePath());
        if (!qrcode.getParentFile().exists() && !qrcode.getParentFile().mkdirs()) {
            CommonUtil.printMessage(listener, true, "Oh, my god, download the qr code failed……" + "\n");
            return;
        }
        File file = CommonUtil.download(pgyerBeanV1.getData().getAppQRCodeURL(), qrcode.getParentFile().getAbsolutePath(), qrcode.getName());
        if (file != null) CommonUtil.printMessage(listener, true, "Download the qr code successfully! " + file + "\n");
        else CommonUtil.printMessage(listener, true, "Oh, my god, download the qr code failed……" + "\n");
    }

    /**
     * Writing the environment variable to the file.
     *
     * @param paramsBeanV1 paramsBeanV1
     * @param pgyerBeanV1  pgyerBeanV1
     * @param listener     listener
     */
    private static void writeEnvVars(ParamsBeanV1 paramsBeanV1, PgyerBeanV1 pgyerBeanV1, Message listener) {
        if (paramsBeanV1.getEnvVarsPath() == null) return;
        if (CommonUtil.replaceBlank(paramsBeanV1.getEnvVarsPath()).length() == 0) return;
        CommonUtil.printMessage(listener, true, "Writing the environment variable to the file……");
        File envVars = new File(paramsBeanV1.getEnvVarsPath());
        if (!envVars.getParentFile().exists() && !envVars.getParentFile().mkdirs()) {
            CommonUtil.printMessage(listener, true, "Oh my god, the environment variable writes failed……" + "\n");
            return;
        }
        File file = CommonUtil.write(envVars.getAbsolutePath(), getEnvVarsInfo(pgyerBeanV1), "utf-8");
        if (file != null)
            CommonUtil.printMessage(listener, true, "The environment variable is written successfully! " + file + "\n");
        else
            CommonUtil.printMessage(listener, true, "Oh my god, the environment variable writes failed……" + "\n");
    }

    /**
     * Print return log
     *
     * @param pgyerBeanV1 pgyerBeanV1
     * @param listener    listener
     */
    private static void printResultInfo(PgyerBeanV1 pgyerBeanV1, Message listener) {
        PgyerBeanV1.DataBean data = pgyerBeanV1.getData();
        CommonUtil.printMessage(listener, true, "App Key：" + data.getAppKey());
        CommonUtil.printMessage(listener, true, "应用类型：" + data.getAppType());
        CommonUtil.printMessage(listener, true, "是否是最新版：" + data.getAppIsLastest());
        CommonUtil.printMessage(listener, true, "文件大小：" + data.getAppFileSize());
        CommonUtil.printMessage(listener, true, "应用名称：" + data.getAppName());
        CommonUtil.printMessage(listener, true, "版本号：" + data.getAppVersion());
        CommonUtil.printMessage(listener, true, "版本编号：" + data.getAppVersionNo());
        CommonUtil.printMessage(listener, true, "build号：" + data.getAppBuildVersion());
        CommonUtil.printMessage(listener, true, "应用程序包名：" + data.getAppIdentifier());
        CommonUtil.printMessage(listener, true, "应用的Icon图标key：" + data.getAppIcon());
        CommonUtil.printMessage(listener, true, "应用介绍：" + data.getAppDescription());
        CommonUtil.printMessage(listener, true, "应用更新说明：" + data.getAppUpdateDescription());
        CommonUtil.printMessage(listener, true, "应用截图的key：" + data.getAppScreenshots());
        CommonUtil.printMessage(listener, true, "应用短链接：" + data.getAppShortcutUrl());
        CommonUtil.printMessage(listener, true, "应用二维码地址：" + data.getAppQRCodeURL());
        CommonUtil.printMessage(listener, true, "应用上传时间：" + data.getAppCreated());
        CommonUtil.printMessage(listener, true, "应用更新时间：" + data.getAppUpdated());
        CommonUtil.printMessage(listener, true, "应用主页：" + data.getAppPgyerURL());
        CommonUtil.printMessage(listener, true, "应用构建主页：" + data.getAppBuildURL());
        CommonUtil.printMessage(listener, false, "");
    }

    /**
     * Format the return information.
     *
     * @param pgyerBeanV1 pgyerBeanV1
     * @return Formatted log
     */
    private static String getEnvVarsInfo(PgyerBeanV1 pgyerBeanV1) {
        StringBuffer sb = new StringBuffer();
        sb.append("appKey").append("=").append(pgyerBeanV1.getData().getAppKey()).append("\n");
        sb.append("appType").append("=").append(pgyerBeanV1.getData().getAppType()).append("\n");
        sb.append("appIsLastest").append("=").append(pgyerBeanV1.getData().getAppIsLastest()).append("\n");
        sb.append("appFileSize").append("=").append(pgyerBeanV1.getData().getAppFileSize()).append("\n");
        sb.append("appName").append("=").append(pgyerBeanV1.getData().getAppName()).append("\n");
        sb.append("appVersion").append("=").append(pgyerBeanV1.getData().getAppVersion()).append("\n");
        sb.append("appVersionNo").append("=").append(pgyerBeanV1.getData().getAppVersionNo()).append("\n");
        sb.append("appBuildVersion").append("=").append(pgyerBeanV1.getData().getAppBuildVersion()).append("\n");
        sb.append("appIdentifier").append("=").append(pgyerBeanV1.getData().getAppIdentifier()).append("\n");
        sb.append("appIcon").append("=").append(pgyerBeanV1.getData().getAppIcon()).append("\n");
        sb.append("appDescription").append("=").append(pgyerBeanV1.getData().getAppDescription()).append("\n");
        sb.append("appUpdateDescription").append("=").append(pgyerBeanV1.getData().getAppUpdateDescription()).append("\n");
        sb.append("appScreenshots").append("=").append(pgyerBeanV1.getData().getAppScreenshots()).append("\n");
        sb.append("appShortcutUrl").append("=").append(pgyerBeanV1.getData().getAppShortcutUrl()).append("\n");
        sb.append("appCreated").append("=").append(pgyerBeanV1.getData().getAppCreated()).append("\n");
        sb.append("appUpdated").append("=").append(pgyerBeanV1.getData().getAppUpdated()).append("\n");
        sb.append("appQRCodeURL").append("=").append(pgyerBeanV1.getData().getAppQRCodeURL()).append("\n");
        sb.append("appPgyerURL").append("=").append(pgyerBeanV1.getData().getAppPgyerURL()).append("\n");
        sb.append("appBuildURL").append("=").append(pgyerBeanV1.getData().getAppBuildURL()).append("\n");
        return sb.toString();
    }
}
