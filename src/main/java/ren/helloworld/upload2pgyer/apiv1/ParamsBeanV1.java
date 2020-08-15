package ren.helloworld.upload2pgyer.apiv1;

public class ParamsBeanV1 {
    private String ukey;
    private String apiKey;
    // file start
    private String scandir;
    private String wildcard;
    private String uploadFile;
    // file end
    private String installType;
    private String password;
    private String updateDescription;
    private String channelShortcut;

    private String qrcodePath;
    private String envVarsPath;

    public String getUkey() {
        return ukey;
    }

    public void setUkey(String ukey) {
        this.ukey = ukey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getScandir() {
        return scandir;
    }

    public void setScandir(String scandir) {
        this.scandir = scandir;
    }

    public String getWildcard() {
        return wildcard;
    }

    public void setWildcard(String wildcard) {
        this.wildcard = wildcard;
    }

    public String getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(String uploadFile) {
        this.uploadFile = uploadFile;
    }

    public String getInstallType() {
        return installType;
    }

    public void setInstallType(String installType) {
        this.installType = installType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUpdateDescription() {
        return updateDescription;
    }

    public void setUpdateDescription(String updateDescription) {
        this.updateDescription = updateDescription;
    }

    public String getChannelShortcut() {
        return channelShortcut;
    }

    public void setChannelShortcut(String channelShortcut) {
        this.channelShortcut = channelShortcut;
    }

    public String getQrcodePath() {
        return qrcodePath;
    }

    public void setQrcodePath(String qrcodePath) {
        this.qrcodePath = qrcodePath;
    }

    public String getEnvVarsPath() {
        return envVarsPath;
    }

    public void setEnvVarsPath(String envVarsPath) {
        this.envVarsPath = envVarsPath;
    }
}
