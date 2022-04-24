package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.ResourceBundle;

public class OrdersListController implements Initializable {

    @FXML
    private VBox tree;
    @FXML
    private Button menubtn;
    @FXML
    private VBox menubar;
    @FXML
    private VBox restFilt;

    //FILTERS
    private static int page = 0;
    private static int perpage = 4;
    private static int elements = 0;
    private static int totalpg = 0;
    private static String ascending = "asc";
    private static void setAscending(String asc){ OrdersListController.ascending = asc;}
    private static String getAscending(){return OrdersListController.ascending;}
    private static void setPerpage(int perpage){
        OrdersListController.perpage = perpage;
    }
    private static int getPerpage(){
        return OrdersListController.perpage;
    }
    private static void setPage(int page){
        OrdersListController.page = page;
    }
    private static int getPage(){
        return OrdersListController.page;
    }
    private static void setElements(int elements){
        OrdersListController.elements = elements;
    }
    private static int getElements(){
        return OrdersListController.elements;
    }
    private static void setTotalpg(int totalpg){
        OrdersListController.totalpg = totalpg;
    }
    private static int getTotalpg(){
        return OrdersListController.totalpg;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        restaurantSetScreen();
    }

    public String getJSON(String url){
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder().uri(new URI(url)).GET().setHeader("auth",JSONLoaded.getUser().getString("token")).build();
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

    public void restaurantSetScreen(){
        tree.setSpacing(25);
        tree.getChildren().clear();
        restFilt.getChildren().clear();
        JSONObject full = new JSONObject(getJSON("http://localhost:8080/orders?&per_page="+getPerpage()+"&page="+getPage()));
        JSONObject metadata = full.getJSONObject("metadata");
        JSONArray array = full.getJSONArray("restaurants");
        setElements(metadata.getInt("total_elements"));
        setTotalpg(metadata.getInt("total_pages"));
        Text ordersLabel = new Text("Orders");
        ordersLabel.getStyleClass().add("label");
        tree.getChildren().add(ordersLabel);
        if(array.length()==0 && getElements()>0){
            setPage(getPage()-1);
            full = new JSONObject(getJSON("http://localhost:8080/orders?&per_page="+getPerpage()+"&page="+getPage()));
            array = full.getJSONArray("orders");
        }
        menuBarF();
        for(int i=0; i<array.length();i++){
            Pane spacer1 = new Pane();
            Pane spacer2 = new Pane();
            Pane spacer3 = new Pane();
            HBox order = new HBox(25);
            JSONObject object = array.getJSONObject(i);
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            spacer1.setPrefWidth(0);
            spacer3.setPrefWidth(0);

            /*Button deleteBtn = new Button();
            deleteBtn.setText("Delete Order");
            deleteBtn.getStyleClass().add("whitebutton");*/

            /*Button block = new Button();
            if(object.getBoolean("blocked")) block.setText("Unblock");
            else block.setText("Block");
            block.getStyleClass().add("whitebutton");
            block.setOnMouseClicked(event -> {
                handleBlock(object.getInt("id"));
                restaurantSetScreen();
            });*/

            Button delete = new Button();
            delete.setText("Remove");
            delete.getStyleClass().add("whitebutton");
            delete.setOnMouseClicked(event -> {
                handleDel(object.getInt("id"));
                restaurantSetScreen();
            });


/*            byte[] emojiByteCode = new byte[]{(byte)0xE2, (byte)0xAD, (byte)0x90};
            String emoji = new String(emojiByteCode, StandardCharsets.UTF_8);*/
            Label priceText = new Label();
            BigDecimal price;
            if(object.get("rating")!=JSONObject.NULL){
                price = (object.getBigDecimal("price").divide(new BigDecimal(100), 1, RoundingMode.HALF_EVEN));
                priceText.setText(price.toString() + " €");
            }
            else priceText.setText("No\nreviews");
            System.out.println(object.get("rating").getClass().getName());
            priceText.getStyleClass().add("price");

            VBox orderBox = new VBox();
            orderBox.setSpacing(10);
            orderBox.setAlignment(Pos.CENTER_LEFT);
            Text restN = new Text(object.getString("user"));
            restN.getStyleClass().add("username");
            Text restT = new Text(object.getString("ordered_at"));
            restT.getStyleClass().add("ordertime");
            orderBox.getChildren().addAll(restN,restT);

            order.getStyleClass().add("itembutton");
            if(JSONLoaded.getActiveUser() != null){
                if(JSONLoaded.getActiveUser().role.equals("admin")){
                    order.getChildren().addAll(spacer1,priceText,orderBox,spacer2,delete,spacer3);
                }
                else if (JSONLoaded.getActiveUser().role.equals("guest") || JSONLoaded.getActiveUser().role.equals("manager")){
                    order.getChildren().addAll(spacer1,priceText,orderBox,spacer2,spacer3);
                }
            }
            else order.getChildren().addAll(spacer1,priceText,orderBox,spacer2,spacer3);


            // Uncomment to add Order screen on click
            /*order.setOnMouseClicked(e -> {
                JSONLoaded.setRestaurant(object);
                Stage stage = (Stage) order.getScene().getWindow();
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
            });*/

            tree.getChildren().add(order);
        }

        Text ppg = new Text("Show " + getPerpage() + " items");
        ppg.getStyleClass().add("itemnametext");
        Button morepg = new Button("+");
        morepg.setOnMouseClicked(event -> {
            setPerpage(getPerpage()+1);
            restaurantSetScreen();
        });
        morepg.getStyleClass().add("circlebutton");
        Button lesspg = new Button("-");
        lesspg.getStyleClass().add("circlebutton");
        lesspg.setOnMouseClicked(event -> {
            setPerpage(getPerpage()-1);
            restaurantSetScreen();
        });
        HBox perpgbtn = new HBox();
        perpgbtn.setSpacing(5);
        perpgbtn.getChildren().addAll(morepg,lesspg);
        perpgbtn.setAlignment(Pos.CENTER);
        if(getPerpage()==1) lesspg.setVisible(false);

        Text pg = new Text("Page:" + (getPage()+1));
        pg.getStyleClass().add("itemnametext");
        Button nextpg = new Button("+");
        nextpg.setOnMouseClicked(event -> {
            setPage(getPage()+1);
            restaurantSetScreen();
        });
        HBox pgbtn = new HBox();
        pgbtn.setSpacing(5);
        Button prevpg = new Button("-");
        prevpg.setOnMouseClicked(event -> {
            setPage(getPage()-1);
            restaurantSetScreen();
        });
        prevpg.getStyleClass().add("circlebutton");
        if(getPage()==0){
            prevpg.setVisible(false);
        }
        if(getPage()==getTotalpg()-1){
            nextpg.setVisible(false);
        }

        nextpg.getStyleClass().add("circlebutton");
        pgbtn.getChildren().addAll(nextpg,prevpg);
        pgbtn.setAlignment(Pos.CENTER);

        Pane spacer1 = new Pane();
        spacer1.setPrefHeight(0);
        Pane spacer2 = new Pane();
        spacer2.setPrefHeight(0);
        Pane spacer3 = new Pane();
        VBox.setVgrow(spacer3, Priority.ALWAYS);

        restFilt.setSpacing(15);
        restFilt.setAlignment(Pos.TOP_CENTER);

        if(getPerpage()>=getElements()){
            morepg.setVisible(false);
            restFilt.getChildren().addAll(spacer1,spacer3,ppg,perpgbtn,spacer2);
        }
        else{
            restFilt.getChildren().addAll(spacer1,spacer3,ppg,perpgbtn,pg,pgbtn,spacer2);
        }
        if(getElements()==0){
            perpgbtn.setVisible(false);
            pgbtn.setVisible(false);
            ppg.setVisible(false);
            pg.setVisible(false);
        }
    }

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public void handleDel(int id){
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(new URI("http://localhost:8080/orders/"+id))
                    .setHeader("auth", JSONLoaded.getUser().getString("token")) // add request header
                    .build();
        } catch (URISyntaxException e) {
            System.out.println("error");
        }
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
        } catch (InterruptedException | IOException e) {
            System.out.println("error");
        }
    }

    public void menuBarF(){
        menubar.getChildren().clear();
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