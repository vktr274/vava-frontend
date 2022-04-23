package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;

public class ReviewAddController implements Initializable {

    @FXML
    private VBox reviewbox;
    @FXML
    private VBox restInfo;

    @FXML
    private Button menubtn;
    @FXML
    private VBox menubar;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private static File imgfile;
    private void setImgfile(File file){
        ReviewAddController.imgfile = file;
    }
    private File getImgfile(){
        return ReviewAddController.imgfile;
    }

    private static ResourceBundle lang;
    private void setLang(ResourceBundle lang){
        ReviewAddController.lang = lang;
    }
    private ResourceBundle getLang(){
        return ReviewAddController.lang;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale("sk", "SK"));
        setLang(lngBndl);
        SetScreen();
    }


    public void SetScreen(){
        reviewbox.setSpacing(25);
        reviewbox.getChildren().clear();
        restInfo.getChildren().clear();
        menuBarF();
        JSONObject restaurantJson = JSONLoaded.getRestaurant();
        System.out.println(restaurantJson.getInt("id"));
        Text restaurantLabel = new Text(restaurantJson.getString("name"));
        restaurantLabel.getStyleClass().add("label");
        Text revLabel = new Text(getLang().getString("revtext"));
        revLabel.getStyleClass().add("label");
        TextArea revText = new TextArea();
        revText.setWrapText(true);
        revText.setPromptText(getLang().getString("revhere"));
        revText.getStyleClass().add("textareareview");
        Slider stars = new Slider(1, 10, 1);
        stars.setOrientation(Orientation.HORIZONTAL);
        stars.setShowTickLabels(true);
        stars.setShowTickMarks(true);
        stars.setSnapToTicks(true);
        stars.setMajorTickUnit(1);
        stars.setMinorTickCount(0);
        reviewbox.getChildren().addAll(restaurantLabel,revLabel,revText,stars);

        Pane spacer1 = new Pane();
        spacer1.setPrefHeight(0);
        Pane spacer2 = new Pane();
        spacer2.setPrefHeight(0);
        Pane spacer3 = new Pane();
        VBox.setVgrow(spacer3, Priority.ALWAYS);
        ImageView rImageView = new ImageView();
        rImageView.setPreserveRatio(true);
        rImageView.setFitWidth(250);

        Button addImg = new Button(getLang().getString("addimg"));
        addImg.getStyleClass().add("whitebuttonwide");
        Button discard = new Button(getLang().getString("discard"));
        discard.getStyleClass().add("whitebuttonwide");
        Button save = new Button(getLang().getString("save"));
        save.getStyleClass().add("blackbuttonwide");

        addImg.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();

            //Set extension filter
            FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
            fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

            //Show open file dialog
            File file = fileChooser.showOpenDialog(null);
            System.out.println(file.toURI());
            System.out.println(Paths.get(file.toURI()));

            if (file != null) {
                Image image = new Image(file.toURI().toString());
                setImgfile(file);
                rImageView.setImage(image);
            }
        });
        discard.setOnMouseClicked(event -> {
            Stage stage = (Stage) discard.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("reviewList.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });
        save.setOnMouseClicked(event -> {
            addReview("http://localhost:8080/reviews?restaurant_id="+restaurantJson.getInt("id"), revText.getText(), (int) stars.getValue());
            Stage stage = (Stage) discard.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("reviewList.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });

        restInfo.setSpacing(20);
        restInfo.setAlignment(Pos.TOP_CENTER);
        restInfo.getChildren().addAll(spacer1,rImageView,spacer3,addImg,discard,save,spacer2);
    }

    public void addReview(String url, String text, int stars){
        JSONObject requestB = new JSONObject();
        requestB.put("score", stars);
        requestB.put("text", text);

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(requestB.toString()))
                    .uri(new URI(url))
                    .setHeader("auth", JSONLoaded.getUser().getString("token")) // add request header
                    .header("Content-Type", "application/json")
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        int reviewId = 0;
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            reviewId = Integer.parseInt(response.body());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/photos?username=jano4&review_id="+reviewId);
        httpPost.setHeader("auth",JSONLoaded.getUser().getString("token"));
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(
                "file", getImgfile(), ContentType.APPLICATION_OCTET_STREAM, "xx.jpg");

        HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);

        try {
            CloseableHttpResponse response2 = client.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
