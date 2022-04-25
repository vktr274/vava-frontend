package sk.vava.zalospevaci;

public class Restaurant {
  private String name;
  private Address address;
  private Phone phone;

  public Restaurant(String name, Address address, Phone phone) {
    this.name = name;
    this.address = address;
    this.phone = phone;
  }

  public String getName() {
    return name;
  }

  public Address getAddress() {
    return address;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public Phone getPhone() {
    return phone;
  }

  public void setPhone(Phone phone) {
    this.phone = phone;
  }

}
