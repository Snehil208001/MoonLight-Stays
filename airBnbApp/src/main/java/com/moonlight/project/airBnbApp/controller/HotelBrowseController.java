package com.moonlight.project.airBnbApp.controller;

import com.moonlight.project.airBnbApp.dto.HotelInfoDto;
import com.moonlight.project.airBnbApp.dto.HotelPriceDto;
import com.moonlight.project.airBnbApp.dto.HotelSearchRequest;
import com.moonlight.project.airBnbApp.dto.RoomPriceDto;
import com.moonlight.project.airBnbApp.service.HotelService;
import com.moonlight.project.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/Search")
    public ResponseEntity<List<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest) {
        var page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page.getContent());
    }

    /** GET variant for search - supports query params for clients that use GET */
    @GetMapping("/search")
    public ResponseEntity<List<HotelPriceDto>> searchHotelsGet(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "1") Integer roomsCount,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        HotelSearchRequest req = new HotelSearchRequest();
        req.setCity(city);
        LocalDate ci = checkInDate != null ? checkInDate : LocalDate.now();
        LocalDate end = endDate != null ? endDate : ci.plusDays(1);
        req.setCheckInDate(ci);
        req.setEndDate(end);
        req.setRoomsCount(roomsCount != null ? roomsCount : 1);
        req.setPage(page != null ? page : 0);
        req.setSize(size != null ? size : 10);
        var result = inventoryService.searchHotels(req);
        return ResponseEntity.ok(result.getContent());
    }

    /** POST variant for search - browsers don't support GET with body */
    @PostMapping("/search")
    public ResponseEntity<List<HotelPriceDto>> searchHotelsPost(@RequestBody HotelSearchRequest hotelSearchRequest) {
        var page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }

    @GetMapping("/{hotelId}/room-prices")
    public ResponseEntity<List<RoomPriceDto>> getRoomPrices(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") Integer roomsCount) {
        return ResponseEntity.ok(inventoryService.getRoomPricesForHotel(hotelId, checkIn, checkOut, roomsCount));
    }
}
