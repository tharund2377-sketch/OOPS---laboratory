package application;

import controller.AdminController;
import controller.BookingController;
import controller.LoginController;
import controller.MovieController;
import dao.DatabaseConnection;
import dsa.UndoStack;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {

    private final LoginController loginController = new LoginController();
    private final MovieController movieController = new MovieController();
    private final BookingController bookingController = new BookingController();
    private final AdminController adminController = new AdminController();

    private Stage stage;
    private String css = "";
    private String selectedGenreFilter = "All";

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("CineBook — Movie Ticket Booking System");

        var cssUrl = getClass().getResource("/styles/application.css");
        if (cssUrl != null) {
            css = cssUrl.toExternalForm();
        }

        try {
            DatabaseConnection.initialize();
            showLogin();
        } catch (SQLException e) {
            showError("Database Initialization Error", e.getMessage());
        }
    }

    private Scene createScene(Parent root) {
        Scene scene = new Scene(root, 1220, 800);
        if (!css.isEmpty()) {
            scene.getStylesheets().add(css);
        }
        return scene;
    }

    private Label createText(String value, String styleClass) {
        Label label = new Label(value);
        if (styleClass != null && !styleClass.isBlank()) {
            label.getStyleClass().add(styleClass);
        }
        label.setWrapText(true);
        return label;
    }

    private Button createButton(String text) {
        return new Button(text);
    }

    private Node createPosterWidget(String posterPath, String title, double width, double height) {
        if (posterPath != null && (posterPath.startsWith("http://") || posterPath.startsWith("https://") || posterPath.startsWith("file:") || posterPath.endsWith(".png") || posterPath.endsWith(".jpg") || posterPath.endsWith(".jpeg"))) {
            try {
                String url = posterPath.startsWith("file:") || posterPath.startsWith("http") ? posterPath : new File(posterPath).toURI().toString();
                Image img = new Image(url, width, height, false, true, true);
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(width);
                imgView.setFitHeight(height);
                imgView.setPreserveRatio(false);

                Rectangle clip = new Rectangle(width, height);
                clip.setArcWidth(16);
                clip.setArcHeight(16);
                imgView.setClip(clip);
                return imgView;
            } catch (Exception ignored) { }
        }

        StackPane coloredBox = new StackPane();
        coloredBox.setPrefSize(width, height);
        coloredBox.setMinSize(width, height);
        coloredBox.setMaxSize(width, height);
        String bgColor = (posterPath != null && posterPath.startsWith("#")) ? posterPath : "#D32F2F";
        coloredBox.setStyle("-fx-background-color: linear-gradient(to bottom right, " + bgColor + ", #0F0303); -fx-background-radius: 14;");
        coloredBox.getChildren().add(createText(title == null ? "MOVIE" : title.toUpperCase(), "brand"));
        return coloredBox;
    }

    // ==========================================
    // AUTHENTICATION SCREENS
    // ==========================================

    private void showLogin() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("screen");

        VBox pitch = new VBox(22);
        pitch.setAlignment(Pos.CENTER_LEFT);
        pitch.setPadding(new Insets(60));
        pitch.setMaxWidth(480);

        Label brandLogo = createText("CINEBOOK 🎬", "brand");
        Label heroTitle = createText("Cinema Tickets,\nReimagined.", "hero");
        Label heroDesc = createText("Select your favorite movie, pick your seats interactively, and get instant digital mobile tickets.", "muted");

        HBox highlights = new HBox(12);
        Label h1 = createText("🍿 VIP & Dolby 3D", "chip");
        Label h2 = createText("⚡ Instant E-Ticket", "chip");
        highlights.getChildren().addAll(h1, h2);

        pitch.getChildren().addAll(brandLogo, heroTitle, heroDesc, highlights);
        root.setLeft(pitch);

        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("panel");
        formCard.setMaxWidth(400);
        formCard.setPrefWidth(400);
        formCard.setAlignment(Pos.CENTER_LEFT);

        Label formTitle = createText("Welcome Back", "heading");
        Label formSubtitle = createText("Sign in to your CineBook account.", "muted");

        TextField emailField = new TextField("user@cinebook.com");
        emailField.setPromptText("Email address");
        emailField.getStyleClass().add("field");

        PasswordField passwordField = new PasswordField();
        passwordField.setText("user123");
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("field");

        Label messageLabel = createText("Demo Customer: user@cinebook.com / user123\nAdmin: admin@cinebook.com / admin123", "muted");

        Button signInBtn = createButton("Sign In");
        signInBtn.setMaxWidth(Double.MAX_VALUE);

        Button registerBtn = createButton("Create New Account");
        registerBtn.getStyleClass().add("secondary");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        signInBtn.setOnAction(e -> {
            try {
                User u = loginController.login(emailField.getText().trim(), passwordField.getText());
                if (u == null) {
                    messageLabel.setText("Invalid email or password.");
                    messageLabel.getStyleClass().setAll("error");
                } else {
                    showHome(u);
                }
            } catch (IllegalStateException blockEx) {
                messageLabel.setText("Account Blocked: " + blockEx.getMessage());
                messageLabel.getStyleClass().setAll("error");
            } catch (SQLException ex) {
                messageLabel.setText("Login Error: " + ex.getMessage());
                messageLabel.getStyleClass().setAll("error");
            }
        });

        registerBtn.setOnAction(e -> showRegister());

        formCard.getChildren().addAll(formTitle, formSubtitle, emailField, passwordField, signInBtn, registerBtn, new Separator(), messageLabel);
        
        StackPane center = new StackPane(formCard);
        center.setPadding(new Insets(50));
        root.setCenter(center);

        stage.setScene(createScene(root));
        stage.show();
    }

    private void showRegister() {
        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("panel");
        formCard.setMaxWidth(440);
        formCard.setAlignment(Pos.CENTER_LEFT);

        Label title = createText("Create Your CineBook Account", "heading");
        Label subtitle = createText("Join CineBook to enjoy fast ticket booking.", "muted");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.getStyleClass().add("field");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.getStyleClass().add("field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("field");

        Label messageLabel = createText("", "error");

        Button createBtn = createButton("Register Account");
        createBtn.setMaxWidth(Double.MAX_VALUE);

        Button backBtn = createButton("← Back to Sign In");
        backBtn.getStyleClass().add("secondary");
        backBtn.setMaxWidth(Double.MAX_VALUE);

        createBtn.setOnAction(e -> {
            if (nameField.getText().isBlank() || emailField.getText().isBlank() || passwordField.getText().isBlank()) {
                messageLabel.setText("Please fill out all fields.");
                return;
            }
            try {
                loginController.register(nameField.getText().trim(), emailField.getText().trim(), passwordField.getText());
                showInformation("Registration Successful", "Your account has been created! Please log in.");
                showLogin();
            } catch (SQLException ex) {
                messageLabel.setText("This email address is already registered.");
            }
        });

        backBtn.setOnAction(e -> showLogin());

        formCard.getChildren().addAll(title, subtitle, nameField, emailField, passwordField, createBtn, backBtn, messageLabel);
        
        StackPane root = new StackPane(formCard);
        root.getStyleClass().add("screen");
        root.setPadding(new Insets(60));

        stage.setScene(createScene(root));
    }

    // ==========================================
    // CUSTOMER DASHBOARD
    // ==========================================

    private void showHome(User user) {
        try {
            List<Movie> allMovies = movieController.all();

            BorderPane root = new BorderPane();
            root.getStyleClass().add("screen");

            HBox navBar = new HBox(16);
            navBar.getStyleClass().add("navbar");

            Label brand = createText("CINEBOOK 🎬", "brand");

            TextField searchField = new TextField();
            searchField.setPromptText("🔍 Search movies by title or genre...");
            searchField.getStyleClass().add("field");
            searchField.setPrefWidth(300);

            Region navSpacer = new Region();
            HBox.setHgrow(navSpacer, Priority.ALWAYS);

            Button profileBtn = createButton("👤 " + user.getName());
            profileBtn.getStyleClass().add("secondary");
            profileBtn.setOnAction(e -> showUserProfile(user));

            Button historyBtn = createButton("My Bookings");
            historyBtn.getStyleClass().add("secondary");

            Button signOutBtn = createButton("Sign Out");
            signOutBtn.getStyleClass().add("secondary");

            navBar.getChildren().addAll(brand, searchField, navSpacer, profileBtn, historyBtn, signOutBtn);

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                Button adminBtn = createButton("⚡ Admin Centre");
                navBar.getChildren().add(4, adminBtn);
                adminBtn.setOnAction(e -> showAdmin(user));
            }

            root.setTop(navBar);

            VBox mainContent = new VBox(24);
            mainContent.setPadding(new Insets(20, 36, 36, 36));

            if (!allMovies.isEmpty()) {
                Movie heroMovie = allMovies.get(0);
                VBox heroBanner = new VBox(14);
                heroBanner.getStyleClass().add("hero-banner");

                HBox badgeRow = new HBox(10,
                    createText("⚡ NOW SHOWING IN IMAX 3D", "text-red"),
                    createText("★ " + heroMovie.getRating(), "chip")
                );

                Label heroTitle = createText(heroMovie.getTitle(), "hero");
                Label heroDesc = createText(heroMovie.getDescription() + " (" + heroMovie.getGenre() + " · " + heroMovie.getDuration() + ")", "muted");
                heroDesc.setMaxWidth(700);

                Button bookHeroBtn = createButton("Book Seats Now →");
                bookHeroBtn.setOnAction(e -> showBookingScreen(user, heroMovie));

                heroBanner.getChildren().addAll(badgeRow, heroTitle, heroDesc, bookHeroBtn);
                mainContent.getChildren().add(heroBanner);
            }

            HBox chipsBar = new HBox(12);
            chipsBar.setAlignment(Pos.CENTER_LEFT);
            chipsBar.getChildren().add(createText("Categories:", "subheading"));

            List<String> genres = List.of("All", "Sci-Fi", "Drama", "Animation", "Action", "Romance");
            ToggleGroup chipsGroup = new ToggleGroup();

            FlowPane movieCardsPane = new FlowPane(20, 20);
            movieCardsPane.setPrefWrapLength(1100);

            for (String genre : genres) {
                ToggleButton chip = new ToggleButton(genre);
                chip.setToggleGroup(chipsGroup);
                chip.getStyleClass().add("chip");
                if (genre.equals(selectedGenreFilter)) {
                    chip.setSelected(true);
                }
                chip.setOnAction(e -> {
                    selectedGenreFilter = genre;
                    filterAndRenderMovies(movieCardsPane, allMovies, searchField.getText(), selectedGenreFilter, user);
                });
                chipsBar.getChildren().add(chip);
            }
            if (chipsGroup.getSelectedToggle() == null && !chipsGroup.getToggles().isEmpty()) {
                chipsGroup.getToggles().get(0).setSelected(true);
            }

            mainContent.getChildren().addAll(chipsBar, createText("Explore All Movies", "heading"), movieCardsPane);

            renderMovieCards(movieCardsPane, allMovies, user);

            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterAndRenderMovies(movieCardsPane, allMovies, newVal, selectedGenreFilter, user);
            });

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("screen");
            root.setCenter(scrollPane);

            historyBtn.setOnAction(e -> showHistory(user));
            signOutBtn.setOnAction(e -> showLogin());

            stage.setScene(createScene(root));

        } catch (SQLException e) {
            showError("Could Not Load Movies", e.getMessage());
        }
    }

    private void filterAndRenderMovies(FlowPane pane, List<Movie> allMovies, String query, String genreFilter, User user) {
        List<Movie> filtered = movieController.search(allMovies, query);
        if (!"All".equalsIgnoreCase(genreFilter)) {
            filtered = filtered.stream()
                .filter(m -> m.getGenre().toLowerCase().contains(genreFilter.toLowerCase()))
                .collect(Collectors.toList());
        }
        renderMovieCards(pane, filtered, user);
    }

    private void renderMovieCards(FlowPane cardsPane, List<Movie> movies, User user) {
        cardsPane.getChildren().clear();
        if (movies.isEmpty()) {
            cardsPane.getChildren().add(createText("No movies found matching your search or filter.", "muted"));
            return;
        }

        for (Movie movie : movies) {
            VBox card = new VBox(12);
            card.getStyleClass().add("movie-card");

            Node posterNode = createPosterWidget(movie.getPosterPath(), movie.getTitle(), 215, 230);

            HBox metaRow = new HBox(8);
            metaRow.setAlignment(Pos.CENTER_LEFT);
            Label ratingBadge = createText("★ " + movie.getRating(), "text-red");
            Label durBadge = createText("⏱ " + movie.getDuration(), "muted");
            metaRow.getChildren().addAll(ratingBadge, durBadge);

            Label genreLabel = createText(movie.getGenre(), "muted");
            Label descLabel = createText(movie.getDescription(), "muted");

            Button selectSeatsBtn = createButton("Select Seats");
            selectSeatsBtn.setMaxWidth(Double.MAX_VALUE);
            selectSeatsBtn.setOnAction(e -> showBookingScreen(user, movie));

            card.getChildren().addAll(posterNode, createText(movie.getTitle(), "heading"), metaRow, genreLabel, descLabel, selectSeatsBtn);
            cardsPane.getChildren().add(card);
        }
    }

    // ==========================================
    // MOVIE SHOW & TIER SEAT SELECTION
    // ==========================================

    private void showBookingScreen(User user, Movie movie) {
        try {
            List<Show> showsList = bookingController.shows(movie);
            if (showsList.isEmpty()) {
                showInformation("No Showtimes Available", "There are currently no showtimes scheduled for " + movie.getTitle() + ".");
                return;
            }

            VBox container = new VBox(22);
            container.setPadding(new Insets(30));

            Button backBtn = createButton("← Back to Movies");
            backBtn.getStyleClass().add("secondary");
            backBtn.setOnAction(e -> showHome(user));

            HBox headerBanner = new HBox(20);
            headerBanner.setAlignment(Pos.CENTER_LEFT);
            headerBanner.getStyleClass().add("hero-banner");
            headerBanner.setPadding(new Insets(20, 24, 20, 24));

            Node posterThumb = createPosterWidget(movie.getPosterPath(), movie.getTitle(), 80, 100);

            VBox movieMeta = new VBox(6);
            movieMeta.getChildren().addAll(
                createText(movie.getTitle(), "hero"),
                createText(movie.getGenre() + "  ·  ⏱ " + movie.getDuration() + "  ·  ★ " + movie.getRating(), "text-red")
            );

            headerBanner.getChildren().addAll(posterThumb, movieMeta);

            HBox dateSelectorBox = new HBox(12);
            dateSelectorBox.setAlignment(Pos.CENTER_LEFT);
            dateSelectorBox.getChildren().add(createText("Select Date:", "subheading"));

            LocalDate today = LocalDate.now();
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd EEE");

            ToggleGroup dateGroup = new ToggleGroup();
            for (int i = 0; i < 5; i++) {
                LocalDate date = today.plusDays(i);
                String dateLabel = (i == 0 ? "Today (" + date.format(dayFmt) + ")" : date.format(dayFmt));
                ToggleButton dateBtn = new ToggleButton(dateLabel);
                dateBtn.setToggleGroup(dateGroup);
                dateBtn.getStyleClass().add("chip");
                if (i == 0) dateBtn.setSelected(true);
                dateSelectorBox.getChildren().add(dateBtn);
            }

            HBox showtimeBox = new HBox(12);
            showtimeBox.setAlignment(Pos.CENTER_LEFT);
            showtimeBox.getChildren().add(createText("Select Showtime:", "subheading"));

            ToggleGroup showGroup = new ToggleGroup();
            for (Show show : showsList) {
                ToggleButton btn = new ToggleButton(show.getShowTime() + "  (" + show.getTheatreName() + " · Screen " + show.getScreenNumber() + ")");
                btn.setToggleGroup(showGroup);
                btn.getStyleClass().add("secondary");
                showtimeBox.getChildren().add(btn);
            }

            VBox screenArcBox = new VBox(8);
            screenArcBox.setAlignment(Pos.CENTER);

            Region arcLine = new Region();
            arcLine.getStyleClass().add("screen-bar");
            arcLine.setMaxWidth(560);

            Label screenText = createText("── 🎬 CINEMA SCREEN THIS WAY ──", "screen-label");
            screenArcBox.getChildren().addAll(screenText, arcLine);

            HBox legendBox = new HBox(24);
            legendBox.setAlignment(Pos.CENTER);
            legendBox.getChildren().addAll(
                createLegendItem("Available", "#2D3748"),
                createLegendItem("Selected", "#00E5FF"),
                createLegendItem("Booked", "#E50914"),
                createText("👑 VIP (Row A): ₹350  ·  ⭐ Premium (Rows B-C): ₹250  ·  🎟 Regular (Rows D-E): ₹150", "muted")
            );

            GridPane seatGrid = new GridPane();
            seatGrid.setHgap(10);
            seatGrid.setVgap(10);
            seatGrid.setAlignment(Pos.CENTER);

            HBox bottomCheckoutBar = new HBox(20);
            bottomCheckoutBar.setAlignment(Pos.CENTER_LEFT);
            bottomCheckoutBar.getStyleClass().add("panel");
            bottomCheckoutBar.setPadding(new Insets(16, 24, 16, 24));

            VBox seatsSummaryInfo = new VBox(4);
            Label seatsCountLabel = createText("0 Seats Selected", "subheading");
            Label seatsDetailLabel = createText("Seats: None", "muted");
            seatsSummaryInfo.getChildren().addAll(seatsCountLabel, seatsDetailLabel);

            Region barSpacer = new Region();
            HBox.setHgrow(barSpacer, Priority.ALWAYS);

            Label totalPriceLabel = createText("₹0", "hero");
            totalPriceLabel.getStyleClass().add("text-cyan");

            Button undoSeatBtn = createButton("Undo Seat");
            undoSeatBtn.getStyleClass().add("secondary");

            Button checkoutBtn = createButton("Book Tickets →");

            bottomCheckoutBar.getChildren().addAll(seatsSummaryInfo, barSpacer, totalPriceLabel, undoSeatBtn, checkoutBtn);

            Set<String> selectedSeats = new LinkedHashSet<>();
            UndoStack undoStack = new UndoStack();

            Runnable refreshSeatsGrid = () -> {
                seatGrid.getChildren().clear();
                int selectedIndex = showGroup.getSelectedToggle() == null ? 0 : showGroup.getToggles().indexOf(showGroup.getSelectedToggle());
                Show currentShow = showsList.get(selectedIndex);

                try {
                    List<String> bookedSeats = bookingController.shows(movie).isEmpty() ? List.of() : new dao.BookingDAO().bookedSeats(currentShow.getId());

                    for (int row = 0; row < 5; row++) {
                        char rowChar = (char) ('A' + row);
                        for (int col = 0; col < 8; col++) {
                            String seatId = "" + rowChar + (col + 1);
                            ToggleButton seatBtn = new ToggleButton(seatId);
                            seatBtn.getStyleClass().add("seat");
                            seatBtn.setSelected(selectedSeats.contains(seatId));
                            seatBtn.setDisable(bookedSeats.contains(seatId));

                            seatBtn.setOnAction(e -> {
                                if (seatBtn.isSelected()) {
                                    selectedSeats.add(seatId);
                                    undoStack.push(seatId);
                                } else {
                                    selectedSeats.remove(seatId);
                                }
                                updateTierCheckoutSummary(seatsCountLabel, seatsDetailLabel, totalPriceLabel, selectedSeats);
                            });

                            seatGrid.add(seatBtn, col + (col >= 4 ? 1 : 0), row);
                        }
                    }
                } catch (SQLException ex) {
                    showError("Error Loading Seat Status", ex.getMessage());
                }
            };

            showGroup.selectToggle(showGroup.getToggles().get(0));
            showGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                selectedSeats.clear();
                undoStack.clear();
                updateTierCheckoutSummary(seatsCountLabel, seatsDetailLabel, totalPriceLabel, selectedSeats);
                refreshSeatsGrid.run();
            });

            refreshSeatsGrid.run();

            undoSeatBtn.setOnAction(e -> {
                String seatToUndo = undoStack.undo();
                if (seatToUndo != null) {
                    selectedSeats.remove(seatToUndo);
                    refreshSeatsGrid.run();
                    updateTierCheckoutSummary(seatsCountLabel, seatsDetailLabel, totalPriceLabel, selectedSeats);
                }
            });

            checkoutBtn.setOnAction(e -> {
                if (selectedSeats.isEmpty()) {
                    showError("No Seats Selected", "Please select at least one seat before continuing.");
                    return;
                }
                int showIndex = showGroup.getToggles().indexOf(showGroup.getSelectedToggle());
                Show targetShow = showsList.get(showIndex);

                ChoiceDialog<String> paymentDialog = new ChoiceDialog<>("UPI (Online)", List.of("UPI (Online)", "Credit/Debit Card", "Cash at Counter"));
                paymentDialog.setTitle("Select Payment Method");
                paymentDialog.setHeaderText("Payment Execution Gateway");
                paymentDialog.setContentText("Payment Method:");

                paymentDialog.showAndWait().ifPresent(method -> {
                    try {
                        Booking booking = bookingController.book(user, movie, targetShow, new ArrayList<>(selectedSeats), method);
                        showETicketModal(user, movie, targetShow, booking, method);
                        showHome(user);
                    } catch (Exception ex) {
                        showError("Booking Failed", ex.getMessage());
                    }
                });
            });

            container.getChildren().addAll(backBtn, headerBanner, dateSelectorBox, showtimeBox, screenArcBox, legendBox, seatGrid, bottomCheckoutBar);

            ScrollPane scrollPane = new ScrollPane(new StackPane(container));
            scrollPane.setFitToWidth(true);
            stage.setScene(createScene(scrollPane));

        } catch (SQLException e) {
            showError("Error Loading Showtimes", e.getMessage());
        }
    }

    private void updateTierCheckoutSummary(Label countLabel, Label detailLabel, Label priceLabel, Set<String> selectedSeats) {
        int count = selectedSeats.size();
        double totalPrice = 0.0;
        for (String seatId : selectedSeats) {
            char rowChar = seatId.charAt(0);
            totalPrice += Seat.getCategoryPrice(rowChar);
        }

        countLabel.setText(count + (count == 1 ? " Seat Selected" : " Seats Selected"));
        detailLabel.setText("Seats: " + (count == 0 ? "None" : String.join(", ", selectedSeats)));
        priceLabel.setText("₹" + (int) totalPrice);
    }

    private HBox createLegendItem(String labelText, String hexColor) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER);

        Region colorDot = new Region();
        colorDot.setPrefSize(14, 14);
        colorDot.setStyle("-fx-background-color: " + hexColor + "; -fx-background-radius: 4;");

        Label label = createText(labelText, "muted");
        item.getChildren().addAll(colorDot, label);
        return item;
    }

    // ==========================================
    // DIGITAL E-TICKET STUB MODAL
    // ==========================================

    private void showETicketModal(User user, Movie movie, Show show, Booking booking, String paymentMethod) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("CineBook E-Ticket Stub");

        VBox stubCard = new VBox(16);
        stubCard.getStyleClass().add("ticket-stub");
        stubCard.setMinWidth(420);

        Label ticketBrand = createText("CINEBOOK MOBILE TICKET 🎟", "brand");
        Label statusBadge = createText("✓ BOOKING CONFIRMED #" + booking.getId(), "text-cyan");

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(16);
        infoGrid.setVgap(10);

        infoGrid.add(createText("Movie:", "muted"), 0, 0);
        infoGrid.add(createText(movie.getTitle(), "heading"), 1, 0);

        infoGrid.add(createText("Showtime:", "muted"), 0, 1);
        infoGrid.add(createText(show.getShowTime(), "subheading"), 1, 1);

        infoGrid.add(createText("Cinema:", "muted"), 0, 2);
        infoGrid.add(createText(show.getTheatreName() + " (Screen " + show.getScreenNumber() + ")", "subheading"), 1, 2);

        infoGrid.add(createText("Seats Reserved:", "muted"), 0, 3);
        infoGrid.add(createText(String.join(", ", booking.getSeatNumbers()), "hero"), 1, 3);

        infoGrid.add(createText("Total Paid:", "muted"), 0, 4);
        infoGrid.add(createText("₹" + booking.getTotalAmount() + " via " + paymentMethod, "text-red"), 1, 4);

        infoGrid.add(createText("Customer:", "muted"), 0, 5);
        infoGrid.add(createText(user.getName() + " (" + user.getEmail() + ")", "muted"), 1, 5);

        Region divider = new Region();
        divider.getStyleClass().add("ticket-divider");

        VBox barcodeBox = new VBox(4);
        barcodeBox.setAlignment(Pos.CENTER);
        Label barcodeLines = createText("║▌│█║▌│ █║▌│█│║▌║│█║▌│█║▌", "heading");
        Label barcodeCode = createText("TICKET-REF-" + booking.getId() + "-" + System.currentTimeMillis() % 100000, "muted");
        barcodeBox.getChildren().addAll(barcodeLines, barcodeCode);

        stubCard.getChildren().addAll(ticketBrand, statusBadge, new Separator(), infoGrid, divider, barcodeBox);

        dialog.getDialogPane().setContent(stubCard);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ==========================================
    // MY BOOKINGS & UNDO CANCELLATION
    // ==========================================

    private void showHistory(User user) {
        try {
            VBox box = new VBox(20);
            box.setPadding(new Insets(34));

            HBox headerBox = new HBox(16);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            Button backBtn = createButton("← Back to Movies");
            backBtn.getStyleClass().add("secondary");
            backBtn.setOnAction(e -> showHome(user));

            Label title = createText("Your Booking History", "hero");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            headerBox.getChildren().addAll(backBtn, title, spacer);

            if (bookingController.hasCancelledStack()) {
                Button undoCancelBtn = createButton("↩ Undo Last Cancellation");
                undoCancelBtn.setOnAction(e -> {
                    try {
                        if (bookingController.undoLastCancellation()) {
                            showInformation("Cancellation Restored", "Your last cancelled ticket has been restored!");
                            showHistory(user);
                        }
                    } catch (SQLException ex) {
                        showError("Restore Failed", ex.getMessage());
                    }
                });
                headerBox.getChildren().add(undoCancelBtn);
            }

            box.getChildren().add(headerBox);

            List<Booking> userBookings = bookingController.userBookings(user.getId());
            if (userBookings.isEmpty()) {
                box.getChildren().add(createText("You have no past or current bookings.", "muted"));
            } else {
                for (Booking b : userBookings) {
                    HBox card = new HBox(20);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.getStyleClass().add("panel");
                    card.setPadding(new Insets(18, 24, 18, 24));

                    VBox info = new VBox(6);
                    info.getChildren().addAll(
                        createText("Booking Ticket #" + b.getId() + "  ·  Status: " + b.getStatus(), "heading"),
                        createText("Seats: " + String.join(", ", b.getSeatNumbers()) + "  ·  Total: ₹" + b.getTotalAmount(), "subheading")
                    );

                    Region cardSpacer = new Region();
                    HBox.setHgrow(cardSpacer, Priority.ALWAYS);

                    card.getChildren().addAll(info, cardSpacer);

                    if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                        Button cancelBtn = createButton("Cancel Ticket");
                        cancelBtn.getStyleClass().add("secondary");
                        cancelBtn.setOnAction(e -> {
                            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel Booking #" + b.getId() + "?", ButtonType.YES, ButtonType.NO);
                            confirmAlert.setHeaderText("Ticket Cancellation Confirmation");
                            confirmAlert.showAndWait().ifPresent(res -> {
                                if (res == ButtonType.YES) {
                                    try {
                                        bookingController.cancelBooking(b.getId());
                                        showInformation("Booking Cancelled", "Booking #" + b.getId() + " has been cancelled. You can undo this action.");
                                        showHistory(user);
                                    } catch (SQLException ex) {
                                        showError("Cancellation Failed", ex.getMessage());
                                    }
                                }
                            });
                        });
                        card.getChildren().add(cancelBtn);
                    }

                    box.getChildren().add(card);
                }
            }

            ScrollPane scrollPane = new ScrollPane(new StackPane(box));
            scrollPane.setFitToWidth(true);
            stage.setScene(createScene(scrollPane));

        } catch (SQLException e) {
            showError("Could Not Load Bookings History", e.getMessage());
        }
    }

    // ==========================================
    // USER PROFILE & DYNAMIC RE-RENDERING FIX
    // ==========================================

    private void showUserProfile(User user) {
        VBox root = new VBox(22);
        root.setPadding(new Insets(32));

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = createButton("← Back to Movies");
        backBtn.getStyleClass().add("secondary");
        backBtn.setOnAction(e -> showHome(user));

        Label title = createText("User Profile & Statistics", "hero");
        topBar.getChildren().addAll(backBtn, title);
        root.getChildren().add(topBar);

        HBox statsBox = new HBox(18);
        try {
            Map<String, Object> stats = bookingController.getUserProfileStats(user.getId());
            statsBox.getChildren().addAll(
                createStatCard("Total Tickets Bought", String.valueOf(stats.get("totalTickets")), "🎟"),
                createStatCard("Active Bookings", String.valueOf(stats.get("activeBookings")), "🍿"),
                createStatCard("Total Spent", "₹" + String.format("%.2f", stats.get("totalSpent")), "💰")
            );
        } catch (SQLException e) {
            statsBox.getChildren().add(createText("Could not load personal stats.", "error"));
        }
        root.getChildren().add(statsBox);

        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("panel");
        formCard.setMaxWidth(500);

        Label formTitle = createText("Account Information", "heading");
        
        TextField nameField = new TextField(user.getName());
        nameField.setPromptText("Full Name");
        nameField.getStyleClass().add("field");

        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Email Address");
        emailField.getStyleClass().add("field");

        PasswordField newPwField = new PasswordField();
        newPwField.setPromptText("New Password (optional)");
        newPwField.getStyleClass().add("field");

        Button saveProfileBtn = createButton("Save Profile Updates");
        Label statusMsg = createText("", "muted");

        saveProfileBtn.setOnAction(e -> {
            try {
                loginController.updateProfile(user.getId(), nameField.getText().trim(), emailField.getText().trim());
                if (!newPwField.getText().isBlank()) {
                    loginController.changePassword(user.getId(), newPwField.getText().trim());
                }
                
                // Fetch fresh User entity from database and re-render view
                User freshUser = loginController.getUserById(user.getId());
                showInformation("Profile Updated", "Account information updated successfully!");
                showUserProfile(freshUser == null ? user : freshUser);

            } catch (SQLException ex) {
                statusMsg.setText("Failed to update profile: " + ex.getMessage());
                statusMsg.getStyleClass().setAll("error");
            }
        });

        formCard.getChildren().addAll(formTitle, createText("Name:", "subheading"), nameField, createText("Email:", "subheading"), emailField, createText("Change Password:", "subheading"), newPwField, saveProfileBtn, statusMsg);
        root.getChildren().add(formCard);

        ScrollPane scrollPane = new ScrollPane(new StackPane(root));
        scrollPane.setFitToWidth(true);
        stage.setScene(createScene(scrollPane));
    }

    // ==========================================
    // POLISHED ADMIN CONTROL CENTRE
    // ==========================================

    private void showAdmin(User user) {
        VBox root = new VBox(22);
        root.setPadding(new Insets(28, 36, 36, 36));

        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("navbar");

        Label title = createText("⚡ Admin Control Centre", "hero");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = createButton("← Back to Customer View");
        backBtn.getStyleClass().add("secondary");
        backBtn.setOnAction(e -> showHome(user));

        topBar.getChildren().addAll(title, spacer, backBtn);
        root.getChildren().add(topBar);

        HBox statsBox = new HBox(16);
        try {
            Map<String, Object> stats = adminController.getDashboardStats();
            statsBox.getChildren().addAll(
                createStatCard("Total Movies", String.valueOf(stats.get("totalMovies")), "🎬"),
                createStatCard("Total Shows", String.valueOf(stats.get("totalShows")), "🕒"),
                createStatCard("Bookings", String.valueOf(stats.get("totalBookings")), "🎟"),
                createStatCard("Total Revenue", "₹" + String.format("%.2f", stats.get("totalRevenue")), "💰"),
                createStatCard("Today's Revenue", "₹" + String.format("%.2f", stats.get("todayRevenue")), "📈"),
                createStatCard("Cancelled", String.valueOf(stats.get("cancelledBookings")), "❌")
            );
        } catch (SQLException ex) {
            statsBox.getChildren().add(createText("Failed to load metrics.", "error"));
        }
        root.getChildren().add(statsBox);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab movieTab = new Tab("🎬 Movies Catalogue", createMovieManagerView());
        Tab showTab = new Tab("🕒 Showtimes Manager", createShowtimesManagerView());
        Tab theatreTab = new Tab("🏛 Theatre Manager", createTheatreManagerView());
        Tab userTab = new Tab("👥 User Management", createUserManagementView());
        Tab bookingTab = new Tab("📑 Transaction Audit Log", createBookingsView());

        tabPane.getTabs().addAll(movieTab, showTab, theatreTab, userTab, bookingTab);
        root.getChildren().add(tabPane);

        ScrollPane scrollPane = new ScrollPane(new StackPane(root));
        scrollPane.setFitToWidth(true);
        stage.setScene(createScene(scrollPane));
    }

    private VBox createStatCard(String labelText, String valueText, String icon) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        card.getChildren().addAll(
            createText(icon + " " + labelText, "muted"),
            createText(valueText, "heading")
        );
        return card;
    }

    private VBox createMovieManagerView() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label subtitle = createText("Add, Edit, or Remove Movies & Poster Images", "heading");
        Label status = createText("", "muted");

        HBox mainFormLayout = new HBox(24);

        VBox formFields = new VBox(10);
        formFields.setPrefWidth(550);

        TextField titleF = new TextField(); titleF.setPromptText("Movie Title"); titleF.getStyleClass().add("field");
        TextField genreF = new TextField(); genreF.setPromptText("Genre (e.g. Sci-Fi / Action)"); genreF.getStyleClass().add("field");
        TextField durF = new TextField(); durF.setPromptText("Duration (e.g. 2h 46m)"); durF.getStyleClass().add("field");
        TextField ratF = new TextField(); ratF.setPromptText("Rating (e.g. 8.7)"); ratF.getStyleClass().add("field");
        
        TextField posterF = new TextField(); 
        posterF.setPromptText("Poster Path / URL or Hex Color (#D32F2F)"); 
        posterF.setText("#D32F2F"); 
        posterF.getStyleClass().add("field");

        Button browsePosterBtn = createButton("🖼 Choose Image File...");
        browsePosterBtn.getStyleClass().add("secondary");

        HBox posterRow = new HBox(8, posterF, browsePosterBtn);
        HBox.setHgrow(posterF, Priority.ALWAYS);

        Button addBtn = createButton("Add New Movie");
        Button updateBtn = createButton("Save Movie Updates");
        updateBtn.getStyleClass().add("secondary");

        HBox actionRow = new HBox(10, addBtn, updateBtn);

        formFields.getChildren().addAll(
            createText("Title:", "subheading"), titleF,
            createText("Genre:", "subheading"), genreF,
            createText("Duration & Rating:", "subheading"), new HBox(10, durF, ratF),
            createText("Movie Poster Image / Color:", "subheading"), posterRow,
            actionRow
        );

        VBox previewBox = new VBox(8);
        previewBox.setAlignment(Pos.CENTER);
        previewBox.setPadding(new Insets(10));
        previewBox.getStyleClass().add("panel");
        previewBox.setPrefWidth(220);

        Label previewTitle = createText("Poster Preview", "subheading");
        StackPane previewPane = new StackPane();
        previewPane.getChildren().add(createPosterWidget("#D32F2F", "PREVIEW", 180, 220));

        previewBox.getChildren().addAll(previewTitle, previewPane);

        posterF.textProperty().addListener((obs, oldVal, newVal) -> {
            previewPane.getChildren().clear();
            previewPane.getChildren().add(createPosterWidget(newVal, titleF.getText(), 180, 220));
        });

        browsePosterBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Movie Poster Image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                posterF.setText(file.toURI().toString());
            }
        });

        mainFormLayout.getChildren().addAll(formFields, previewBox);

        ListView<Movie> movieListView = new ListView<>();
        movieListView.setPrefHeight(240);

        Runnable refreshList = () -> {
            try {
                movieListView.getItems().setAll(movieController.all());
            } catch (SQLException e) {
                status.setText("Failed to refresh movie list.");
            }
        };
        refreshList.run();

        movieListView.getSelectionModel().selectedItemProperty().addListener((obs, oldMovie, newMovie) -> {
            if (newMovie != null) {
                titleF.setText(newMovie.getTitle());
                genreF.setText(newMovie.getGenre());
                durF.setText(newMovie.getDuration());
                ratF.setText(newMovie.getRating());
                posterF.setText(newMovie.getPosterPath());
            }
        });

        addBtn.setOnAction(e -> {
            if (titleF.getText().isBlank()) return;
            try {
                Movie m = new Movie(titleF.getText(), genreF.getText(), durF.getText(), ratF.getText(), "A CineBook feature presentation.", posterF.getText());
                adminController.addMovie(m);
                status.setText("Movie '" + m.getTitle() + "' added successfully!");
                status.getStyleClass().setAll("success");
                refreshList.run();
            } catch (SQLException ex) {
                status.setText("Error adding movie: " + ex.getMessage());
                status.getStyleClass().setAll("error");
            }
        });

        updateBtn.setOnAction(e -> {
            Movie selected = movieListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                status.setText("Please select a movie from the list to update.");
                return;
            }
            try {
                Movie updated = new Movie(selected.getId(), titleF.getText(), genreF.getText(), durF.getText(), ratF.getText(), selected.getDescription(), posterF.getText());
                adminController.updateMovie(updated);
                status.setText("Movie '" + updated.getTitle() + "' updated successfully!");
                status.getStyleClass().setAll("success");
                refreshList.run();
            } catch (SQLException ex) {
                status.setText("Error updating movie: " + ex.getMessage());
                status.getStyleClass().setAll("error");
            }
        });

        Button deleteBtn = createButton("Delete Selected Movie");
        deleteBtn.setOnAction(e -> {
            Movie selected = movieListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    adminController.deleteMovie(selected.getId());
                    status.setText("Movie deleted.");
                    refreshList.run();
                } catch (SQLException ex) {
                    status.setText("Error deleting movie: " + ex.getMessage());
                }
            }
        });

        box.getChildren().addAll(subtitle, mainFormLayout, status, movieListView, deleteBtn);
        return box;
    }

    private VBox createShowtimesManagerView() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label subtitle = createText("Schedule Showtimes for Movies", "heading");

        ComboBox<Movie> movieCombo = new ComboBox<>();
        try {
            movieCombo.getItems().setAll(movieController.all());
        } catch (SQLException ignored) { }
        movieCombo.setPromptText("Select Movie");

        TextField timeField = new TextField(); timeField.setPromptText("Show Time (e.g. 04:30 PM)"); timeField.getStyleClass().add("field");
        TextField theatreField = new TextField("CineBook Central"); theatreField.getStyleClass().add("field");
        TextField screenField = new TextField("1"); screenField.setPromptText("Screen #"); screenField.getStyleClass().add("field");
        screenField.setMaxWidth(80);

        Button addShowBtn = createButton("Add Showtime");
        HBox showForm = new HBox(10, movieCombo, timeField, theatreField, screenField, addShowBtn);

        Label status = createText("", "muted");
        ListView<Show> showListView = new ListView<>();
        showListView.setPrefHeight(260);

        Runnable refreshShows = () -> {
            try {
                showListView.getItems().setAll(adminController.allShows());
            } catch (SQLException e) {
                status.setText("Failed to load shows.");
            }
        };
        refreshShows.run();

        addShowBtn.setOnAction(e -> {
            Movie selMovie = movieCombo.getValue();
            if (selMovie == null || timeField.getText().isBlank()) {
                status.setText("Please select a movie and specify a showtime.");
                return;
            }
            try {
                int scNum = Integer.parseInt(screenField.getText().trim());
                Show s = new Show(0, selMovie.getId(), timeField.getText().trim(), theatreField.getText().trim(), scNum);
                adminController.addShow(s);
                status.setText("Showtime added successfully!");
                status.getStyleClass().setAll("success");
                refreshShows.run();
            } catch (Exception ex) {
                status.setText("Error adding show: " + ex.getMessage());
                status.getStyleClass().setAll("error");
            }
        });

        Button deleteShowBtn = createButton("Delete Selected Showtime");
        deleteShowBtn.setOnAction(e -> {
            Show selected = showListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    adminController.deleteShow(selected.getId());
                    status.setText("Showtime deleted.");
                    refreshShows.run();
                } catch (SQLException ex) {
                    status.setText("Error deleting show: " + ex.getMessage());
                }
            }
        });

        box.getChildren().addAll(subtitle, showForm, status, showListView, deleteShowBtn);
        return box;
    }

    private VBox createTheatreManagerView() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label subtitle = createText("Manage Theatre Venues & Seating Layouts", "heading");

        TextField nameField = new TextField(); nameField.setPromptText("Theatre Name"); nameField.getStyleClass().add("field");
        TextField cityField = new TextField(); cityField.setPromptText("City"); cityField.getStyleClass().add("field");
        TextField rowsField = new TextField("5"); rowsField.setPromptText("Rows (e.g. 5)"); rowsField.getStyleClass().add("field"); rowsField.setMaxWidth(90);
        TextField colsField = new TextField("8"); colsField.setPromptText("Cols (e.g. 8)"); colsField.getStyleClass().add("field"); colsField.setMaxWidth(90);

        Button addTheatreBtn = createButton("Add Theatre");
        HBox form = new HBox(10, nameField, cityField, rowsField, colsField, addTheatreBtn);

        Label status = createText("", "muted");
        ListView<Theatre> theatreListView = new ListView<>();
        theatreListView.setPrefHeight(260);

        Runnable refreshTheatres = () -> {
            try {
                theatreListView.getItems().setAll(adminController.allTheatres());
            } catch (SQLException e) {
                status.setText("Failed to load theatres.");
            }
        };
        refreshTheatres.run();

        addTheatreBtn.setOnAction(e -> {
            if (nameField.getText().isBlank() || cityField.getText().isBlank()) return;
            try {
                int r = Integer.parseInt(rowsField.getText().trim());
                int c = Integer.parseInt(colsField.getText().trim());
                Theatre t = new Theatre(0, nameField.getText().trim(), cityField.getText().trim(), r, c);
                adminController.addTheatre(t);
                status.setText("Theatre '" + t.getName() + "' added!");
                status.getStyleClass().setAll("success");
                refreshTheatres.run();
            } catch (Exception ex) {
                status.setText("Error: " + ex.getMessage());
                status.getStyleClass().setAll("error");
            }
        });

        Button deleteTheatreBtn = createButton("Delete Selected Theatre");
        deleteTheatreBtn.setOnAction(e -> {
            Theatre sel = theatreListView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                try {
                    adminController.deleteTheatre(sel.getId());
                    status.setText("Theatre deleted.");
                    refreshTheatres.run();
                } catch (SQLException ex) {
                    status.setText("Error: " + ex.getMessage());
                }
            }
        });

        box.getChildren().addAll(subtitle, form, status, theatreListView, deleteTheatreBtn);
        return box;
    }

    private VBox createUserManagementView() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label subtitle = createText("Registered Users Management & Access Control", "heading");
        Label status = createText("", "muted");

        ListView<User> userListView = new ListView<>();
        userListView.setPrefHeight(280);

        Runnable refreshUsers = () -> {
            try {
                userListView.getItems().setAll(adminController.allUsers());
            } catch (SQLException e) {
                status.setText("Failed to load users.");
            }
        };
        refreshUsers.run();

        Button blockBtn = createButton("Block User");
        blockBtn.getStyleClass().add("secondary");

        Button unblockBtn = createButton("Unblock User");

        HBox actions = new HBox(12, blockBtn, unblockBtn);

        blockBtn.setOnAction(e -> {
            User selected = userListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    adminController.setUserBlocked(selected.getId(), true);
                    status.setText("User " + selected.getEmail() + " is now BLOCKED.");
                    status.getStyleClass().setAll("error");
                    refreshUsers.run();
                } catch (SQLException ex) {
                    status.setText("Error: " + ex.getMessage());
                }
            }
        });

        unblockBtn.setOnAction(e -> {
            User selected = userListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    adminController.setUserBlocked(selected.getId(), false);
                    status.setText("User " + selected.getEmail() + " is now UNBLOCKED.");
                    status.getStyleClass().setAll("success");
                    refreshUsers.run();
                } catch (SQLException ex) {
                    status.setText("Error: " + ex.getMessage());
                }
            }
        });

        box.getChildren().addAll(subtitle, userListView, actions, status);
        return box;
    }

    private VBox createBookingsView() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label subtitle = createText("All Customer Transactions & Bookings Log", "heading");
        ListView<String> bookingsListView = new ListView<>();
        bookingsListView.setPrefHeight(300);

        try {
            List<Booking> list = adminController.allBookings();
            for (Booking b : list) {
                bookingsListView.getItems().add(
                    "Booking #" + b.getId() + "  |  User ID: " + b.getUserId() +
                    "  |  Movie ID: " + b.getMovieId() + "  |  Seats: " + String.join(", ", b.getSeatNumbers()) +
                    "  |  Total: ₹" + b.getTotalAmount() + "  |  Status: " + b.getStatus()
                );
            }
        } catch (SQLException e) {
            bookingsListView.getItems().add("Error loading bookings log.");
        }

        box.getChildren().addAll(subtitle, bookingsListView);
        return box;
    }

    // ==========================================
    // UTILITY DIALOGS
    // ==========================================

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setHeaderText(header);
        alert.setTitle("Error");
        alert.showAndWait();
    }

    private void showInformation(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.setHeaderText(header);
        alert.setTitle("Information");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
