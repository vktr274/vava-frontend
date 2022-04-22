package sk.vava.zalospevaci;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.ResourceBundle;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
        JSONArray array = full.getJSONArray("reviews");
        setElements(metadata.getInt("total_elements"));
        setTotalpg(metadata.getInt("total_pages"));
        Text restaurantLabel = new Text("Restaurants");
        restaurantLabel.getStyleClass().add("label");
        tree.getChildren().add(restaurantLabel);
        if(array.length()==0 && getElements()>0){
            setPage(getPage()-1);
            full = new JSONObject(getJSON("http://localhost:8080/restaurants?&per_page="+getPerpage()+"&page="+getPage()+"&sort="+getAscending()+"&name="+getName()+"&city="+getCity()+"&blocked="+getBlocked()));
            array = full.getJSONArray("reviews");
        }
        menuBarF();
        for(int i=0; i<array.length();i++){
            Image image = new Image("https://i.imgur.com/Tf3j0rU.jpg");
            Pane spacer1 = new Pane();
            Pane spacer2 = new Pane();
            Pane spacer3 = new Pane();
            HBox restaurant = new HBox(25);
            JSONObject object = array.getJSONObject(i);
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            spacer1.setPrefWidth(0);
            spacer3.setPrefWidth(0);

            Button addReview = new Button();
            addReview.setText("Add Review");
            addReview.getStyleClass().add("whitebutton");

            Button block = new Button();
            if(object.getBoolean("blocked")) block.setText("Unblock");
            else block.setText("Block");
            block.getStyleClass().add("whitebutton");

            Button delete = new Button();
            delete.setText("Remove");
            delete.getStyleClass().add("whitebutton");

            ImageView imageView = new ImageView();
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(90);

            restaurant.getStyleClass().add("itembutton");
            if(JSONLoaded.getActiveUser() != null){
                if(JSONLoaded.getActiveUser().role.equals("admin")){
                    restaurant.getChildren().addAll(spacer1,imageView,new Text(object.getString("name")),spacer2,block,delete,spacer3);
                }
                else if (JSONLoaded.getActiveUser().role.equals("guest") || JSONLoaded.getActiveUser().role.equals("manager")){
                    restaurant.getChildren().addAll(spacer1,imageView,new Text(object.getString("name")),spacer2,addReview,spacer3);
                }
            }
            else restaurant.getChildren().addAll(spacer1,imageView,new Text(object.getString("name")),spacer2,spacer3);


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
        if(getBlocked().equals("false")) blockbtn.setText("Blocked hidden");
        if(getBlocked().equals("true")) blockbtn.setText("Blocked shown");
        if(getBlocked().equals("")) blockbtn.setText("All shown");
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
            asc.setText("Ascending order");
        }
        else if (getAscending().equals("desc")){
            asc.setText("Descending order");
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
        city.setPromptText("City");
        city.setMaxWidth(250);

        TextField restname = new TextField();
        restname.getStyleClass().add("whitebuttonwide");
        restname.setPromptText("Restaurant name");
        restname.setMaxWidth(250);

        Button apply = new Button("Apply filters");
        apply.getStyleClass().add("blackbuttonwide");
        apply.setOnMouseClicked(event -> {
            setCity(city.getText());
            setName(restname.getText());
            restaurantSetScreen();
        });
        Button reset = new Button("Reset filters");
        reset.getStyleClass().add("whitebuttonwide");
        reset.setOnMouseClicked(event -> {
            setCity("");
            setName("");
            restaurantSetScreen();
        });
        if(getCity().equals("") && getName().equals("")){
            reset.setVisible(false);
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

        if(getPerpage()==getElements()){
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