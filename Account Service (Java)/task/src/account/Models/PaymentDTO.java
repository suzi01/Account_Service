package account.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class PaymentDTO {
    private String employee;
    private String period;
    private Long salary;


    @JsonCreator
    public PaymentDTO(
            @JsonProperty("employee") String employee,
            @JsonProperty("period") String period,
            @JsonProperty("salary") Long salary
    ) {
        this.period = period;
        this.salary = salary;
        this.employee = employee;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }
}
