package S502.virtualPets.dto;

import S502.virtualPets.persistence.enums.PetTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePetRequestDTO(@NotBlank(message = "The name of the pet is mandatory")
                                  @Size(max = 50, message = "The pet's name should not exceed 50 characters.")
                                  String name,

                                  @NotNull(message = "The type of pet is mandatory. You can choose between: San Bernardo")
                                  PetTypeEnum petType) {
}
