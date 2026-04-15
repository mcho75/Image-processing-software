package ImageApp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.Objects;

public class AppLauncher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("AppView.fxml")));
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResource("icon.png")).toExternalForm()));
        primaryStage.setTitle("ImageData FX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
