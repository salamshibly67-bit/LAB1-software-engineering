
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Handles the login screen and checks the user's credentials.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private ArrayList<User> users;

    /**
     * Receives the list of valid users loaded from the input file.
     */
    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    /**
     * Called when the Login button is clicked.
     */
    @FXML
    private void handleLogin() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (users == null) {
            errorLabel.setText("User data is unavailable");
            return;
        }

        for (User user : users) {

            if (user.getName().equals(username) &&
                    user.getPassword().equals(password)) {

                openWelcomeScreen();
                return;
            }
        }

        errorLabel.setText("Invalid username or password");
    }

    /**
     * Replaces the login screen with the welcome screen.
     */
    private void openWelcomeScreen() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Welcome.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage)
                    usernameField.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Welcome to GCM");

        } catch (IOException e) {
            errorLabel.setText("Cannot open welcome screen");
            e.printStackTrace();
        }
    }
}
