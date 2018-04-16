package ren.helloworld.upload2pgyer.apiv1;

/**
 * Created by dafan on 2017/5/4 0004.
 */
public class PgyerBeanV1 {

    private int code;
    private String message;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {

        private String appKey;
        private String userKey;
        private String appType;
        private String appIsLastest;
        private String appFileSize;
        private String appName;
        private String appVersion;
        private String appVersionNo;
        private String appBuildVersion;
        private String appIdentifier;
        private String appIcon;
        private String appDescription;
        private String appUpdateDescription;
        private String appScreenshots;
        private String appShortcutUrl;
        private String appCreated;
        private String appUpdated;
        private String appQRCodeURL;
        private String appPgyerURL;
        private String appBuildURL;

        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }

        public String getUserKey() {
            return userKey;
        }

        public void setUserKey(String userKey) {
            this.userKey = userKey;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }

        public String getAppIsLastest() {
            return appIsLastest;
        }

        public void setAppIsLastest(String appIsLastest) {
            this.appIsLastest = appIsLastest;
        }

        public String getAppFileSize() {
            return appFileSize;
        }

        public void setAppFileSize(String appFileSize) {
            this.appFileSize = appFileSize;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getAppVersionNo() {
            return appVersionNo;
        }

        public void setAppVersionNo(String appVersionNo) {
            this.appVersionNo = appVersionNo;
        }

        public String getAppBuildVersion() {
            return appBuildVersion;
        }

        public void setAppBuildVersion(String appBuildVersion) {
            this.appBuildVersion = appBuildVersion;
        }

        public String getAppIdentifier() {
            return appIdentifier;
        }

        public void setAppIdentifier(String appIdentifier) {
            this.appIdentifier = appIdentifier;
        }

        public String getAppIcon() {
            return appIcon;
        }

        public void setAppIcon(String appIcon) {
            this.appIcon = appIcon;
        }

        public String getAppDescription() {
            return appDescription;
        }

        public void setAppDescription(String appDescription) {
            this.appDescription = appDescription;
        }

        public String getAppUpdateDescription() {
            return appUpdateDescription;
        }

        public void setAppUpdateDescription(String appUpdateDescription) {
            this.appUpdateDescription = appUpdateDescription;
        }

        public String getAppScreenshots() {
            return appScreenshots;
        }

        public void setAppScreenshots(String appScreenshots) {
            this.appScreenshots = appScreenshots;
        }

        public String getAppShortcutUrl() {
            return appShortcutUrl;
        }

        public void setAppShortcutUrl(String appShortcutUrl) {
            this.appShortcutUrl = appShortcutUrl;
        }

        public String getAppCreated() {
            return appCreated;
        }

        public void setAppCreated(String appCreated) {
            this.appCreated = appCreated;
        }

        public String getAppUpdated() {
            return appUpdated;
        }

        public void setAppUpdated(String appUpdated) {
            this.appUpdated = appUpdated;
        }

        public String getAppQRCodeURL() {
            return appQRCodeURL;
        }

        public void setAppQRCodeURL(String appQRCodeURL) {
            this.appQRCodeURL = appQRCodeURL;
        }

        public String getAppPgyerURL() {
            return appPgyerURL;
        }

        public void setAppPgyerURL(String appPgyerURL) {
            this.appPgyerURL = appPgyerURL;
        }

        public String getAppBuildURL() {
            return appBuildURL;
        }

        public void setAppBuildURL(String appBuildURL) {
            this.appBuildURL = appBuildURL;
        }
    }
}
