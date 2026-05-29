package lt.viko.eif.astrukcinskas.grupinis_playground.repository;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByUsername(String username);
}
