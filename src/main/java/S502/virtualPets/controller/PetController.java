package S502.virtualPets.controller;

import S502.virtualPets.dto.CreatePetRequestDTO;
import S502.virtualPets.dto.PetResponseDTO;
import S502.virtualPets.dto.UpdatePetRequestDTO;
import S502.virtualPets.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/pets")
@Tag(name = "Virtual pets", description = "Endpoints for virtual pet management.")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class PetController {

    @Autowired
    private PetService petService;

    @PostMapping
    @Operation(summary = "Create a new pet", description = "Create a virtual pet associated with the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pet successfully created.",
                    content = @Content(schema = @Schema(implementation = PetResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request (eg. incomplete pet data).",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<PetResponseDTO> createPet (@RequestBody @Valid CreatePetRequestDTO createPetRequestDTO){
        log.info("Post request received to create pet: {}", createPetRequestDTO.name());
        PetResponseDTO createPet = petService.createPet(createPetRequestDTO);
        log.info("Pet successfully created with ID: {}", createPet.id());
        return new ResponseEntity<>(createPet, HttpStatus.CREATED);

    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all pets (only admin)", description = "It allows administrators to see all pets in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all pets.",
                    content = @Content(schema = @Schema(implementation = PetResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Denied access (it is not admin).",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<List<PetResponseDTO>> getAllPets(){
        log.info("GET petition received to obtain all pets (administrator access).");
        List<PetResponseDTO> pets = petService.getAllPets();
        log.info("returned {} pets.", pets.size());
        return new ResponseEntity<>(pets,HttpStatus.OK);

    }

    @GetMapping("/my-pets")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get my pets", description = "Get all the authenticated user pets.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User pet list.",
                    content = @Content(schema = @Schema(implementation = PetResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<List<PetResponseDTO>> getMyPets(){
        log.info("GET petition received to obtain the authenticated user pets");
        List<PetResponseDTO> pets = petService.getMyPets();
        log.info("Returned {} Pets for the current user.", pets.size());
        return new ResponseEntity<>(pets, HttpStatus.OK);

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get pet by ID", description = "Obtains a specific pet for his id. Users can only see their own, any administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet found.",
                    content = @Content(schema = @Schema(implementation = PetResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Denied access (not owner or admin).",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Pet not found.",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<PetResponseDTO> getPetById (@PathVariable Long id){
        log.info("GET petition received for pet with ID: {}", id);
        PetResponseDTO pet = petService.getPetById(id);
        log.info("Pet with ID {} successfully recovered.", id);
        return new ResponseEntity<>(pet, HttpStatus.OK);

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update pet by ID", description = "Update an existing pet for your ID. Users can only update their own, any administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet updated successfully.",
                    content = @Content(schema = @Schema(implementation = PetResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request.",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Access denied.",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Pet not found.",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<PetResponseDTO> updatePet (@PathVariable Long id, @RequestBody @Valid UpdatePetRequestDTO updatePetRequestDTO){
        log.info("PUT petition received to update pet with ID: {}. Info: {}", id, updatePetRequestDTO);
        PetResponseDTO updatedPet = petService.updatePet(id, updatePetRequestDTO);
        log.info("Pet with ID {} updated successfully.", id);
        return new ResponseEntity<>(updatedPet, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Remove pet by ID", description = "Eliminates an existing pet for your ID. Users can only eliminate their own, any administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pet successfully eliminated."),
            @ApiResponse(responseCode = "401", description = "Not authenticated.",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Access denied.",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Pet not found.",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        log.info("Petition Delete received for pet with ID: {}", id);
        petService.deletePet(id);
        log.info("Pet with ID {} successfully eliminated.", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }




}
