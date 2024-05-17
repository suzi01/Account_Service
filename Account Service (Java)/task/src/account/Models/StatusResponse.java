package account.Models;

public class StatusResponse {
    private String status;

    public StatusResponse(String status) {
        this.status = status;
    }

    public StatusResponse() {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
