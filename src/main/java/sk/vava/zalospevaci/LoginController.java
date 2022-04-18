package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginController implements Initializable  {
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loginScreen();
    }

    @FXML
    private VBox mainVBox;

    public void loginScreen(){
        VBox container = new VBox();
        VBox form = new VBox();

        container.getChildren().add(form);
        mainVBox.getChildren().add(container);

        container.setAlignment(Pos.CENTER);
        container.prefHeightProperty().setValue(680);

        form.setMaxWidth(400);
        form.setSpacing(30);
        form.setAlignment(Pos.CENTER);
        form.getStyleClass().add("form");

        StackPane titlePane = new StackPane();
        Text label = new Text("Login");
        label.getStyleClass().add("formTitle");

        titlePane.getChildren().add(label);
        titlePane.getStyleClass().add("formTitlePane");

        TextField username = new TextField();
        username.getStyleClass().add("formInput");
        username.setPromptText("Username...");
        username.setPrefWidth(360);
        username.setMaxWidth(360);

        TextField password = new TextField();
        password.getStyleClass().add("formInput");
        password.setPromptText("Password...");
        password.setPrefWidth(360);
        password.setMaxWidth(360);

        Button loginButton = new Button("Log In");
        loginButton.getStyleClass().add("formButton");

        loginButton.setOnMouseClicked(e -> {
            label.setText("token");
        });

        Text or = new Text("or");
        Button createAccount = new Button("Create Account");
        createAccount.getStyleClass().add("altButton");

        createAccount.setOnMouseClicked(e -> {
            Stage stage = (Stage) createAccount.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("register.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        VBox bottomContainer = new VBox();

        bottomContainer.setAlignment(Pos.CENTER);
        bottomContainer.setSpacing(5);
        bottomContainer.getChildren().addAll(loginButton, or, createAccount);

        form.getChildren().addAll(titlePane, username, password, bottomContainer);
    }
}
