package sk.vava.zalospevaci;

import org.json.JSONObject;

public class JSONLoaded {
    private static JSONObject restaurant;
    private static int[][] order;

    public static void setRestaurant(JSONObject restaurant){
        JSONLoaded.restaurant = restaurant;
    }
    public static JSONObject getRestaurant(){
        return JSONLoaded.restaurant;
    }

    public static void setOrder(int[][] order){
        JSONLoaded.order = order;
    }
    public static int[][] getOrder(){
        return  JSONLoaded.order;
    }
}
