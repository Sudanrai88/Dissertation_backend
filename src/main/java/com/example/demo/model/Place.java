package com.example.demo.model;

public class Place {
    private String businessStatus;
    private String name;
    private String placeId;
    private String longitude;
    private String latitude;
    private String imagesRef;
    private double rating;
    private int rating_amount;
    private int price;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getImages() {
        return imagesRef;
    }

    public void setImages(String images) {
        this.imagesRef = images;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRating_amount() {
        return rating_amount;
    }

    public void setRating_amount(int rating_amount) {
        this.rating_amount = rating_amount;
    }

    public String getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }

    @Override
    public String toString() {
        return "Place{" +
                "businessStatus='" + businessStatus + '\'' +
                ", name='" + name + '\'' +
                ", placeId='" + placeId + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", imagesRef='" + imagesRef + '\'' +
                ", rating=" + rating +
                ", rating_amount=" + rating_amount +
                ", price_level=" + price +
                '}';
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

}
