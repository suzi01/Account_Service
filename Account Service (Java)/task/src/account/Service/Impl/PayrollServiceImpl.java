package account.Service.Impl;

import account.Entity.Payroll;
import account.Entity.User;
import account.Exceptions.InvalidPayrollDataException;
import account.Exceptions.PayrollDoesNotExistException;
import account.Models.EmployeePayDTO;
import account.Models.PaymentDTO;
import account.Repository.PayrollRepository;
import account.Repository.UserRepository;
import account.Service.PayrollService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PayrollServiceImpl implements PayrollService {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy");

    private final UserRepository repository;

    private final PayrollRepository payrollRepository;

    public PayrollServiceImpl(UserRepository repository, PayrollRepository payrollRepository) {
        this.repository = repository;
        this.payrollRepository = payrollRepository;
    }

    @Override
    @Transactional
    public String uploadPayrolls(List<PaymentDTO> paymentDTO) {
        List<String> error = new ArrayList<>();
        for( int i = 0; i < paymentDTO.size(); i++){
            String errorMessage = paymentValidator(paymentDTO.get(i), i);
            if(errorMessage.equals("true")){
                User user = repository.findUserByEmailIgnoreCase(paymentDTO.get(i).getEmployee());
                YearMonth yearMonth = YearMonth.parse(paymentDTO.get(i).getPeriod(), dateTimeFormatter);
                Payroll addPayroll = new Payroll(paymentDTO.get(i).getEmployee(), yearMonth, paymentDTO.get(i).getSalary());
                addPayroll.setUser(user);
                payrollRepository.save(addPayroll);
            } else {
                error.add(paymentValidator(paymentDTO.get(i), i));
            }
        }
        if (!error.isEmpty()){
            String errorMessages = String.join(", ", error);
            throw new InvalidPayrollDataException(errorMessages);
        }
        return "Added successfully!";
    }


    @Override
    public void changePayroll(PaymentDTO paymentDTO) {
        if(paymentDTO.getSalary()< 0 || !isValidMonthYear(paymentDTO.getPeriod())){
            throw new InvalidPayrollDataException("Salary format incorrect");
        }
        YearMonth yearMonth = YearMonth.parse(paymentDTO.getPeriod(), dateTimeFormatter);
        Optional<Payroll> optionalPayroll = payrollRepository.findByEmployeeAndPeriod(paymentDTO.getEmployee(),yearMonth );
        if(optionalPayroll.isPresent()){
            Payroll payroll = optionalPayroll.get();
            payroll.setSalary(paymentDTO.getSalary());
            payrollRepository.save(payroll);
        }
        else {
            throw new PayrollDoesNotExistException("User does not exist!");
        }

    }

    public List<EmployeePayDTO> getAllPayroll( String email) {
        List<EmployeePayDTO> uniquePayroll = new ArrayList<>();

        User user = repository.findUserByEmailIgnoreCase(email);
        if(user == null){
            throw new InvalidPayrollDataException("User does not exist");
        }

        List<Payroll> payrolls = payrollRepository.findAllByEmployeeOrderByPeriodDesc(email);
        for(Payroll employee : payrolls){
            EmployeePayDTO createdEmployee = createEmployeePayDTO(employee,user);
            uniquePayroll.add(createdEmployee);
        }
        return uniquePayroll;
    }

    @Override
    public EmployeePayDTO getByPeriod(String period, String email) {

        User user = repository.findUserByEmailIgnoreCase(email);
        if(user == null){
            throw new InvalidPayrollDataException("User does not exist");
        }


        if(!isValidMonthYear(period)){
            throw new InvalidPayrollDataException("Period has wrong format!");
        }

        YearMonth yearMonth = YearMonth.parse(period, dateTimeFormatter);
        Optional<Payroll> optionalPayroll = payrollRepository.findByEmployeeAndPeriod(email, yearMonth);
        if(optionalPayroll.isEmpty()){
            return new EmployeePayDTO();
        }

        Payroll payroll = optionalPayroll.get();
        return createEmployeePayDTO(payroll, user);
    }


    private String paymentValidator(PaymentDTO payment, int i) {
        if(!repository.existsUserByEmailIgnoreCase(payment.getEmployee())) {
            return "payments[" + i + "].employee: Employee does not exist!";
        }
        if(!isValidMonthYear(payment.getPeriod())){
            return "payments["+ i + "].period: Period format is invalid!";
        }
        YearMonth yearMonth = YearMonth.parse(payment.getPeriod(), dateTimeFormatter);
        if(payrollRepository.existsUniqueSalaryByEmployeeAndPeriod(payment.getEmployee(),
                yearMonth) ) {
            return "payments["+ i + "].period: Period already exists!";
        }
        if(payment.getSalary() < 0){
            return "payments["+ i + "].Salary: Salary must be non negative!";
        }
        return "true";
    }

    private boolean isValidMonthYear(String date){
        try{
            YearMonth yearMonth = YearMonth.parse(date, dateTimeFormatter);
            return true;
        } catch (DateTimeException e){
            return false;
        }
    }

    private String monthConverter(YearMonth period) {
        DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern("MMMM-yyyy");
        return period.format(dateTimeFormatter1);
    }

    private String moneyConverter(long salary){
        long dollars = salary / 100;
        long cents = salary % 100;

        return dollars + " dollar(s) " + cents + " cent(s)";

    }

    private EmployeePayDTO createEmployeePayDTO(Payroll payroll, User foundUser) {
        String convertedPeriod = monthConverter(payroll.getPeriod());
        String convertedMoney = moneyConverter(payroll.getSalary());
        return new EmployeePayDTO(
                foundUser.getName(),
                foundUser.getLastname(),
                convertedPeriod,
                convertedMoney
        );
    }


}
