package com.moonlight.project.airBnbApp.strategy;

import com.moonlight.project.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;

    // Comprehensive list of major Indian holidays & festivals for 2026
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            // January
            LocalDate.of(2026, 1, 1),   // New Year's Day
            LocalDate.of(2026, 1, 14),  // Makar Sankranti / Pongal
            LocalDate.of(2026, 1, 26),  // Republic Day

            // February
            LocalDate.of(2026, 2, 15),  // Maha Shivaratri

            // March
            LocalDate.of(2026, 3, 3),   // Holika Dahan
            LocalDate.of(2026, 3, 4),   // Holi
            LocalDate.of(2026, 3, 20),  // Eid-ul-Fitr (Tentative)
            LocalDate.of(2026, 3, 31),  // Mahavir Jayanti

            // April
            LocalDate.of(2026, 4, 3),   // Good Friday
            LocalDate.of(2026, 4, 14),  // Ambedkar Jayanti / Vaisakhi

            // May & June
            LocalDate.of(2026, 5, 27),  // Eid al-Adha (Bakrid - Tentative)
            LocalDate.of(2026, 6, 26),  // Muharram (Tentative)

            // August
            LocalDate.of(2026, 8, 15),  // Independence Day
            LocalDate.of(2026, 8, 28),  // Raksha Bandhan

            // September
            LocalDate.of(2026, 9, 4),   // Janmashtami
            LocalDate.of(2026, 9, 14),  // Ganesh Chaturthi

            // October
            LocalDate.of(2026, 10, 2),  // Mahatma Gandhi Jayanti
            LocalDate.of(2026, 10, 18), // Maha Navami
            LocalDate.of(2026, 10, 19), // Dussehra (Vijayadashami)

            // November
            LocalDate.of(2026, 11, 8),  // Diwali / Deepavali
            LocalDate.of(2026, 11, 10), // Bhai Dooj
            LocalDate.of(2026, 11, 13), // Chhath Puja (Massive travel demand)
            LocalDate.of(2026, 11, 24), // Guru Nanak Jayanti

            // December
            LocalDate.of(2026, 12, 25), // Christmas Day
            LocalDate.of(2026, 12, 31)  // New Year's Eve
    );

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        LocalDate date = inventory.getDate();

        // Check if it's a known holiday or a weekend
        boolean isHolidayOrWeekend = HOLIDAYS.contains(date) ||
                date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY;

        if (isHolidayOrWeekend) {
            price = price.multiply(BigDecimal.valueOf(1.25)); // 25% price surge
        }
        return price;
    }
}