package com.smalaca.rentalapplication.infrastructure.rest.api.booking;

import com.google.common.collect.ImmutableMap;
import com.smalaca.rentalapplication.application.apartment.ApartmentBookingDto;
import com.smalaca.rentalapplication.application.apartment.ApartmentDto;
import com.smalaca.rentalapplication.infrastructure.json.JsonFactory;
import com.smalaca.rentalapplication.infrastructure.persistence.jpa.apartment.SpringJpaApartmentTestRepository;
import com.smalaca.rentalapplication.infrastructure.persistence.jpa.apartmentbookinghistory.SpringJpaApartmentBookingHistoryTestRepository;
import com.smalaca.rentalapplication.infrastructure.persistence.jpa.booking.SpringJpaBookingTestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("SystemTest")
class BookingRestControllerSystemTest {
    private static final String OWNER_ID = "1234";
    private static final String STREET = "Florianska";
    private static final String POSTAL_CODE = "12-345";
    private static final String HOUSE_NUMBER = "1";
    private static final String APARTMENT_NUMBER = "13";
    private static final String CITY = "Cracow";
    private static final String COUNTRY = "Poland";
    private static final String DESCRIPTION = "Nice place to stay";
    private static final Map<String, Double> SPACES_DEFINITION = ImmutableMap.of("Toilet", 10.0, "Bedroom", 30.0);
    
    private final JsonFactory jsonFactory = new JsonFactory();
    private final List<String> apartmentIds = new ArrayList<>();
    private final List<String> bookingIds = new ArrayList<>();

    @Autowired private MockMvc mockMvc;
    @Autowired private SpringJpaApartmentTestRepository apartmentRepository;
    @Autowired private SpringJpaApartmentBookingHistoryTestRepository apartmentBookingHistoryRepository;
    @Autowired private SpringJpaBookingTestRepository bookingRepository;

    @AfterEach
    void deleteBookings() {
        apartmentRepository.deleteAll(apartmentIds);
        apartmentBookingHistoryRepository.deleteAll(apartmentIds);
        bookingRepository.deleteAll(bookingIds);
    }
    
    @Test
    void shouldRejectBooking() throws Exception {
        String url = getUrlToExistingBooking().replace("booking/", "booking/reject/");

        mockMvc.perform(put(url)).andExpect(status().isOk());
    }

    @Test
    void shouldAcceptBooking() throws Exception {
        String url = getUrlToExistingBooking().replace("booking/", "booking/accept/");

        mockMvc.perform(put(url)).andExpect(status().isOk());
    }

    private String getUrlToExistingBooking() throws Exception {
        String url = save(givenApartment()).getResponse().getRedirectedUrl();
        String apartmentId = url.replace("/apartment/", "");
        apartmentIds.add(apartmentId);
        ApartmentBookingDto apartmentBookingDto = new ApartmentBookingDto(apartmentId, "1357", LocalDate.of(2020, 11, 12), LocalDate.of(2020, 12, 1));

        MvcResult mvcResult = mockMvc.perform(put(url.replace("apartment/", "apartment/book/")).contentType(MediaType.APPLICATION_JSON).content(jsonFactory.create(apartmentBookingDto)))
                .andExpect(status().isCreated())
                .andReturn();
        storeBookingId(mvcResult);
        return mvcResult.getResponse().getRedirectedUrl();
    }

    private void storeBookingId(MvcResult result) {
        bookingIds.add(result.getResponse().getRedirectedUrl().replace("/booking/", ""));
    }

    private ApartmentDto givenApartment() {
        return new ApartmentDto(OWNER_ID, STREET, POSTAL_CODE, HOUSE_NUMBER, APARTMENT_NUMBER, CITY, COUNTRY, DESCRIPTION, SPACES_DEFINITION);
    }

    private MvcResult save(ApartmentDto apartmentDto) throws Exception {
        return mockMvc.perform(post("/apartment").contentType(MediaType.APPLICATION_JSON).content(jsonFactory.create(apartmentDto))).andReturn();
    }
}