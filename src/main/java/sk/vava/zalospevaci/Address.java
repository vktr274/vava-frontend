package sk.vava.zalospevaci;

public class Address {
    int id;
    String name;
    String street;
    String city;
    String state;
    String postcode;
    String building_number;

    public Address(int id, String name, String street, String city, String state, String postcode, String building_number) {
        this.id = id;
        this.name = name;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postcode = postcode;
        this.building_number = building_number;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getBuilding_number() {
        return building_number;
    }

    public String toString() {
        return name + " " + street + " " + building_number + " " + city + " " + state + " " + postcode;
    }
}
