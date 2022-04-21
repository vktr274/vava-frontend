package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import static java.net.http.HttpRequest.BodyPublishers.ofInputStream;


public class LoginController implements Initializable  {
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loginScreen();
    }

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();


    @FXML
    private VBox mainVBox;

    public String handleLogin(String url, String username, String password){
        JSONObject requestB = new JSONObject();
        requestB.put("login", username);
        requestB.put("password", password);

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(requestB.toString()))
                    .uri(new URI(url))
                    .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                    .header("Content-Type", "application/json")
                    .build();
        } catch (URISyntaxException e) {
            return "ERROR";
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                JSONObject user = new JSONObject(response.body()).put("name", username);
                JSONLoaded.setUser(user);
                return response.body();
            }
            else{
                System.out.println(response.statusCode());
            }
        } catch (InterruptedException | IOException e) {
            return "ERROR";
        }
        return "ERROR";
    }

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
        Button goBack = new Button("X");

        goBack.setTranslateX(180);

        goBack.setOnMouseClicked(e -> {
            Stage stage = (Stage) goBack.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("homeScreen.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        label.getStyleClass().add("formTitle");

        titlePane.getChildren().addAll(label, goBack);
        titlePane.getStyleClass().add("formTitlePane");

        TextField username = new TextField();
        username.getStyleClass().add("formInput");
        username.setPromptText("Username...");
        username.setPrefWidth(360);
        username.setMaxWidth(360);

        PasswordField password = new PasswordField();
        password.getStyleClass().add("formInput");
        password.setPromptText("Password...");
        password.setPrefWidth(360);
        password.setMaxWidth(360);

        Button loginButton = new Button("Log In");
        loginButton.getStyleClass().add("formButton");

        loginButton.setOnMouseClicked(e -> {
            handleLogin("http://localhost:8080/token", username.getText(), password.getText());
            JSONObject user = JSONLoaded.getUser();
            if (user != null && !Objects.equals(user.getString("token"), "")) {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Parent root = null;
                try {
                    root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("homeScreen.fxml")));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                assert root != null;
                Scene scene = new Scene(root, 1280, 720);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
                stage.setScene(scene);
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid Credentials");
                alert.setContentText("Please check your username and password and try again.");
                alert.showAndWait();
            }
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
