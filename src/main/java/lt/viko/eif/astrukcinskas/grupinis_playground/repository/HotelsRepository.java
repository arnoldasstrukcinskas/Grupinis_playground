package lt.viko.eif.astrukcinskas.grupinis_playground.repository;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelsRepository extends JpaRepository<Hotel, Integer> {

}
