# User Profile Information Store

The **User Profile Information Store** is a microservice within the [Distributed System Backend (DSW Backend)](https://github.com/ASE-group10/dsw-backend). It manages user profiles, preferences, and provides route-related queries.

---

## Getting Started

This service is part of the `dsw-backend` repository. Follow the [main repository's README](https://github.com/ASE-group10/dsw-backend) for instructions on:
- Cloning the repository
- Setting up submodules
- Running the full system with Docker Compose

---

## Running the Service Locally
To run this service independently:
1. Navigate to the service directory:
   ```bash
   cd user-profile-information-store
   ```
2. Run the service:
   ```bash
   mvn spring-boot:run
   ```

---

## API Documentation

This service uses Swagger for API documentation. Once the service is running, visit the Swagger UI for detailed API information and testing:
```plaintext
http://<host>:<port>/swagger-ui.html
```

---

## Environment Configuration

Environment variables are managed via the `.env` file in the root of the `dsw-backend` repository. Ensure all required variables are defined, including:
- `AUTH0_DOMAIN`
- `AUTH0_CLIENT_ID`
- `AUTH0_CLIENT_SECRET`
- Database configuration (`DB_HOST`, `DB_PORT`, etc.)

Sensitive files like `application.properties` are excluded from version control. Use the provided `application.example.properties` as a reference.

---

## Notes

- This service requires an Auth0 configuration for authentication.
- Use the `.env` file for environment-specific settings.
- For API usage details, refer to the Swagger UI.

