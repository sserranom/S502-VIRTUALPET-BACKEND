package S502.virtualPets.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Virtual Pet API",
                description = "API Restful to manage virtual pets, including JWT authentication and Based Access Control.",
                version = "1.0.0",
                contact = @Contact(
                        name = "Sergio Serrano",
                        email = "sergiomelgara@gmail.com",
                        url = "https://github.com/sserranom/S502-VIRTUALPET-BACKEND.git"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )

        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server")
        }
)

@SecurityScheme(
        name = "bearerAuth",
        description = "JWT de autenticación requerido para acceder a los endpoints protegidos. Prefijo 'Bearer '.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
}
