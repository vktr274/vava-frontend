package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        register();
    }

    @FXML
    private VBox mainVBox;

    public void register() {
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
        Text label = new Text("Create Account");
        label.getStyleClass().add("formTitle");

        titlePane.getChildren().add(label);
        titlePane.getStyleClass().add("formTitlePane");

        TextField name = new TextField();
        name.getStyleClass().add("formInput");
        name.setPromptText("name");
        name.setPrefWidth(360);
        name.setMaxWidth(360);

        TextField email = new TextField();
        email.getStyleClass().add("formInput");
        email.setPromptText("email");
        email.setPrefWidth(360);
        email.setMaxWidth(360);

        TextField password = new TextField();
        password.getStyleClass().add("formInput");
        password.setPromptText("password");
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
        phone.setPromptText("phone");
        phone.setPrefWidth(290);
        phone.setMaxWidth(290);

        inputs.setAlignment(Pos.CENTER);
        inputs.setSpacing(10);
        inputs.getChildren().addAll(prefix, phone);

        RadioButton user = new RadioButton("User");
        RadioButton manager = new RadioButton("Restaurant Manager");

        ToggleGroup radioGroup = new ToggleGroup();
        user.setToggleGroup(radioGroup);
        manager.setToggleGroup(radioGroup);

        Button registerButton = new Button("Create");
        registerButton.getStyleClass().add("formButton");

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
}
