# 🐳 Complete Docker Guide: Student Management System

Welcome to Docker! This guide explains **how Docker works**, **how this project is containerized**, and **how to run and manage it** step-by-step.

---

## 💡 1. Why Did We Use Docker Here?

Earlier, you asked if this project would run on **Tomcat 9**. 
* **The issue:** Tomcat 9 uses the older **`javax.servlet`** specification (Java EE 8), whereas this project is built on modern **`jakarta.servlet`** (Jakarta EE 10 / Servlet 6.0). Running it on Tomcat 9 causes `ClassNotFoundException` errors.
* **The problem with converting:** Downgrading the source code means rewriting imports, updating descriptors, and moving backward in technology.
* **The Docker solution:** Instead of modifying your code or messing with your computer's installed servers, Docker creates an **isolated mini-environment (container)** that runs **Tomcat 10.1 + Java 17 + MySQL 8.0**. Your host machine doesn't need to change anything!

---

## 🧠 2. Docker Concepts Explained Simply

| Concept | Real-World Analogy | What It Is in This Project |
| :--- | :--- | :--- |
| **Dockerfile** | A Recipe Book | A text file containing instructions to compile your code with Maven and put the `.war` onto Tomcat 10.1. |
| **Docker Image** | A Blueprint / ISO File | A package holding everything needed to run: OS libraries, Java 17, Tomcat 10.1, and your compiled code. |
| **Docker Container** | A Running Computer | A live, running instance of an Image. It behaves like its own isolated machine. |
| **Docker Compose** | The Conductor of an Orchestra | A tool (`docker-compose.yml`) that starts and connects multiple containers together (e.g. **Tomcat App** + **MySQL Database**). |
| **Volumes** | An External Hard Drive | Persistent storage that keeps your MySQL data safe even when containers are stopped or deleted. |

---

## 🏗️ 3. Architecture & Port Mapping

Your computer already had services running on standard ports:
- Port `8080` was used by your local Tomcat 9.
- Port `3306` was used by your local MySQL.

To prevent any port collisions, Docker maps ports like this:

```
+-------------------------------------------------------------+
| HOST COMPUTER (Your PC)                                     |
|                                                             |
|   Browser: http://localhost:8085                            |
|             │                                               |
|             ▼                                               |
|      [Port 8085] ──maps to──► [Port 8080 inside App]        |
|                                ┌────────────────────────┐   |
|                                │ student_management_app │   |
|                                │ (Tomcat 10.1 + JDK 17) │   |
|                                └───────────┬────────────┘   |
|                                            │ Internal DNS:  |
|                                            │ "db:3306"      |
|                                            ▼                |
|      [Port 3307] ──maps to──► [Port 3306 inside DB]         |
|                                ┌────────────────────────┐   |
|                                │ student_management_db  │   |
|                                │ (MySQL 8.0)            │   |
|                                └────────────────────────┘   |
+-------------------------------------------------------------+
```

* Inside Docker, `app` communicates with `db` directly using the container name `db:3306`.
* On your browser, you access the application at **`http://localhost:8085/`**.

---

## 🚀 4. How to Run the Project

Make sure Docker Desktop is open and running.

### Start the Entire System (Build + Run)
Open a terminal (PowerShell or Command Prompt) in the project root directory and run:

```bash
docker compose up -d --build
```

**What each flag does:**
* `up`: Creates and starts the containers.
* `-d` (*detached*): Runs the containers in the background so your terminal remains free.
* `--build`: Forces Docker to re-compile your code and rebuild the image if you made any code changes.

---

## 🌐 5. Accessing the Application

Once started, open your web browser and go to:
👉 **[http://localhost:8085/](http://localhost:8085/)**

It will automatically take you to the login screen.

### 🔑 Default Login Credentials

| Role | Username | Password |
| :--- | :--- | :--- |
| **Administrator** | `admin` | `admin` |
| **Faculty** | `faculty` | `faculty123` |
| **Supervisor** | `supervisor` | `supervisor123` |
| **Registrar** | `registrar` | `registrar123` |
| **Accounts** | `accounts` | `accounts123` |

*(Student accounts can be registered using the **Sign Up as Student** link on the login page).*

---

## 🛠️ 6. Useful Docker Commands to Learn

### Check Status of Containers
See which containers are currently running:
```bash
docker compose ps
```

### View Live Application Logs
If you want to see console output or debug errors:
```bash
# View Tomcat logs
docker compose logs -f app

# View MySQL logs
docker compose logs -f db
```
*(Press `Ctrl + C` to stop watching logs).*

### Stop the Containers
When you are done working:
```bash
docker compose stop
```
*(This pauses the containers without deleting any data).*

### Start Them Back Up
```bash
docker compose start
```

### Completely Remove Containers (Keeps Database Data)
```bash
docker compose down
```

### Reset Everything (Including Database)
If you ever want to wipe out the database volume and start fresh with a clean install:
```bash
docker compose down -v
docker compose up -d --build
```

---

## 🔍 7. How the Files Work Together

### `Dockerfile` (Multi-stage build)
1. **Stage 1 (`builder`)**: Starts a `maven:3.9.6-eclipse-temurin-17` container, copies your `pom.xml` and `src/`, and runs `mvn clean package -DskipTests` to generate `StudentManagement.war`.
2. **Stage 2 (`runner`)**: Starts a clean `tomcat:10.1-jdk17` container, removes default demo apps, and copies the WAR file as `ROOT.war`.
3. When the container boots, Tomcat starts and immediately serves your app at `/`.

### `docker-compose.yml`
* Defines two services: `db` (MySQL) and `app` (Tomcat).
* Configures MySQL to run `database.sql` automatically on initial setup.
* Passes the database URL (`jdbc:mysql://db:3306/student_management`) and credentials into `app` through environment variables.
* Waits for `db` to pass its healthcheck before starting `app`.

### `.dockerignore`
* Prevents temporary files, local build outputs (`target/`), and local Tomcat folders from being copied into the container during the build, keeping build times fast.
