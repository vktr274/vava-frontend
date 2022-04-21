package sk.vava.zalospevaci;

public class Phone {
    int id;
    String number;
    String countryCode;

    public Phone(int id, String number, String countryCode) {
        this.id = id;
        this.number = number;
        this.countryCode = countryCode;
    }

    public int getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

}
