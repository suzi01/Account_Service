package account.Controller;

import account.Exceptions.*;
import account.Models.*;
import account.Service.PayrollService;
import account.Service.RoleService;
import account.Service.SecurityService;
import account.Service.UserService;
import account.UserAdaptor.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private PayrollService payrollService;


    @Autowired
    private RoleService roleService;

    @Autowired
    private SecurityService securityService;


    @GetMapping("/hello")
    public UserDTO hello( @AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody UserDTO userDTO){
        System.out.println(customUserDetails);
        return userDTO;
    }



    @PostMapping("/auth/signup")
    public UserDTO userSignup(@Valid @RequestBody UserDTO userDto){
        return userService.verifyUser(userDto);
    }


    @PostMapping("/auth/changepass")
    public SuccessDTO userChangePassword(@RequestBody Map<String, String> password, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        return userService.changePassword(password.get("new_password"), customUserDetails.getPassword(), customUserDetails.getUsername());
    }

    @PostMapping("acct/payments")
    public ResponseEntity<StatusResponse> uploadPayrolls(@Valid @RequestBody List<PaymentDTO> paymentDTO){
        String message =  payrollService.uploadPayrolls(paymentDTO);
        StatusResponse response = new StatusResponse(message);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/acct/payments")
    public ResponseEntity<StatusResponse> changePayroll(@Valid @RequestBody PaymentDTO paymentDTO){
        payrollService.changePayroll(paymentDTO);
        StatusResponse statusResponse = new StatusResponse("Updated successfully!");
        return new ResponseEntity<>(statusResponse, HttpStatus.OK);
    }

    @GetMapping("/empl/payment")
    public ResponseEntity<?> getEmployeesByPeriod(@RequestParam(name = "period", required = false) String period,
                                                       @AuthenticationPrincipal CustomUserDetails customUserDetails){
        if(period == null){
            List<EmployeePayDTO> payment = payrollService.getAllPayroll(customUserDetails.getUsername());
            return new ResponseEntity<>(payment, HttpStatus.OK);
        }
        EmployeePayDTO employeePayDTO = payrollService.getByPeriod(period, customUserDetails.getUsername());
        return new ResponseEntity<>(employeePayDTO, HttpStatus.OK);
    }


    @GetMapping("/admin/user/")
    public List<UserDTO> getAllUsers(){
        return userService.getAllUsers();
    }

    @DeleteMapping("/admin/user/{user_email}")
    public ResponseEntity<DeleteSuccess> deleteUser(@PathVariable("user_email") String userEmail,
                                                    @AuthenticationPrincipal CustomUserDetails customUserDetails){
        if(userEmail.equals(customUserDetails.getUsername())){
            throw new UserAlreadyExistsException("Can't remove ADMINISTRATOR role!");
        }
        userService.deleteUserByEmail(userEmail, customUserDetails.getUsername());
        DeleteSuccess deleteSuccess = new DeleteSuccess("Deleted successfully!", userEmail);
        return new ResponseEntity<>(deleteSuccess, HttpStatus.OK);
    }

    @PutMapping("admin/user/role")
    public UserDTO changeUserRoles(@Valid @RequestBody ChangeRole changeRole,
                                   @AuthenticationPrincipal CustomUserDetails  customUserDetails) {
        return roleService.changeUserRole(changeRole, customUserDetails.getUsername());
    }

    @PutMapping("admin/user/access")
    public ResponseEntity<StatusResponse> changeAccess(@Valid @RequestBody ChangeAccess access,
                                                       @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String response = userService.changeAccess(access, customUserDetails.getUsername());
        StatusResponse statusResponse = new StatusResponse(response);
        return new ResponseEntity<>(statusResponse, HttpStatus.OK);
    }

    @GetMapping("security/events/")
    public List<SecurityDto> getEventsLog(){
        return securityService.getAllEvents();
    }



    @ExceptionHandler(
            {
                    UserAlreadyExistsException.class,
                    BreachedPasswordException.class,
                    PasswordLengthException.class,
                    SamePasswordException.class,
                    InvalidPayrollDataException.class,
                    PayrollDoesNotExistException.class,
                    InvalidRoleException.class,
                    InvalidUserException.class
            })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<CustomErrorResponse> handleUserAlreadyExistsException(Exception ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(), // Error message goes here
                ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Void> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler({
            NotFoundUserException.class,
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<CustomErrorResponse> handleUserNotFoundException(Exception ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(), // Error message goes here
                ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

}
