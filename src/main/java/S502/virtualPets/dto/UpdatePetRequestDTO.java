package S502.virtualPets.dto;

import S502.virtualPets.persistence.enums.MoodEnum;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdatePetRequestDTO(@Nullable
                                  @Size(max = 50, message = "The pet's name cannot exceed 50 characters.")
                                  String name,

                                  @Nullable
                                  MoodEnum mood,

                                  @Nullable
                                  @Min(value = 0, message = "The energy level cannot be less than 0.")
                                  @Max(value = 100, message = "The energy level cannot be greater than 100.")
                                  Integer energyLevel,

                                  @Nullable
                                  @Min(value = 0, message = "The level of hunger cannot be less than 0.")
                                  @Max(value = 100, message = "The level of hunger cannot be greater than 100.")
                                  Integer hungerLevel) {
}
