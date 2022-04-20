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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;

public class HomeScreenController implements Initializable {

    @FXML
    private VBox infoVBox;
    @FXML
    private HBox restaurantsHBox;
    @FXML
    private Button menubtn;
    @FXML
    private Button userbtn;
    @FXML
    private VBox menubar;
    @FXML
    private VBox userBar;

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
        JSONObject user = JSONLoaded.getUser();

        if (user != null) {
            if(!Objects.equals(user.getString("token"), "")){
                if(Objects.equals(user.getString("role"), "manager")) {
                    userBarManagerF();
                }
            }
        } else {
            userBarF();
        }

        menuBarF();

        Text brandLabel = new Text("Get Your Meal");
        TextField searchInput = new TextField();
        Button searchButton = new Button();
        StackPane inputPane = new StackPane();
        Image image = new Image("https://i.imgur.com/Tf3j0rU.jpg");

        brandLabel.getStyleClass().add("brandLabel");
        searchInput.getStyleClass().add("searchInput");
        searchInput.setPromptText("Search...");

        inputPane.getChildren().addAll(searchInput, searchButton);

        searchButton.setTranslateX(420);

        searchButton.getStyleClass().add("searchButton");

        infoVBox.setAlignment(Pos.CENTER);
        infoVBox.setSpacing(25);
        infoVBox.getChildren().addAll(brandLabel, inputPane);
        int length = Math.min(restaurantsArray.length(), 2);
        for(int i = 0; i < length; i++){
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

    public void userBarF() {
        userBar.getStyleClass().add("menubar");
        userBar.setVisible(false);
        userBar.setSpacing(20);
        Button goBack = new Button("\uD83E\uDC14 Close");
        Pane spacer = new Pane();
        spacer.setPrefHeight(200);
        Button login = new Button("Login");
        Button register = new Button("Register");

        login.setOnMouseClicked(e -> {
            Stage stage = (Stage) login.getScene().getWindow();
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
        });

        register.setOnMouseClicked(e -> {
            Stage stage = (Stage) register.getScene().getWindow();
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

        login.getStyleClass().add("whitebuttonmenu");
        register.getStyleClass().add("whitebuttonmenu");
        goBack.getStyleClass().add("backbutton");
        userBar.getChildren().addAll(goBack,spacer,login,register);
        userbtn.setOnMouseClicked(e -> {
            userBar.setVisible(true);
        });
        goBack.setOnMouseClicked(e -> {
            userBar.setVisible(false);
        });
    }

    public void userBarManagerF() {
        userBar.getStyleClass().add("menubar");
        userBar.setVisible(false);
        userBar.setSpacing(20);
        Button goBack = new Button("\uD83E\uDC14 Close");
        Pane spacer = new Pane();
        spacer.setPrefHeight(200);
        Button accountSettings = new Button("Account Settings");
        Button createAccount = new Button("Create Account");
        Button manageRestaurant = new Button("Manage Restaurant");
        Button logout = new Button("Logout");

        logout.setOnMouseClicked(e -> {
            JSONLoaded.setUser(null);
            Stage stage = (Stage) logout.getScene().getWindow();
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

        accountSettings.getStyleClass().add("whitebuttonmenu");
        createAccount.getStyleClass().add("whitebuttonmenu");
        manageRestaurant.getStyleClass().add("whitebuttonmenu");
        logout.getStyleClass().add("whitebuttonmenu");

        goBack.getStyleClass().add("backbutton");
        userBar.getChildren().addAll(goBack,spacer,accountSettings,createAccount, manageRestaurant, logout);
        userbtn.setOnMouseClicked(e -> {
            userBar.setVisible(true);
        });
        goBack.setOnMouseClicked(e -> {
            userBar.setVisible(false);
        });
    }
}
