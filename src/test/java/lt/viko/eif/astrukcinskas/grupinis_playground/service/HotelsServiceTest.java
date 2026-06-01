package lt.viko.eif.astrukcinskas.grupinis_playground.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.springframework.test.util.ReflectionTestUtils;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.Hotel;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.HotelsRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;

/**
 * Unit tests for hotel conversion, persistence delegation, and in-memory hotel cache behavior.
 */
class HotelsServiceTest {

    private HotelsRepository hotelsRepository;
    private HotelsService hotelsService;

    @BeforeEach
    void setUp() {
        hotelsRepository = mock(HotelsRepository.class);
        hotelsService = new HotelsService();
        ReflectionTestUtils.setField(hotelsService, "hotelsRepository", hotelsRepository);
    }

    @Test
    void converterDtoToHotelCopiesCoreFields() {
        HotelDto hotelDto = sampleHotelDto();

        Hotel hotel = hotelsService.converterDtoToHotel(hotelDto);

        assertEquals("Catalonia Sagrada Familia", hotel.getHotelName());
        assertEquals("Hotel", hotel.getAccomodationType());
        assertEquals(3, hotel.getHotelStars());
        assertEquals("Sant Marti", hotel.getDistrict());
        assertEquals("2.6 km", hotel.getDistanceToCenter());
        assertEquals("481.20", hotel.getPrice());
        assertEquals("481.20", hotel.getPriceAllInclusive());
        assertEquals("Arago 577-579", hotel.getAddress());
        assertEquals("main.jpg", hotel.getMainPhotoUrl());
        assertEquals("photo1440.jpg", hotel.getPhotoUrl1440());
        assertEquals("hotel-url", hotel.getHotelUrl());
    }

    @Test
    void converterHotelToDtoCopiesCoreFields() {
        Hotel hotel = sampleHotel();

        HotelDto hotelDto = hotelsService.converterHotelToDto(hotel);

        assertEquals("Catalonia Sagrada Familia", hotelDto.getHotelName());
        assertEquals("Hotel", hotelDto.getAccomodationType());
        assertEquals(3, hotelDto.getHotelStars());
        assertEquals("Sant Marti", hotelDto.getDistrict());
        assertEquals("2.6 km", hotelDto.getDistanceToCenter());
        assertEquals("481.20", hotelDto.getPrice());
        assertEquals("481.20", hotelDto.getPriceAllInclusive());
        assertEquals("Arago 577-579", hotelDto.getAddress());
        assertEquals("main.jpg", hotelDto.getMainPhotoUrl());
        assertEquals("photo1440.jpg", hotelDto.getPhotoUrl1440());
        assertEquals("hotel-url", hotelDto.getHotelUrl());
    }

    @Test
    void addHotelsToDbSavesHotelsAndReturnsDtos() {
        Hotel hotel = sampleHotel();

        List<HotelDto> response = hotelsService.addHotelsToDb(List.of(hotel));

        verify(hotelsRepository).saveAll(List.of(hotel));
        assertEquals(1, response.size());
        assertEquals("Catalonia Sagrada Familia", response.get(0).getHotelName());
    }

    @Test
    void removeHotelFromDbDeletesById() {
        String response = hotelsService.removeHotelFromDb(7);

        verify(hotelsRepository).deleteById(7);
        assertEquals("Hotel service: hotel with id: 7, deleted", response);
    }

    @Test
    void clearHotelsFromMemmoryEmptiesStoredHotels() {
        hotelsService.setHotels(List.of(sampleHotelDto()));

        String response = hotelsService.clearHotelsFromMemmory();

        assertEquals("Hotels service: Hotels cleared", response);
        assertTrue(hotelsService.getHotels().isEmpty());
    }

    /** 
     * @return HotelDto
     */
    private HotelDto sampleHotelDto() {
        HotelDto hotelDto = new HotelDto();
        hotelDto.setHotelName("Catalonia Sagrada Familia");
        hotelDto.setAccomodationType("Hotel");
        hotelDto.setHotelStars(3);
        hotelDto.setDistrict("Sant Marti");
        hotelDto.setDistanceToCenter("2.6 km");
        hotelDto.setBeachFront(false);
        hotelDto.setPrice("481.20");
        hotelDto.setPriceAllInclusive("481.20");
        hotelDto.setAddress("Arago 577-579");
        hotelDto.setMainPhotoUrl("main.jpg");
        hotelDto.setPhotoUrl1440("photo1440.jpg");
        hotelDto.setHotelUrl("hotel-url");
        return hotelDto;
    }

    /** 
     * @return Hotel
     */
    private Hotel sampleHotel() {
        Hotel hotel = new Hotel();
        hotel.setHotelName("Catalonia Sagrada Familia");
        hotel.setAccomodationType("Hotel");
        hotel.setHotelStars(3);
        hotel.setDistrict("Sant Marti");
        hotel.setDistanceToCenter("2.6 km");
        hotel.setBeachFront(false);
        hotel.setPrice("481.20");
        hotel.setPriceAllInclusive("481.20");
        hotel.setAddress("Arago 577-579");
        hotel.setMainPhotoUrl("main.jpg");
        hotel.setPhotoUrl1440("photo1440.jpg");
        hotel.setHotelUrl("hotel-url");
        return hotel;
    }
}
