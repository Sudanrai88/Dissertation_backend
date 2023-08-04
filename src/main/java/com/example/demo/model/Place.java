package com.example.demo.model;

import java.util.ArrayList;

//Gene

public class Place {
    private String businessStatus;
    private String name;
    private String placeId;
    private double longitude;
    private double latitude;
    private String imagesRef;
    private ArrayList<String> placeTypes;
    private double rating;
    private int rating_amount;
    private int price;

    public Place(String businessStatus, String name, String placeId, double longitude, double latitude, String imagesRef, ArrayList<String> placeTypes, double rating, int rating_amount, int price) {
        this.businessStatus = businessStatus;
        this.name = name;
        this.placeId = placeId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.imagesRef = imagesRef;
        this.placeTypes = placeTypes;
        this.rating = rating;
        this.rating_amount = rating_amount;
        this.price = price;
    }

    public Place() {

    }

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

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
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

    public String getImagesRef() {
        return imagesRef;
    }

    public void setImagesRef(String imagesRef) {
        this.imagesRef = imagesRef;
    }

    public ArrayList<String> getPlaceTypes() {
        return placeTypes;
    }

    public void setPlaceTypes(ArrayList<String> placeTypes) {
        this.placeTypes = placeTypes;
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
                ", placeTypes=" + placeTypes +
                '}';
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

}
