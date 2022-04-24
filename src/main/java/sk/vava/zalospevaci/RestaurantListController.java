package sk.vava.zalospevaci;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
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
import org.json.*;

public class RestaurantListController implements Initializable {

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
    private static String city = "";
    private static String name = "";
    private static String blocked = "false";
    private static void setBlocked(String blocked){
        RestaurantListController.blocked = blocked;
    }
    private static String getBlocked(){
        return RestaurantListController.blocked;
    }
    private static void setCity(String city){
        RestaurantListController.city = city;
    }
    private static String getCity(){
        return  RestaurantListController.city;
    }
    private static void setName(String name){
        RestaurantListController.name = name;
    }
    private static String getName(){
        return RestaurantListController.name;
    }
    private static void setAscending(String asc){ RestaurantListController.ascending = asc;}
    private static String getAscending(){return RestaurantListController.ascending;}
    private static void setPerpage(int perpage){
        RestaurantListController.perpage = perpage;
    }
    private static int getPerpage(){
        return RestaurantListController.perpage;
    }
    private static void setPage(int page){
        RestaurantListController.page = page;
    }
    private static int getPage(){
        return RestaurantListController.page;
    }
    private static void setElements(int elements){
        RestaurantListController.elements = elements;
    }
    private static int getElements(){
        return RestaurantListController.elements;
    }
    private static void setTotalpg(int totalpg){
        RestaurantListController.totalpg = totalpg;
    }
    private static int getTotalpg(){
        return RestaurantListController.totalpg;
    }

