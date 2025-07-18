# Runnify

Let's be honest, you just came for these:

```
docker-compose up postgres -d
./mvnw spring-boot:run

```

And later,

```
docker-compose down -v
```

**GPX meets Spotify**

(scottMorrison.jpeg) _How good is Kotlin?_. Only one way to find out. This project is for me to map a Spotify playlist to a GPX file.

## Quick notes:

When you run `docker-compose up`, the postgres container automatically creates the `runnify` user and `runnify` database (see docker-compose.yml for this)
Can connect to the database using `psql -h localhost -p 5433 -U runnify -d runnify`.

## Running the Application

(Make sure you've changed `env.template` to `.env` and filled it in)

### 1. All the Docker!

This starts both PostgreSQL and the application:

```bash
cd backend
docker-compose up --build
```

Serves at: http://localhost:8080

### Option 2: Dev mode (Only Postgres in docker, app is local)

1. **Start docker only with Postgres:**

   ```bash
   cd backend
   docker-compose up postgres -d
   ```

2. **Run the application locally:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

### Option 3: Locals only

1. **Install and start PostgreSQL locally**
2. **Update application.yml or use environment variables:**

   ```bash
   export DB_HOST=localhost
   export DB_PORT=5433
   export SPOTIFY_CLIENT_ID=your_client_id
   export SPOTIFY_CLIENT_SECRET=your_client_secret
   ```

3. **Run the application:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

## Database

- **Local Port:** 5433 (mapped from container's 5433)
  Note: I use 5433 because my work things are usually 5432.
- **Database Name:** runnify
- **Username:** runnify
- **Password:** runnify

Want to connect to the database in terminal for whatever reason?:

```bash
psql -h localhost -p 5433 -U runnify -d runnify
```

## Development

### Building the Project

```bash
cd backend
./mvnw clean install
```

### Running Tests

```bash
cd backend
./mvnw test
```

### Hot Reload Development

```bash
cd backend
./mvnw spring-boot:run
```

Catches changes, restarts the app.

## Docker Commands

Because I absolutely forget these.

```bash
# Start everything
docker-compose up --build

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f

# Stop everything
docker-compose down

# Remove volumes (clean database)
docker-compose down -v
```
