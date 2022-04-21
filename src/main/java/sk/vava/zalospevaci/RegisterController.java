package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Objects;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        register();
    }

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    @FXML
    private VBox mainVBox;

    private boolean successful;

    public String handleRegister(String url, String username, String password, String email, boolean isManager, String countryCode, String number){
        if (username == "" || password == "" || email == "" || countryCode == "" || number == "") {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Empty fields");
            alert.setHeaderText("Empty fields");
            alert.setContentText("Please fill all fields");
            alert.showAndWait();
            return null;
        }

        //check if email is in correct format
        if (!email.contains("@") || !email.contains(".")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid email");
            alert.setHeaderText("Invalid email");
            alert.setContentText("Please enter a valid email");
            alert.showAndWait();
            return null;
        }

        //check if number is in correct format
        if (!number.matches("[0-9]+") || number.length() != 9) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid number");
            alert.setHeaderText("Invalid number");
            alert.setContentText("Please enter a valid number");
            alert.showAndWait();
            return null;
        }

        //check if country code is in correct format
        if (!countryCode.matches("\\+[0-9]+") || countryCode.length() > 4) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid country code");
            alert.setHeaderText("Invalid country code");
            alert.setContentText("Please enter a valid country code");
            alert.showAndWait();
            return null;
        }

        JSONObject requestB = new JSONObject();
        requestB.put("username", username);
        requestB.put("password", password);
        requestB.put("email", email);

        if (isManager) {
            requestB.put("role", "manager");
        } else {
            requestB.put("role", "user");
        }

        requestB.put("phone", new JSONObject().put("country_code", countryCode).put("number", number));
        requestB.put("address", new JSONObject().put("name", "Not Set").put("street","Not Set").put("building_number", "0").put("city", "Not Set").put("state", "Not Set").put("postcode","0"));

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(requestB.toString()))
                    .uri(new URI(url))
                    .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                    .header("Content-Type", "application/json")
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "ERROR";
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 201){ successful = true; }
            else {
                System.out.println(response.statusCode());
                System.out.println(response.body());
            }
            return response.body();
        } catch (InterruptedException | IOException e) {
            return "ERROR";
        }
    }

    public void register() {
        VBox container = new VBox();
        VBox form = new VBox();

        container.getChildren().add(form);
        mainVBox.getChildren().add(container);

        container.setAlignment(Pos.CENTER);
        container.prefHeightProperty().setValue(680);

        form.setMaxWidth(400);
        form.setSpacing(15);
        form.setAlignment(Pos.CENTER);
        form.getStyleClass().add("form");

        StackPane titlePane = new StackPane();
        Text label = new Text("Create Account");
        label.getStyleClass().add("formTitle");

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

        titlePane.getChildren().addAll(label, goBack);
        titlePane.getStyleClass().add("formTitlePane");

        TextField name = new TextField();
        name.getStyleClass().add("formInput");
        name.setPromptText("name");
        name.setPrefWidth(360);
        name.setMaxWidth(360);

        TextField email = new TextField();
        email.getStyleClass().add("formInput");
        email.setPromptText("email");
        email.setPrefWidth(360);
        email.setMaxWidth(360);

        PasswordField password = new PasswordField();
        password.getStyleClass().add("formInput");
        password.setPromptText("password");
        password.setPrefWidth(360);
        password.setMaxWidth(360);

        HBox inputs = new HBox();
        TextField prefix = new TextField();
        prefix.getStyleClass().add("formInput");
        prefix.setPromptText("+421");
        prefix.setPrefWidth(60);
        prefix.setMaxWidth(60);

        TextField phone = new TextField();
        phone.getStyleClass().add("formInput");
        phone.setPromptText("phone");
        phone.setPrefWidth(290);
        phone.setMaxWidth(290);

        inputs.setAlignment(Pos.CENTER);
        inputs.setSpacing(10);
        inputs.getChildren().addAll(prefix, phone);

        RadioButton user = new RadioButton("User");
        RadioButton manager = new RadioButton("Restaurant Manager");

        ToggleGroup radioGroup = new ToggleGroup();
        user.setToggleGroup(radioGroup);
        manager.setToggleGroup(radioGroup);

        user.setSelected(true);

        Button registerButton = new Button("Create");
        registerButton.getStyleClass().add("formButton");

        registerButton.setOnMouseClicked(e -> {
            RadioButton rb = (RadioButton) radioGroup.getSelectedToggle();
            boolean isManager = Objects.equals(rb.getText(), "Restaurant Manager");
            handleRegister("http://localhost:8080/users", name.getText(), password.getText(), email.getText(), isManager, prefix.getText(), phone.getText());
            if(successful) {
                Stage stage = (Stage) registerButton.getScene().getWindow();
                Parent root = null;
                try {
                    root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("login.fxml")));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                assert root != null;
                Scene scene = new Scene(root, 1280, 720);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
                stage.setScene(scene);
            }
        });

        HBox radioButtons = new HBox();
        radioButtons.setAlignment(Pos.CENTER);
        radioButtons.getChildren().addAll(user, manager);
        radioButtons.setSpacing(10);
        VBox bottomContainer = new VBox();

        bottomContainer.setAlignment(Pos.CENTER);
        bottomContainer.setSpacing(20);
        bottomContainer.getChildren().addAll(radioButtons, registerButton);

        HBox spacer = new HBox();
        spacer.setSpacing(20);

        form.getChildren().addAll(titlePane, name, email, password, inputs, bottomContainer, spacer);
    }
}