    private static ResourceBundle lang;
    private void setLang(ResourceBundle lang){
        RestaurantListController.lang = lang;
    }
    private ResourceBundle getLang(){
        return RestaurantListController.lang;
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

    public void restaurantSetScreen(){
        if(JSONLoaded.getActiveUser() != null){
            if(!Objects.equals(JSONLoaded.getActiveUser().role, "admin")) setBlocked("false");
        }
        else setBlocked("false");
        tree.setSpacing(25);
        tree.getChildren().clear();
        restFilt.getChildren().clear();
        JSONObject full = new JSONObject(getJSON("http://localhost:8080/restaurants?&per_page="+getPerpage()+"&page="+getPage()+"&sort="+getAscending()+"&name="+getName()+"&city="+getCity()+"&blocked="+getBlocked()));
        JSONObject metadata = full.getJSONObject("metadata");
        JSONArray array = full.getJSONArray("restaurants");
        setElements(metadata.getInt("total_elements"));
        setTotalpg(metadata.getInt("total_pages"));
        Text restaurantLabel = new Text(getLang().getString("restaurants"));
        restaurantLabel.getStyleClass().add("label");
        tree.getChildren().add(restaurantLabel);
        if(array.length()==0 && getElements()>0){
            setPage(getPage()-1);
            full = new JSONObject(getJSON("http://localhost:8080/restaurants?&per_page="+getPerpage()+"&page="+getPage()+"&sort="+getAscending()+"&name="+getName()+"&city="+getCity()+"&blocked="+getBlocked()));
            array = full.getJSONArray("restaurants");
        }
        menuBarF();
        for(int i=0; i<array.length();i++){
            Pane spacer1 = new Pane();
            Pane spacer2 = new Pane();
            Pane spacer3 = new Pane();
            HBox restaurant = new HBox(25);
            JSONObject object = array.getJSONObject(i);
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            spacer1.setPrefWidth(0);
            spacer3.setPrefWidth(0);

            Button addReview = new Button();
            addReview.setText(getLang().getString("addrev"));
            addReview.getStyleClass().add("whitebutton");

            Button block = new Button();
            if(object.getBoolean("blocked")) block.setText(getLang().getString("unblock"));
            else block.setText(getLang().getString("block"));
            block.getStyleClass().add("whitebutton");
            block.setOnMouseClicked(event -> {
                handleBlock(object.getInt("id"));
                restaurantSetScreen();
            });

            Button delete = new Button();
            delete.setText(getLang().getString("delete"));
            delete.getStyleClass().add("whitebutton");
            delete.setOnMouseClicked(event -> {
                handleDel(object.getInt("id"));
                restaurantSetScreen();
            });


            byte[] emojiByteCode = new byte[]{(byte)0xE2, (byte)0xAD, (byte)0x90};
            String emoji = new String(emojiByteCode, StandardCharsets.UTF_8);
            Label ratingText = new Label();
            BigDecimal ratingScore;
            if(object.get("rating")!=JSONObject.NULL){
                ratingScore = object.getBigDecimal("rating");
                ratingScore = ratingScore.setScale(1, RoundingMode.HALF_EVEN);
                ratingText.setText(getLang().getString("score")+"\n"+ ratingScore+emoji);
            }
            else ratingText.setText(getLang().getString("noscore"));
            System.out.println(object.get("rating").getClass().getName());
            ratingText.getStyleClass().add("score");

            VBox name = new VBox();
            name.setSpacing(10);
            name.setAlignment(Pos.CENTER_LEFT);
            Text restN = new Text(object.getString("name"));
            restN.getStyleClass().add("itemname");
            Text restC = new Text(object.getJSONObject("address").getString("city"));
            restC.getStyleClass().add("itemdesc");
            name.getChildren().addAll(restN,restC);

            restaurant.getStyleClass().add("itembutton");
            if(JSONLoaded.getActiveUser() != null){
                if(JSONLoaded.getActiveUser().role.equals("admin")){
                    restaurant.getChildren().addAll(spacer1,ratingText,name,spacer2,block,delete,spacer3);
                }
                else if (JSONLoaded.getActiveUser().role.equals("guest") || JSONLoaded.getActiveUser().role.equals("manager")){
                    restaurant.getChildren().addAll(spacer1,ratingText,name,spacer2,addReview,spacer3);
                }
            }
            else restaurant.getChildren().addAll(spacer1,ratingText,name,spacer2,spacer3);


            restaurant.setOnMouseClicked(e -> {
                JSONLoaded.setRestaurant(object);
                Stage stage = (Stage) restaurant.getScene().getWindow();
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
            });

            tree.getChildren().add(restaurant);
        }

        Button blockbtn = new Button();
        blockbtn.getStyleClass().add("whitebuttonwide");
        if(getBlocked().equals("false")) blockbtn.setText(getLang().getString("blh"));
        if(getBlocked().equals("true")) blockbtn.setText(getLang().getString("bls"));
        if(getBlocked().equals("")) blockbtn.setText(getLang().getString("als"));
        blockbtn.setOnMouseClicked(event -> {
            if(getBlocked().equals("false")) setBlocked("true");
            else if(getBlocked().equals("true")) setBlocked("");
            else if(getBlocked().equals("")) setBlocked("false");
            restaurantSetScreen();
        });
        if(JSONLoaded.getActiveUser() != null){
            if(!Objects.equals(JSONLoaded.getActiveUser().role, "admin")) blockbtn.setVisible(false);
        }
        else blockbtn.setVisible(false);

        Button asc = new Button();
        asc.getStyleClass().add("whitebuttonwide");
        if(getAscending().equals("asc")){
            asc.setText(getLang().getString("as"));
        }
        else if (getAscending().equals("desc")){
            asc.setText(getLang().getString("ds"));
        }
        asc.setOnMouseClicked(event -> {
            if(getAscending().equals("asc")){
                setAscending("desc");
                restaurantSetScreen();
            }
            else if (getAscending().equals("desc")){
                setAscending("asc");
                restaurantSetScreen();
            }
        });

        TextField city = new TextField();
        city.getStyleClass().add("whitebuttonwide");
        city.setPromptText(getLang().getString("city"));
        city.setMaxWidth(250);

        TextField restname = new TextField();
        restname.getStyleClass().add("whitebuttonwide");
        restname.setPromptText(getLang().getString("restn"));
        restname.setMaxWidth(250);

        Button apply = new Button(getLang().getString("applfil"));
        apply.getStyleClass().add("blackbuttonwide");
        apply.setOnMouseClicked(event -> {
            setCity(city.getText());
            setName(restname.getText());
            restaurantSetScreen();
        });
        Button reset = new Button(getLang().getString("restfil"));
        reset.getStyleClass().add("whitebuttonwide");
        reset.setOnMouseClicked(event -> {
            setCity("");
            setName("");
            restaurantSetScreen();
        });
        if(getCity().equals("") && getName().equals("")){
            reset.setVisible(false);
        }

        Text ppg = new Text(getLang().getString("show") + getPerpage() + getLang().getString("items"));
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

        Text pg = new Text(getLang().getString("page") + (getPage()+1));
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
            restFilt.getChildren().addAll(spacer1,asc,restname,city,apply,reset,blockbtn,spacer3,ppg,perpgbtn,spacer2);
        }
        else{
            restFilt.getChildren().addAll(spacer1,asc,restname,city,apply,reset,spacer3,blockbtn,ppg,perpgbtn,pg,pgbtn,spacer2);
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

    public void handleBlock(int id){
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .uri(new URI("http://localhost:8080/restaurants/"+id+"/state"))
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

    public void handleDel(int id){
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(new URI("http://localhost:8080/restaurants/"+id))
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
        Button home = new Button("Home");
        Button restaurant = new Button("Restaurants");
        Button settings = new Button("Language");
        home.getStyleClass().add("whitebuttonmenu");
        restaurant.getStyleClass().add("whitebuttonmenu");
        settings.getStyleClass().add("whitebuttonmenu");
        if(JSONLoaded.getLang().equals("sk")){
            settings.setText("Language - SK");
        }
        if(JSONLoaded.getLang().equals("en")){
            settings.setText("Language - EN");
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
}