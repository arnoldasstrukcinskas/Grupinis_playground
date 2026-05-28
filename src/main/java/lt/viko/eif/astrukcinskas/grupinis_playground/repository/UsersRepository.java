package lt.viko.eif.astrukcinskas.grupinis_playground.repository;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<User, Integer> {
}
