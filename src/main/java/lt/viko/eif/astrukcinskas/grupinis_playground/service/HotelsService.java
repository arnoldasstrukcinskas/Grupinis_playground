package lt.viko.eif.astrukcinskas.grupinis_playground.service;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.Hotel;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.HotelsRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HotelsService {

    @Autowired
    private HotelsRepository hotelsRepository;

    private List<HotelDto> hotels;

    /**
     * Adds all hotels from list to database
     * @param hotels hotels list
     * @return list of hotel data transfer objects
     */
    public List<HotelDto> addHotelsToDb(List<Hotel> hotels){

        List<HotelDto> hotelsDto = new ArrayList<>();

        for(Hotel hotel : hotels){
            HotelDto hotelDto = new HotelDto(hotel);
            hotelsDto.add(hotelDto);
        }

        hotelsRepository.saveAll(hotels);

        return hotelsDto;
    }


    /**
     * Removes hotel from database
     * @param id hotel id number
     * @return Message of success
     */
    public String removeHotelFromDb(int id) throws InvalidParameterException {

        if (!hotelsRepository.existsById(id)){
            throw new InvalidParameterException("Hotel service: such hotel do not exists");
        }

        hotelsRepository.deleteById(id);

        return "Hotel service: hotel with id: %d, deleted".formatted(id);
    }


    /**
     * Clears hotels from memory
     * @return Message about success
     */
    public String clearHotelsFromMemmory(){
        hotels = new ArrayList<>();

        return "Hotels service: Hotels cleared";
    }

//    Helpers

    /**
     * Converts hotel data transfer object to hotel
     * @param hotelDto data transfer object
     * @return hotel object
     */
    public Hotel converterDtoToHotel(HotelDto hotelDto){
        Hotel hotel = new Hotel();

        hotel.setHotelName(hotelDto.getHotelName());
        hotel.setAccomodationType(hotelDto.getAccomodationType());
        hotel.setHotelStars(hotelDto.getHotelStars());
        hotel.setDistrict(hotelDto.getDistrict());
        hotel.setDistanceToCenter(hotelDto.getDistanceToCenter());
        hotel.setBeachFront(hotelDto.isBeachFront());
        hotel.setPrice(hotelDto.getPrice());
        hotel.setPriceAllInclusive(hotelDto.getPriceAllInclusive());
        hotel.setAddress(hotelDto.getAddress());
        hotel.setMainPhotoUrl(hotelDto.getMainPhotoUrl());
        hotel.setPhotoUrl1440(hotelDto.getPhotoUrl1440());
        hotel.setHotelUrl(hotelDto.getHotelUrl());

        return hotel;
    }

    /**
     * Gets hotels from memmory
     * @return list of hotel data transfer object
     */
    public List<HotelDto> getHotels() {
        return hotels;
    }

    /**
     * Adds list of hotels to memory
     * @param hotels list og hotel data transfer objects
     */
    public void setHotels(List<HotelDto> hotels) {
        this.hotels = hotels;
    }
}
