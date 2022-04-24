package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale(JSONLoaded.getLang(), JSONLoaded.getCountry()));
        setLang(lngBndl);
        register();
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
        RegisterController.lang = lang;
    }
    private ResourceBundle getLang(){
        return RegisterController.lang;
    }

    private boolean successful;

    public void handleRegister(String url, String username, String password, String email, boolean isManager, String countryCode, String number){
        if (Objects.equals(username, "") || Objects.equals(password, "") || Objects.equals(email, "") || Objects.equals(countryCode, "") || Objects.equals(number, "")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText(getLang().getString("fillall"));
            alert.showAndWait();
            return;
        }

        //check if email is in correct format
        if (!email.contains("@") || !email.contains(".")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText(getLang().getString("validemail"));
            alert.showAndWait();
            return;
        }

        //check if country code is in correct format
        if (!countryCode.matches("\\+[0-9]+") || countryCode.length() > 4) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText(getLang().getString("validcountrycode"));
            alert.showAndWait();
            return;
        }

        //check if number is in correct format
        if (!number.matches("[0-9]+") || number.length() != 9) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText(getLang().getString("validphone"));
            alert.showAndWait();
            return;
        }

        JSONObject requestB = new JSONObject();
        requestB.put("username", username);
        requestB.put("password", password);
        requestB.put("email", email);

        if (isManager) {
            requestB.put("role", "manager");
        } else {
            requestB.put("role", "guest");
        }

        requestB.put("phone", new JSONObject().put("country_code", countryCode).put("number", number));
        requestB.put("address", JSONObject.NULL);
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
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 201){ successful = true; }
            else {
                System.out.println(response.statusCode());
                System.out.println(response.body());
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public void register() {
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
        form.setSpacing(15);
        form.setAlignment(Pos.CENTER);
        form.getStyleClass().add("form");

        StackPane titlePane = new StackPane();
        Text label = new Text(getLang().getString("crtacc"));
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
        name.setPromptText(getLang().getString("usrnm"));
        name.setPrefWidth(360);
        name.setMaxWidth(360);

        TextField email = new TextField();
        email.getStyleClass().add("formInput");
        email.setPromptText(getLang().getString("eml"));
        email.setPrefWidth(360);
        email.setMaxWidth(360);

        PasswordField password = new PasswordField();
        password.getStyleClass().add("formInput");
        password.setPromptText(getLang().getString("pass"));
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
        phone.setPromptText(getLang().getString("tel"));
        phone.setPrefWidth(290);
        phone.setMaxWidth(290);

        inputs.setAlignment(Pos.CENTER);
        inputs.setSpacing(10);
        inputs.getChildren().addAll(prefix, phone);

        RadioButton user = new RadioButton(getLang().getString("usr"));
        RadioButton manager = new RadioButton(getLang().getString("restmngr"));

        ToggleGroup radioGroup = new ToggleGroup();
        user.setToggleGroup(radioGroup);
        manager.setToggleGroup(radioGroup);

        user.setSelected(true);

        Button registerButton = new Button(getLang().getString("create"));
        registerButton.getStyleClass().add("formButton");

        registerButton.setOnMouseClicked(e -> {
            String hashedPassword = password.getText();

            //password regex
            if (!hashedPassword.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText(getLang().getString("validpassword"));
                alert.showAndWait();
                return;
            }

            //hash md5
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

            RadioButton rb = (RadioButton) radioGroup.getSelectedToggle();
            boolean isManager = Objects.equals(rb.getText(), "Restaurant Manager");
            handleRegister("http://localhost:8080/users", name.getText(), hashedPassword, email.getText(), isManager, prefix.getText(), phone.getText());
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
                register();
                menubar.setVisible(true);
            }
            else if(JSONLoaded.getLang().equals("en")){
                JSONLoaded.setCountry("SK");
                JSONLoaded.setLang("sk");
                ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale(JSONLoaded.getLang(), JSONLoaded.getCountry()));
                setLang(lngBndl);
                register();
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
        Image image = new Image(imageFile.toURI().toString());
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
        Image image = new Image(imageFile.toURI().toString());
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
        Image image = new Image(imageFile.toURI().toString());
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
        Image image = new Image(imageFile.toURI().toString());
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
