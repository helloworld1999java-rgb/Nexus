package com.marketplace.model;

import com.google.gson.annotations.SerializedName;

public class Address {
    private String id;

    @SerializedName("user_id")
    private String userId;

    private String label;
    private String city;
    private String street;

    @SerializedName("house")
    private String building;

    private String apartment;

    @SerializedName("zip_code")
    private String zipCode;

    @SerializedName("is_default")
    private boolean isDefault;

    private transient String fullName;
    private transient String phone;
    private transient String country;

    public Address() {}

    public String getId()                    { return id; }
    public void setId(String id)             { this.id = id; }
    public String getUserId()                { return userId; }
    public void setUserId(String u)          { this.userId = u; }
    public String getLabel()                 { return label; }
    public void setLabel(String l)           { this.label = l; }
    public String getCity()                  { return city; }
    public void setCity(String c)            { this.city = c; }
    public String getStreet()                { return street; }
    public void setStreet(String s)          { this.street = s; }
    public String getBuilding()              { return building; }
    public void setBuilding(String b)        { this.building = b; }
    public String getApartment()             { return apartment; }
    public void setApartment(String a)       { this.apartment = a; }
    public String getZipCode()               { return zipCode; }
    public void setZipCode(String z)         { this.zipCode = z; }
    public boolean isDefault()               { return isDefault; }
    public void setDefault(boolean d)        { this.isDefault = d; }
    public String getFullName()              { return fullName; }
    public void setFullName(String f)        { this.fullName = f; }
    public String getPhone()                 { return phone; }
    public void setPhone(String p)           { this.phone = p; }
    public String getCountry()               { return country; }
    public void setCountry(String c)         { this.country = c; }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (city      != null && !city.isEmpty())      sb.append(city).append(", ");
        if (street    != null && !street.isEmpty())    sb.append("ул. ").append(street);
        if (building  != null && !building.isEmpty())  sb.append(", д. ").append(building);
        if (apartment != null && !apartment.isEmpty()) sb.append(", кв. ").append(apartment);
        return sb.toString();
    }
}