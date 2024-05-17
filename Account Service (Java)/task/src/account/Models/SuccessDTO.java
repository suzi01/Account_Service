package account.Models;

public class SuccessDTO {
    private String email;
    private String status;


    public SuccessDTO(String email, String status) {
        this.status=status;
        this.email = email;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
