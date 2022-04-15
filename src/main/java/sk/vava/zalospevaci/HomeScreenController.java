package sk.vava.zalospevaci;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.scene.paint.Color;

public class HomeScreenController implements Initializable {

    @FXML
    private VBox infoVBox;
    @FXML
    private HBox restaurantsHBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        homeScreen();
    }

    public String getJSON(String url){
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }  catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return "ERROR";
        }

    }

    public void homeScreen(){
        JSONArray restaurantsArray = new JSONArray(getJSON("http://localhost:8080/restaurants"));

        Text brandLabel = new Text("Get Your Meal");
        TextField searchInput = new TextField();
        Image image = new Image("https://i.imgur.com/Tf3j0rU.jpg");

        brandLabel.getStyleClass().add("brandLabel");
        searchInput.getStyleClass().add("searchInput");
        searchInput.setPromptText("Search...");

        infoVBox.setAlignment(Pos.CENTER);
        infoVBox.setSpacing(25);
        infoVBox.getChildren().add(brandLabel);
        infoVBox.getChildren().add(searchInput);

        for(int i = 0; i < 2; i++){
            JSONObject restaurant = restaurantsArray.getJSONObject(i);

            ImageView imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(300);
            imageView.setImage(image);

            Text imageText = new Text(restaurant.getString("name"));
            imageText.getStyleClass().add("restaurantBoxLabel");
            imageText.setFill(Color.WHITE);

            StackPane pane = new StackPane();

            pane.getChildren().addAll(imageView, imageText);

            restaurantsHBox.setSpacing(20);
            restaurantsHBox.getChildren().addAll(pane);

            pane.setOnMouseClicked(e -> {
                JSONLoaded.setRestaurant(restaurant);
                Stage stage = (Stage) pane.getScene().getWindow();
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
        }


    }
}
