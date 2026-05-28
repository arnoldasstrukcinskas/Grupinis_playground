package lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO;

public class RequestDto {

    private String hobbiesAndInterests;
    private String promptToOllama;
    private String locale = "en-gb";
    private String destinationType = "city";
    private int destinationId;
    private String checkInDate;
    private String checkOutDate;
    private int roomNumber;
    private int adultsNumber;
    private String filterByCurrency = "EUR";
    private String orderBy = "popularity";
    private String units = "metric";
    private Boolean includeAdjency = false;

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getPromptToOllama() {
        return promptToOllama;
    }

    public void setPromptToOllama(String promptToOllama) {
        this.promptToOllama = promptToOllama;
    }

    public String getHobbiesAndInterests() {
        return hobbiesAndInterests;
    }

    public void setHobbiesAndInterests(String hobbiesAndInterests) {
        this.hobbiesAndInterests = hobbiesAndInterests;
    }

    public String getLocale() {
        return locale;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public String getFilterByCurrency() {
        return filterByCurrency;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getUnits() {
        return units;
    }

    public Boolean getIncludeAdjency() {
        return includeAdjency;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getAdultsNumber() {
        return adultsNumber;
    }

    public void setAdultsNumber(int adultsNumber) {
        this.adultsNumber = adultsNumber;
    }


//    For testing
    @Override
    public String toString() {
        return "RequestDto{" +
                "locale='" + locale + '\'' +
                ", destinationType='" + destinationType + '\'' +
                ", destinationId=" + destinationId +
                ", checkInDate='" + checkInDate + '\'' +
                ", checkOutDate='" + checkOutDate + '\'' +
                ", roomNumber=" + roomNumber +
                ", adultsNumber=" + adultsNumber +
                ", filterByCurrency='" + filterByCurrency + '\'' +
                ", orderBy='" + orderBy + '\'' +
                ", units='" + units + '\'' +
                ", includeAdjency=" + includeAdjency +
                '}';
    }
}
