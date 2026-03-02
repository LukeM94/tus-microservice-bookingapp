package com.tus.bookingapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tus.bookingapp.controller.EventController;
import com.tus.bookingapp.domain.Event;
import com.tus.bookingapp.dto.DtoMapper;
import com.tus.bookingapp.dto.EventDTO;
import com.tus.bookingapp.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(EventController.class)
@Import(DtoMapper.class)
class BookingappApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEvent_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        EventDTO invalidEvent = new EventDTO();
        invalidEvent.setArtist("");
        invalidEvent.setShowTime(LocalDateTime.now().minusDays(1));
        invalidEvent.setVenueId(null);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed for one or more fields."))
                .andExpect(jsonPath("$.errors.artist").exists())
                .andExpect(jsonPath("$.errors.showTime").exists())
                .andExpect(jsonPath("$.errors.venueId").exists());
    }

    @Test
    void createEvent_WithValidData_ShouldReturnCreated() throws Exception {
        EventDTO validEvent = new EventDTO();
        validEvent.setArtist("Valid Artist");
        validEvent.setVenueId(1L);
        validEvent.setVenueType("Stadium");
        validEvent.setShowTime(LocalDateTime.now().plusDays(10));

        when(bookingService.createEvent(any(Event.class))).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            e.setUuid(java.util.UUID.randomUUID());
            return e;
        });

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEvent)))
                .andExpect(status().isCreated());
    }
}
