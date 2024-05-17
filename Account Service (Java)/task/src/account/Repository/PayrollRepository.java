package account.Repository;

import account.Entity.Payroll;
import org.springframework.data.repository.CrudRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends CrudRepository<Payroll, Long> {

    List<Payroll> findAllByEmployeeOrderByPeriodDesc(String email);

    boolean existsUniqueSalaryByEmployeeAndPeriod(String employee, YearMonth period);

    Optional<Payroll> findByEmployeeAndPeriod(String employee, YearMonth period);

}
