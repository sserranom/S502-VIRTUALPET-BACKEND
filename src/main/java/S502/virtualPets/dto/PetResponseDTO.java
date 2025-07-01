package S502.virtualPets.dto;

import S502.virtualPets.persistence.enums.MoodEnum;
import S502.virtualPets.persistence.enums.PetTypeEnum;

import java.time.LocalDateTime;

public record PetResponseDTO(Long id,
                             String name,
                             PetTypeEnum type,
                             MoodEnum mood,
                             Integer energyLevel,
                             Integer hungerLevel,
                             Long userId,
                             String username,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
}
