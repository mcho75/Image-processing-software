package ImageApp.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import java.io.IOException;

public class AboutDialog extends Dialog<Void> {

    public AboutDialog() {
        super();

        DialogPane aboutPane;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AboutDialog.fxml"));
            fxmlLoader.setController(this);
            aboutPane = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setDialogPane(aboutPane);
    }
}
