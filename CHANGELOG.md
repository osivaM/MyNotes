# CHANGELOG

## [1.0.0] - 2024-08-06

### Added

- Dependencies in `pom.xml`
- Main project structure

### Packages and Classes

- **Configuration**:
    - `SchedulerConfiguration`
    - `SecurityConfiguration`
- **Controllers**:
    - `TelegramBot`
    - `UpdateController`
    - `UserController`
    - `IndexController`
- **Initializer**:
    - `DataLoader`
- **Models**:
    - `Note`
    - `NoteCategory`
    - `Role`
    - `User`
    - `UserDetailsImpl`
    - `UserStatus`
- **Repositories**:
    - `NoteCategoryRepository`
    - `NoteRepository`
    - `RoleRepository`
    - `UserRepository`
    - `UserStatusRepository`
- **Services**:
    - `NoteCategoryService`
    - `NoteService`
    - `RegistrationService`
    - `RoleService`
    - `UserService`
    - `UserStatusService`
    - Service Implementations
- **Utils**:
    - `Transliterator`

### Web Interface

- Templates: `index`, `user/dashboard`, `user/category`, `user/note`, `user/create_note`

### Application Settings

- Settings in `application.properties`

### Telegram Bot

- Classes: `TelegramBot`, `UpdateController`
- Functionality: user registration, CRUD operations with categories and notes, message and command handling
