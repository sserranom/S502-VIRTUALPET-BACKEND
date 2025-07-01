package S502.virtualPets.service;

import S502.virtualPets.dto.CreatePetRequestDTO;
import S502.virtualPets.dto.PetResponseDTO;
import S502.virtualPets.dto.UpdatePetRequestDTO;
import S502.virtualPets.persistence.entity.PetEntity;
import S502.virtualPets.persistence.entity.UserEntity;
import S502.virtualPets.persistence.enums.RoleEnum;
import S502.virtualPets.persistence.repository.PetRepository;
import S502.virtualPets.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("There is no authenticated user.");
        }
        String username = authentication.getName();
        return userRepository.findUserEntityByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found in the database."));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + RoleEnum.ADMIN.name()));
    }

    private PetResponseDTO convertToDto(PetEntity petEntity) {
        return new PetResponseDTO(
                petEntity.getId(),
                petEntity.getName(),
                petEntity.getPetType(),
                petEntity.getMood(),
                petEntity.getEnergyLevel(),
                petEntity.getHungerLevel(),
                petEntity.getUser().getId(),
                petEntity.getUser().getUsername(),
                petEntity.getCreatedAt(),
                petEntity.getUpdatedAt()
        );
    }

    public PetResponseDTO createPet(CreatePetRequestDTO createPetRequestDTO) {
        UserEntity currentUser = getAuthenticatedUser();

        PetEntity newPet = PetEntity.builder()
                .name(createPetRequestDTO.name())
                .petType(createPetRequestDTO.petType())
                .user(currentUser)
                .build();

        PetEntity savedPet = petRepository.save(newPet);
        return convertToDto(savedPet);
    }

    public List<PetResponseDTO> getAllPets() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(authentication)) {
            throw new AccessDeniedException("Access denied. Only administrators can see all pets.");
        }
        List<PetEntity> pets = petRepository.findAll();
        return pets.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<PetResponseDTO> getMyPets() {
        UserEntity currentUser = getAuthenticatedUser();
        List<PetEntity> pets = petRepository.findByUserId(currentUser.getId());
        return pets.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public PetResponseDTO getPetById(Long petId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = getAuthenticatedUser();

        Optional<PetEntity> petOptional;

        if (isAdmin(authentication)) {
            petOptional = petRepository.findById(petId);
        } else {
            petOptional = petRepository.findByIdAndUserId(petId, currentUser.getId());
        }

        PetEntity pet = petOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found or you have no permission to see it."));

        return convertToDto(pet);
    }

    public PetResponseDTO updatePet(Long petId, UpdatePetRequestDTO updatePetRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = getAuthenticatedUser();

        PetEntity petToUpdate;

        if (isAdmin(authentication)) {
            petToUpdate = petRepository.findById(petId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found."));
        } else {
            petToUpdate = petRepository.findByIdAndUserId(petId, currentUser.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found or you have no permission to update it."));
        }

        Optional.ofNullable(updatePetRequestDTO.name()).ifPresent(petToUpdate::setName);
        Optional.ofNullable(updatePetRequestDTO.mood()).ifPresent(petToUpdate::setMood);
        Optional.ofNullable(updatePetRequestDTO.energyLevel()).ifPresent(petToUpdate::setEnergyLevel);
        Optional.ofNullable(updatePetRequestDTO.hungerLevel()).ifPresent(petToUpdate::setHungerLevel);

        PetEntity updatedPet = petRepository.save(petToUpdate);
        return convertToDto(updatedPet);
    }

    public void deletePet(Long petId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = getAuthenticatedUser();

        PetEntity petToDelete;

        if (isAdmin(authentication)) {
            petToDelete = petRepository.findById(petId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found."));
        } else {
            petToDelete = petRepository.findByIdAndUserId(petId, currentUser.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found or you have no permission to eliminate it."));
        }

        petRepository.delete(petToDelete);
    }
}
