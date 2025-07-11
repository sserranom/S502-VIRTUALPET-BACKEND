package S502.virtualPets.config;

import S502.virtualPets.config.filter.JwtTokenValidator;
import S502.virtualPets.service.UserDetailServiceImpl;
import S502.virtualPets.utils.JwtUtils;
import S502.virtualPets.persistence.repository.UserRepository;
import S502.virtualPets.persistence.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize y @PostAuthorize
public class SecurityConfig {

    // Inyecta los componentes que Spring gestiona como @Service o @Repository.
    // Estos @Autowired se resolverán automáticamente por Spring.
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtUtils jwtUtils; // JwtUtils es un @Component


    // Definimos JwtTokenValidator como un @Bean para que Spring pueda encontrarlo.
    @Bean
    public JwtTokenValidator jwtTokenValidator(UserDetailServiceImpl userDetailService) {
        // El constructor de JwtTokenValidator espera JwtUtils y UserDetailServiceImpl
        return new JwtTokenValidator(jwtUtils, userDetailService);
    }


    // ¡CORRECCIÓN CLAVE! Inyectamos JwtTokenValidator directamente como parámetro aquí.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtTokenValidator jwtTokenValidator) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para APIs REST sin estado
                .cors(Customizer.withDefaults()) // Habilita CORS usando la configuración del bean corsConfigurationSource()
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API sin estado
                .authorizeHttpRequests(authorize -> {
                    // Rutas públicas (no requieren autenticación)
                    authorize.requestMatchers(HttpMethod.POST, "/auth/**").permitAll(); // Login y registro

                    // ¡CRUCIAL PARA SWAGGER UI! Permite el acceso a las rutas de documentación
                    // Estas rutas deben estar ANTES de cualquier 'anyRequest().authenticated()'
                    authorize.requestMatchers(
                            "/swagger-ui/**",          // Interfaz de usuario de Swagger
                            "/v3/api-docs/**",         // Definición OpenAPI en formato JSON/YAML
                            "/swagger-resources/**",   // Recursos de Swagger
                            "/swagger-resources/configuration/ui", // Configuración de UI
                            "/swagger-resources/configuration/security", // Configuración de seguridad
                            "/webjars/**"              // Recursos estáticos de webjars (ej. jQuery, Bootstrap)
                    ).permitAll();

                    // Rutas protegidas: Cualquier otra petición requiere autenticación
                    authorize.anyRequest().authenticated();
                })
                // Añade tu filtro JWT antes del filtro de autenticación de usuario y contraseña de Spring Security
                // ¡CORRECCIÓN CLAVE! Usa el bean inyectado directamente como parámetro.
                .addFilterBefore(jwtTokenValidator, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite los orígenes de tu frontend. Si tienes más, añádelos aquí.
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        // Métodos HTTP permitidos para las peticiones CORS
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // Añadido PATCH
        // Cabeceras permitidas (ej. Authorization, Content-Type)
        configuration.setAllowedHeaders(List.of("*")); // Permite todas las cabeceras
        // Permite el envío de credenciales (ej. cookies, headers de autorización)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailServiceImpl userDetailService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Definir UserDetailServiceImpl como un bean para que Spring lo gestione.
    // Sus dependencias se pasan como parámetros, resolviendo la circularidad.
    @Bean
    public UserDetailServiceImpl userDetailService(UserRepository userRepository, JwtUtils jwtUtils, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        return new UserDetailServiceImpl(userRepository, jwtUtils, passwordEncoder, roleRepository);
    }
}