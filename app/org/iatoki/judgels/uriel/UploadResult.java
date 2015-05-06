package org.iatoki.judgels.uriel;

public final class UploadResult {
    private String value;
    private String status;

    public UploadResult(String value, String status) {
        this.value = value;
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public String getStatus() {
        return status;
    }
}
