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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class LoginController implements Initializable  {
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale(JSONLoaded.getLang(), JSONLoaded.getCountry()));
        setLang(lngBndl);
        loginScreen();
    }

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();


    @FXML
    private VBox mainVBox;

    @FXML
    private Button menubtn;
    @FXML
    private Button userbtn;
    @FXML
    private VBox menubar;
    @FXML
    private VBox userBar;

    private static ResourceBundle lang;
    private void setLang(ResourceBundle lang){
        LoginController.lang = lang;
    }
    private ResourceBundle getLang(){
        return LoginController.lang;
    }

    public void handleLogin(String url, String username, String password){
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
            System.out.println("Error: " + e.getMessage());
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                JSONObject user = new JSONObject(response.body()).put("name", username);
                JSONLoaded.setUser(user);
            }
            else{
                System.out.println(response.statusCode());
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void loginScreen(){
        VBox container = new VBox();
        VBox form = new VBox();

        if(JSONLoaded.getActiveUser() != null){
            if(Objects.equals(JSONLoaded.getActiveUser().role, "manager")) {
                userBarManagerF();
            }
            if(Objects.equals(JSONLoaded.getActiveUser().role, "guest")) {
                userBarUserF();
            }
            if(Objects.equals(JSONLoaded.getActiveUser().role, "admin")) {
                userBarAdminF();
            }
        }
        else{
            guestBarF();
        }

        menuBarF();

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
        username.setPromptText(getLang().getString("usrnm")+"...");
        username.setPrefWidth(360);
        username.setMaxWidth(360);

        PasswordField password = new PasswordField();
        password.getStyleClass().add("formInput");
        password.setPromptText(getLang().getString("pass")+"...");
        password.setPrefWidth(360);
        password.setMaxWidth(360);

        Button loginButton = new Button("Log In");
        loginButton.getStyleClass().add("formButton");

        loginButton.setOnMouseClicked(e -> {
            String hashedPassword = password.getText();

            //md5 hash
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(hashedPassword.getBytes());
                byte[] digest = md.digest();
                StringBuffer sb = new StringBuffer();
                for (byte b : digest) {
                    sb.append(String.format("%02x", b & 0xff));
                }
                hashedPassword = sb.toString();
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            }

            System.out.println("hash: " + hashedPassword);

            handleLogin("http://localhost:8080/token", username.getText(), hashedPassword);
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
                alert.setHeaderText("Error");
                alert.setContentText(getLang().getString("invalidcred"));
                alert.showAndWait();
            }
        });

        Text or = new Text(getLang().getString("or?"));
        Button createAccount = new Button(getLang().getString("crtacc"));
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

    public void menuBarF(){
        menubar.getChildren().clear();
        menubar.getStyleClass().add("menubar");
        menubar.setVisible(false);
        menubar.setSpacing(20);
        Button goBack = new Button("\uD83E\uDC14"+getLang().getString("cls"));
        Pane spacer = new Pane();
        spacer.setPrefHeight(200);
        Button home = new Button(getLang().getString("home"));
        Button restaurant = new Button(getLang().getString("restaurants"));
        Button settings = new Button(getLang().getString("lng"));
        home.getStyleClass().add("whitebuttonmenu");
        restaurant.getStyleClass().add("whitebuttonmenu");
        settings.getStyleClass().add("whitebuttonmenu");
        if(JSONLoaded.getLang().equals("sk")){
            settings.setText(getLang().getString("lng")+" - SK");
        }
        if(JSONLoaded.getLang().equals("en")){
            settings.setText(getLang().getString("lng")+" - EN");
        }
        settings.setOnMouseClicked(event -> {
            if(JSONLoaded.getLang().equals("sk")){
                JSONLoaded.setCountry("EN");
                JSONLoaded.setLang("en");
                ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale(JSONLoaded.getLang(), JSONLoaded.getCountry()));
                setLang(lngBndl);
                loginScreen();
                menubar.setVisible(true);
            }
            else if(JSONLoaded.getLang().equals("en")){
                JSONLoaded.setCountry("SK");
                JSONLoaded.setLang("sk");
                ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale(JSONLoaded.getLang(), JSONLoaded.getCountry()));
                setLang(lngBndl);
                loginScreen();
                menubar.setVisible(true);
            }
        });
        goBack.getStyleClass().add("backbutton");
        menubar.getChildren().addAll(goBack,spacer,home, restaurant,settings);
        menubtn.setOnMouseClicked(e -> menubar.setVisible(true));
        goBack.setOnMouseClicked(e -> menubar.setVisible(false));
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

    public void guestBarF() {
        userBar.getChildren().clear();
        userBar.getStyleClass().add("menubar");
        userBar.setVisible(false);
        userBar.setSpacing(20);
        Button goBack = new Button("\uD83E\uDC14"+getLang().getString("cls"));
        Pane spacer = new Pane();
        Pane spacer2 = new Pane();
        spacer.setPrefHeight(40);
        spacer2.setPrefHeight(30);

        File imageFile = new File("src/main/resources/sk/vava/zalospevaci/images/account_circle.png");
        javafx.scene.image.Image image = new javafx.scene.image.Image(imageFile.toURI().toString());
        ImageView userImage = new ImageView(image);

        Text userName = new Text(getLang().getString("guestt"));

        userImage.setFitHeight(130);
        userImage.setFitWidth(130);
        userImage.setPreserveRatio(true);

        Button login = new Button(getLang().getString("lgn"));
        Button register = new Button(getLang().getString("rgs"));

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
        userBar.getChildren().addAll(goBack, spacer, userImage, userName,spacer2, login,register);
        userbtn.setOnMouseClicked(e -> userBar.setVisible(true));
        goBack.setOnMouseClicked(e -> userBar.setVisible(false));
    }

    public void userBarManagerF() {
        userBar.getChildren().clear();
        userBar.getStyleClass().add("menubar");
        userBar.setVisible(false);
        userBar.setSpacing(20);
        Button goBack = new Button("\uD83E\uDC14"+getLang().getString("cls"));
        Button accountSettings = new Button(getLang().getString("accset"));
        Button manageRestaurant = new Button(getLang().getString("mngres"));
        Button logout = new Button(getLang().getString("lgo"));

        System.out.println(JSONLoaded.getActiveUser().username);
        System.out.println(JSONLoaded.getActiveUser().role);

        Pane spacer = new Pane();
        Pane spacer2 = new Pane();
        spacer.setPrefHeight(20);
        spacer2.setPrefHeight(15);

        File imageFile = new File("src/main/resources/sk/vava/zalospevaci/images/account_circle.png");
        javafx.scene.image.Image image = new javafx.scene.image.Image(imageFile.toURI().toString());
        ImageView userImage = new ImageView(image);

        Text userName = new Text(JSONLoaded.getActiveUser().username);

        userImage.setFitHeight(130);
        userImage.setFitWidth(130);
        userImage.setPreserveRatio(true);

        accountSettings.setOnMouseClicked(e -> {
            Stage stage = (Stage) accountSettings.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("profile.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        logout.setOnMouseClicked(e -> {
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

        manageRestaurant.setOnMouseClicked(e -> {
            Stage stage = (Stage) manageRestaurant.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("createRestaurant.fxml")));
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        accountSettings.getStyleClass().add("whitebuttonmenu");
        manageRestaurant.getStyleClass().add("whitebuttonmenu");
        logout.getStyleClass().add("whitebuttonmenu");

        goBack.getStyleClass().add("backbutton");
        userBar.getChildren().addAll(goBack,spacer,userImage,userName,spacer2,accountSettings, manageRestaurant, logout);
        userbtn.setOnMouseClicked(e -> userBar.setVisible(true));
        goBack.setOnMouseClicked(e -> userBar.setVisible(false));
    }

    public void userBarAdminF() {
        userBar.getChildren().clear();
        userBar.getStyleClass().add("menubar");
        userBar.setVisible(false);
        userBar.setSpacing(20);
        Button goBack = new Button("\uD83E\uDC14"+getLang().getString("cls"));
        Button accountSettings = new Button(getLang().getString("accset"));
        Button manageRestaurant = new Button(getLang().getString("mngresall"));
        Button manageUsers = new Button(getLang().getString("mngusr"));
        Button manageOrders = new Button(getLang().getString("mngord"));

        Button logout = new Button(getLang().getString("lgo"));

        System.out.println(JSONLoaded.getActiveUser().username);
        System.out.println(JSONLoaded.getActiveUser().role);

        Pane spacer = new Pane();
        Pane spacer2 = new Pane();
        spacer.setPrefHeight(20);
        spacer2.setPrefHeight(15);

        File imageFile = new File("src/main/resources/sk/vava/zalospevaci/images/account_circle.png");
        javafx.scene.image.Image image = new javafx.scene.image.Image(imageFile.toURI().toString());
        ImageView userImage = new ImageView(image);

        Text userName = new Text(JSONLoaded.getActiveUser().username);

        userImage.setFitHeight(130);
        userImage.setFitWidth(130);
        userImage.setPreserveRatio(true);

        accountSettings.setOnMouseClicked(e -> {
            Stage stage = (Stage) accountSettings.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("profile.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        logout.setOnMouseClicked(e -> {
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

        manageOrders.setOnMouseClicked(e -> {
            Stage stage = (Stage) manageOrders.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ordersList.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        manageUsers.setOnMouseClicked(e -> {
            Stage stage = (Stage) manageUsers.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userList.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        manageRestaurant.setOnMouseClicked(e -> {
            Stage stage = (Stage) manageRestaurant.getScene().getWindow();
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

        accountSettings.getStyleClass().add("whitebuttonmenu");
        manageRestaurant.getStyleClass().add("whitebuttonmenu");
        manageUsers.getStyleClass().add("whitebuttonmenu");
        logout.getStyleClass().add("whitebuttonmenu");
        manageOrders.getStyleClass().add("whitebuttonmenu");

        goBack.getStyleClass().add("backbutton");
        userBar.getChildren().addAll(goBack,spacer,userImage,userName,spacer2,accountSettings, manageRestaurant, manageUsers, manageOrders, logout);
        userbtn.setOnMouseClicked(e -> userBar.setVisible(true));
        goBack.setOnMouseClicked(e -> userBar.setVisible(false));
    }

    public void userBarUserF() {
        userBar.getChildren().clear();
        userBar.getStyleClass().add("menubar");
        userBar.setVisible(false);
        userBar.setSpacing(20);
        Button goBack = new Button("\uD83E\uDC14"+getLang().getString("cls"));
        Button accountSettings = new Button(getLang().getString("accset"));
        Button myOrders = new Button(getLang().getString("myord"));

        Button logout = new Button(getLang().getString("lgo"));

        System.out.println(JSONLoaded.getActiveUser().username);
        System.out.println(JSONLoaded.getActiveUser().role);

        Pane spacer = new Pane();
        Pane spacer2 = new Pane();
        spacer.setPrefHeight(20);
        spacer2.setPrefHeight(15);

        File imageFile = new File("src/main/resources/sk/vava/zalospevaci/images/account_circle.png");
        javafx.scene.image.Image image = new Image(imageFile.toURI().toString());
        ImageView userImage = new ImageView(image);

        Text userName = new Text(JSONLoaded.getActiveUser().username);

        userImage.setFitHeight(130);
        userImage.setFitWidth(130);
        userImage.setPreserveRatio(true);

        accountSettings.setOnMouseClicked(e -> {
            Stage stage = (Stage) accountSettings.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("profile.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        logout.setOnMouseClicked(e -> {
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

        myOrders.setOnMouseClicked(e -> {
            Stage stage = (Stage) myOrders.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ordersList.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        myOrders.setOnMouseClicked(e -> {
            Stage stage = (Stage) myOrders.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ordersList.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        accountSettings.getStyleClass().add("whitebuttonmenu");
        logout.getStyleClass().add("whitebuttonmenu");
        myOrders.getStyleClass().add("whitebuttonmenu");

        goBack.getStyleClass().add("backbutton");
        userBar.getChildren().addAll(goBack,spacer,userImage,userName,spacer2,accountSettings, myOrders, logout);
        userbtn.setOnMouseClicked(e -> userBar.setVisible(true));
        goBack.setOnMouseClicked(e -> userBar.setVisible(false));
    }
}
