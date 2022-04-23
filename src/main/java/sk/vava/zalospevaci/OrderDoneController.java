package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class OrderDoneController implements Initializable {

    @FXML
    private Button menubtn;
    @FXML
    private VBox menubar;
    @FXML
    private VBox orderD;

    private static ResourceBundle lang;
    private void setLang(ResourceBundle lang){
        OrderDoneController.lang = lang;
    }
    private ResourceBundle getLang(){
        return OrderDoneController.lang;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale("sk", "SK"));
        setLang(lngBndl);
        SetScreen();
    }

    public void SetScreen(){
        menuBarF();
        orderD.setSpacing(30);
        orderD.setAlignment(Pos.CENTER);
        Text orderFinText = new Text(getLang().getString("finthanks"));
        orderFinText.getStyleClass().add("ordertoptext");
        Text orderFinText2 = new Text(getLang().getString("fincour"));
        orderFinText2.getStyleClass().add("orderbottomtext");
        Button backToRest = new Button(getLang().getString("finback"));
        backToRest.getStyleClass().add("blackbuttonwide");
        Button logout = new Button(getLang().getString("finlo"));
        logout.getStyleClass().add("whitebuttonwide");
        logout.setOnMouseClicked(event -> {
            JSONLoaded.setUser(null);
            JSONLoaded.setActiveUser(null);
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
        backToRest.setOnMouseClicked(event -> {
            Stage stage = (Stage) backToRest.getScene().getWindow();
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
        orderD.getChildren().addAll(orderFinText,orderFinText2,backToRest,logout);
    }

    public void menuBarF(){
        menubar.getStyleClass().add("menubar");
        menubar.setVisible(false);
        menubar.setSpacing(20);
        Button goBack = new Button("\uD83E\uDC14 Close");
        Pane spacer = new Pane();
        spacer.setPrefHeight(200);
        Button home = new Button("Home");
        Button restaurant = new Button("Restaurants");
        Button settings = new Button("Settings");
        home.getStyleClass().add("whitebuttonmenu");
        restaurant.getStyleClass().add("whitebuttonmenu");
        settings.getStyleClass().add("whitebuttonmenu");
        goBack.getStyleClass().add("backbutton");
        menubar.getChildren().addAll(goBack,spacer,home, restaurant,settings);
        menubtn.setOnMouseClicked(e -> {
            menubar.setVisible(true);
        });
        goBack.setOnMouseClicked(e -> {
            menubar.setVisible(false);
        });
        home.setOnMouseClicked(e -> {
            Stage stage = (Stage) home.getScene().getWindow();
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
