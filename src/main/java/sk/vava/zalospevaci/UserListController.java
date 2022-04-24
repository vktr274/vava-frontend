package sk.vava.zalospevaci;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private Button userbtn;
    @FXML
    private VBox menubar;
    @FXML
    private VBox userBar;
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

    private static ResourceBundle lang;
    private void setLang(ResourceBundle lang){
        UserListController.lang = lang;
    }
    private ResourceBundle getLang(){
        return UserListController.lang;
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
        if(getBlocked().equals("false")) blockbtn.setText(getLang().getString("blh"));
        if(getBlocked().equals("true")) blockbtn.setText(getLang().getString("bls"));
        if(getBlocked().equals("")) blockbtn.setText(getLang().getString("als"));
        blockbtn.setOnMouseClicked(event -> {
            switch (getBlocked()) {
                case "false" -> setBlocked("true");
                case "true" -> setBlocked("");
                case "" -> setBlocked("false");
            }
            restaurantSetScreen();
        });

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


        Button role = new Button();
        role.getStyleClass().add("whitebuttonwide");
        if(getRole().equals("guest")){
            role.setText(getLang().getString("su"));
            restaurantLabel.setText(getLang().getString("usu"));
        }
        if(getRole().equals("manager")){
            role.setText(getLang().getString("mg"));
            restaurantLabel.setText(getLang().getString("umg"));
        }
        if(getRole().equals("admin")){
            role.setText(getLang().getString("ad"));
            restaurantLabel.setText(getLang().getString("uad"));
        }
        if(getRole().equals("")){
            role.setText(getLang().getString("alu"));
            restaurantLabel.setText(getLang().getString("ual"));
        }
        role.setOnMouseClicked(event -> {
            switch (getRole()) {
                case "guest" -> setRole("manager");
                case "manager" -> setRole("admin");
                case "admin" -> setRole("");
                case "" -> setRole("guest");
            }
            restaurantSetScreen();
        });

        TextField restname = new TextField();
        restname.getStyleClass().add("whitebuttonwide");
        restname.setPromptText(getLang().getString("userfilt"));
        restname.setMaxWidth(250);

        Button apply = new Button(getLang().getString("applfil"));
        apply.getStyleClass().add("blackbuttonwide");
        apply.setOnMouseClicked(event -> {
            setName(restname.getText());
            restaurantSetScreen();
        });
        Button reset = new Button(getLang().getString("restfil"));
        reset.getStyleClass().add("whitebuttonwide");
        reset.setOnMouseClicked(event -> {
            setName("");
            restaurantSetScreen();
        });
        if(getName().equals("")){
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
        menubtn.setOnMouseClicked(e -> menubar.setVisible(true));
        goBack.setOnMouseClicked(e -> menubar.setVisible(false));
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
        userbtn.setOnMouseClicked(e -> userBar.setVisible(true));
        goBack.setOnMouseClicked(e -> userBar.setVisible(false));
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
        userbtn.setOnMouseClicked(e -> userBar.setVisible(true));
        goBack.setOnMouseClicked(e -> userBar.setVisible(false));
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
        userbtn.setOnMouseClicked(e -> userBar.setVisible(true));
        goBack.setOnMouseClicked(e -> userBar.setVisible(false));
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
        userbtn.setOnMouseClicked(e -> userBar.setVisible(true));
        goBack.setOnMouseClicked(e -> userBar.setVisible(false));
    }
}