
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Main JavaFX application for the GCM login system.
 */
public class GCMApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Read and validate users using Lab 1 logic.
        ArrayList<User> users = UsersApp.loadUsers("Users.txt");

        // Load the login interface.
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/Login.fxml")
        );

        Parent root = loader.load();

        // Give the controller the valid users.
        LoginController controller = loader.getController();
        controller.setUsers(users);

        // Create and display the login window.
        Scene scene = new Scene(root);

        primaryStage.setTitle("GCM Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
