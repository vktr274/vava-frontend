package sk.vava.zalospevaci;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

public class ReviewListController implements Initializable {

    @FXML
    private VBox reviews;
    @FXML
    private VBox restInfo;

    @FXML
    private Button menubtn;
    @FXML
    private VBox menubar;

    private static int page = 0;
    private static int perpage = 2;
    private static int elements = 0;
    private static int totalpg = 0;
    private static void setPerpage(int perpage){
        ReviewListController.perpage = perpage;
    }
    private static int getPerpage(){
        return ReviewListController.perpage;
    }
    private static void setPage(int page){
        ReviewListController.page = page;
    }
    private static int getPage(){
        return ReviewListController.page;
    }
    private static void setElements(int elements){
        ReviewListController.elements = elements;
    }
    private static int getElements(){
        return ReviewListController.elements;
    }
    private static void setTotalpg(int totalpg){
        ReviewListController.totalpg = totalpg;
    }
    private static int getTotalpg(){
        return ReviewListController.totalpg;
    }

    private static ResourceBundle lang;
    private void setLang(ResourceBundle lang){
        ReviewListController.lang = lang;
    }
    private ResourceBundle getLang(){
        return ReviewListController.lang;
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
        reviews.setSpacing(25);
        reviews.getChildren().clear();
        restInfo.getChildren().clear();
        menuBarF();
        JSONObject restaurantJson = JSONLoaded.getRestaurant();
        System.out.println(restaurantJson.getInt("id"));
        JSONObject obj = new JSONObject(getJSON("http://localhost:8080/reviews?restaurant_id="+restaurantJson.getInt("id")+"&sort=desc&sort_by=createdAt&per_page="+getPerpage()+"&page="+getPage()));
        JSONArray array = obj.getJSONArray("reviews");
        JSONObject metadata = obj.getJSONObject("metadata");
        setElements(metadata.getInt("total_elements"));
        setTotalpg(metadata.getInt("total_pages"));
        System.out.println(array.length());
        if(array.length()==0 && getElements()>0){
            setPage(getPage()-1);
            obj = new JSONObject(getJSON("http://localhost:8080/reviews?restaurant_id="+restaurantJson.getInt("id")+"&sort=desc&sort_by=createdAt&per_page="+getPerpage()+"&page="+getPage()));
            array = obj.getJSONArray("reviews");
        }
        Text restaurantLabel = new Text(getLang().getString("reviews"));
        restaurantLabel.getStyleClass().add("label");
        reviews.getChildren().add(restaurantLabel);
        for(int i=0; i<array.length();i++){
            Pane spacer1 = new Pane();
            Pane spacer2 = new Pane();
            Pane spacer3 = new Pane();
            HBox itemMenu = new HBox(25);
            VBox texts = new VBox(5);
            JSONObject object = array.getJSONObject(i);
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            spacer1.setPrefWidth(0);
            spacer3.setPrefWidth(0);
            ImageView imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(150);
            JSONArray photos = object.getJSONArray("photos");
            //System.out.println(String.valueOf(photos) + photos.length());
            if(photos.length()>0){
                final AtomicInteger[] index = {new AtomicInteger()};
                final Image[] img = {new Image("http://localhost:8080/photos/" + photos.getInt(index[0].get()))};
                imageView.setImage(img[0]);
                imageView.setOnMouseClicked(event -> {
                    if(index[0].get() +1<photos.length()){
                        index[0].addAndGet(1);
                        img[0] = new Image("http://localhost:8080/photos/"+photos.getInt(index[0].get()));
                        imageView.setImage(img[0]);
                    }
                    else{
                        index[0].set(0);
                    }
                });
            }
            Text itemName = new Text(object.getString("username"));
            itemName.getStyleClass().add("itemname");
            Label itemDesc = new Label(object.getString("text"));
            itemDesc.setWrapText(true);
            itemDesc.getStyleClass().add("itemdesc");
            Text itemScore = new Text(object.getInt("score")+"/10");
            itemScore.getStyleClass().add("itemname");
            texts.setAlignment(Pos.CENTER_LEFT);
            texts.getChildren().addAll(itemName,itemDesc);
            itemMenu.getStyleClass().add("reviewitem");
            itemMenu.getChildren().addAll(spacer1,imageView,texts,spacer2,itemScore,spacer3);
            reviews.getChildren().add(itemMenu);
        }
        Button addReview = new Button();
        addReview.setText(getLang().getString("addrev"));
        addReview.getStyleClass().add("whitebutton");
        addReview.setOnMouseClicked(event -> {
            Stage stage = (Stage) addReview.getScene().getWindow();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("reviewAdd.fxml")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            assert root != null;
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            stage.setScene(scene);
        });
        if(JSONLoaded.getActiveUser() != null) reviews.getChildren().add(addReview);
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

        Text ppg = new Text(getLang().getString("show") + getPerpage() + getLang().getString("items"));
        ppg.getStyleClass().add("itemnametext");
        Button morepg = new Button("+");
        morepg.setOnMouseClicked(event -> {
            setPerpage(getPerpage()+1);
            SetScreen();
        });
        morepg.getStyleClass().add("circlebutton");
        Button lesspg = new Button("-");
        lesspg.getStyleClass().add("circlebutton");
        lesspg.setOnMouseClicked(event -> {
            setPerpage(getPerpage()-1);
            SetScreen();
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
            SetScreen();
        });
        HBox pgbtn = new HBox();
        pgbtn.setSpacing(5);
        Button prevpg = new Button("-");
        prevpg.setOnMouseClicked(event -> {
            setPage(getPage()-1);
            SetScreen();
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


        restInfo.setSpacing(20);
        restInfo.setAlignment(Pos.TOP_CENTER);
        if(getPerpage()==getElements()){
            morepg.setVisible(false);
            restInfo.getChildren().addAll(spacer1,rN,addr,ph,spacer3,ppg,perpgbtn,spacer2);
        }
        else{
            restInfo.getChildren().addAll(spacer1,rN,addr,ph,spacer3,ppg,perpgbtn,pg,pgbtn,spacer2);
        }
        if(getElements()==0){
            perpgbtn.setVisible(false);
            pgbtn.setVisible(false);
            ppg.setVisible(false);
            pg.setVisible(false);
            restaurantLabel.setText(getLang().getString("noreviews"));
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
