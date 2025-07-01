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
import org.hibernate.sql.Update;
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

        PetResponseDTO createPed = petService.createPet(createPetRequestDTO);
        return new ResponseEntity<>(createPed, HttpStatus.CREATED);

    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PetResponseDTO>> getAllPets(){

        List<PetResponseDTO> pets = petService.getAllPets();
        return new ResponseEntity<>(pets,HttpStatus.OK);

    }

    @GetMapping("/my-pets")
    @PreAuthorize("hasRole('USER', 'ADMIN')")
    public ResponseEntity<List<PetResponseDTO>> getMyPets(){

        List<PetResponseDTO> pets = petService.getMyPets();
        return new ResponseEntity<>(pets, HttpStatus.OK);

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER', 'ADMIN')")
    public ResponseEntity<PetResponseDTO> getPetById (@PathVariable Long id){

        PetResponseDTO pet = petService.getPetById(id);
        return new ResponseEntity<>(pet, HttpStatus.OK);

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER', 'ADMIN')")
    public ResponseEntity<PetResponseDTO> updatePet (@PathVariable Long id, @RequestBody @Valid UpdatePetRequestDTO updatePetRequestDTO){

        PetResponseDTO updatePet = petService.updatePet(id, updatePetRequestDTO);
        return new ResponseEntity<>(updatePet, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }




}
