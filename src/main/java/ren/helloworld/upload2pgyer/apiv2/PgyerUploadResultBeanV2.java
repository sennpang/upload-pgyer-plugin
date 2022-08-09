package ren.helloworld.upload2pgyer.apiv2;

public class PgyerUploadResultBeanV2 {

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

        private String aKey;
        private String viewBuildInfo;
        private String viewApp;

        public String getaKey() {
            return aKey;
        }

        public void setaKey(String aKey) {
            this.aKey = aKey;
        }

        public String getViewBuildInfo() {
            return viewBuildInfo;
        }

        public void setViewBuildInfo(String viewBuildInfo) {
            this.viewBuildInfo = viewBuildInfo;
        }

        public String getViewApp() {
            return viewApp;
        }

        public void setViewApp(String viewApp) {
            this.viewApp = viewApp;
        }
    }
}
