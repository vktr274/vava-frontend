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

public class CreateRestaurantController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createRestaurants();
    }

    @FXML
    private VBox mainVBox;

    private Button registerButton = new Button("Register");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public String handleCreate(String url, Restaurant restaurant){
        JSONObject requestB = new JSONObject(restaurant);
        System.out.println(requestB);

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(requestB.toString()))
                    .uri(new URI(url))
                    .setHeader("auth", JSONLoaded.getUser().getString("token")) // add request header
                    .header("Content-Type", "application/json")
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "ERROR";
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            if (response.statusCode() == 201) {
                Stage stage = (Stage) registerButton.getScene().getWindow();
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
            return response.body();
        } catch (InterruptedException | IOException e) {
            return "ERROR";
        }
    }

    private void createRestaurants() {
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
        Text label = new Text("Register Restaurant");
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

        TextField addrName = new TextField();
        addrName.getStyleClass().add("formInput");
        addrName.setPromptText("Address name");
        addrName.setPrefWidth(360);
        addrName.setMaxWidth(360);

        HBox inputs1 = new HBox();
        TextField street = new TextField();
        street.getStyleClass().add("formInput");
        street.setPromptText("street");
        street.setPrefWidth(200);

        TextField building_number = new TextField();
        building_number.getStyleClass().add("formInput");
        building_number.setPromptText("Build. no.");
        building_number.setPrefWidth(150);

        inputs1.setAlignment(Pos.CENTER);
        inputs1.setSpacing(10);
        inputs1.getChildren().addAll(street, building_number);

        HBox inputs2 = new HBox();
        TextField city = new TextField();
        city.getStyleClass().add("formInput");
        city.setPromptText("city");
        city.setPrefWidth(175);

        TextField country = new TextField();
        country.getStyleClass().add("formInput");
        country.setPromptText("country");
        country.setPrefWidth(175);

        inputs2.setAlignment(Pos.CENTER);
        inputs2.setSpacing(10);
        inputs2.getChildren().addAll(city, country);

        HBox phoneInputs = new HBox();
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

        phoneInputs.setAlignment(Pos.CENTER);
        phoneInputs.setSpacing(10);
        phoneInputs.getChildren().addAll(prefix, phone);

        TextField postCode = new TextField();
        postCode.getStyleClass().add("formInput");
        postCode.setPromptText("post code");
        postCode.setPrefWidth(120);
        postCode.setMaxWidth(120);

        registerButton.getStyleClass().add("formButton");

        HBox spacer = new HBox();
        spacer.setSpacing(20);

        registerButton.setOnMouseClicked(e -> {
            if (name.getText().isEmpty() || street.getText().isEmpty() || building_number.getText().isEmpty() || city.getText().isEmpty() || country.getText().isEmpty() || phone.getText().isEmpty() || prefix.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Please fill in all fields");
                alert.showAndWait();
            }
            //check if phone number is valid
            else if (phone.getText().length() != 9) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Phone number is not valid");
                alert.showAndWait();
            }
            //check if post code is valid
            else if (postCode.getText().length() != 5) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Post code is not valid");
                alert.showAndWait();
            }
            else {
                Phone restaurantPhone = new Phone(phone.getText(), prefix.getText());
                Address restaurantAddress = new Address(addrName.getText(), street.getText(), city.getText(), country.getText(), postCode.getText(), building_number.getText());
                Restaurant restaurant = new Restaurant(name.getText(), restaurantAddress, restaurantPhone);

                handleCreate("http://localhost:8080/restaurants", restaurant);
            }
        });

        form.getChildren().addAll(titlePane, name, addrName, inputs1, inputs2, postCode, phoneInputs,registerButton, spacer);
    }
}
