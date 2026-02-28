package com.moonlight.project.airBnbApp.repository;

import com.moonlight.project.airBnbApp.dto.HotelPriceDto;
import com.moonlight.project.airBnbApp.entity.Hotel;
import com.moonlight.project.airBnbApp.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {

    @Query("""
            SELECT NEW com.moonlight.project.airBnbApp.dto.HotelPriceDto(i.hotel, AVG(i.price))
            FROM HotelMinPrice i
            WHERE LOWER(TRIM(i.hotel.city)) = LOWER(TRIM(:city))
                AND i.date BETWEEN :startDate AND :endDate
                AND i.hotel.active = true
                AND (:roomType IS NULL OR EXISTS (SELECT 1 FROM Room r WHERE r.hotel = i.hotel AND r.types = :roomType))
                AND (:amenity IS NULL OR CAST(function('array_to_string', i.hotel.amenities, ',') AS String) LIKE CONCAT('%', CAST(:amenity AS String), '%'))
            GROUP BY i.hotel
            HAVING COUNT(i.date) = :dateCount
                AND (:minPrice IS NULL OR AVG(i.price) >= :minPrice)
                AND (:maxPrice IS NULL OR AVG(i.price) <= :maxPrice)
            """)
    Page<HotelPriceDto> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Integer dateCount,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("roomType") String roomType,
            @Param("amenity") String amenity,
            Pageable pageable
    );

    @Query("""
            SELECT NEW com.moonlight.project.airBnbApp.dto.HotelPriceDto(i.hotel, AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.date BETWEEN :startDate AND :endDate
                AND i.hotel.active = true
                AND (:roomType IS NULL OR EXISTS (SELECT 1 FROM Room r WHERE r.hotel = i.hotel AND r.types = :roomType))
                AND (:amenity IS NULL OR CAST(function('array_to_string', i.hotel.amenities, ',') AS String) LIKE CONCAT('%', CAST(:amenity AS String), '%'))
            GROUP BY i.hotel
            HAVING COUNT(i.date) = :dateCount
                AND (:minPrice IS NULL OR AVG(i.price) >= :minPrice)
                AND (:maxPrice IS NULL OR AVG(i.price) <= :maxPrice)
            """)
    Page<HotelPriceDto> findHotelsWithAvailableInventoryAllCities(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Integer dateCount,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("roomType") String roomType,
            @Param("amenity") String amenity,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}