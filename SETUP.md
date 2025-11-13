# Setup Instructions

## Prerequisites

1. **Java 17+** - Install JDK 17 or higher
2. **Maven 3.6+** - Install Apache Maven
3. **MySQL 8.0+** - Install MySQL Server
4. **MySQL Workbench** - For database management
5. **Postman** - For API testing

## Database Setup

### Step 1: Create MySQL Database

1. Open MySQL Workbench
2. Connect to your MySQL server (usually `localhost:3306`)
3. Create a new database for development:

```sql
CREATE DATABASE trust_db_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 2: Update Database Credentials

The project uses environment-specific configuration files:

- **`application.properties`** - Base configuration (common settings)
- **`application-dev.properties`** - Development environment
- **`application-qa.properties`** - QA environment  
- **`application-prod.properties`** - Production environment

**For Development:**
Edit `src/main/resources/application-dev.properties` and update:

```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

Default values in dev:
- Username: `root`
- Password: `root`
- Database: `trust_db_dev`

**To switch environments**, change the active profile in `application.properties`:
```properties
spring.profiles.active=dev  # or qa, or prod
```

## Running the Application

### Option 1: Using Maven (Development Profile)

```bash
mvn spring-boot:run
```

This will use the `dev` profile by default (as set in `application.properties`).

### Running with Specific Profile

**Development:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**QA:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=qa
```

**Production:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Option 2: Using Java

First, build the application:
```bash
mvn clean package
```

Then run with specific profile:
```bash
# Development
java -jar target/trust-app-backend-1.0.0.jar --spring.profiles.active=dev

# QA
java -jar target/trust-app-backend-1.0.0.jar --spring.profiles.active=qa

# Production
java -jar target/trust-app-backend-1.0.0.jar --spring.profiles.active=prod
```

### Option 3: Using IDE (IntelliJ IDEA / Eclipse)

1. Import the project as a Maven project
2. Run the `TrustAppApplication` class (main method)
3. To change profile in IDE:
   - **IntelliJ IDEA**: Run → Edit Configurations → Environment variables → Add `SPRING_PROFILES_ACTIVE=dev`
   - **Eclipse**: Run Configurations → Arguments → VM arguments → Add `-Dspring.profiles.active=dev`

## Verify Application is Running

The application will start on **http://localhost:8080**

### Test Endpoints

1. **Health Check:**
   ```
   GET http://localhost:8080/api/health
   ```


## Swagger UI

Once the application is running, access Swagger UI at:
```
http://localhost:8080/api/swagger-ui.html
```

## Testing with Postman

1. Open Postman
2. Create a new GET request
3. Enter URL: `http://localhost:8080/api/health`
4. Click "Send"
5. You should receive a JSON response with status "UP"

### Sample Postman Collection

You can test this endpoint:

- **GET** `http://localhost:8080/api/health`

## Environment Configuration

### Development (dev)
- Database: `trust_db_dev`
- Default credentials: root/root
- Verbose logging enabled
- Swagger UI enabled
- Connection pool: 2-10 connections

### QA (qa)
- Database: `trust_db_qa`
- Update credentials in `application-qa.properties`
- Moderate logging
- Swagger UI enabled
- Connection pool: 5-20 connections

### Production (prod)
- Database: `trust_db`
- Use environment variables for credentials: `DB_USERNAME`, `DB_PASSWORD`
- Minimal logging
- Swagger UI disabled
- Connection pool: 10-50 connections
- SSL required for database
- Security headers enabled

### Setting Environment Variables (Production)

For production, set these environment variables:
```bash
export DB_USERNAME=your_prod_username
export DB_PASSWORD=your_prod_password
export ALLOWED_ORIGINS=https://yourdomain.com
```

## Troubleshooting

### Database Connection Issues

1. **Check MySQL is running:**
   ```bash
   # Windows
   net start MySQL80
   
   # Linux/Mac
   sudo systemctl status mysql
   ```

2. **Verify database exists:**
   - Open MySQL Workbench
   - Check if `trust_db_dev` database exists (for dev profile)

3. **Check credentials:**
   - Verify username and password in `application-dev.properties` (for dev profile)
   - Test connection in MySQL Workbench

### Port Already in Use

If port 8080 is already in use, change it in `application.properties`:
```properties
server.port=8081
```

### Maven Build Issues

1. Clean and rebuild:
   ```bash
   mvn clean install
   ```

2. Check Java version:
   ```bash
   java -version
   ```
   Should be Java 17 or higher.

## Next Steps

After verifying the application runs successfully:

1. Create database tables (as per master data implementation document)
2. Implement master data modules
3. Add authentication and security
4. Implement business logic modules

