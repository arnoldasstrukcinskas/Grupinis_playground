package lt.viko.eif.astrukcinskas.grupinis_playground.service;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.Hotel;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.HotelsRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HotelsService {

    @Autowired
    private HotelsRepository hotelsRepository;

    private List<HotelDto> hotels;

    public List<HotelDto> addHotelsToDb(List<Hotel> hotels){
        List<HotelDto> hotelsDto = new ArrayList<>();

        for(Hotel hotel : hotels){
            HotelDto hotelDto = new HotelDto(hotel);
            hotelsDto.add(hotelDto);
        }

        hotelsRepository.saveAll(hotels);

        return hotelsDto;
    }

    public String removeHotelFromDb(int id){
        hotelsRepository.deleteById(id);

        return "Hotel service: hotel with id: %d, deleted".formatted(id);
    }

    public String clearHotelsFromMemmory(){
        hotels = new ArrayList<>();

        return "Hotels service: Hotels cleared";
    }

//    Helpers
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

    public HotelDto converterHotelToDto(Hotel hotel) {
        HotelDto hotelDto = new HotelDto();

        hotelDto.setHotelName(hotel.getHotelName());
        hotelDto.setAccomodationType(hotel.getAccomodationType());
        hotelDto.setHotelStars(hotel.getHotelStars());
        hotelDto.setDistrict(hotel.getDistrict());
        hotelDto.setDistanceToCenter(hotel.getDistanceToCenter());
        hotelDto.setBeachFront(hotel.isBeachFront());
        hotelDto.setPrice(hotel.getPrice());
        hotelDto.setPriceAllInclusive(hotel.getPriceAllInclusive());
        hotelDto.setAddress(hotel.getAddress());
        hotelDto.setMainPhotoUrl(hotel.getMainPhotoUrl());
        hotelDto.setPhotoUrl1440(hotel.getPhotoUrl1440());
        hotelDto.setHotelUrl(hotel.getHotelUrl());

        return hotelDto;
    }

    public List<HotelDto> getHotels() {
        return hotels;
    }

    public void setHotels(List<HotelDto> hotels) {
        this.hotels = hotels;
    }
}
