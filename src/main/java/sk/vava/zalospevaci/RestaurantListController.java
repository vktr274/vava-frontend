package sk.vava.zalospevaci;

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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.*;

public class RestaurantListController implements Initializable {

    @FXML
    private VBox tree;
    @FXML
    private Button menubtn;
    @FXML
    private VBox menubar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        restaurantSetScreen();
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

    public void restaurantSetScreen(){
        tree.setSpacing(25);
        JSONArray array = new JSONArray(getJSON("http://localhost:8080/restaurants"));
        Text restaurantLabel = new Text("Restaurants");
        restaurantLabel.getStyleClass().add("label");
        tree.getChildren().add(restaurantLabel);

        menuBarF();
        for(int i=0; i<array.length();i++){
            Image image = new Image("https://i.imgur.com/Tf3j0rU.jpg");
            Pane spacer1 = new Pane();
            Pane spacer2 = new Pane();
            Pane spacer3 = new Pane();
            HBox restaurant = new HBox(25);
            Button addReview = new Button();
            JSONObject object = array.getJSONObject(i);
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            spacer1.setPrefWidth(0);
            spacer3.setPrefWidth(0);

            addReview.setText("Add Review");
            addReview.getStyleClass().add("whitebutton");

            ImageView imageView = new ImageView();
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(90);

            restaurant.getStyleClass().add("itembutton");
            restaurant.getChildren().addAll(spacer1,imageView,new Text(object.getString("name")),spacer2,addReview,spacer3);

            restaurant.setOnMouseClicked(e -> {
                JSONLoaded.setRestaurant(object);
                Stage stage = (Stage) restaurant.getScene().getWindow();
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

            tree.getChildren().add(restaurant);
        }
    }
    public void menuBarF(){
        menubar.getStyleClass().add("menubar");
        menubar.setVisible(false);
        menubar.setSpacing(20);
        Button goBack = new Button("\uD83E\uDC14 Close");
        Pane spacer = new Pane();
        spacer.setPrefHeight(200);
        Button restaurant = new Button("Restaurants");
        Button settings = new Button("Settings");
        restaurant.getStyleClass().add("whitebuttonmenu");
        settings.getStyleClass().add("whitebuttonmenu");
        goBack.getStyleClass().add("backbutton");
        menubar.getChildren().addAll(goBack,spacer,restaurant,settings);
        menubtn.setOnMouseClicked(e -> {
            menubar.setVisible(true);
        });
        goBack.setOnMouseClicked(e -> {
            menubar.setVisible(false);
        });
        restaurant.setOnMouseClicked(e -> {
            Stage stage = (Stage) restaurant.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("restaurantList.fxml")));
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