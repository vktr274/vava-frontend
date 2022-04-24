package sk.vava.zalospevaci;

public class Phone {
    String number;
    String country_code;

    public Phone(String number, String country_code) {
        this.number = number;
        this.country_code = country_code;
    }

    public String getNumber() {
        return number;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setCountry_code(String countryCode) {
        this.country_code = countryCode;
    }

}
