package S502.virtualPets.persistence.repository;

import S502.virtualPets.persistence.entity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<PetEntity, Long> {

    List<PetEntity> findByUserId(Long userId);

    Optional<PetEntity> findByIdAndUserId(Long petId, Long userId);

}
