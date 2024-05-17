package account.Entity;

import jakarta.persistence.*;

import java.time.YearMonth;

@Entity
@Table(name = "payroll")
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String employee;
    private YearMonth period;
    private Long salary;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Payroll(String employee, YearMonth period, Long salary) {
        this.employee = employee;
        this.period = period;
        this.salary = salary;
    }

    public Payroll() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public YearMonth getPeriod() {
        return period;
    }

    public void setPeriod(YearMonth period) {
        this.period = period;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }
}
