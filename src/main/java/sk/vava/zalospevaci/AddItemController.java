package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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

public class AddItemController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createRestaurants();
    }

    @FXML
    private VBox mainVBox;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public String handleCreate(String url, String name, String description, int price) {
        JSONObject requestB = new JSONObject();

        requestB.put("name", name);
        requestB.put("description", description);
        requestB.put("price", price);

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
        Text label = new Text("Create item");
        label.getStyleClass().add("formTitle");

        Button goBack = new Button("X");

        goBack.setTranslateX(180);

        goBack.setOnMouseClicked(e -> {
            Stage stage = (Stage) goBack.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("restaurantMenu.fxml")));
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

        TextArea description = new TextArea();
        description.getStyleClass().add("formInput");
        description.setPromptText("decription");
        description.setPrefWidth(360);
        description.setMaxWidth(360);

        description.setWrapText(true);

        TextField price = new TextField();
        price.getStyleClass().add("formInput");
        price.setPromptText("price");
        price.setPrefWidth(150);
        price.setMaxWidth(150);


        Button createButton = new Button("Create");
        createButton.getStyleClass().add("formButton");

        HBox spacer = new HBox();
        spacer.setSpacing(20);

        createButton.setOnMouseClicked(e -> {
            String nameText = name.getText();
            String descriptionText = description.getText();
            int priceText = Integer.parseInt(price.getText())*100;

            if (nameText.isEmpty() || descriptionText.isEmpty() || price.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("Please fill in all fields");
                alert.showAndWait();
            }

            System.out.println(descriptionText);
            handleCreate("http://localhost:8080/items?restaurantId="+JSONLoaded.getRestaurant().getInt("id"), nameText, descriptionText, priceText);
        });

        form.getChildren().addAll(titlePane, name,description, price, createButton, spacer);
    }
}
