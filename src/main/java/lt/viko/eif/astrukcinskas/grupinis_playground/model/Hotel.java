package lt.viko.eif.astrukcinskas.grupinis_playground.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;

@Entity
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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
    private String mainPhotoUrl; //main_photo_url
    private String maxPhotoUrl;
    private String PhotoUrl1440;
    private String hotelUrl;

    //Review section
    private double reviewScoreNumber;
    private String reviewScoreWord;
    private int reviewNumber;
    private String additionals; //ribon_text

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getDistanceToCenter() {
        return distanceToCenter;
    }

    public void setDistanceToCenter(String distanceToCenter) {
        this.distanceToCenter = distanceToCenter;
    }

    public boolean isBeachFront() {
        return isBeachFront;
    }

    public void setBeachFront(boolean beachFront) {
        isBeachFront = beachFront;
    }

    public String getPriceAllInclusive() {
        return priceAllInclusive;
    }

    public void setPriceAllInclusive(String priceAllInclusive) {
        this.priceAllInclusive = priceAllInclusive;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
}
