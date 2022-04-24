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
import java.util.Locale;
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

    private static ResourceBundle lang;
    private void setLang(ResourceBundle lang){
        RestaurantMenuController.lang = lang;
    }
    private ResourceBundle getLang(){
        return RestaurantMenuController.lang;
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale("sk", "SK"));
        setLang(lngBndl);
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
        Text currentPrice = new Text(getLang().getString("emptybasket"));
        currentPrice.getStyleClass().add("itemname");
        AtomicReference<Double> price = new AtomicReference<>((double) 0);
        int[][] orderById = new int[array.length()][3];
        for(int i=0; i<array.length();i++){
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
                currentPrice.setText(getLang().getString("inb") + price.get() / 100 +"\u20ac");
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
                currentPrice.setText(getLang().getString("inb") + price.get() / 100 +"\u20ac");
                addToCart.setText(amount+"x "+(double) object.getInt("price")/100+"\u20ac");
                if(amount.get() == 0){
                    if(price.get()==0) currentPrice.setText(getLang().getString("emptybasket"));
                    addToCart.setText((double) object.getInt("price")/100+"\u20ac");
                    removeFromCart.setVisible(false);
                }
            });


            Text itemName = new Text(object.getString("name"));
            itemName.getStyleClass().add("itemname");
            Text itemDesc = new Text(object.getString("description"));
            itemDesc.getStyleClass().add("itemdesc");
            texts.setAlignment(Pos.CENTER_LEFT);
            texts.getChildren().addAll(itemName,itemDesc);

            itemMenu.getStyleClass().add("menuitem");
            Text itemPrice = new Text(addToCart.getText());
            itemPrice.getStyleClass().add("itemname");
            if(JSONLoaded.getActiveUser()!=null && JSONLoaded.getIsManaging()){
                itemMenu.getChildren().addAll(spacer1,texts,spacer2,itemPrice,spacer3);
            }
            else{
                itemMenu.getChildren().addAll(spacer1,texts,spacer2,removeFromCart,addToCart,spacer3);
            }
            menu.getChildren().add(itemMenu);
        }

        Pane spacer1 = new Pane();
        spacer1.setPrefHeight(0);
        Pane spacer2 = new Pane();
        spacer2.setPrefHeight(0);
        Pane spacer3 = new Pane();
        VBox.setVgrow(spacer3, Priority.ALWAYS);

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

        Button goback = new Button(getLang().getString("goback"));
        Button reviews = new Button(getLang().getString("reviews"));
        Button checkout = new Button(getLang().getString("check"));
        reviews.getStyleClass().add("whitebuttonwide");
        goback.getStyleClass().add("whitebuttonwide");
        checkout.getStyleClass().add("blackbuttonwide");

        goback.setOnMouseClicked(event -> {
            Stage stage = (Stage) goback.getScene().getWindow();
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
        if(JSONLoaded.getActiveUser()!=null && Objects.equals(JSONLoaded.getActiveUser().role, "admin") || JSONLoaded.getIsManaging()){
            checkout.setText(getLang().getString("mngradd"));
        }
        checkout.setOnMouseClicked(e -> {
            if(JSONLoaded.getActiveUser()!=null && JSONLoaded.getIsManaging()){
                Stage stage = (Stage) checkout.getScene().getWindow();
                Parent root = null;
                try {
                    root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("addItem.fxml")));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                assert root != null;
                Scene scene = new Scene(root, 1280, 720);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
                stage.setScene(scene);
            }
            else{
                if(checkout.getText().equals(getLang().getString("checkptl"))){
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
                    checkout.setText(getLang().getString("checkptl"));
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
                else checkout.setText(getLang().getString("addsmth"));
            }
        });


        restInfo.setSpacing(20);
        restInfo.setAlignment(Pos.TOP_CENTER);
        if(JSONLoaded.getActiveUser()!=null && JSONLoaded.getIsManaging()){
            restInfo.getChildren().addAll(spacer1,rN,addr,ph,spacer3,goback,reviews,checkout,spacer2);
        }
        else{
            restInfo.getChildren().addAll(spacer1,rN,addr,ph,spacer3,currentPrice,goback,reviews,checkout,spacer2);
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
