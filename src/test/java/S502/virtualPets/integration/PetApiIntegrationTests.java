package S502.virtualPets.integration;

import S502.virtualPets.VirtualpetsApplication;
import S502.virtualPets.dto.*;
import S502.virtualPets.persistence.enums.MoodEnum;
import S502.virtualPets.persistence.enums.PetTypeEnum;
import S502.virtualPets.persistence.repository.PetRepository;
import S502.virtualPets.persistence.repository.RoleRepository;
import S502.virtualPets.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = VirtualpetsApplication.class)
@Transactional
@ActiveProfiles("test")
@DisplayName("Integration tests for pet operations (Crud and Authorization)")
public class PetApiIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PetRepository petRepository;

    private String petsBaseUrl;
    private String authBaseUrl;

    private String userToken;
    private String adminToken;
    private Long userId;
    private Long adminId;

    @BeforeEach
    void setUp() {
        petsBaseUrl = "http://localhost:" + port + "/api/pets";
        authBaseUrl = "http://localhost:" + port + "/auth";

        if (roleRepository.count() == 0) {
            roleRepository.save(new S502.virtualPets.persistence.entity.RoleEntity(0, S502.virtualPets.persistence.enums.RoleEnum.ADMIN, null));
            roleRepository.save(new S502.virtualPets.persistence.entity.RoleEntity(0, S502.virtualPets.persistence.enums.RoleEnum.USER, null));
        }

        AuthCreateUserRequestDTO userRegisterRequest = new AuthCreateUserRequestDTO(
                "user_test", "userpass", new AuthCreateRoleRequestDTO(List.of("USER")));
        ResponseEntity<AuthResponseDTO> userRegisterResponse = restTemplate.postForEntity(
                authBaseUrl + "/sign-up", userRegisterRequest, AuthResponseDTO.class);
        userToken = userRegisterResponse.getBody().jwt();
        userId = userRepository.findUserEntityByUsername("user_test").orElseThrow().getId();

        AuthCreateUserRequestDTO adminRegisterRequest = new AuthCreateUserRequestDTO(
                "admin_test", "adminpass", new AuthCreateRoleRequestDTO(List.of("ADMIN")));
        ResponseEntity<AuthResponseDTO> adminRegisterResponse = restTemplate.postForEntity(
                authBaseUrl + "/sign-up", adminRegisterRequest, AuthResponseDTO.class);
        adminToken = adminRegisterResponse.getBody().jwt();
        adminId = userRepository.findUserEntityByUsername("admin_test").orElseThrow().getId();
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("User: should create a pet successfully")
    void userShouldCreatePetSuccessfully() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("San_Bernardo", PetTypeEnum.SAN_BERNARDO);
        HttpEntity<CreatePetRequestDTO> requestEntity = new HttpEntity<>(createRequest, createAuthHeaders(userToken));

        ResponseEntity<PetResponseDTO> response = restTemplate.postForEntity(
                petsBaseUrl, requestEntity, PetResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Pet");
        assertThat(response.getBody().type()).isEqualTo(PetTypeEnum.SAN_BERNARDO);
        assertThat(response.getBody().userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Admin: should create a pet successfully")
    void adminShouldCreatePetSuccessfully() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("Dragon Admin", PetTypeEnum.SAN_BERNARDO);
        HttpEntity<CreatePetRequestDTO> requestEntity = new HttpEntity<>(createRequest, createAuthHeaders(adminToken));

        ResponseEntity<PetResponseDTO> response = restTemplate.postForEntity(
                petsBaseUrl, requestEntity, PetResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Dragon Admin");
        assertThat(response.getBody().type()).isEqualTo(PetTypeEnum.SAN_BERNARDO);
        assertThat(response.getBody().userId()).isEqualTo(adminId);
    }

    @Test
    @DisplayName("User: should obtain only your own pets")
    void userShouldGetOnlyOwnPets() {
        CreatePetRequestDTO userPetRequest = new CreatePetRequestDTO("UserPet", PetTypeEnum.SAN_BERNARDO);
        restTemplate.postForEntity(petsBaseUrl, new HttpEntity<>(userPetRequest, createAuthHeaders(userToken)), PetResponseDTO.class);

        CreatePetRequestDTO adminPetRequest = new CreatePetRequestDTO("AdminPet", PetTypeEnum.SAN_BERNARDO);
        restTemplate.postForEntity(petsBaseUrl, new HttpEntity<>(adminPetRequest, createAuthHeaders(adminToken)), PetResponseDTO.class);

        ResponseEntity<List> response = restTemplate.exchange(
                petsBaseUrl + "/my-pets", HttpMethod.GET, new HttpEntity<>(createAuthHeaders(userToken)), List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        Map<String, Object> pet = (Map<String, Object>) response.getBody().get(0);
        assertThat(pet.get("name")).isEqualTo("UserPet");
        assertThat(pet.get("userId")).isEqualTo(userId.intValue());
    }

    @Test
    @DisplayName("Admin: You should get all system pets")
    void adminShouldGetAllPets() {
        CreatePetRequestDTO userPetRequest = new CreatePetRequestDTO("UserPet", PetTypeEnum.SAN_BERNARDO);
        restTemplate.postForEntity(petsBaseUrl, new HttpEntity<>(userPetRequest, createAuthHeaders(userToken)), PetResponseDTO.class);

        CreatePetRequestDTO adminPetRequest = new CreatePetRequestDTO("AdminPet", PetTypeEnum.SAN_BERNARDO);
        restTemplate.postForEntity(petsBaseUrl, new HttpEntity<>(adminPetRequest, createAuthHeaders(adminToken)), PetResponseDTO.class);

        ResponseEntity<List> response = restTemplate.exchange(
                petsBaseUrl + "/all", HttpMethod.GET, new HttpEntity<>(createAuthHeaders(adminToken)), List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("User: You should get your own pet by ID")
    void userShouldGetOwnPetById() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("UserPetById", PetTypeEnum.SAN_BERNARDO);
        ResponseEntity<PetResponseDTO> createResponse = restTemplate.postForEntity(
                petsBaseUrl, new HttpEntity<>(createRequest, createAuthHeaders(userToken)), PetResponseDTO.class);
        Long petId = createResponse.getBody().id();

        ResponseEntity<PetResponseDTO> response = restTemplate.exchange(
                petsBaseUrl + "/" + petId, HttpMethod.GET, new HttpEntity<>(createAuthHeaders(userToken)), PetResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(petId);
        assertThat(response.getBody().name()).isEqualTo("UserPetById");
    }

    @Test
    @DisplayName("User: shouldn't get another user's pet by ID")
    void userShouldNotGetOtherUserPetById() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("AdminPetById", PetTypeEnum.SAN_BERNARDO);
        ResponseEntity<PetResponseDTO> createResponse = restTemplate.postForEntity(
                petsBaseUrl, new HttpEntity<>(createRequest, createAuthHeaders(adminToken)), PetResponseDTO.class);
        Long adminPetId = createResponse.getBody().id();

        ResponseEntity<Map> response = restTemplate.exchange(
                petsBaseUrl + "/" + adminPetId, HttpMethod.GET, new HttpEntity<>(createAuthHeaders(userToken)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Admin: you should get any pet by ID")
    void adminShouldGetAnyPetById() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("UserPetForAdmin", PetTypeEnum.SAN_BERNARDO);
        ResponseEntity<PetResponseDTO> createResponse = restTemplate.postForEntity(
                petsBaseUrl, new HttpEntity<>(createRequest, createAuthHeaders(userToken)), PetResponseDTO.class);
        Long userPetId = createResponse.getBody().id();

        ResponseEntity<PetResponseDTO> response = restTemplate.exchange(
                petsBaseUrl + "/" + userPetId, HttpMethod.GET, new HttpEntity<>(createAuthHeaders(adminToken)), PetResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(userPetId);
        assertThat(response.getBody().name()).isEqualTo("UserPetForAdmin");
    }

    @Test
    @DisplayName("You should update your own pet ")
    void userShouldUpdateOwnPet() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("PetToUpdate", PetTypeEnum.SAN_BERNARDO);
        ResponseEntity<PetResponseDTO> createResponse = restTemplate.postForEntity(
                petsBaseUrl, new HttpEntity<>(createRequest, createAuthHeaders(userToken)), PetResponseDTO.class);
        Long petId = createResponse.getBody().id();

        UpdatePetRequestDTO updateRequest = new UpdatePetRequestDTO("UpdatedPet", MoodEnum.HAPPY, 90, 10);
        HttpEntity<UpdatePetRequestDTO> requestEntity = new HttpEntity<>(updateRequest, createAuthHeaders(userToken));

        ResponseEntity<PetResponseDTO> response = restTemplate.exchange(
                petsBaseUrl + "/" + petId, HttpMethod.PUT, requestEntity, PetResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(petId);
        assertThat(response.getBody().name()).isEqualTo("UpdatedPet");
        assertThat(response.getBody().mood()).isEqualTo(MoodEnum.HAPPY);
        assertThat(response.getBody().energyLevel()).isEqualTo(90);
        assertThat(response.getBody().hungerLevel()).isEqualTo(10);
    }

    @Test
    @DisplayName("User: I shouldn't update another user's pet")
    void userShouldNotUpdateOtherUserPet() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("AdminPetToUpdate", PetTypeEnum.SAN_BERNARDO);
        ResponseEntity<PetResponseDTO> createResponse = restTemplate.postForEntity(
                petsBaseUrl, new HttpEntity<>(createRequest, createAuthHeaders(adminToken)), PetResponseDTO.class);
        Long adminPetId = createResponse.getBody().id();

        UpdatePetRequestDTO updateRequest = new UpdatePetRequestDTO("AttemptedUpdate", null, null, null);
        HttpEntity<UpdatePetRequestDTO> requestEntity = new HttpEntity<>(updateRequest, createAuthHeaders(userToken));

        ResponseEntity<Map> response = restTemplate.exchange(
                petsBaseUrl + "/" + adminPetId, HttpMethod.PUT, requestEntity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Admin: should update any pet")
    void adminShouldUpdateAnyPet() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("UserPetForAdminUpdate", PetTypeEnum.SAN_BERNARDO);
        ResponseEntity<PetResponseDTO> createResponse = restTemplate.postForEntity(
                petsBaseUrl, new HttpEntity<>(createRequest, createAuthHeaders(userToken)), PetResponseDTO.class);
        Long userPetId = createResponse.getBody().id();

        UpdatePetRequestDTO updateRequest = new UpdatePetRequestDTO("UpdatedByUserAdmin", MoodEnum.SAD, 50, 50);
        HttpEntity<UpdatePetRequestDTO> requestEntity = new HttpEntity<>(updateRequest, createAuthHeaders(adminToken));

        ResponseEntity<PetResponseDTO> response = restTemplate.exchange(
                petsBaseUrl + "/" + userPetId, HttpMethod.PUT, requestEntity, PetResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(userPetId);
        assertThat(response.getBody().name()).isEqualTo("UpdatedByUserAdmin");
        assertThat(response.getBody().mood()).isEqualTo(MoodEnum.SAD);
    }

    @Test
    @DisplayName("User: You should eliminate your own pet")
    void userShouldDeleteOwnPet() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("PetToDelete", PetTypeEnum.SAN_BERNARDO);
        ResponseEntity<PetResponseDTO> createResponse = restTemplate.postForEntity(
                petsBaseUrl, new HttpEntity<>(createRequest, createAuthHeaders(userToken)), PetResponseDTO.class);
        Long petId = createResponse.getBody().id();

        ResponseEntity<Void> response = restTemplate.exchange(
                petsBaseUrl + "/" + petId, HttpMethod.DELETE, new HttpEntity<>(createAuthHeaders(userToken)), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(petRepository.findById(petId)).isEmpty();
    }

    @Test
    @DisplayName("User: shouldn't eliminate another user's pet \"")
    void userShouldNotDeleteOtherUserPet() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("AdminPetToDelete", PetTypeEnum.SAN_BERNARDO);
        ResponseEntity<PetResponseDTO> createResponse = restTemplate.postForEntity(
                petsBaseUrl, new HttpEntity<>(createRequest, createAuthHeaders(adminToken)), PetResponseDTO.class);
        Long adminPetId = createResponse.getBody().id();

        ResponseEntity<Map> response = restTemplate.exchange(
                petsBaseUrl + "/" + adminPetId, HttpMethod.DELETE, new HttpEntity<>(createAuthHeaders(userToken)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(petRepository.findById(adminPetId)).isPresent();
    }

    @Test
    @DisplayName("Admin: should eliminate any pet")
    void adminShouldDeleteAnyPet() {
        CreatePetRequestDTO createRequest = new CreatePetRequestDTO("UserPetForAdminDelete", PetTypeEnum.SAN_BERNARDO);
        ResponseEntity<PetResponseDTO> createResponse = restTemplate.postForEntity(
                petsBaseUrl, new HttpEntity<>(createRequest, createAuthHeaders(userToken)), PetResponseDTO.class);
        Long userPetId = createResponse.getBody().id();

        ResponseEntity<Void> response = restTemplate.exchange(
                petsBaseUrl + "/" + userPetId, HttpMethod.DELETE, new HttpEntity<>(createAuthHeaders(adminToken)), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(petRepository.findById(userPetId)).isEmpty();
    }
}
