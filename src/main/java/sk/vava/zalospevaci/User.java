package sk.vava.zalospevaci;

public class User {
    String username;
    String email;
    String role;
    String phone;
    String addressName;
    String street;
    int buildingNumber;
    String city;
    String state;
    String postcode;

    public User(String username, String email, String role, String phone, String addressName, String street, int buildingNumber, String city, String state, String postcode) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.addressName = addressName;
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.city = city;
        this.state = state;
        this.postcode = postcode;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressName() {
        return addressName;
    }

    public String getStreet() {
        return street;
    }

    public int getBuildingNumber() {
        return buildingNumber;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setBuildingNumber(int buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
}
