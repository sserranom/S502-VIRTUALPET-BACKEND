package S502.virtualPets.controller;

import S502.virtualPets.dto.AuthCreateUserRequestDTO;
import S502.virtualPets.dto.AuthLoginRequestDTO;
import S502.virtualPets.dto.AuthResponseDTO;
import S502.virtualPets.service.UserDetailServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for registration and user login.")
public class AuthenticationController {

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @PostMapping("/sign-up")
    @Operation(summary = "Register a new user", description = "Create a new user account and return a JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully registered user.",
                content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or username already exists.",
            content = @Content(schema = @Schema(implementation = Map.class)))

    })
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid AuthCreateUserRequestDTO userRequest){
        return new ResponseEntity<>(this.userDetailService.createUser(userRequest), HttpStatus.CREATED);
    }

    @PostMapping("/log-in")
    @Operation(summary = "User log", description = "Authentic to the user and return a JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login and JWT generated.",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthLoginRequestDTO userRequest){
        return new ResponseEntity<>(this.userDetailService.loginUser(userRequest), HttpStatus.OK);
    }
}
