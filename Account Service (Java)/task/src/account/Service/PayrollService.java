package account.Service;

import account.Models.EmployeePayDTO;
import account.Models.PaymentDTO;

import java.util.List;

public interface PayrollService {
    String uploadPayrolls(List<PaymentDTO> paymentDTO);

    void changePayroll(PaymentDTO paymentDTO);

    List<EmployeePayDTO> getAllPayroll(String email);

    EmployeePayDTO getByPeriod(String period, String email);

}
