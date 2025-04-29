package controllers.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import edu.up_next.entities.User;
import edu.up_next.tools.MyConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject; // Add this import for handling images


public class UsersManagement {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Text AuthenticatedUser;

    @FXML
    private Hyperlink Carpooling;

    @FXML
    private Hyperlink EventLink;

    @FXML
    private Hyperlink Logout;

    @FXML
    private Hyperlink ProductLink;

    @FXML
    private ImageView ProfileImage;

    @FXML
    private Hyperlink ProfileLink;

    @FXML
    private Hyperlink QuizManagement;

    @FXML
    private Hyperlink Reclamation;

    @FXML
    private Hyperlink UsersManagement;

    @FXML
    private TextField search;

    // TableViews for each role
    @FXML
    private TableView<User> adminTable;

    @FXML
    private TableView<User> artistTable;

    @FXML
    private TableView<User> clientTable;

    // Admin table columns
    @FXML
    private TableColumn<User, Integer> adminIdColumn;

    @FXML
    private TableColumn<User, String> adminEmailColumn;

    @FXML
    private TableColumn<User, String> adminRolesColumn;

    @FXML
    private TableColumn<User, String> adminFirstnameColumn;

    @FXML
    private TableColumn<User, String> adminLastnameColumn;

    @FXML
    private TableColumn<User, String> adminSpecialityColumn;

    @FXML
    private TableColumn<User, Boolean> adminActiveColumn;

    @FXML
    private TableColumn<User, Boolean> adminVerifiedColumn;

    @FXML
    private TableColumn<User, String> adminYouColumn; // New column for "You" indicator

    @FXML
    private TableColumn<User, Void> adminSelectColumn;

    @FXML
    private TableColumn<User, Void> adminActionColumn;

    // Artist table columns
    @FXML
    private TableColumn<User, Integer> artistIdColumn;

    @FXML
    private TableColumn<User, String> artistEmailColumn;

    @FXML
    private TableColumn<User, String> artistRolesColumn;

    @FXML
    private TableColumn<User, String> artistFirstnameColumn;

    @FXML
    private TableColumn<User, String> artistLastnameColumn;

    @FXML
    private TableColumn<User, String> artistSpecialityColumn;

    @FXML
    private TableColumn<User, Boolean> artistActiveColumn;

    @FXML
    private TableColumn<User, Boolean> artistVerifiedColumn;

    @FXML
    private TableColumn<User, Void> artistSelectColumn;

    @FXML
    private TableColumn<User, Void> artistActionColumn;

    // Client table columns
    @FXML
    private TableColumn<User, Integer> clientIdColumn;

    @FXML
    private TableColumn<User, String> clientEmailColumn;

    @FXML
    private TableColumn<User, String> clientRolesColumn;

    @FXML
    private TableColumn<User, String> clientFirstnameColumn;

    @FXML
    private TableColumn<User, String> clientLastnameColumn;

    @FXML
    private TableColumn<User, String> clientSpecialityColumn;

    @FXML
    private TableColumn<User, Boolean> clientActiveColumn;

    @FXML
    private TableColumn<User, Boolean> clientVerifiedColumn;

    @FXML
    private TableColumn<User, Void> clientSelectColumn;

    @FXML
    private TableColumn<User, Void> clientActionColumn;

    @FXML
    private Button backHome;
    @FXML
    private Button ExportPDF;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    @FXML
    private Button desactivateButton;

    @FXML
    private Button addAdminButton; // New button for adding an admin

    private User currentUser;

    private ObservableList<User> adminList = FXCollections.observableArrayList();
    private ObservableList<User> artistList = FXCollections.observableArrayList();
    private ObservableList<User> clientList = FXCollections.observableArrayList();
    private Set<User> selectedUsers = new HashSet<>();

    public void setUser(User user) {
        this.currentUser = user;
        updateUI();
    }

