package lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
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

//    @JsonProperty("hotel_name")
    private String hotelName;

//    @JsonProperty("accommodation_type_name")
    private String accomodationType;

//    @JsonProperty("class")
    private int hotelStars;

//    @JsonProperty("district")
    private String district;

//    @JsonProperty("distance_to_cc_formatted")
    private String distanceToCenter;

//    @JsonProperty("is_beach_front")
    private boolean isBeachFront;

//    @JsonProperty("amount_unrounded")
    private String price;

//    @JsonProperty("amount_unrounded")// url
    private String priceAllInclusive;

//    @JsonProperty("address")
    private String address;

//    @JsonProperty("main_photo_url")
    private String mainPhotoUrl; //main_photo_url

//    @JsonProperty("max_photo_url")
    private String maxPhotoUrl;

//    @JsonProperty("max_1440_photo_url")
    private String PhotoUrl1440;

//    @JsonProperty("url")
    private String hotelUrl;

    //Review section
//    @JsonProperty("review_score")
    private double reviewScoreNumber;

//    @JsonProperty("review_score_word")
    private String reviewScoreWord;

//    @JsonProperty("review_nr")
    private int reviewNumber;

//    @JsonProperty("ribbon_text")
    private String additionals; //ribon_text


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
