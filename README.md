# Personal Blog Lite

A lightweight personal blog with public read access and admin panel for content management. Built with Vaadin 25, Java 21, and MariaDB.

## Features

- **Public Views**: Home page with post list, individual post detail view
- **Admin Panel**: Dashboard to manage posts (create, edit, delete)
- **HTTP Basic Auth**: Secure admin access via environment variables
- **Markdown Support**: Write posts in Markdown, rendered to HTML
- **Responsive Design**: Clean, minimal interface using Vaadin Lumo theme

## Tech Stack

| Layer | Technology |
|-------|-------------|
| Frontend | Vaadin 25.1 (Flow) |
| Backend | Java 21 |
| Database | MariaDB 11.7 |
| ORM | jOOQ 3.21 |
| Migrations | Flyway 12 |
| Server | Jetty 12 (vaadin-boot) |
| Build | Gradle (Groovy DSL) |

## Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)

### 1. Clone and Configure

```bash
git clone <repo-url>
cp .env.example .env
```

Edit `.env` with your credentials:

```bash
DB_ROOT_PASSWORD=your_root_password
DB_NAME=blog_lite
DB_USER=blog_user
DB_PASSWORD=your_password
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_admin_password
```

### 2. Start Database

```bash
docker compose up -d db
```

### 3. Run Application

```bash
./gradlew run
```

Open http://localhost:8080

### 4. Admin Access

Click the **Admin** button in the header or navigate to `/admin/dashboard`. You'll be prompted for HTTP Basic Auth credentials.

## Screenshots

### Home Page

![Home Page](assets/home1.png)
![Home Page](assets/home2.png)

### Post Detail

![Post View](assets/post_view1.png)
![Post View](assets/post_view2.png)

### Admin Dashboard

![Admin Dashboard](assets/dashboard_admin.png)

### Post Management

![New Post](assets/new_post.png)
![Edit Post](assets/edit_post.png)
![Delete Post](assets/delete_post.png)

## Project Structure

```
src/main/java/io/vekzzdev/personal_blog_lite/
├── Main.java                    # Entry point
├── config/                      # Bootstrap, InstantiatorFactory
├── model/                       # Domain models (Post)
├── service/                     # Business logic (PostService, MarkdownService)
├── repository/                  # Repository interfaces
│   └── jooq/                    # jOOQ implementations
├── security/                    # BasicAuthFilter
├── ui/
│   ├── components/              # Reusable components (BlogHeader)
│   └── view/                    # Vaadin views
│       ├── HomeView.java        # Public: post list
│       ├── PostDetailView.java  # Public: single post
│       └── admin/               # Admin views (protected)
└── generated/jooq/             # jOOQ generated code
```

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DB_URL` | JDBC database URL | Yes |
| `DB_USER` | Database username | Yes |
| `DB_PASSWORD` | Database password | Yes |
| `ADMIN_USERNAME` | Admin HTTP Basic Auth username | Yes |
| `ADMIN_PASSWORD` | Admin HTTP Basic Auth password | Yes |

## Docker Deployment

```bash
docker compose up -d
```

The app will be available at http://localhost:8080

## Development

### Run Tests

```bash
./gradlew test
```

### Generate jOOQ Code

```bash
./gradlew jooqCodegen
```

### Build Production

```bash
./gradlew clean build -Pvaadin.productionMode
```

## License

MIT
