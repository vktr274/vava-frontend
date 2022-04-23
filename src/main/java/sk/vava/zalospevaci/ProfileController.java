package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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

    public String handleEdit(User editedUser, String url){
        JSONObject requestB = new JSONObject(editedUser);

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
                JSONLoaded.setUser(new JSONObject(response.body()));
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

    public void profileScreen() {
        mainVBox.setSpacing(20);

        User user = JSONLoaded.getActiveUser();

        VBox profileVBox = new VBox();
        profileVBox.setAlignment(Pos.CENTER);
        profileVBox.setSpacing(10);
        profileVBox.setPrefWidth(500);

        File imageFile = new File("src/main/resources/sk/vava/zalospevaci/images/account_circle.png");
        Image image = new Image(imageFile.toURI().toString());
        ImageView userImage = new ImageView(image);

        Text usernameLabel = new Text(user.getUsername());
        Text emailLabel = new Text(user.getEmail());

        usernameLabel.getStyleClass().add("formTitle");
        emailLabel.getStyleClass().add("formTitle");

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
        TextField name = new TextField();
        name.setPromptText("Name");
        name.setPrefWidth(350);
        name.getStyleClass().add("formInput");

        addr1.getChildren().add(name);

        HBox addr2 = new HBox();
        addr2.setSpacing(10);
        addr2.setAlignment(Pos.CENTER);
        TextField street = new TextField();
        TextField building_number = new TextField();
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
        TextField city = new TextField();
        TextField state = new TextField();
        city.setPrefWidth(170);
        state.setPrefWidth(170);

        city.setPromptText("City");
        state.setPromptText("State");

        city.getStyleClass().add("formInput");
        state.getStyleClass().add("formInput");

        addr3.getChildren().addAll(city, state);

        HBox addr4 = new HBox();
        addr4.setAlignment(Pos.CENTER);
        TextField postalcode = new TextField();

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

        HBox passwordFieldHBox = new HBox();
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

        HBox buttonsHBox = new HBox();
        buttonsHBox.setAlignment(Pos.CENTER);
        buttonsHBox.setSpacing(10);
        buttonsHBox.getChildren().addAll(discardButton, ConfirmButton);

        buttonsHBox.setVisible(false);

        changePasswordButton.setOnMouseClicked(event -> {
            passwordFieldHBox.setVisible(true);
            buttonsHBox.setVisible(true);
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

            name.setText("");
            street.setText("");
            building_number.setText("");
            city.setText("");
            state.setText("");
            postalcode.setText("");
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

        ConfirmButton.setOnMouseClicked(event -> {
            User editedUser = new User(JSONLoaded.getActiveUser().getId(), null, null, null, JSONLoaded.getActiveUser().isBlocked(), null, null);
            if (editingAddress) {
                Address editedAddress = new Address(JSONLoaded.getActiveUser().getAddress().getId(), name.getText(), street.getText(), city.getText(), state.getText(), postalcode.getText(), building_number.getText());
                editedUser.setAddress(editedAddress);
                handleEdit(editedUser, "http://localhost:8080/users");
            }
        });

        profileVBox.getChildren().addAll(userImage, usernameLabel, emailLabel, addressTitleHBox, addressesVBox, spacer, passwordHBox, passwordFieldHBox, buttonsHBox);
        mainVBox.getChildren().addAll(profileVBox);
    }
}
