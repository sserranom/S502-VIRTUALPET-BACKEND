## S502-VIRTUALPET-BACKEND

# 🐾 Virtual Pets API

RESTful API for virtual pet management, developed with Java 21 and Spring Boot 3. It implements JWT-based authentication and authorization, role-based access control, and Swagger documentation. Designed to be a solid, secure, and extensible architecture.

---

## 🚀 Technologies Used

- ☕ Java 21
- 🧩 Spring Boot 3
- 🔐 Spring Security (JWT)
- 🗃️ Spring Data JPA + MySQL
- 📦 Caffeine Cache
- 📝 Swagger / OpenAPI
- 🛡️ Bean Validation (Jakarta)
- 💡 Lombok + SLF4J
- 🧪 Postman para pruebas

---

## 📦 Main Features

- User registration and login
- JWT-based authentication
- Role-based authorization (USER, ADMIN)
- CRUD operations for virtual pets
- Data validation in DTOs
- Interactive API documentation with Swagger
- Global exception handling with custom messages
- Role and permission caching for high performance
- Professional, production-ready architecture

---

## 🔐 Security

- Authentication via JWT token (Bearer)
- Declarative authorization using `@PreAuthorize`
- Passwords encrypted with `BCryptPasswordEncoder`
- Loading roles/permissions from the database
- Permission caching strategy using Caffeine

---

## 🔄 Endpoints Principales

| Método | Endpoint               | Descripción                 | Rol requerido |
|--------|------------------------|-----------------------------|---------------|
| POST   | /auth/sign-up          | User Registration           | Public        |
| POST   | /auth/log-in           | Authentication (Login)      | Public        |
| GET    | /api/pets              | List All Virtual Pets       | USER / ADMIN  |
| POST   | /pets                  | Create New Virtual Pet      | ADMIN / ADMIN |
| PUT    | /pets/{id}             | Update Virtual Pet          | ADMIN / ADMIN |
| DELETE | /pets/{id}             | Delete Virtual Pet          | ADMIN /ADMIN  |

---


## 📄 Swagger Documentation

> Accessible locally at:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

Includes:  
- Endpoint exploration  
- Parameters and response schemas  
- Requires JWT token for protected routes (authenticate first)


 ## ⚙️ Cómo Ejecutar Localmente

1. Clone the repository:

`bash
git clone https://github.com/tuusuario/virtual-pets-api.git
cd virtual-pets-api`

2.Configure the MySQL databaseL:

`CREATE DATABASE virtual_pets;`
sql
Copiar
Editar

3. Configure application.yml or .properties with your MySQL credentials.

4. Run the app with Maven:

`./mvnw spring-boot:run`

Access Swagger UI:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)




