# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2025-04-03

### Added

- **Public Views**:
  - Home page with post list displaying title, excerpt, and created date
  - Post detail view with full Markdown content rendering
  - Responsive design using Vaadin Lumo theme

- **Admin Panel**:
  - Dashboard view (`/admin/dashboard`) with post management table
  - Create new post form (`/admin/new`)
  - Edit existing post form (`/admin/edit/{id}`)
  - Delete post functionality with confirmation

- **Security**:
  - HTTP Basic Authentication for all `/admin/*` routes
  - Credentials loaded from environment variables (`ADMIN_USERNAME`, `ADMIN_PASSWORD`)

- **Database**:
  - MariaDB 11.7 with jOOQ 3.21 for type-safe SQL
  - Flyway migrations for schema versioning
  - Initial schema with `posts` table
  - Sample data seeded on first run

- **Configuration**:
  - Environment-based configuration (no hardcoded defaults)
  - Required environment variables: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`
  - `.env.example` template for easy setup

- **Docker**:
  - Multi-stage Dockerfile for production builds
  - Docker Compose with MariaDB and app services
  - Health checks for both containers
  - Non-root user for security

- **Documentation**:
  - Comprehensive README with screenshots
  - Quick Start guide
  - Docker deployment instructions
  - Development workflow documentation

### Changed

- **Database Configuration**: Changed from optional defaults to required environment variables
- **jOOQ Code**: Regenerated to match current schema
- **Build Process**: Added `runDev` and `runProd` Gradle tasks

### Fixed

- Unused imports in `Main.java`, `PostDetailView.java`
- Flyway checksum mismatch handling with automatic repair
- Admin button navigation to trigger Basic Auth flow

### Security

- Removed hardcoded database credentials
- Enforced required environment variables
- Added non-root user in Docker container

[Unreleased]: https://github.com/vekzz-dev/personal-blog-lite/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/vekzz-dev/personal-blog-lite/releases/tag/v1.0.0
