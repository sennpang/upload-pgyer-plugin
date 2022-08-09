package ren.helloworld.upload2pgyer.apiv2;

public class PgyerTokenBeanV2 {

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

        private String key;
        private String policy;
        private String signature;
        private String OSSAccessKeyId;
        private String callback;
        private String success_action_status;
        private String host;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getOSSAccessKeyId() {
            return OSSAccessKeyId;
        }

        public void setOSSAccessKeyId(String OSSAccessKeyId) {
            this.OSSAccessKeyId = OSSAccessKeyId;
        }

        public String getCallback() {
            return callback;
        }

        public void setCallback(String callback) {
            this.callback = callback;
        }

        public String getSuccess_action_status() {
            return success_action_status;
        }

        public void setSuccess_action_status(String success_action_status) {
            this.success_action_status = success_action_status;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }
}
