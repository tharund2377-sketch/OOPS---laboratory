# CineBook — Movie Ticket Booking System

A state-of-the-art desktop movie ticket booking system built with **Java 21**, **JavaFX**, **SQLite JDBC**, and pure **Object-Oriented Programming (OOP)** and **Data Structures and Algorithms (DSA)** principles. Features a modern Figma-inspired Crimson Dark user interface with interactive seat matrices, dynamic undo stack operations, binary search movie filtering, synchronized booking queues, and dedicated administrative analytics.

---

## Architecture Overview

```text
CineBook/
├── pom.xml                                  # Maven dependencies and JavaFX configuration
├── database_setup.sql                       # SQLite relational schema
├── cinebook.db                              # Pre-seeded SQLite database file
├── README.md                                # Project documentation
├── resources/
│   └── styles/
│       └── application.css                  # Modern Crimson Dark UI styling
└── src/
    ├── application/
    │   ├── AppRouter.java                   # View routing and scene manager
    │   └── Main.java                        # Primary JavaFX Application entry point
    ├── controller/
    │   ├── AdminController.java             # Admin dashboard, analytics & movie/show CRUD
    │   ├── BookingController.java           # Seat selection, undo stack, receipt & cancellation
    │   ├── LoginController.java             # Authentication, session & registration handling
    │   └── MovieController.java             # Movie catalogue cards & binary search filtering
    ├── dao/
    │   ├── BookingDAO.java                  # Booking persistence, seat tracking & financial analytics
    │   ├── DatabaseConnection.java          # Thread-safe SQLite JDBC connection provider & table seeder
    │   ├── MovieDAO.java                    # Movie CRUD operations (add, read, edit, delete, count)
    │   ├── ShowDAO.java                     # Showtime scheduling, theatre linking & query by movie
    │   ├── TheatreDAO.java                  # Cinema halls, screen numbers & auditorium capacity
    │   └── UserDAO.java                     # User authentication queries & registration inserts
    ├── dsa/
    │   ├── BookingHashMap.java              # Key-value hash map for O(1) active booking lookups
    │   ├── BookingQueue.java                # FIFO Queue for sequential, thread-safe booking requests
    │   ├── BookingSearch.java               # Binary Search algorithm (O(log N)) for movie titles
    │   ├── CancellationStack.java           # LIFO Stack recording canceled bookings for rollback/audit
    │   └── UndoStack.java                   # LIFO Stack for interactive seat selection rollback
    ├── model/
    │   ├── Admin.java                       # Admin entity extending User with administrative roles
    │   ├── Bookable.java                    # Interface contract for reservable resources
    │   ├── Booking.java                     # Domain entity modeling ticket reservations & statuses
    │   ├── CashPayment.java                 # Concrete Payment subclass for cash counter transactions
    │   ├── Customer.java                    # Customer entity extending User with booking privileges
    │   ├── Movie.java                       # Movie entity (title, genre, runtime, rating, poster color)
    │   ├── OnlinePayment.java               # Concrete Payment subclass for UPI/Card digital gateway
    │   ├── Payable.java                     # Interface contract for financial payment processing
    │   ├── Payment.java                     # Abstract base class for transaction management
    │   ├── Seat.java                        # Seat entity representing matrix nodes (row, col, status)
    │   ├── Show.java                        # Showtime entity linking movies to schedules & screens
    │   ├── Theatre.java                     # Theatre entity modeling screen numbers & capacities
    │   └── User.java                        # Abstract base entity with shared authentication fields
    └── service/
        ├── BookingService.java              # Business validation, concurrency & queue processing
        └── PaymentService.java              # Payment gateway dispatch & transaction verification
```

---

## Detailed Component & File Explanations

### 1. Application Layer
- [`AppRouter.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/application/AppRouter.java): Central router for stage management; smoothly switches scenes between Login, Customer Movie Browsing, Seat Booking Grid, and Admin Console without reloading root windows.
- [`Main.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/application/Main.java): JavaFX entry class. Initializes the window stage, applies `application.css`, ensures the SQLite database connection is active, and boots the initial authentication view.

