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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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

public class ProfileController implements Initializable {

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
        ProfileController.lang = lang;
    }
    private ResourceBundle getLang(){
        return ProfileController.lang;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale(JSONLoaded.getLang(), JSONLoaded.getCountry()));
        setLang(lngBndl);
        profileScreen();
    }

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public User buildUser(JSONObject response){
        JSONObject JSONAddress = response.getJSONObject("address");
        JSONObject JSONPhone = response.getJSONObject("phone");

        Address address = new Address(JSONAddress.getString("name"), JSONAddress.getString("street"), JSONAddress.getString("city"), JSONAddress.getString("state"), JSONAddress.getString("postcode"), JSONAddress.getString("building_number"));
        Phone phone = new Phone(JSONPhone.getString("number"), JSONPhone.getString("country_code"));
        return new User(response.getInt("id"), response.getString("username"), response.getString("email"), response.getString("role"), response.getBoolean("blocked"), address, phone);
    }

    public void handleSuccess() {
        if(editingUsername){
            usernameLabel.setText(JSONLoaded.getActiveUser().getUsername());
            editUsernameTextField.setText(JSONLoaded.getActiveUser().getUsername());
            editUsernameTextField.setVisible(false);
            editUsernameTextField.setManaged(false);
            usernameLabel.setVisible(true);
            usernameLabel.setManaged(true);
        }
        if(editingEmail){
            emailLabel.setText(JSONLoaded.getActiveUser().getEmail());
            editEmailTextField.setText(JSONLoaded.getActiveUser().getEmail());
            editEmailTextField.setVisible(false);
            editEmailTextField.setManaged(false);
            emailLabel.setVisible(true);
            emailLabel.setManaged(true);
        }
        if(editingAddress){
            name.setText(JSONLoaded.getActiveUser().getAddress().getName());
            street.setText(JSONLoaded.getActiveUser().getAddress().getStreet());
            city.setText(JSONLoaded.getActiveUser().getAddress().getCity());
            state.setText(JSONLoaded.getActiveUser().getAddress().getState());
            postalcode.setText(JSONLoaded.getActiveUser().getAddress().getPostcode());
            building_number.setText(JSONLoaded.getActiveUser().getAddress().getBuilding_number());

            name.setDisable(true);
            street.setDisable(true);
            city.setDisable(true);
            state.setDisable(true);
            postalcode.setDisable(true);
            building_number.setDisable(true);
        }

        if(editingPassword){
            passwordFieldHBox.setVisible(false);
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getLang().getString("success"));
        alert.setHeaderText(getLang().getString("success"));
        alert.setContentText(getLang().getString("changedinfo"));
        alert.showAndWait();

        buttonsHBox.setVisible(false);
    }

    public String handleEdit(User editedUser, String password, String url){
        JSONObject requestB = new JSONObject(editedUser);
        if(password != null){
            requestB.put("password", password);
        }
        System.out.println(requestB);

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofString(requestB.toString()))
                    .uri(new URI(url))
                    .setHeader("auth", JSONLoaded.getUser().getString("token")) // add request header
                    .header("Content-Type", "application/json")
                    .build();
        } catch (URISyntaxException e) {
            return "ERROR";
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                JSONLoaded.setActiveUser(buildUser(new JSONObject(response.body())));
                usernameLabel.setText(JSONLoaded.getActiveUser().getUsername());
                handleSuccess();
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


    @FXML
    private VBox mainVBox;
    private boolean editingAddress = false;
    private boolean editingPassword = false;
    private boolean editingUsername = false;
    private boolean editingEmail = false;


    private Text usernameLabel = new Text();
    private Text emailLabel = new Text();
    private TextField editUsernameTextField = new TextField();
    private TextField editEmailTextField = new TextField();
    private TextField name = new TextField();
    private TextField street = new TextField();
    private TextField city = new TextField();
    private TextField state = new TextField();
    private TextField postalcode = new TextField();
    private TextField building_number = new TextField();
    private HBox buttonsHBox = new HBox();
    private HBox passwordFieldHBox = new HBox();


    public void profileScreen() {
        mainVBox.setSpacing(20);

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

        User user = JSONLoaded.getActiveUser();

        VBox profileVBox = new VBox();
        profileVBox.setAlignment(Pos.CENTER);
        profileVBox.setSpacing(10);
        profileVBox.setPrefWidth(500);

        //user info ocmponents
        File imageFile = new File("src/main/resources/sk/vava/zalospevaci/images/account_circle.png");
        Image image = new Image(imageFile.toURI().toString());
        ImageView userImage = new ImageView(image);

        //username
        HBox usernameHBox = new HBox();
        usernameHBox.setAlignment(Pos.CENTER);
        usernameHBox.setSpacing(10);

        Button editUsernameButton = new Button(getLang().getString("edit"));
        editUsernameButton.getStyleClass().add("profileButton");

        usernameLabel.setText(user.getUsername());
        editUsernameTextField.setText(user.getUsername());
        editUsernameTextField.getStyleClass().add("formInput");
        editUsernameTextField.setPrefWidth(200);

        editUsernameTextField.setVisible(false);
        editUsernameTextField.setManaged(false);

        usernameHBox.getChildren().addAll(usernameLabel, editUsernameTextField, editUsernameButton);

        //email
        HBox emailHBox = new HBox();
        emailHBox.setAlignment(Pos.CENTER);
        emailHBox.setSpacing(10);

        Button editEmailButton = new Button(getLang().getString("edit"));
        editEmailButton.getStyleClass().add("profileButton");

        emailLabel.setText(user.getEmail());

        editEmailTextField.setText(user.getEmail());
        editEmailTextField.getStyleClass().add("formInput");
        editEmailTextField.setPrefWidth(200);

        editEmailTextField.setVisible(false);
        editEmailTextField.setManaged(false);

        emailHBox.getChildren().addAll(emailLabel, editEmailTextField, editEmailButton);

        usernameLabel.getStyleClass().add("formTitle");
        emailLabel.getStyleClass().add("formTitle");

        //address components
        VBox addressesVBox = new VBox();
        addressesVBox.setAlignment(Pos.CENTER_LEFT);
        addressesVBox.setSpacing(20);

        HBox addressTitleHBox = new HBox();
        addressTitleHBox.setAlignment(Pos.CENTER);
        addressTitleHBox.setSpacing(200);
        addressTitleHBox.setPrefWidth(500);

        Text addressTitle = new Text(getLang().getString("addr"));
        addressTitle.getStyleClass().add("formTitle");
        Button editAddressButton = new Button(getLang().getString("editaddr"));
        editAddressButton.getStyleClass().add("profileButton");
        addressTitleHBox.getChildren().addAll(addressTitle, editAddressButton);

        HBox addr1 = new HBox();
        addr1.setAlignment(Pos.CENTER);

        if (user.getAddress() != null) {
            name.setText(user.getAddress().getName());
            street.setText(user.getAddress().getStreet());
            building_number.setText(user.getAddress().getBuilding_number());
            city.setText(user.getAddress().getCity());
            state.setText(user.getAddress().getState());
            postalcode.setText(user.getAddress().getPostcode());
        }

        name.setPromptText(getLang().getString("title"));
        name.setPrefWidth(350);
        name.getStyleClass().add("formInput");

        addr1.getChildren().add(name);

        HBox addr2 = new HBox();
        addr2.setSpacing(10);
        addr2.setAlignment(Pos.CENTER);

        street.setPrefWidth(200);
        building_number.setPrefWidth(140);

        street.setPromptText(getLang().getString("strt"));
        building_number.setPromptText(getLang().getString("buildno"));

        street.getStyleClass().add("formInput");
        building_number.getStyleClass().add("formInput");

        addr2.getChildren().addAll(street, building_number);

        HBox addr3 = new HBox();
        addr3.setSpacing(10);
        addr3.setAlignment(Pos.CENTER);
        city.setPrefWidth(170);
        state.setPrefWidth(170);

        city.setPromptText(getLang().getString("cty"));
        state.setPromptText(getLang().getString("cntry"));

        city.getStyleClass().add("formInput");
        state.getStyleClass().add("formInput");

        addr3.getChildren().addAll(city, state);

        HBox addr4 = new HBox();
        addr4.setAlignment(Pos.CENTER);

        postalcode.setPromptText(getLang().getString("zip"));
        postalcode.getStyleClass().add("formInput");

        addr4.getChildren().add(postalcode);

        addressesVBox.getChildren().addAll(addr1, addr2, addr3, addr4);
        Text passwordLabel = new Text(getLang().getString("cappass"));
        Button changePasswordButton = new Button(getLang().getString("chngpass"));

        passwordLabel.getStyleClass().add("formTitle");
        changePasswordButton.getStyleClass().add("profileButton");

        Pane spacer = new Pane();
        spacer.setPrefHeight(30);

        HBox passwordHBox = new HBox();
        passwordHBox.setAlignment(Pos.CENTER);
        passwordHBox.setSpacing(170);
        passwordHBox.setPrefWidth(500);

        passwordHBox.getChildren().addAll(passwordLabel, changePasswordButton);

        PasswordField password = new PasswordField();
        password.setPromptText(getLang().getString("pass"));
        password.getStyleClass().add("formInput");
        password.setPrefWidth(350);

        passwordFieldHBox.getChildren().add(password);
        passwordFieldHBox.setAlignment(Pos.CENTER);

        passwordFieldHBox.setVisible(false);

        Button discardButton = new Button(getLang().getString("dscard"));
        Button ConfirmButton = new Button(getLang().getString("cnfirm"));

        discardButton.getStyleClass().add("profileButton");
        ConfirmButton.getStyleClass().add("profileButton");

        buttonsHBox.setAlignment(Pos.CENTER);
        buttonsHBox.setSpacing(10);
        buttonsHBox.getChildren().addAll(discardButton, ConfirmButton);

        buttonsHBox.setVisible(false);

        changePasswordButton.setOnMouseClicked(event -> {
            passwordFieldHBox.setVisible(true);
            buttonsHBox.setVisible(true);
            editingPassword = true;
        });

        name.setDisable(true);
        street.setDisable(true);
        building_number.setDisable(true);
        city.setDisable(true);
        state.setDisable(true);
        postalcode.setDisable(true);

        discardButton.setOnMouseClicked(event -> {
            passwordFieldHBox.setVisible(false);
            buttonsHBox.setVisible(false);
            name.setDisable(true);
            street.setDisable(true);
            building_number.setDisable(true);
            city.setDisable(true);
            state.setDisable(true);
            postalcode.setDisable(true);

            if(user.getAddress() != null) {
                name.setText(user.getAddress().getName() != null ? user.getAddress().getName() : "");
                street.setText(user.getAddress().getStreet() != null ? user.getAddress().getStreet() : "");
                building_number.setText(user.getAddress().getBuilding_number() != null ? user.getAddress().getBuilding_number() : "");
                city.setText(user.getAddress().getCity() != null ? user.getAddress().getCity() : "");
                state.setText(user.getAddress().getState() != null ? user.getAddress().getState() : "");
                postalcode.setText(user.getAddress().getPostcode() != null ? user.getAddress().getPostcode() : "");
            }

            if(editingUsername) {
                editUsernameTextField.setVisible(false);
                editUsernameTextField.setManaged(false);
                usernameLabel.setVisible(true);
                usernameLabel.setManaged(true);
                editUsernameTextField.setText(user.getUsername());
                editingUsername = false;
            }

            if(editingEmail) {
                editEmailTextField.setVisible(false);
                editEmailTextField.setManaged(false);
                emailLabel.setVisible(true);
                emailLabel.setManaged(true);
                editEmailTextField.setText(user.getEmail());
                editingEmail = false;
            }

            if(editingPassword) {
                passwordFieldHBox.setVisible(false);
                buttonsHBox.setVisible(false);
                password.setText("");
                editingPassword = false;
            }
        });

        if (user.getAddress() != null) {
            name.setText(user.getAddress().getName());
            street.setText(user.getAddress().getStreet());
            building_number.setText(user.getAddress().getBuilding_number());
            city.setText(user.getAddress().getCity());
            state.setText(user.getAddress().getState());
            postalcode.setText(user.getAddress().getPostcode());
        }

        editAddressButton.setOnMouseClicked(event -> {
            buttonsHBox.setVisible(true);
            name.setDisable(false);
            street.setDisable(false);
            building_number.setDisable(false);
            city.setDisable(false);
            state.setDisable(false);
            postalcode.setDisable(false);
            editingAddress = true;
        });

        //edit username
        editUsernameButton.setOnAction(event -> {
            if(!editingUsername){
                editingUsername = true;
                usernameLabel.setVisible(false);
                usernameLabel.setManaged(false);

                editUsernameTextField.setVisible(true);
                editUsernameTextField.setManaged(true);
                buttonsHBox.setVisible(true);
            }
        });

        //edit email
        editEmailButton.setOnAction(event -> {
            if(!editingEmail){
                editingEmail = true;
                emailLabel.setVisible(false);
                emailLabel.setManaged(false);

                editEmailTextField.setVisible(true);
                editEmailTextField.setManaged(true);
                buttonsHBox.setVisible(true);
            }
        });

        ConfirmButton.setOnMouseClicked(event -> {
            String newPassword = null;

            User editedUser = new User(JSONLoaded.getActiveUser().getId(), null, null, null, JSONLoaded.getActiveUser().isBlocked(), null, null);
            if (editingAddress) {
                Address editedAddress = new Address(name.getText(), street.getText(), city.getText(), state.getText(), postalcode.getText(), building_number.getText());
                editedUser.setAddress(editedAddress);
            }

            if(editingUsername){
                editedUser.setUsername(editUsernameTextField.getText());
            }

            if(editingEmail){
                if(editEmailTextField.getText().matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")){
                    editedUser.setEmail(editEmailTextField.getText());
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error");
                    alert.setContentText(getLang().getString("validemail"));
                    alert.showAndWait();
                    return;
                }
            }

            if(editingPassword){
                newPassword = password.getText();

                //password regex
                if (!newPassword.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error");
                    alert.setContentText(getLang().getString("validpassword"));
                    alert.showAndWait();
                    return;
                }

                //hash to md5
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(newPassword.getBytes());
                    byte[] digest = md.digest();
                    StringBuffer sb = new StringBuffer();
                    for (byte b : digest) {
                        sb.append(String.format("%02x", b & 0xff));
                    }
                    newPassword = sb.toString();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            if(editingUsername || editingAddress || editingEmail || editingPassword){
                handleEdit(editedUser, newPassword, "http://localhost:8080/users");
            }
        });

        profileVBox.getChildren().addAll(userImage, usernameHBox, emailHBox, addressTitleHBox, addressesVBox, spacer, passwordHBox, passwordFieldHBox, buttonsHBox);
        mainVBox.getChildren().addAll(profileVBox);
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
                profileScreen();
                menubar.setVisible(true);
            }
            else if(JSONLoaded.getLang().equals("en")){
                JSONLoaded.setCountry("SK");
                JSONLoaded.setLang("sk");
                ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale(JSONLoaded.getLang(), JSONLoaded.getCountry()));
                setLang(lngBndl);
                profileScreen();
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
