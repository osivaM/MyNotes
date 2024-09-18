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

---

## [1.0.0] - 2024-08-14

### Refactor

#### Telegram bot

- Refactored the bot's interaction mechanism with Telegram servers - replaced `TelegramLongPollingBot` with `TelegramWebhookBot`.

#### Security

- Refactored security configuration - for csrf the /callback/update path is included in the exceptions
- also, /callback/update is marked as permission all

### Added

- **Controllers**:
    - `TelegramWebhookController`

---

## [1.0.0] - 2024-09-19

### Refactor

#### Security

- Authentication using JWT tokens has been implemented
- Implemented JWT filter and service for working with JWT

#### Interaction with the client

- Removed all templates used by Thymeleaf, now interaction with the client part will be carried out using the REST API
- The corresponding REST Controllers are written

#### Other changes

- Completely rewritten UserService
- The order of forming the list of users who cannot be deleted has been changed - now the list is formed by the admin on his control panel
- Unique user counter implemented
- User roles are no longer stored in the DB, they are now an enumeration class
- Minor changes have affected almost all classes

### Added

- **configuration**:
  - `ApplicationConfigaration`
  - `JwtAuthenticationFilter`
- **controllers**
  - `AdminController`
  - `AuthenticationController`
  - `ContentController`
  - `UserController`
- **exceptions**
  - `DuplicateEntityException`
  - `EmptyNameException`
- **models**
  - `CreateCategoryRequest`
  - `CreateNoteRequest`
  - `AuthenticationRequest`
  - `AuthenticationResponse`
  - `UniqueUser`
- **repositories**
  - `UniqueUserRepository`
- **services**
  - `AuthenticationResponseService`
  - `JwtService`