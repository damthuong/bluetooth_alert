package com.example.bluetoothscanner;

public class User {
    private String Name;
    private String Phone;
    private String Address;
    private String BlAddress;

    public User() {
    }

    public User(String name, String phone, String address, String blAddress) {

        Name = name;
        Phone = phone;
        Address = address;
        BlAddress = blAddress;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getBlAddress() {
        return BlAddress;
    }

    public void setBlAddress(String blAddress) {
        BlAddress = blAddress;
    }
}
