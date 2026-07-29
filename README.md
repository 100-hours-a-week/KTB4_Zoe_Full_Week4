# kr.adapterz backend

## Database configuration

The application uses Flyway for schema migrations and requires an explicit
MySQL connection outside automated tests.

Set the following environment variables before starting the application:

```text
DB_URL=jdbc:mysql://localhost:3306/votle
DB_USERNAME=votle_app
DB_PASSWORD=change-me
JWT_SECRET=replace-with-secure-random-value
```

Flyway migrations are stored in `src/main/resources/db/migration`. Hibernate
validates the migrated schema and does not create or update tables.

Automated tests use H2 in MySQL compatibility mode. The MySQL migration test
uses Testcontainers when Docker is available.

```shell
./gradlew test
./gradlew mysqlTest
```

## Container image

Build the production image from the repository root:

```shell
docker build -t votle-backend:local .
```

The runtime container runs as a non-root user, stores uploaded files below
`/app/uploads`, and exposes the health endpoint at `/actuator/health`.
