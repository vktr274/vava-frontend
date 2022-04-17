package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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

public class OrderSummaryController implements Initializable {

    @FXML
    private VBox order;

    @FXML
    private VBox subtotal;

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
        order.setSpacing(25);
        menuBarF();
        JSONObject restaurantJson = JSONLoaded.getRestaurant();
        JSONArray array = new JSONArray(getJSON("http://localhost:8080/items/"+restaurantJson.getInt("id")));

        int[][] orderArr = JSONLoaded.getOrder();
        AtomicReference<Double> price = new AtomicReference<>((double) 0);


        Text currentPrice = new Text("Please wait");
        Text addrLabel = new Text("Set Address");
        TextField address = new TextField();
        Text noteLabel = new Text("Optional Note");
        TextField note = new TextField();
        Text summLabel = new Text("Your Order Summary");
        Text subTotalLabel = new Text("Sub total");
        Text vatInfo = new Text("Including VAT");

        currentPrice.getStyleClass().add("label");
        addrLabel.getStyleClass().add("label");
        address.getStyleClass().add("textfieldorder");
        noteLabel.getStyleClass().add("label");
        note.getStyleClass().add("textfieldorder");
        summLabel.getStyleClass().add("label");
        subTotalLabel.getStyleClass().add("itemname");
        vatInfo.getStyleClass().add("itemnamebar");

        order.getChildren().addAll(addrLabel,address,noteLabel,note,summLabel);

        for(int i=0; i<array.length();i++){
            Image image = new Image("https://i.imgur.com/Tf3j0rU.jpg");
            Pane spacer1 = new Pane();
            Pane spacer2 = new Pane();
            Pane spacer3 = new Pane();
            HBox item = new HBox(25);
            JSONObject object = array.getJSONObject(i);

            HBox.setHgrow(spacer2, Priority.ALWAYS);
            spacer1.setPrefWidth(0);
            spacer3.setPrefWidth(0);

            ImageView imageView = new ImageView();
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(90);

            Button removeFromCart = new Button("-");
            removeFromCart.getStyleClass().add("circlebutton");

            VBox texts = new VBox(5);
            Text itemName = new Text(object.getString("name"));
            itemName.getStyleClass().add("itemname");
            Text itemDesc = new Text(object.getString("description"));
            itemDesc.getStyleClass().add("itemdesc");
            texts.setAlignment(Pos.CENTER_LEFT);
            texts.getChildren().addAll(itemName,itemDesc);
            Double priceDouble = (double) (object.getInt("price"));
            Text itemPrice = new Text();
            itemPrice.getStyleClass().add("itemname");


            item.getStyleClass().add("menuitem");
            for (int j = 0; j < orderArr.length; j++) {
                if(orderArr[j][0] == object.getInt("id")){
                    AtomicInteger amount = new AtomicInteger(orderArr[j][1]);
                    itemPrice.setText(amount.get()+"x "+priceDouble/100+"\u20ac");
                    int finalJ = j;
                    price.updateAndGet(v -> v + orderArr[finalJ][1] * object.getInt("price"));
                    currentPrice.setText(price.get()/100+"\u20ac");
                    System.out.println(price.get()/100);
                    removeFromCart.setOnMouseClicked(e ->{
                        amount.addAndGet(-1);
                        orderArr[finalJ][1] = amount.get();
                        itemPrice.setText(amount.get()+"x "+priceDouble/100+"\u20ac");
                        price.updateAndGet(v -> v - object.getInt("price"));
                        currentPrice.setText(price.get()/100+"\u20ac");
                        System.out.println(price.get()/100);
                        JSONLoaded.setOrder(orderArr);
                        if(amount.get() == 0){
                            removeFromCart.setVisible(false);
                            item.setVisible(false);
                            item.setManaged(false);
                        }
                        if(price.get() == 0){
                            Stage stage = (Stage) removeFromCart.getScene().getWindow();
                            Parent root = null;
                            try {
                                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("restaurantMenu.fxml")));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            assert root != null;
                            Scene scene = new Scene(root, 1280, 720);
                            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
                            stage.setScene(scene);
                        }
                    });
                    item.getChildren().addAll(spacer1,imageView,texts,spacer2,itemPrice,removeFromCart,spacer3);
                    if(orderArr[j][1]==0){
                        item.setVisible(false);
                        item.setManaged(false);
                    }
                }
            }
            order.getChildren().add(item);
        }

        Pane spacerSubTop = new Pane();
        spacerSubTop.setPrefHeight(0);
        Pane spacerSubBot = new Pane();
        spacerSubBot.setPrefHeight(0);
        Pane spacerMiddle = new Pane();
        VBox.setVgrow(spacerMiddle, Priority.ALWAYS);

        Button payCash = new Button("Pay with Cash");
        Button payCard = new Button("Pay with Card");
        payCash.getStyleClass().add("whitebuttonwide");
        payCard.getStyleClass().add("blackbuttonwide");

        subtotal.setSpacing(20);
        subtotal.setAlignment(Pos.TOP_CENTER);
        subtotal.getChildren().addAll(spacerSubTop,subTotalLabel,currentPrice,vatInfo,spacerMiddle,payCash,payCard,spacerSubBot);
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