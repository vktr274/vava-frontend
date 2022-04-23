package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class RestaurantMenuController implements Initializable {

    @FXML
    private VBox menu;
    @FXML
    private VBox restInfo;

    @FXML
    private Button menubtn;
    @FXML
    private VBox menubar;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SetScreen();
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

    public void SetScreen(){
        menu.setSpacing(25);
        menuBarF();
        JSONObject restaurantJson = JSONLoaded.getRestaurant();
        System.out.println(restaurantJson.getInt("id"));
        JSONArray array = new JSONArray(getJSON("http://localhost:8080/items/"+restaurantJson.getInt("id")));
        Text restaurantLabel = new Text("Menu");
        restaurantLabel.getStyleClass().add("label");
        menu.getChildren().add(restaurantLabel);
        Text currentPrice = new Text("Empty basket");
        currentPrice.getStyleClass().add("itemname");
        AtomicReference<Double> price = new AtomicReference<>((double) 0);
        int[][] orderById = new int[array.length()][3];
        for(int i=0; i<array.length();i++){
            Image image = new Image("https://i.imgur.com/Tf3j0rU.jpg");
            Pane spacer1 = new Pane();
            Pane spacer2 = new Pane();
            Pane spacer3 = new Pane();
            HBox itemMenu = new HBox(25);
            VBox texts = new VBox(5);
            Button addToCart = new Button();
            Button removeFromCart = new Button("-");
            JSONObject object = array.getJSONObject(i);
            orderById[i][0] = object.getInt("id");
            orderById[i][2] = object.getInt("price");
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            spacer1.setPrefWidth(0);
            spacer3.setPrefWidth(0);
            AtomicInteger amount = new AtomicInteger();
            addToCart.setText((double) object.getInt("price")/100+"\u20ac");
            addToCart.getStyleClass().add("whitebutton");
            int finalI = i;
            addToCart.setOnMouseClicked(e ->{
                amount.addAndGet(1);
                orderById[finalI][1] = amount.get();
                JSONLoaded.setOrder(orderById);
                price.updateAndGet(v -> v + (double) object.getInt("price"));
                currentPrice.setText("In basket: " + price.get() / 100 +"\u20ac");
                addToCart.setText(amount+"x "+(double) object.getInt("price")/100+"\u20ac");
                removeFromCart.setVisible(true);
            });

            removeFromCart.getStyleClass().add("circlebutton");
            removeFromCart.setVisible(false);
            removeFromCart.setOnMouseClicked(e ->{
                amount.addAndGet(-1);
                orderById[finalI][1] = amount.get();
                JSONLoaded.setOrder(orderById);
                price.updateAndGet(v -> v - (double) object.getInt("price"));
                currentPrice.setText("In basket: " + price.get() / 100 +"\u20ac");
                addToCart.setText(amount+"x "+(double) object.getInt("price")/100+"\u20ac");
                if(amount.get() == 0){
                    if(price.get()==0) currentPrice.setText("Empty basket");
                    addToCart.setText((double) object.getInt("price")/100+"\u20ac");
                    removeFromCart.setVisible(false);
                }
            });

            ImageView imageView = new ImageView();
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(90);

            Text itemName = new Text(object.getString("name"));
            itemName.getStyleClass().add("itemname");
            Text itemDesc = new Text(object.getString("description"));
            itemDesc.getStyleClass().add("itemdesc");
            texts.setAlignment(Pos.CENTER_LEFT);
            texts.getChildren().addAll(itemName,itemDesc);

            itemMenu.getStyleClass().add("menuitem");
            itemMenu.getChildren().addAll(spacer1,imageView,texts,spacer2,removeFromCart,addToCart,spacer3);
            menu.getChildren().add(itemMenu);
        }

        Pane spacer1 = new Pane();
        spacer1.setPrefHeight(0);
        Pane spacer2 = new Pane();
        spacer2.setPrefHeight(0);
        Pane spacer3 = new Pane();
        VBox.setVgrow(spacer3, Priority.ALWAYS);
        Image restaurantImage = new Image("https://i.imgur.com/Tf3j0rU.jpg");
        ImageView rImageView = new ImageView();
        rImageView.setImage(restaurantImage);
        rImageView.setPreserveRatio(true);
        rImageView.setFitWidth(250);

        String restaurantName = restaurantJson.getString("name");
        JSONObject address = restaurantJson.getJSONObject("address");
        JSONObject phone = restaurantJson.getJSONObject("phone");
        String phoneNumber = phone.getString("country_code") + phone.getString("number");
        //String x = address.getString("street");
        String fullAddress = address.getString("name") + "\n"
                + address.getString("building_number") + "\n" + address.getString("city")
                + "\n" +  address.getString("state") + " " + address.getString("postcode");
        Text rN = new Text(restaurantName);
        rN.getStyleClass().add("itemnamebar");
        Text addr = new Text(fullAddress);
        addr.getStyleClass().add("itemnametext");
        Text ph = new Text(phoneNumber);
        ph.getStyleClass().add("itemnamephone");

        Button reviews = new Button("Reviews");
        Button checkout = new Button("Checkout");
        reviews.getStyleClass().add("whitebuttonwide");
        checkout.getStyleClass().add("blackbuttonwide");


        reviews.setOnMouseClicked(e -> {
                Stage stage = (Stage) reviews.getScene().getWindow();
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

        checkout.setOnMouseClicked(e -> {
            if(checkout.getText().equals("Press to login")){
                Stage stage = (Stage) checkout.getScene().getWindow();
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
            if(JSONLoaded.getActiveUser()==null){
                checkout.setText("Press to login");
            }
            else if(price.get()>0){
                Stage stage = (Stage) checkout.getScene().getWindow();
                Parent root = null;
                try {
                    root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("orderSummary.fxml")));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                assert root != null;
                Scene scene = new Scene(root, 1280, 720);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
                stage.setScene(scene);
            }
            else checkout.setText("Add something");
        });


        restInfo.setSpacing(20);
        restInfo.setAlignment(Pos.TOP_CENTER);
        restInfo.getChildren().addAll(spacer1,rImageView,rN,addr,ph,spacer3,currentPrice,reviews,checkout,spacer2);

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
