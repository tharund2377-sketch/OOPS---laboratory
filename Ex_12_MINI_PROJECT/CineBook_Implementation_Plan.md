# CineBook — Complete Implementation & Feature Plan

## 1. Project Overview

### Project Name
**CineBook — Movie Ticket Booking System**

### Project Type
Java desktop mini project using:
- Java 21
- Object-Oriented Programming (OOP)
- Data Structures and Algorithms (DSA)
- JavaFX GUI
- JDBC Database Connectivity
- SQLite Database

### Project Objective
CineBook is a desktop-based movie ticket booking application that allows users to:
1. Create an account and log in.
2. Browse available movies in a cinematic card view.
3. Search movies using **Binary Search** (`BookingSearch.findByTitle` / prefix filter).
4. View showtimes and select seats interactively (5x8 grid).
5. Undo seat selections dynamically using an **Undo Stack** (`UndoStack`).
6. Confirm a booking and receive an **E-Ticket Receipt** modal.
7. View booking history and **Cancel Bookings** to release seat reservations.

Administrators can:
1. View real-time **Analytics Dashboard Cards**: Total Movies, Total Shows, Total Bookings, Total Revenue (₹).
2. **Add, Edit, and Delete Movies** in the catalogue.
3. **Manage Showtimes**: Add showtimes to specific movies (theatre, screen #, time) and delete showtimes.
4. **Audit All Bookings**: View a full log of all customer bookings and transaction statuses.

---

# 2. Final Project Architecture

```text
CineBook/
│
├── src/
│   ├── application/
│   │   ├── Main.java               # Main JavaFX GUI application with all screens & modals
│   │   └── AppRouter.java          # Routing helper
│   │
│   ├── controller/
│   │   ├── LoginController.java    # Auth handling
│   │   ├── MovieController.java    # Movie catalogue & binary search
│   │   ├── BookingController.java  # Booking flow & ticket cancellation
│   │   └── AdminController.java    # Admin CRUD, showtimes & dashboard stats
│   │
│   ├── model/
│   │   ├── User.java               # Abstract base user class
│   │   ├── Customer.java           # Customer domain model (extends User)
│   │   ├── Admin.java              # Admin domain model (extends User)
│   │   ├── Movie.java              # Movie entity
│   │   ├── Show.java               # Showtime entity
│   │   ├── Seat.java               # Seat entity
│   │   ├── Booking.java            # Booking entity
│   │   └── Payment.java            # Payment entity
│   │
│   ├── dao/
│   │   ├── DatabaseConnection.java # SQLite JDBC connection & table seeding
│   │   ├── UserDAO.java            # User login & registration queries
│   │   ├── MovieDAO.java           # Movie CRUD queries (add, update, delete, count)
│   │   ├── ShowDAO.java            # Show CRUD queries (add, delete, count)
│   │   └── BookingDAO.java         # Booking creation, cancellation, revenue & stats
│   │
│   ├── service/
│   │   ├── BookingService.java     # Booking validation & queue processing
│   │   └── PaymentService.java     # Payment processing simulation
│   │
│   └── dsa/
│       ├── BookingQueue.java       # FIFO Queue for booking request processing
│       ├── UndoStack.java          # LIFO Stack for seat selection undo operation
│       └── BookingSearch.java      # Binary Search for movie title lookups
│
├── resources/
│   └── styles/
│       └── application.css         # Dark cinematic CSS styling system
│
├── cinebook.db                     # SQLite local database file
├── pom.xml                         # Maven build configuration
└── README.md                       # Project execution guide
```

---

# 3. Technology Stack & Configuration

| Layer | Technology | Version |
|---|---|---|
| Programming Language | Java | JDK 21 |
| GUI Framework | JavaFX | 21.0.4 |
| Database | SQLite | 3.46.1.0 |
| Database Connectivity | JDBC | Standard Java SQL |
| Build System | Maven | 3.9.11 |
| Visual Styling | JavaFX CSS | Custom Cinematic Theme |

---

# 4. Data Structures & Algorithms (DSA) Implementation

| DSA Component | Class Name | Concept | Application Usage |
|---|---|---|---|
| **Binary Search** | [`BookingSearch.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dsa/BookingSearch.java) | $O(\log N)$ Title Search | Performs binary search over alphabetically sorted movie titles for search bar filtering. |
| **Undo Stack** | [`UndoStack.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dsa/UndoStack.java) | LIFO Stack (`push`/`pop`) | Allows customers to undo their most recent seat selection in the grid. |
| **Booking Queue** | [`BookingQueue.java`](file:///d:/OOP_LAB/OOP_JAVA__161/Ex_12_MINI_PROJECT/CineBook/src/dsa/BookingQueue.java) | FIFO Queue (`offer`/`poll`) | Queues incoming booking requests and processes them sequentially to avoid race conditions. |

---

# 5. Database Schema

```sql
-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL
);

-- Movies Table
CREATE TABLE IF NOT EXISTS movies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    genre TEXT,
    duration TEXT,
    rating TEXT,
    description TEXT,
    poster_path TEXT
);

-- Shows Table
CREATE TABLE IF NOT EXISTS shows (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    movie_id INTEGER REFERENCES movies(id) ON DELETE CASCADE,
    show_time TEXT NOT NULL,
    theatre TEXT,
    screen_number INTEGER
);

-- Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER REFERENCES users(id),
    movie_id INTEGER REFERENCES movies(id),
    show_id INTEGER REFERENCES shows(id),
    seats TEXT NOT NULL,
    total_amount REAL,
    status TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_id INTEGER REFERENCES bookings(id),
    amount REAL,
    payment_method TEXT,
    payment_status TEXT
);
```

---

# 6. Admin Control Centre Features

### 1. Real-time Metrics Dashboard
Displays four metric cards at the top of the Admin View:
- **Total Movies**: Count of movies in catalogue.
- **Total Shows**: Count of scheduled showtimes.
- **Confirmed Bookings**: Total active bookings.
- **Total Revenue (₹)**: Sum of total revenue collected.

### 2. Movie Management Tab
- **Add Movie**: Create new movie with title, genre, duration, rating, and poster color code.
- **Edit Movie**: Select an existing movie from the list, populate fields automatically, and click **Save Updates**.
- **Delete Movie**: Remove a movie title (cascades to associated shows).

### 3. Showtimes Management Tab
- Select movie from dropdown.
- Enter showtime (e.g., `11:30 AM`), theatre name, and screen number.
- Add showtime or delete selected showtime.

### 4. All Bookings Audit Tab
- Displays full log of all customer transactions, seat selections, amounts, and statuses (`CONFIRMED` / `CANCELLED`).

---

# 7. Customer Flow & Features

1. **Authentication**: Sign in with existing account or register new customer account.
2. **Movie Discovery**: Browse movie cards with poster colors, genre, duration, and ratings.
3. **Binary Search**: Type in search bar to filter titles via binary search algorithm.
4. **Show & Seat Selection**: Choose showtime, click seats in 5x8 grid. Use **Undo Last Seat** stack button if needed.
5. **Payment Method**: Select UPI, Credit/Debit Card, or Cash.
6. **Cinematic E-Ticket Modal**: View booking confirmation receipt with reference ID, showtime, screen number, seats, and price.
7. **My Bookings & Cancellation**: View past bookings and click **Cancel Booking** to release seat reservations.

---

# 8. Execution & Verification

### Run via Maven
```powershell
cd d:\OOP_LAB\OOP_JAVA__161\Ex_12_MINI_PROJECT\CineBook
mvn clean javafx:run
```

### Pre-seeded Demo Accounts
- **Customer**: `user@cinebook.com` / `user123`
- **Admin**: `admin@cinebook.com` / `admin123`
