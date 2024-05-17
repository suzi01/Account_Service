package account.Repository;
import account.Entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
        boolean existsUserByEmailIgnoreCase(String email);

        User findUserByEmailIgnoreCase(String email);

        @Query("SELECT COUNT(u) > 0 FROM User u")
        boolean existsAnyUser();

        List<User> findAllByOrderByIdAsc();

        void deleteUserById(Long id);

        @Modifying
        @Query("UPDATE User u SET u.failedAttempts = :failedAttempts WHERE u.email = :email")
        void updateFailedAttempts(@Param("failedAttempts") int failAttempts, @Param("email") String email);

}