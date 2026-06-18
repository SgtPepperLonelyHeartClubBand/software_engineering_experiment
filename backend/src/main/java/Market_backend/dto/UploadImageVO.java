package Market_backend.dto;

public class UploadImageVO {

    private String url;

    public UploadImageVO() {
    }

    public UploadImageVO(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
