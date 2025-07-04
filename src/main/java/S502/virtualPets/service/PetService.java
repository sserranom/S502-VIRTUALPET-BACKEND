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

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;

@Service
@Slf4j
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity getAuthenticatedUser() {
        log.debug("Trying to recover authenticated user from the security context.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("No authenticated user was found or is an anonymous user.");
            throw new AccessDeniedException("There is no authenticated user.");
        }
        String username = authentication.getName();
        log.info("Recovered authenticated user: {}", username);
        return userRepository.findUserEntityByUsername(username)
                .orElseThrow(() -> {
                    log.error("Authenticated user '{}' Not found in the database.", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found in the database.");
                });
    }

    private boolean isAdmin(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + RoleEnum.ADMIN.name()));
        log.debug("Verifying admin role for the user '{}': {}", authentication.getName(), isAdmin);
        return isAdmin;
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

    @CachePut(value = "pets", key = "#result.id")
    @CacheEvict(value = "myPets", key = "#currentUser.id", allEntries = true)
    public PetResponseDTO createPet(CreatePetRequestDTO createPetRequestDTO) {
        UserEntity currentUser = getAuthenticatedUser();
        log.info("Starting pet Creation '{}' of type '{}' For the user '{}'.",
                createPetRequestDTO.name(), createPetRequestDTO.petType(), currentUser.getUsername());

        PetEntity newPet = PetEntity.builder()
                .name(createPetRequestDTO.name())
                .petType(createPetRequestDTO.petType())
                .user(currentUser)
                .build();

        PetEntity savedPet = petRepository.save(newPet);
        log.info("Pet '{}' (ID: {}) successfully created for the user '{}'.",
                savedPet.getName(), savedPet.getId(), currentUser.getUsername());
        return convertToDto(savedPet);
    }

    @Cacheable(value = "allPets", unless = "#result.empty")
    public List<PetResponseDTO> getAllPets() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Request to obtain all the pets by the user'{}' (rol ADMIN).", authentication.getName());
        if (!isAdmin(authentication)) {
            log.warn("User '{}' He tried to access all pets without a role admin. Access denied.", authentication.getName());
            throw new AccessDeniedException("Access denied. Only administrators can see all pets.");
        }
        List<PetEntity> pets = petRepository.findAll();
        log.info("They recovered {} User pets ADMIN '{}'.", pets.size(), authentication.getName());
        return pets.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Cacheable(value = "myPets", key = "#currentUser.id", unless = "#result.empty")
    public List<PetResponseDTO> getMyPets() {
        UserEntity currentUser = getAuthenticatedUser();
        log.info("Request to obtain user pets '{}'.", currentUser.getUsername());
        List<PetEntity> pets = petRepository.findByUserId(currentUser.getId());
        log.info("They recovered {} User pets '{}'.", pets.size(), currentUser.
                getUsername());
        return pets.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Cacheable(value = "pets", key = "#petId")
    public PetResponseDTO getPetById(Long petId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = getAuthenticatedUser();
        log.info("Request to obtain pet with ID: {} by the user '{}'.", petId, currentUser.getUsername());

        Optional<PetEntity> petOptional;

        if (isAdmin(authentication)) {
            petOptional = petRepository.findById(petId);
            log.debug("User ADMIN '{}' looking for pet with ID: {}.", currentUser.getUsername(), petId);
        } else {
            petOptional = petRepository.findByIdAndUserId(petId, currentUser.getId());
            log.debug("User '{}' looking for pet with ID: {}.", currentUser.getUsername(), petId);
        }

        PetEntity pet = petOptional.orElseThrow(() ->{
                    log.warn("Pet with ID {} Not found or user '{}' It has no permission.", petId, currentUser.getUsername());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found or you have no permission to see it.");
                });
        log.info("Pet '{}' (ID: {}) successfully recovered.", pet.getName(), pet.getId());
        return convertToDto(pet);
    }

    @CachePut(value = "pets", key = "#petId")
    @CacheEvict(value = {"allPets", "myPets"}, allEntries = true)
    public PetResponseDTO updatePet(Long petId, UpdatePetRequestDTO updatePetRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = getAuthenticatedUser();
        log.info("Request to update pet with ID: {} by the user '{}'. info: {}", petId, currentUser.getUsername(), updatePetRequestDTO);

        PetEntity petToUpdate;

        if (isAdmin(authentication)) {
            petToUpdate = petRepository.findById(petId)
                    .orElseThrow(() -> {
                        log.warn("User ADMIN '{}' He tried to update pet not existing with ID: {}.", currentUser.getUsername(), petId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada.");
                    });
            log.debug("User ADMIN '{}' Updating pet with ID: {}.", currentUser.getUsername(), petId);
        } else {
            petToUpdate = petRepository.findByIdAndUserId(petId, currentUser.getId())
                    .orElseThrow(() -> {
                        log.warn("User '{}' He tried to update pet with ID{} No permission or not found.", currentUser.getUsername(), petId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found or you have no permission to update it.");
                    });
            log.debug("User '{}' updating your pet with ID: {}.", currentUser.getUsername(), petId);
        }

        Optional.ofNullable(updatePetRequestDTO.name()).ifPresent(petToUpdate::setName);
        Optional.ofNullable(updatePetRequestDTO.mood()).ifPresent(petToUpdate::setMood);
        Optional.ofNullable(updatePetRequestDTO.energyLevel()).ifPresent(petToUpdate::setEnergyLevel);
        Optional.ofNullable(updatePetRequestDTO.hungerLevel()).ifPresent(petToUpdate::setHungerLevel);

        PetEntity updatedPet = petRepository.save(petToUpdate);
        log.info("Pet '{}' (ID: {}) Updated successfully by the user '{}'.",
                updatedPet.getName(), updatedPet.getId(), currentUser.getUsername());
        return convertToDto(updatedPet);
    }

    @CachePut(value = "pets", key = "#petId")
    @CacheEvict(value = {"allPets", "myPets"}, allEntries = true)
    public void deletePet(Long petId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = getAuthenticatedUser();
        log.info("Request to eliminate pet with ID: {} by the user '{}'.", petId, currentUser.getUsername());

        PetEntity petToDelete;

        if (isAdmin(authentication)) {
            petToDelete = petRepository.findById(petId)
                    .orElseThrow(() -> {
                        log.warn("UsER ADMIN '{}' He tried to eliminate pet not existing with ID: {}.", currentUser.getUsername(), petId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found.");
                    });
            log.debug("UsER ADMIN '{}' eliminating pet with ID: {}.", currentUser.getUsername(), petId);
        } else {
            petToDelete = petRepository.findByIdAndUserId(petId, currentUser.getId())
                    .orElseThrow(() -> {
                        log.warn("UsER '{}' tried to eliminate pet with ID {} No permission or not found.", currentUser.getUsername(), petId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found or you have no permission to eliminate it.");
                    });
            log.debug("UsER '{}' Eliminating your pet with ID: {}.", currentUser.getUsername(), petId);
        }

        petRepository.delete(petToDelete);
        log.info("Pet with ID {} successfully eliminated by the user '{}'.", petId, currentUser.getUsername());
    }
}
