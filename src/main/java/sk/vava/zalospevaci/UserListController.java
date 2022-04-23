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

public class UserListController implements Initializable {

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
    private static String role = "";
    private static String name = "";
    private static String blocked = "false";
    private static void setBlocked(String blocked){
        UserListController.blocked = blocked;
    }
    private static String getBlocked(){
        return UserListController.blocked;
    }
    private static void setRole(String role){
        UserListController.role = role;
    }
    private static String getRole(){
        return  UserListController.role;
    }
    private static void setName(String name){
        UserListController.name = name;
    }
    private static String getName(){
        return UserListController.name;
    }
    private static void setAscending(String asc){ UserListController.ascending = asc;}
    private static String getAscending(){return UserListController.ascending;}
    private static void setPerpage(int perpage){
        UserListController.perpage = perpage;
    }
    private static int getPerpage(){
        return UserListController.perpage;
    }
    private static void setPage(int page){
        UserListController.page = page;
    }
    private static int getPage(){
        return UserListController.page;
    }
    private static void setElements(int elements){
        UserListController.elements = elements;
    }
    private static int getElements(){
        return UserListController.elements;
    }
    private static void setTotalpg(int totalpg){
        UserListController.totalpg = totalpg;
    }
    private static int getTotalpg(){
        return UserListController.totalpg;
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
            System.out.println(response.statusCode());
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
        JSONObject full = new JSONObject(getJSON("http://localhost:8080/users?&per_page="+getPerpage()+"&page="+getPage()+"&sort="+getAscending()+"&name="+getName()+"&role="+getRole()+"&blocked="+getBlocked()));
        JSONObject metadata = full.getJSONObject("metadata");
        JSONArray array = full.getJSONArray("users");
        setElements(metadata.getInt("total_elements"));
        setTotalpg(metadata.getInt("total_pages"));
        Text restaurantLabel = new Text("Users");
        restaurantLabel.getStyleClass().add("label");
        tree.getChildren().add(restaurantLabel);
        if(array.length()==0 && getElements()>0){
            setPage(getPage()-1);
            full = new JSONObject(getJSON("http://localhost:8080/users?&per_page="+getPerpage()+"&page="+getPage()+"&sort="+getAscending()+"&name="+getName()+"&role="+getRole()+"&blocked="+getBlocked()));
            array = full.getJSONArray("users");
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
            addReview.setText("Add Review");
            addReview.getStyleClass().add("whitebutton");

            Button block = new Button();
            if(object.getBoolean("blocked")) block.setText("Unblock");
            else block.setText("Block");
            block.getStyleClass().add("whitebutton");
            block.setOnMouseClicked(event -> {
                handleBlock(object.getInt("id"));
                restaurantSetScreen();
            });

            Button delete = new Button();
            delete.setText("Remove");
            delete.getStyleClass().add("whitebutton");

            restaurant.getStyleClass().add("itembutton");
            restaurant.getChildren().addAll(spacer1,new Text(object.getString("username")),spacer2,block,delete,spacer3);


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


        Button role = new Button();
        role.getStyleClass().add("whitebuttonwide");
        if(getRole().equals("guest")){
            role.setText("Showing users");
            restaurantLabel.setText("Users - Only users");
        }
        if(getRole().equals("manager")){
            role.setText("Showing managers");
            restaurantLabel.setText("Users - Only managers");
        }
        if(getRole().equals("admin")){
            role.setText("Showing admins");
            restaurantLabel.setText("Users - Only admins");
        }
        if(getRole().equals("")){
            role.setText("All shown");
            restaurantLabel.setText("Users - All");
        }
        role.setOnMouseClicked(event -> {
            if(getRole().equals("guest")) setRole("manager");
            else if(getRole().equals("manager")) setRole("admin");
            else if(getRole().equals("admin")) setRole("");
            else if(getRole().equals("")) setRole("guest");
            restaurantSetScreen();
        });

        TextField restname = new TextField();
        restname.getStyleClass().add("whitebuttonwide");
        restname.setPromptText("User Name");
        restname.setMaxWidth(250);

        Button apply = new Button("Apply filters");
        apply.getStyleClass().add("blackbuttonwide");
        apply.setOnMouseClicked(event -> {
            setName(restname.getText());
            restaurantSetScreen();
        });
        Button reset = new Button("Reset filters");
        reset.getStyleClass().add("whitebuttonwide");
        reset.setOnMouseClicked(event -> {
            setName("");
            restaurantSetScreen();
        });
        if(getName().equals("")){
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

        if(getPerpage()>=getElements()){
            morepg.setVisible(false);
            restFilt.getChildren().addAll(spacer1,asc,role,restname,apply,reset,blockbtn,spacer3,ppg,perpgbtn,spacer2);
        }
        else{
            restFilt.getChildren().addAll(spacer1,asc,role,restname,apply,reset,spacer3,blockbtn,ppg,perpgbtn,pg,pgbtn,spacer2);
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
                    .uri(new URI("http://localhost:8080/users/"+id+"/state"))
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