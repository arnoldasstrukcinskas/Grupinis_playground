package lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lt.viko.eif.astrukcinskas.grupinis_playground.model.Hotel;

@JsonPropertyOrder({
        "id",
        "hotelName",
        "accomodationType",
        "hotelStars",
        "district",
        "distanceToCenter",
        "isBeachFront",
        "price",
        "priceAllInclusive",
        "reviewScoreNumber",
        "reviewScoreWord",
        "reviewNumber",
        "additionals",
        "mainPhotoUrl",
        "maxPhotoUrl",
        "photoUrl1440",
        "hotelUrl"
})

public class HotelDto {

    private int id;
    private String hotelName;
    private String accomodationType;
    private int hotelStars;
    private String district;
    private String distanceToCenter;
    private boolean isBeachFront;
    private String price;
    private String priceAllInclusive;
    private String address;
    private String mainPhotoUrl;
    private String maxPhotoUrl;
    private String PhotoUrl1440;
    private String hotelUrl;

    //Review section
    private double reviewScoreNumber;
    private String reviewScoreWord;
    private int reviewNumber;
    private String additionals;

    public HotelDto() {}

    public HotelDto(Hotel hotel) {
        this.id = hotel.getId();
        this.hotelName = hotel.getHotelName();
        this.accomodationType = hotel.getAccomodationType();
        this.hotelStars = hotel.getHotelStars();
        this.district = hotel.getDistrict();
        this.distanceToCenter = hotel.getDistanceToCenter();
        this.isBeachFront = hotel.isBeachFront();
        this.price = hotel.getPrice();
        this.priceAllInclusive = hotel.getPriceAllInclusive();
        this.address = hotel.getAddress();
        this.mainPhotoUrl = hotel.getMainPhotoUrl();
        this.maxPhotoUrl = hotel.getMaxPhotoUrl();
        this.PhotoUrl1440 = hotel.getPhotoUrl1440();
        this.hotelUrl = hotel.getHotelUrl();
        this.reviewScoreNumber = hotel.getReviewScoreNumber();
        this.reviewScoreWord = hotel.getReviewScoreWord();
        this.reviewNumber = hotel.getReviewNumber();
        this.additionals = hotel.getAdditionals();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getAccomodationType() {
        return accomodationType;
    }

    public void setAccomodationType(String accomodationType) {
        this.accomodationType = accomodationType;
    }

    public String getDistanceToCenter() {
        return distanceToCenter;
    }

    public void setDistanceToCenter(String distanceToCenter) {
        this.distanceToCenter = distanceToCenter;
    }

    public int getHotelStars() {
        return hotelStars;
    }

    public void setHotelStars(int hotelStars) {
        this.hotelStars = hotelStars;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public boolean isBeachFront() {
        return isBeachFront;
    }

    public void setBeachFront(boolean beachFront) {
        isBeachFront = beachFront;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPriceAllInclusive() {
        return priceAllInclusive;
    }

    public void setPriceAllInclusive(String priceAllInclusive) {
        this.priceAllInclusive = priceAllInclusive;
    }

    public double getReviewScoreNumber() {
        return reviewScoreNumber;
    }

    public void setReviewScoreNumber(double reviewScoreNumber) {
        this.reviewScoreNumber = reviewScoreNumber;
    }

    public String getReviewScoreWord() {
        return reviewScoreWord;
    }

    public void setReviewScoreWord(String reviewScoreWord) {
        this.reviewScoreWord = reviewScoreWord;
    }

    public int getReviewNumber() {
        return reviewNumber;
    }

    public void setReviewNumber(int reviewNumber) {
        this.reviewNumber = reviewNumber;
    }

    public String getAdditionals() {
        return additionals;
    }

    public void setAdditionals(String additionals) {
        this.additionals = additionals;
    }

    public String getMainPhotoUrl() {
        return mainPhotoUrl;
    }

    public void setMainPhotoUrl(String mainPhotoUrl) {
        this.mainPhotoUrl = mainPhotoUrl;
    }

    public String getMaxPhotoUrl() {
        return maxPhotoUrl;
    }

    public void setMaxPhotoUrl(String maxPhotoUrl) {
        this.maxPhotoUrl = maxPhotoUrl;
    }

    public String getPhotoUrl1440() {
        return PhotoUrl1440;
    }

    public void setPhotoUrl1440(String photoUrl1440) {
        PhotoUrl1440 = photoUrl1440;
    }

    public String getHotelUrl() {
        return hotelUrl;
    }

    public void setHotelUrl(String hotelUrl) {
        this.hotelUrl = hotelUrl;
    }
}
