package account.Repository;

import account.Entity.SecurityEvents;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SecurityServiceRepository extends CrudRepository<SecurityEvents, Long> {

    List<SecurityEvents> findAllByOrderByIdAsc();

}
