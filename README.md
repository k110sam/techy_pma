# ⬡ Techy PMA — Project Management System

![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue?style=flat-square)
![SQLite](https://img.shields.io/badge/SQLite-3.45.1-green?style=flat-square&logo=sqlite)
![Maven](https://img.shields.io/badge/Maven-3.8.x-red?style=flat-square&logo=apachemaven)
![BCrypt](https://img.shields.io/badge/BCrypt-0.4-purple?style=flat-square)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-2025.3.2-black?style=flat-square&logo=intellijidea)

> A fully functional desktop Project Management System built with Java and JavaFX. Users can register, authenticate securely, create projects, join teams, track progress, and manage project status — all from a clean, modern interface.

---

## 📸 Screens

| Screen | Description |
|--------|-------------|
| 🔐 Login | Secure authentication with BCrypt password verification |
| 📝 Signup | User registration with real-time password strength indicator |
| 📊 Dashboard | Overview of stats, quick actions, and current projects |
| ✦ Create Project | Form-based project creation with progress slider and status selector |
| ⊕ Browse Projects | Search, filter, and join available projects |
| 📋 Project Details | Full project info, member list, owner controls, and leave option |

---

## ✨ Features

- **User Authentication** — Signup and login with BCrypt-hashed passwords
- **Session Management** — Global session tracking across all screens
- **Project Creation** — Create projects with name, description, progress, and status
- **Project Discovery** — Browse and search all available projects
- **Team Joining** — Join projects with a single click
- **Role System** — Owner, Admin, and Member roles per project
- **Progress Tracking** — Visual progress bars and percentage indicators
- **Status Management** — Four statuses: Not Started, In Progress, Completed, Published
- **Owner Controls** — Update progress and status (Owner/Admin only)
- **Leave Project** — Members can leave projects with confirmation dialog
- **Responsive Layout** — Dark sidebar + clean content area design

---

## 🏗️ Architecture

The project follows a **layered MVC architecture** with the DAO pattern:

```
┌──────────────────────────────────────────────┐
│             UI Layer (JavaFX)                │
│   FXML + Controllers + CSS                   │
└─────────────────┬────────────────────────────┘
                  │
┌─────────────────▼────────────────────────────┐
│           Utils Layer                        │
│   Session.java  │  PasswordUtil.java         │
│   SelectedProject.java                       │
└─────────────────┬────────────────────────────┘
                  │
┌─────────────────▼────────────────────────────┐
│           Database Layer (DAO)               │
│   UserDAO  │  ProjectDAO  │  ProjectMemberDAO│
└─────────────────┬────────────────────────────┘
                  │
┌─────────────────▼────────────────────────────┐
│           Database                           │
│   SQLite  →  projectmanager.db               │
└──────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
techy_pma/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/sam/projectmanager/techy_pma/
│   │   │       ├── Main.java                        # Application entry point
│   │   │       ├── module-info.java                 # Java module declarations
│   │   │       │
│   │   │       ├── controllers/                     # JavaFX FXML Controllers
│   │   │       │   ├── LoginController.java
│   │   │       │   ├── SignupController.java
│   │   │       │   ├── DashboardController.java
│   │   │       │   ├── CreateProjectController.java
│   │   │       │   ├── BrowseProjectsController.java
│   │   │       │   └── ProjectDetailsController.java
│   │   │       │
│   │   │       ├── database/                        # DAO + Database Manager
│   │   │       │   ├── DatabaseManager.java
│   │   │       │   ├── UserDAO.java
│   │   │       │   ├── ProjectDAO.java
│   │   │       │   └── ProjectMemberDAO.java
│   │   │       │
│   │   │       ├── models/                          # Plain Java Objects (POJOs)
│   │   │       │   ├── User.java
│   │   │       │   ├── Project.java
│   │   │       │   └── ProjectMember.java
│   │   │       │
│   │   │       └── utils/                           # Shared Utilities
│   │   │           ├── PasswordUtil.java
│   │   │           ├── Session.java
│   │   │           └── SelectedProject.java
│   │   │
│   │   └── resources/
│   │       └── org/sam/projectmanager/techy_pma/
│   │           ├── fxml/                            # UI Layout Files
│   │           │   ├── login.fxml
│   │           │   ├── signup.fxml
│   │           │   ├── dashboard.fxml
│   │           │   ├── create-project.fxml
│   │           │   ├── browse-projects.fxml
│   │           │   └── project-details.fxml
│   │           │
│   │           └── css/                             # Stylesheets
│   │               ├── styles.css
│   │               └── dashboard.css
│
├── data/
│   └── projectmanager.db                            # SQLite database (auto-created)
│
└── pom.xml                                          # Maven build config
```

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java (OpenJDK) | 25.0.2 | Primary programming language |
| JavaFX | 21.0.6 | Desktop UI framework |
| SQLite (via JDBC) | 3.45.1.0 | Embedded file-based database |
| jBCrypt | 0.4 | Password hashing and verification |
| Maven | 3.8.x | Build automation and dependency management |
| IntelliJ IDEA | 2025.3.2 | IDE |

---

## 🗄️ Database Schema

### `users` table
| Column | Type | Constraints |
|--------|------|-------------|
| user_id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| username | TEXT | UNIQUE NOT NULL |
| email | TEXT | UNIQUE NOT NULL |
| password | TEXT | NOT NULL (BCrypt hash) |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP |

### `projects` table
| Column | Type | Constraints |
|--------|------|-------------|
| project_id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| project_name | TEXT | NOT NULL |
| project_description | TEXT | |
| project_progress | INTEGER | DEFAULT 0 |
| created_by | INTEGER | FK → users(user_id) |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP |
| status | TEXT | CHECK IN ('not started', 'in progress', 'completed', 'published') |

### `project_members` table
| Column | Type | Constraints |
|--------|------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| project_id | INTEGER | FK → projects(project_id) |
| user_id | INTEGER | FK → users(user_id) |
| role | TEXT | NOT NULL ('Owner', 'Admin', 'Member') |
| joined_at | DATETIME | DEFAULT CURRENT_TIMESTAMP |
| | | UNIQUE(project_id, user_id) |

---

## ⚙️ System Requirements

- **OS:** Windows 10/11, macOS 12+, or Ubuntu 20.04+
- **JDK:** 17 or 21+ (OpenJDK recommended)
- **IDE:** IntelliJ IDEA 2024.x or later
- **Build:** Maven 3.8+ (bundled with IntelliJ)
- **RAM:** 4 GB minimum (8 GB recommended)
- **Disk:** 500 MB free space

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/techy_pma.git
cd techy_pma
```

### 2. Open in IntelliJ IDEA

```
File → Open → Select the techy_pma folder → Click OK
```

### 3. Configure the JDK

```
File → Project Structure → Project → SDK → Select JDK 21
```

### 4. Reload Maven Dependencies

```
Right-click pom.xml → Maven → Reload Project
```

Or click the **circular arrows icon** in the top-right of the pom.xml editor.

### 5. Run the Application

```
Maven Panel (right side) → Plugins → javafx → javafx:run
```

Or right-click `Main.java` → **Run 'Main.main()'**

### 6. First Launch

On first launch the app will automatically:
- Create the `data/` directory
- Create `data/projectmanager.db`
- Initialize all three database tables

You'll land on the **Login screen** — click **Sign up here** to create your first account!

---

## 📦 Maven Dependencies

```xml
<!-- JavaFX Controls -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.6</version>
</dependency>

<!-- JavaFX FXML -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21.0.6</version>
</dependency>

<!-- SQLite JDBC Driver -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.45.1.0</version>
</dependency>

<!-- BCrypt Password Hashing -->
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
```

---

## 🔐 Security

- Passwords are **never stored in plain text**
- All passwords are hashed using **BCrypt** with automatic salt generation
- Each hash is unique even for identical passwords
- Password verification uses `BCrypt.checkpw()` — the original password is never reconstructed

```java
// Hashing (on signup)
String hash = PasswordUtil.hashPassword("myPassword");
// → "$2a$10$YIQTPXIWKM00e7Y06I2QUe..."

// Verifying (on login)
boolean valid = PasswordUtil.verifyPassword("myPassword", hash);
// → true
```

---

## 🗺️ User Flow

```
App Start
    │
    ▼
Login Screen ──────────────────────────────────────┐
    │                                               │
    │ Click "Sign up here"                          │ Enter credentials
    ▼                                               │
Signup Screen                                       │
    │                                               │
    │ Fill form → Validate → Hash → Save → Auto-login
    │                                               │
    └───────────────────────────────────────────────┘
                        │
                        ▼
                   Dashboard
                   │   │   │
          ─────────┘   │   └─────────
          │            │            │
          ▼            ▼            ▼
    Create Project  Browse      My Projects
          │         Projects         │
          │            │             │
          │       Join Project        │
          │            │             │
          └────────────┴─────────────┘
                        │
                        ▼
                 Project Details
                 (View, Update, Leave)
                        │
                        ▼
                      Logout
                        │
                        ▼
                   Login Screen
```

---

## 🧩 Key Design Patterns

### DAO Pattern (Data Access Object)
All database operations are encapsulated in dedicated DAO classes. Controllers never write SQL directly.

```java
// Clean controller code (no SQL):
User user = UserDAO.getUserByUsername("alice");
boolean joined = ProjectMemberDAO.isMember(projectId, userId);
```

### Session Pattern (Singleton)
A static Session class tracks the logged-in user across all screens without passing it between controllers.

```java
Session.setCurrentUser(user);        // After login
Session.getCurrentUser();            // Anywhere in the app
Session.getCurrentUserId();          // Quick ID access
Session.clearSession();              // On logout
```

### MVC (Model-View-Controller)
- **Model** → `models/` (User, Project, ProjectMember)
- **View** → `fxml/` + `css/` (layouts and styling)
- **Controller** → `controllers/` (event handling and navigation)

---

## ⚠️ Known Issues & Fixes

| Issue | Cause | Fix |
|-------|-------|-----|
| `IllegalStateException: Location is not set` | Wrong FXML resource path | Use full package path: `/org/sam/projectmanager/techy_pma/fxml/login.fxml` |
| `module not found: javafx.fxml` | Missing module-info.java declarations | Add `requires javafx.fxml` and `opens ... to javafx.fxml` |
| `boolean cannot be converted to int` | Return type mismatch between DAO and Controller | Match variable type to DAO return type |
| Tables not created in database | Silent SQL failure or init method not called | Add debug logging, verify `initializeDatabase()` is called in `start()` |
| Maven wrapper download warning | Network/firewall restriction | Ignore — IntelliJ uses bundled Maven automatically |
| SLF4J NOP logger warning | Missing SLF4J implementation | Add `slf4j-simple` dependency or safely ignore |

---

## 📚 References

| Resource | Link |
|----------|------|
| OpenJFX Official Docs | https://openjfx.io |
| Java 21 Documentation | https://docs.oracle.com/en/java/javase/21 |
| SQLite Documentation | https://sqlite.org/docs.html |
| Xerial SQLite JDBC | https://github.com/xerial/sqlite-jdbc |
| jBCrypt Library | https://www.mindrot.org/projects/jBCrypt |
| Maven Documentation | https://maven.apache.org/guides |
| Baeldung JavaFX Guide | https://www.baeldung.com/javafx |
| DB Browser for SQLite | https://sqlitebrowser.org |

---

## 📄 License

This project was built for educational purposes as part of a Java learning journey.

---

## 👤 Author

**Sam**
Built with ☕ Java + 💙 JavaFX + 🤖 AI pair programming (Claude by Anthropic)

---

> *"The best way to learn programming is to build something real."*
