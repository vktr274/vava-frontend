package sk.vava.zalospevaci;

public class Address {
    int id;
    String name;
    String street;
    String city;
    String state;
    String postcode;
    String buildingNumber;

    public Address(int id, String name, String street, String city, String state, String postcode, String buildingNumber) {
        this.id = id;
        this.name = name;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postcode = postcode;
        this.buildingNumber = buildingNumber;
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

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public String toString() {
        return name + " " + street + " " + buildingNumber + " " + city + " " + state + " " + postcode;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof Address)) {
            return false;
        }
        Address other = (Address)o;
        return id == other.id;
    }

    public int hashCode() {
        return id;
    }
}