### 2. Domain Models & OOP Abstractions
- [`User.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/User.java): Abstract base class implementing encapsulation with private fields (`id`, `name`, `email`, `password`, `role`) and getter/setter contracts.
- [`Customer.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/Customer.java): Extends `User`. Represents standard moviegoers capable of browsing showtimes, selecting seats, and booking tickets.
- [`Admin.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/Admin.java): Extends `User`. Represents cinema management personnel with privileges to add/edit/delete movies, schedule shows, and audit finances.
- [`Movie.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/Movie.java): Represents movie records including title, genre, runtime duration, certification rating, synopsis, and hex poster accents.
- [`Show.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/Show.java): Represents screening slots connecting movie IDs with screening times, auditorium theatre, screen number, and seat price.
- [`Seat.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/Seat.java): Represents an individual seat in the 5x8 auditorium layout, encapsulating row index, column index, seat code (e.g., A1, D5), and current reservation state (`AVAILABLE`, `SELECTED`, `BOOKED`).
- [`Booking.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/Booking.java): Domain record holding booking reference ID, customer ID, movie ID, show ID, comma-separated seat labels, total amount, timestamp, and booking status (`CONFIRMED` / `CANCELLED`).
- [`Theatre.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/Theatre.java): Models cinema premises and auditorium screens.
- [`Bookable.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/Bookable.java): Interface enforcing `book()`, `cancel()`, and `isAvailable()` behavioral contracts across booking targets.
- [`Payable.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/Payable.java): Interface contract standardizing transaction settlement (`processPayment()`, `getPaymentStatus()`).
- [`Payment.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/Payment.java): Abstract base class implementing `Payable`, storing transaction ID, booking ID, amount, and payment timestamp.
- [`OnlinePayment.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/OnlinePayment.java): Subclass extending `Payment` to simulate UPI, Credit/Debit cards, and Net Banking gateways.
- [`CashPayment.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/model/CashPayment.java): Subclass extending `Payment` to handle physical counter cash collection and receipt issuance.

### 3. Data Structures & Algorithms (DSA)
- [`BookingSearch.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dsa/BookingSearch.java): Implements Binary Search ($O(\log N)$) across lexicographically sorted movie lists to deliver instantaneous real-time title search results.
- [`UndoStack.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dsa/UndoStack.java): Custom LIFO Stack (`push`/`pop`/`peek`) allowing customers to undo their most recent seat selection in the grid with a single click.
- [`BookingQueue.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dsa/BookingQueue.java): Custom FIFO Queue (`offer`/`poll`) data structure managing ticket reservation requests sequentially to avoid race conditions.
- [`CancellationStack.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dsa/CancellationStack.java): Custom LIFO Stack storing canceled booking transactions for administrative history auditing and rollback operations.
- [`BookingHashMap.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dsa/BookingHashMap.java): Key-value hash map structure delivering $O(1)$ fast lookups for active user bookings.

### 4. Data Access Layer (DAO)
- [`DatabaseConnection.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dao/DatabaseConnection.java): Singleton JDBC connection provider connecting to SQLite (`cinebook.db`). Automatically verifies table schema and populates seed data on initial startup.
- [`UserDAO.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dao/UserDAO.java): Authenticates credentials, validates email uniqueness, and inserts newly registered customers.
- [`MovieDAO.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dao/MovieDAO.java): Executes SQL operations to insert movies, retrieve sorted titles, update records, delete movies, and compute total movie counts.
- [`ShowDAO.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dao/ShowDAO.java): Manages showtime schedules, inserts screenings, and queries shows linked to specific movie IDs.
- [`TheatreDAO.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dao/TheatreDAO.java): Queries auditorium metadata and screen assignments.
- [`BookingDAO.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dao/BookingDAO.java): Persists confirmed bookings, queries reserved seats per show, updates cancellation statuses, and calculates total revenue.

### 5. Service Layer
- [`BookingService.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/service/BookingService.java): Encapsulates business validation rules: verifies seat availability, prevents collision, enqueues requests, and creates booking records.
- [`PaymentService.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/service/PaymentService.java): Coordinates payment processing and dispatches to digital or cash payment handlers.

### 6. Controller Layer
- [`LoginController.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/controller/LoginController.java): Manages the sign-in and sign-up interfaces, validates form inputs, and redirects to Customer or Admin views.
- [`MovieController.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/controller/MovieController.java): Coordinates catalogue browsing, card rendering, and binary search filtering.
- [`BookingController.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/controller/BookingController.java): Manages interactive seat grid selection, undo stack triggers, ticket checkout modal receipts, and booking cancellation.
- [`AdminController.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/controller/AdminController.java): Manages the administrator control center: live metric cards (Total Movies, Total Shows, Total Bookings, Total Revenue), movie catalogue CRUD, and showtime schedules.

---

## Quick Start & Execution

### Prerequisites
- **Java 21**: JDK 21 installed and configured in `PATH`.
- **Apache Maven 3.8+**: Installed and accessible via command line.

### Run via Maven
```powershell
mvn clean javafx:run
```

### Pre-seeded Demo Accounts
| Role | Email | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Customer** | `user@cinebook.com` | `user123` | Movie Browsing, Binary Search, Interactive Seat Booking, Undo Stack, E-Ticket Receipt, My Bookings, Cancellation |
| **Admin** | `admin@cinebook.com` | `admin123` | Real-time Analytics Cards (Movies, Shows, Bookings, Revenue), Movie CRUD, Showtime Management, Audit Logs |