    private void updateUI() {
        if (currentUser == null) {
            System.out.println("No user provided to home controller");
            return;
        }

        AuthenticatedUser.setText(currentUser.getFirstname() + " " + currentUser.getLastname());
        String imagePath = currentUser.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File("D:/PI java/up-next/uploads/" + imagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                ProfileImage.setImage(image);
            } else {
                System.out.println("Image file not found: " + imagePath);
            }
        } else {
            System.out.println("No image path provided for user");
        }
    }

    private void loadUsersData() {
        adminList.clear();
        artistList.clear();
        clientList.clear();
        MyConnexion db = new MyConnexion();
        Connection conn = db.getConnection();
        if (conn == null) {
            System.err.println("Error: Failed to establish database connection");
            return;
        }
        String query = "SELECT id, email, roles, firstname, lastname, speciality, is_active, is_verified FROM user";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("roles"),
                        "", // password
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("speciality")
                );
                user.setIs_active(rs.getBoolean("is_active"));
                user.setIs_verified(rs.getBoolean("is_verified"));
                String role = user.getRoles().toUpperCase();
                if (role.contains("ADMIN")) {
                    adminList.add(user);
                } else if (role.contains("ARTIST")) {
                    artistList.add(user);
                } else if (role.contains("CLIENT")) {
                    clientList.add(user);
                }
            }
            adminTable.setItems(adminList);
            artistTable.setItems(artistList);
            clientTable.setItems(clientList);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading users: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void GoToAdminHome(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/admin.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /AdminHome.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            AdminHome adminHomeController = loader.getController();
            adminHomeController.setUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading home page: " + e.getMessage());
        }
    }


    @FXML
    void deleteSelectedUsers(ActionEvent event) {
        if (selectedUsers.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one user to delete.");
            alert.showAndWait();
            return;
        }

        // Check if the current user is among the selected users
        boolean isCurrentUserSelected = selectedUsers.stream().anyMatch(user -> user.getId() == currentUser.getId());
        if (isCurrentUserSelected) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Action Denied");
            alert.setHeaderText(null);
            alert.setContentText("You cannot delete your own account.");
            alert.showAndWait();
            // Remove the current user from the selected users to proceed with others
            selectedUsers.removeIf(user -> user.getId() == currentUser.getId());
            // If no other users are selected, return
            if (selectedUsers.isEmpty()) {
                return;
            }
        }

        MyConnexion db = new MyConnexion();
        Connection conn = db.getConnection();
        if (conn == null) {
            System.err.println("Error: Failed to establish database connection");
            return;
        }
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            for (User user : selectedUsers) {
                ps.setInt(1, user.getId());
                ps.executeUpdate();
                adminList.remove(user);
                artistList.remove(user);
                clientList.remove(user);
            }
            selectedUsers.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error deleting users: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void editSelectedUser(ActionEvent event) {
        if (selectedUsers.size() != 1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select exactly one user to edit.");
            alert.showAndWait();
            return;
        }
        User user = selectedUsers.iterator().next();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit User");
        alert.setHeaderText(null);
        alert.setContentText("Edit form for user: " + user.getEmail() + "\n(To be implemented)");
        alert.showAndWait();
    }

    @FXML
    void desactivateSelectedUsers(ActionEvent event) {
        if (selectedUsers.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one user to deactivate/activate.");
            alert.showAndWait();
            return;
        }

        // Check if the current user is among the selected users
        boolean isCurrentUserSelected = selectedUsers.stream().anyMatch(user -> user.getId() == currentUser.getId());
        if (isCurrentUserSelected) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Action Denied");
            alert.setHeaderText(null);
            alert.setContentText("You cannot deactivate/activate your own account.");
            alert.showAndWait();
            // Remove the current user from the selected users to proceed with others
            selectedUsers.removeIf(user -> user.getId() == currentUser.getId());
            // If no other users are selected, return
            if (selectedUsers.isEmpty()) {
                return;
            }
        }

        MyConnexion db = new MyConnexion();
        Connection conn = db.getConnection();
        if (conn == null) {
            System.err.println("Error: Failed to establish database connection");
            return;
        }
        String query = "UPDATE user SET is_active = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            for (User user : selectedUsers) {
                boolean newStatus = !user.isIs_active();
                ps.setBoolean(1, newStatus);
                ps.setInt(2, user.getId());
                ps.executeUpdate();
                user.setIs_active(newStatus);
            }
            adminTable.refresh();
            artistTable.refresh();
            clientTable.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating user status: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void addAdmin(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Admin");
        alert.setHeaderText(null);
        alert.setContentText("Add admin form (to be implemented)");
        alert.showAndWait();
    }

    @FXML
    void initialize() {
        // Initialize Admin table columns
        adminIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        adminEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        adminFirstnameColumn.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        adminLastnameColumn.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        adminSpecialityColumn.setCellValueFactory(new PropertyValueFactory<>("speciality"));
        adminActiveColumn.setCellValueFactory(new PropertyValueFactory<>("is_active"));
        adminVerifiedColumn.setCellValueFactory(new PropertyValueFactory<>("is_verified"));
        adminYouColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (currentUser != null && user.getId() == currentUser.getId()) {
                return new SimpleStringProperty("You");
            }
            return new SimpleStringProperty("");
        });

        // Initialize Artist table columns
        artistIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        artistEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        artistFirstnameColumn.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        artistLastnameColumn.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        artistSpecialityColumn.setCellValueFactory(new PropertyValueFactory<>("speciality"));
        artistActiveColumn.setCellValueFactory(new PropertyValueFactory<>("is_active"));
        artistVerifiedColumn.setCellValueFactory(new PropertyValueFactory<>("is_verified"));

        // Initialize Client table columns
        clientIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        clientEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        clientFirstnameColumn.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        clientLastnameColumn.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        clientSpecialityColumn.setCellValueFactory(new PropertyValueFactory<>("speciality"));
        clientActiveColumn.setCellValueFactory(new PropertyValueFactory<>("is_active"));
        clientVerifiedColumn.setCellValueFactory(new PropertyValueFactory<>("is_verified"));

        // Configure selectColumn with checkboxes for all tables
        adminSelectColumn.setCellFactory(column -> createSelectCell());
        artistSelectColumn.setCellFactory(column -> createSelectCell());
        clientSelectColumn.setCellFactory(column -> createSelectCell());

        // Configure actionColumn with Edit/Delete/Deactivate buttons for all tables
        adminActionColumn.setCellFactory(column -> createActionCell());
        artistActionColumn.setCellFactory(column -> createActionCell());
        clientActionColumn.setCellFactory(column -> createActionCell());

        // Set default profile image
        if (ProfileImage.getImage() == null) {
            ProfileImage.setImage(new Image(getClass().getResourceAsStream("/user.png")));
        }

        // Load user data
        loadUsersData();
    }

    private TableCell<User, Void> createSelectCell() {
        return new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (isSelected) {
                        selectedUsers.add(user);
                    } else {
                        selectedUsers.remove(user);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(selectedUsers.contains(user));
                    setGraphic(checkBox);
                }
            }
        };
    }

    private TableCell<User, Void> createActionCell() {
        return new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button deactivateButton = new Button("Deactivate");

            {
                // Style buttons
                editButton.setStyle("-fx-background-color: #32CD32; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white;");
                deactivateButton.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black;");

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    System.out.println("Edit user: " + user.getEmail());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Edit User");
                    alert.setHeaderText(null);
                    alert.setContentText("Edit form for user: " + user.getEmail() + "\n(To be implemented)");
                    alert.showAndWait();
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    System.out.println("Delete user: " + user.getEmail());
                    MyConnexion db = new MyConnexion();
                    Connection conn = db.getConnection();
                    if (conn != null) {
                        String query = "DELETE FROM user WHERE id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(query)) {
                            ps.setInt(1, user.getId());
                            ps.executeUpdate();
                            adminList.remove(user);
                            artistList.remove(user);
                            clientList.remove(user);
                            selectedUsers.remove(user);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (conn != null) conn.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                deactivateButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    System.out.println("Toggle active status for user: " + user.getEmail());
                    MyConnexion db = new MyConnexion();
                    Connection conn = db.getConnection();
                    if (conn != null) {
                        String query = "UPDATE user SET is_active = ? WHERE id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(query)) {
                            boolean newStatus = !user.isIs_active();
                            ps.setBoolean(1, newStatus);
                            ps.setInt(2, user.getId());
                            ps.executeUpdate();
                            user.setIs_active(newStatus);
                            adminTable.refresh();
                            artistTable.refresh();
                            clientTable.refresh();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (conn != null) conn.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    // Disable delete button for the logged-in admin
                    deleteButton.setDisable(currentUser != null && user.getId() == currentUser.getId());
                    deactivateButton.setDisable(currentUser != null && user.getId() == currentUser.getId());
                    HBox buttons = new HBox(5, editButton, deleteButton, deactivateButton);
                    setGraphic(buttons);
                }
            }
        };
    }


    @FXML
    public void exportUsersToPDF(ActionEvent actionEvent) {
        try {
            // Define the output PDF file path with a directory
            String dest = "exports/UsersReport.pdf";
            File file = new File(dest);

            // Create parent directories if they don't exist
            File parentDir = file.getParentFile();
            if (parentDir != null) {
                parentDir.mkdirs();
            }

            // Initialize PDF document
            PDDocument document = new PDDocument();
            PDPage[] pageHolder = new PDPage[1];
            pageHolder[0] = new PDPage();
            document.addPage(pageHolder[0]);
            PDPageContentStream[] contentStreamHolder = new PDPageContentStream[1];
            contentStreamHolder[0] = new PDPageContentStream(document, pageHolder[0]);

            // Set initial parameters
            float margin = 50;
            float[] yPositionHolder = new float[1];
            yPositionHolder[0] = pageHolder[0].getMediaBox().getHeight() - margin;
            float pageWidth = pageHolder[0].getMediaBox().getWidth();
            float tableWidth = pageWidth - 2 * margin;

            // Define custom column widths
            float[] columnWidths = new float[] {30, 150, 78, 77, 77, 50, 50}; // Adjusted to match tableWidth of 512
            // Verify total width matches tableWidth
            float totalWidth = 0;
            for (float width : columnWidths) {
                totalWidth += width;
            }
            if (Math.abs(totalWidth - tableWidth) > 1) {
                throw new IllegalStateException("Total column widths (" + totalWidth + ") do not match table width (" + tableWidth + ")");
            }

            float rowHeight = 25;
            float cellPadding = 8;

            // Add logo
            PDImageXObject logo = PDImageXObject.createFromFile("src/main/resources/up-next.png", document);
            float logoWidth = 150; // Desired width of the logo
            float logoHeight = 50; // Desired height of the logo
            float logoX = (pageWidth - logoWidth) / 2; // Center the logo
            contentStreamHolder[0].drawImage(logo, logoX, yPositionHolder[0] - logoHeight, logoWidth, logoHeight);
            yPositionHolder[0] -= logoHeight + 20; // Space after logo

            // Add main title
            contentStreamHolder[0].setFont(PDType1Font.HELVETICA_BOLD, 20);
            String title = "Users Report";
            contentStreamHolder[0].beginText();
            float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * 20;
            contentStreamHolder[0].newLineAtOffset((pageWidth - titleWidth) / 2, yPositionHolder[0]);
            contentStreamHolder[0].showText(title);
            contentStreamHolder[0].endText();
            yPositionHolder[0] -= 25; // Reduced space after title

            // Add export date
            contentStreamHolder[0].setFont(PDType1Font.HELVETICA, 12);
            String dateText = "Export Date: " + LocalDate.now().toString();
            contentStreamHolder[0].beginText();
            float dateWidth = PDType1Font.HELVETICA.getStringWidth(dateText) / 1000 * 12;
            contentStreamHolder[0].newLineAtOffset((pageWidth - dateWidth) / 2, yPositionHolder[0]);
            contentStreamHolder[0].showText(dateText);
            contentStreamHolder[0].endText();
            yPositionHolder[0] -= 40; // Space after date

            // Fetch users and categorize by role
            List<String[]> admins = new ArrayList<>();
            List<String[]> artists = new ArrayList<>();
            List<String[]> clients = new ArrayList<>();

            MyConnexion db = new MyConnexion();
            Connection conn = db.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }

            String query = "SELECT id, email, roles, firstname, lastname, speciality, is_active, is_verified FROM user";
            try (PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] rowData = {
                            String.valueOf(rs.getInt("id")),
                            rs.getString("email"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("speciality") != null ? rs.getString("speciality") : "N/A",
                            String.valueOf(rs.getBoolean("is_active")),
                            String.valueOf(rs.getBoolean("is_verified"))
                    };
                    String role = rs.getString("roles").toUpperCase();
                    if (role.contains("ADMIN")) {
                        admins.add(rowData);
                    } else if (role.contains("ARTIST")) {
                        artists.add(rowData);
                    } else if (role.contains("CLIENT")) {
                        clients.add(rowData);
                    }
                }
            } catch (SQLException e) {
                throw new SQLException("Error fetching users: " + e.getMessage());
            } finally {
                try {
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Draw the three tables
            String[] headers = {"ID", "Email", "First Name", "Last Name", "Speciality", "Active", "Verified"};
            if (!admins.isEmpty()) {
                drawTable(admins, "Admins", document, pageHolder, contentStreamHolder, yPositionHolder, headers, margin, columnWidths, rowHeight, cellPadding);
            }
            if (!artists.isEmpty()) {
                drawTable(artists, "Artists", document, pageHolder, contentStreamHolder, yPositionHolder, headers, margin, columnWidths, rowHeight, cellPadding);
            }
            if (!clients.isEmpty()) {
                drawTable(clients, "Clients", document, pageHolder, contentStreamHolder, yPositionHolder, headers, margin, columnWidths, rowHeight, cellPadding);
            }

            // Close the content stream and save the document
            contentStreamHolder[0].close();
            document.save(dest);
            document.close();

            // Open the PDF file
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (file.exists()) {
                    desktop.open(file);
                } else {
                    throw new IOException("PDF file was created but could not be found at: " + dest);
                }
            } else {
                throw new UnsupportedOperationException("Desktop API is not supported on this platform.");
            }

            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Users report has been exported to " + dest + " and opened automatically.");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export or open users report: " + e.getMessage());
            alert.showAndWait();
        }
    }
    private void drawTable(List<String[]> data, String tableTitle, PDDocument document, PDPage[] pageHolder,
                           PDPageContentStream[] contentStreamHolder, float[] yPositionHolder, String[] headers,
                           float margin, float[] columnWidths, float rowHeight, float cellPadding) throws IOException {
        float yPosition = yPositionHolder[0];
        PDPage page = pageHolder[0];
        PDPageContentStream contentStream = contentStreamHolder[0];

        // Check if we need a new page
        if (yPosition < margin + rowHeight * (data.size() + 3)) {
            contentStream.close();
            page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            yPosition = page.getMediaBox().getHeight() - margin;
        }

        // Draw table title
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(tableTitle);
        contentStream.endText();
        yPosition -= 30;

        // Draw table headers
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.setLineWidth(1f);
        float currentX = margin;
        for (int i = 0; i < headers.length; i++) {
            contentStream.moveTo(currentX, yPosition);
            contentStream.lineTo(currentX + columnWidths[i], yPosition);
            contentStream.lineTo(currentX + columnWidths[i], yPosition - rowHeight);
            contentStream.lineTo(currentX, yPosition - rowHeight);
            contentStream.lineTo(currentX, yPosition);
            contentStream.stroke();

            contentStream.beginText();
            contentStream.newLineAtOffset(currentX + cellPadding, yPosition - rowHeight + 5);
            contentStream.showText(headers[i]);
            contentStream.endText();

            currentX += columnWidths[i];
        }
        yPosition -= rowHeight;

        // Draw table data
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        for (String[] rowData : data) {
            if (yPosition < margin + rowHeight) {
                contentStream.close();
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(PDType1Font.HELVETICA, 9);
                yPosition = page.getMediaBox().getHeight() - margin;
            }

            currentX = margin;
            for (int i = 0; i < rowData.length; i++) {
                contentStream.moveTo(currentX, yPosition);
                contentStream.lineTo(currentX + columnWidths[i], yPosition);
                contentStream.lineTo(currentX + columnWidths[i], yPosition - rowHeight);
                contentStream.lineTo(currentX, yPosition - rowHeight);
                contentStream.lineTo(currentX, yPosition);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.newLineAtOffset(currentX + cellPadding, yPosition - rowHeight + 5);
                String text = rowData[i];

                contentStream.showText(text);
                contentStream.endText();

                currentX += columnWidths[i];
            }
            yPosition -= rowHeight;
        }
        yPosition -= 20;

        // Update the holders
        yPositionHolder[0] = yPosition;
        pageHolder[0] = page;
        contentStreamHolder[0] = contentStream;
    }
}

