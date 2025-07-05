package S502.virtualPets.integration;

import S502.virtualPets.VirtualpetsApplication;
import S502.virtualPets.dto.AuthCreateRoleRequestDTO;
import S502.virtualPets.dto.AuthCreateUserRequestDTO;
import S502.virtualPets.dto.AuthLoginRequestDTO;
import S502.virtualPets.dto.AuthResponseDTO;
import S502.virtualPets.persistence.repository.RoleRepository;
import S502.virtualPets.persistence.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = VirtualpetsApplication.class)
@Transactional
@ActiveProfiles("test")
@DisplayName("Integration tests for user authentication")
public class AuthApiIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/auth";

        if (roleRepository.count() == 0) {
            roleRepository.save(new S502.virtualPets.persistence.entity.RoleEntity(0, S502.virtualPets.persistence.enums.RoleEnum.ADMIN, null));
            roleRepository.save(new S502.virtualPets.persistence.entity.RoleEntity(0, S502.virtualPets.persistence.enums.RoleEnum.USER, null));
        }
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    @DisplayName("I should register a new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        AuthCreateUserRequestDTO registerRequest = new AuthCreateUserRequestDTO(
                "testuser", "password123", new AuthCreateRoleRequestDTO(List.of("USER")));

        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
                baseUrl + "/sign-up", registerRequest, AuthResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("testuser");
        assertThat(response.getBody().jwt()).isNotBlank();
        assertThat(response.getBody().status()).isTrue();
    }

    @Test
    @DisplayName("I should not register user if the username already exists")
    void shouldNotRegisterUserIfUsernameAlreadyExists() {
        AuthCreateUserRequestDTO registerRequest = new AuthCreateUserRequestDTO(
                "existinguser", "password123", new AuthCreateRoleRequestDTO(List.of("USER")));
        restTemplate.postForEntity(baseUrl + "/sign-up", registerRequest, AuthResponseDTO.class);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/sign-up", registerRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("I should allow the login of an existing user and return a JWT")
    void shouldLoginExistingUserAndReturnJwt() {
        AuthCreateUserRequestDTO registerRequest = new AuthCreateUserRequestDTO(
                "loginuser", "password456", new AuthCreateRoleRequestDTO(List.of("USER")));
        restTemplate.postForEntity(baseUrl + "/sign-up", registerRequest, AuthResponseDTO.class);

        AuthLoginRequestDTO loginRequest = new AuthLoginRequestDTO("loginuser", "password456");
        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
                baseUrl + "/log-in", loginRequest, AuthResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("loginuser");
        assertThat(response.getBody().jwt()).isNotBlank();
        assertThat(response.getBody().status()).isTrue();
    }

    @Test
    @DisplayName("I should not allow login with invalid credentials")
    void shouldNotLoginWithInvalidCredentials() {
        AuthLoginRequestDTO loginRequest = new AuthLoginRequestDTO("nonexistent", "wrongpassword");
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/log-in", loginRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
    }
}

