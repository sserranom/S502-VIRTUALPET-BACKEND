package S502.virtualPets.controller;

import S502.virtualPets.dto.CreatePetRequestDTO;
import S502.virtualPets.dto.PetResponseDTO;
import S502.virtualPets.dto.UpdatePetRequestDTO;
import S502.virtualPets.service.PetService;
import jakarta.validation.Valid;
import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/pets")
public class PetController {

    @Autowired
    private PetService petService;

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
