package account.Models;

public class DeleteSuccess extends StatusResponse {
    private String user;

    public DeleteSuccess(String status, String user) {
        super(status);
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
