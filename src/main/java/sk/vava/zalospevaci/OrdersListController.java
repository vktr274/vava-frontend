package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

import java.io.File;
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
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class OrdersListController implements Initializable {

    @FXML
    private VBox tree;
    @FXML
    private Button menubtn;
    @FXML
    private Button userbtn;
    @FXML
    private VBox menubar;
    @FXML
    private VBox userBar;
    @FXML
    private VBox restFilt;

    private static ResourceBundle lang;
    private void setLang(ResourceBundle lang){
        OrdersListController.lang = lang;
    }
    private ResourceBundle getLang(){
        return OrdersListController.lang;
    }

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
        ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale(JSONLoaded.getLang(), JSONLoaded.getCountry()));
        setLang(lngBndl);
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

        JSONObject full = new JSONObject(getJSON("http://localhost:8080/orders?&per_page="+getPerpage()+"&page="+getPage()));
        JSONObject metadata = full.getJSONObject("metadata");
        JSONArray array = full.getJSONArray("orders");
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


            byte[] emojiByteCode = new byte[]{(byte)0xE2, (byte)0xAD, (byte)0x90};
            String emoji = new String(emojiByteCode, StandardCharsets.UTF_8);
            Label priceText = new Label();
            BigDecimal price;
            if(object.get("price")!=JSONObject.NULL){
                price = (object.getBigDecimal("price").divide(new BigDecimal(100), 1, RoundingMode.HALF_EVEN));
                priceText.setText(price + " \u20ac");
            }
            else priceText.setText("No\nreviews");
            System.out.println(object.get("price").getClass().getName());
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


            order.setOnMouseClicked(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Order Contents");
                alert.setHeaderText("This order contained:");
                JSONArray items = object.getJSONArray("items");
                StringBuilder itemstring = new StringBuilder();
                for (int j = 0; j < items.length(); j++) {
                    itemstring.append(items.getString(j)).append("\n");
                }
                alert.setContentText(String.valueOf(itemstring));
                alert.showAndWait();
            });

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
                restaurantSetScreen();
                menubar.setVisible(true);
            }
            else if(JSONLoaded.getLang().equals("en")){
                JSONLoaded.setCountry("SK");
                JSONLoaded.setLang("sk");
                ResourceBundle lngBndl = ResourceBundle.getBundle("LangBundle", new Locale(JSONLoaded.getLang(), JSONLoaded.getCountry()));
                setLang(lngBndl);
                restaurantSetScreen();
                menubar.setVisible(true);
            }
        });
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
        userbtn.setOnMouseClicked(e -> {
            userBar.setVisible(true);
        });
        goBack.setOnMouseClicked(e -> {
            userBar.setVisible(false);
        });
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
        userbtn.setOnMouseClicked(e -> {
            userBar.setVisible(true);
        });
        goBack.setOnMouseClicked(e -> {
            userBar.setVisible(false);
        });
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
        userbtn.setOnMouseClicked(e -> {
            userBar.setVisible(true);
        });
        goBack.setOnMouseClicked(e -> {
            userBar.setVisible(false);
        });
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
        userbtn.setOnMouseClicked(e -> {
            userBar.setVisible(true);
        });
        goBack.setOnMouseClicked(e -> {
            userBar.setVisible(false);
        });
    }
}