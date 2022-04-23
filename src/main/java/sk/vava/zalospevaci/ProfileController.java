package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
        alert.setTitle("Success");
        alert.setHeaderText("Account information changed");
        alert.setContentText("Your account information has been changed successfully!");
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

        Button editUsernameButton = new Button("Edit");
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

        Button editEmailButton = new Button("Edit");
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

        Text addressTitle = new Text("Address");
        addressTitle.getStyleClass().add("formTitle");
        Button editAddressButton = new Button("Edit address");
        editAddressButton.getStyleClass().add("profileButton");
        addressTitleHBox.getChildren().addAll(addressTitle, editAddressButton);

        HBox addr1 = new HBox();
        addr1.setAlignment(Pos.CENTER);
        name.setText(user.getAddress().getName());
        name.setPromptText("Name");
        name.setPrefWidth(350);
        name.getStyleClass().add("formInput");

        addr1.getChildren().add(name);

        HBox addr2 = new HBox();
        addr2.setSpacing(10);
        addr2.setAlignment(Pos.CENTER);

        street.setText(user.getAddress().getStreet());
        building_number.setText(user.getAddress().getBuilding_number());

        street.setPrefWidth(200);
        building_number.setPrefWidth(140);

        street.setPromptText("Street");
        building_number.setPromptText("Build. no.");

        street.getStyleClass().add("formInput");
        building_number.getStyleClass().add("formInput");

        addr2.getChildren().addAll(street, building_number);

        HBox addr3 = new HBox();
        addr3.setSpacing(10);
        addr3.setAlignment(Pos.CENTER);
        city.setPrefWidth(170);
        state.setPrefWidth(170);

        city.setText(user.getAddress().getCity());
        state.setText(user.getAddress().getState());

        city.setPromptText("City");
        state.setPromptText("State");

        city.getStyleClass().add("formInput");
        state.getStyleClass().add("formInput");

        addr3.getChildren().addAll(city, state);

        HBox addr4 = new HBox();
        addr4.setAlignment(Pos.CENTER);

        postalcode.setText(user.getAddress().getPostcode());
        postalcode.setPromptText("Postal code");
        postalcode.getStyleClass().add("formInput");

        addr4.getChildren().add(postalcode);

        addressesVBox.getChildren().addAll(addr1, addr2, addr3, addr4);
        Text passwordLabel = new Text("Password");
        Button changePasswordButton = new Button("Change password");

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
        password.setPromptText("Password");
        password.getStyleClass().add("formInput");
        password.setPrefWidth(350);

        passwordFieldHBox.getChildren().add(password);
        passwordFieldHBox.setAlignment(Pos.CENTER);

        passwordFieldHBox.setVisible(false);

        Button discardButton = new Button("Discard");
        Button ConfirmButton = new Button("Confirm");

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

            name.setText(user.getAddress().getName() != null ? user.getAddress().getName() : "");
            street.setText(user.getAddress().getStreet() != null ? user.getAddress().getStreet() : "");
            building_number.setText(user.getAddress().getBuilding_number() != null ? user.getAddress().getBuilding_number() : "");
            city.setText(user.getAddress().getCity() != null ? user.getAddress().getCity() : "");
            state.setText(user.getAddress().getState() != null ? user.getAddress().getState() : "");
            postalcode.setText(user.getAddress().getPostcode() != null ? user.getAddress().getPostcode() : "");

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
                    alert.setHeaderText("Invalid email");
                    alert.setContentText("Please enter a valid email");
                    alert.showAndWait();
                    return;
                }
            }

            if(editingPassword){
                newPassword = password.getText();
            }

            if(editingUsername || editingAddress || editingEmail || editingPassword){
                handleEdit(editedUser, newPassword, "http://localhost:8080/users");
            }
        });

        profileVBox.getChildren().addAll(userImage, usernameHBox, emailHBox, addressTitleHBox, addressesVBox, spacer, passwordHBox, passwordFieldHBox, buttonsHBox);
        mainVBox.getChildren().addAll(profileVBox);
    }
}
