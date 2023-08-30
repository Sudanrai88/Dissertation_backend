package com.example.demo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private List<Double> originLocation;
    private String editorialSummary;
    private int order;
    private Date time;

    public Place(String businessStatus, String name, String placeId, double longitude, double latitude, String imagesRef, ArrayList<String> placeTypes, double rating, int rating_amount, int price, List<Double> originLocation) {
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
        this.originLocation = originLocation;
    }

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

    public String getEditorialSummary() {
        return editorialSummary;
    }

    public void setEditorialSummary(String editorialSummary) {
        this.editorialSummary = editorialSummary;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<Double> getOriginLocation() {
        return originLocation;
    }

    public void setOriginLocation(List<Double> originLocation) {
        this.originLocation = originLocation;
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
                ", order=" + order +
                '}';
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Place clone() {
        Place newPlace = new Place();

        newPlace.businessStatus = this.businessStatus;
        newPlace.name = this.name;
        newPlace.placeId = this.placeId;
        newPlace.longitude = this.longitude;
        newPlace.latitude = this.latitude;
        newPlace.imagesRef = this.imagesRef;

        // Deep copy for placeTypes ArrayList
        if (this.placeTypes != null) {
            newPlace.placeTypes = new ArrayList<>(this.placeTypes);
        }

        newPlace.rating = this.rating;
        newPlace.rating_amount = this.rating_amount;
        newPlace.price = this.price;

        // Deep copy for originLocation List
        if (this.originLocation != null) {
            newPlace.originLocation = new ArrayList<>(this.originLocation);
        }

        newPlace.editorialSummary = this.editorialSummary;
        newPlace.order = this.order;

        return newPlace;
    }

}
