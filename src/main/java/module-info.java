module teamteacher {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.base;

    opens ImageApp to javafx.fxml;
    opens ImageApp.ui to javafx.fxml;
    opens ImageApp.tools to javafx.fxml;
    exports ImageApp;
    exports ImageApp.ui;
    exports ImageApp.tools;
    exports ImageApp.MenuAction;
    exports ImageApp.data;
}
