package account.Service;

import account.Models.ChangeRole;
import account.Models.UserDTO;

public interface RoleService {

    UserDTO changeUserRole(ChangeRole changeRole, String adminEmail);
}
