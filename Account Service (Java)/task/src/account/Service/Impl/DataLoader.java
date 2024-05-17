package account.Service.Impl;

import account.Entity.Role;
import account.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader {

    private RoleRepository roleRepository;

    @Autowired
    public DataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        createRoles();
    }

    private void createRoles() {
        List<String> rolesList = List.of("ROLE_ADMINISTRATOR", "ROLE_USER","ROLE_ACCOUNTANT", "ROLE_AUDITOR");
        for(String role :rolesList){
            if(!roleRepository.existsByName(role)){
                roleRepository.save(new Role(role));
            }
        }
    }
}

