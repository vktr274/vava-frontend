package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
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
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;


public class RestaurantMenuController implements Initializable {

    @FXML
    private VBox menu;
    @FXML
    private VBox restInfo;

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
        JSONObject restaurantJson = JSONLoaded.getRestaurant();
        System.out.println(restaurantJson.getInt("id"));
        JSONArray array = new JSONArray(getJSON("http://localhost:8080/items/"+restaurantJson.getInt("id")));
        Text restaurantLabel = new Text("Menu");
        restaurantLabel.getStyleClass().add("label");
        menu.getChildren().add(restaurantLabel);

        int[][] orderById = new int[array.length()][2];
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
                addToCart.setText(amount+"x "+(double) object.getInt("price")/100+"\u20ac");
                removeFromCart.setVisible(true);
            });

            removeFromCart.getStyleClass().add("circlebutton");
            removeFromCart.setVisible(false);
            removeFromCart.setOnMouseClicked(e ->{
                amount.addAndGet(-1);
                orderById[finalI][1] = amount.get();
                addToCart.setText(amount+"x "+(double) object.getInt("price")/100+"\u20ac");
                if(amount.get() == 0){
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
        Button basket = new Button("Basket");
        Button checkout = new Button("Checkout");
        basket.getStyleClass().add("whitebuttonwide");
        reviews.getStyleClass().add("whitebuttonwide");
        checkout.getStyleClass().add("blackbuttonwide");

        /*Popup basketPopup = new Popup();
        Label label = new Label("This is a Popup");
        label.getStyleClass().add("menuitem");
        basketPopup.getContent().add(label);
        label.setMinWidth(80);
        label.setMinHeight(50);
        basket.setOnMouseClicked(e ->{
            Stage stage = (Stage) basket.getScene().getWindow();
            if (!basketPopup.isShowing())
                basketPopup.show(stage);
        });*/

        restInfo.setSpacing(20);
        restInfo.setAlignment(Pos.TOP_CENTER);
        restInfo.getChildren().addAll(spacer1,rImageView,rN,addr,ph,spacer3,reviews,basket,checkout,spacer2);

    }
}
