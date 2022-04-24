package sk.vava.zalospevaci;

import org.json.JSONObject;

public class JSONLoaded {
    private static JSONObject restaurant;
    private static JSONObject user;
    private static User activeUser;
    private static int[][] order;
    private static String lang;
    private static String country;
    private static boolean isManaging;

    public static void setIsManaging(boolean managing){
        JSONLoaded.isManaging = managing;
    }
    public static boolean getIsManaging(){
        return JSONLoaded.isManaging;
    }

    public static void setLang(String lang){
        JSONLoaded.lang = lang;
    }
    public static String getLang(){
        return JSONLoaded.lang;
    }

    public static void setCountry(String country){
        JSONLoaded.country = country;
    }
    public static String getCountry(){
        return JSONLoaded.country;
    }

    public static void setRestaurant(JSONObject restaurant){ JSONLoaded.restaurant = restaurant; }
    public static JSONObject getRestaurant(){
        return JSONLoaded.restaurant;
    }

    public static void setUser(JSONObject user) { JSONLoaded.user = user; }
    public static JSONObject getUser(){
        return JSONLoaded.user;
    }

    public static void setOrder(int[][] order){
        JSONLoaded.order = order;
    }
    public static int[][] getOrder(){
        return  JSONLoaded.order;
    }

    public static void setActiveUser(User activeUser){
        JSONLoaded.activeUser = activeUser;
    }
    public static User getActiveUser(){
        return JSONLoaded.activeUser;
    }
}
