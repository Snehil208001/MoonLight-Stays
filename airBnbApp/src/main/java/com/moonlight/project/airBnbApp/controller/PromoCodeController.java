package com.moonlight.project.airBnbApp.controller;

import com.moonlight.project.airBnbApp.dto.PromoCodeDto;
import com.moonlight.project.airBnbApp.entity.PromoCode;
import com.moonlight.project.airBnbApp.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeRepository promoCodeRepository;

    /** Public endpoint: list all active promo codes for users */
    @GetMapping("/promocodes")
    public ResponseEntity<List<PromoCodeDto>> getActivePromoCodes() {
        List<PromoCodeDto> promos = promoCodeRepository.findByActiveTrue().stream()
                .map(p -> new PromoCodeDto(p.getCode(), p.getDiscountPercentage()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(promos);
    }

    @GetMapping("/admin/promocodes")
    public ResponseEntity<List<PromoCode>> getAllPromoCodes() {
        return ResponseEntity.ok(promoCodeRepository.findAll());
    }

    @PostMapping("/admin/promocodes")
    public ResponseEntity<PromoCode> createPromoCode(@RequestBody PromoCode promoCode) {
        promoCode.setCode(promoCode.getCode() != null ? promoCode.getCode().trim().toUpperCase() : "");
        if (promoCode.getActive() == null) promoCode.setActive(true);
        return ResponseEntity.ok(promoCodeRepository.save(promoCode));
    }

    @DeleteMapping("/admin/promocodes/{id}")
    public ResponseEntity<Void> deletePromoCode(@PathVariable Long id) {
        if (!promoCodeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        promoCodeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Public endpoint: validate promo code and return discount percentage */
    @GetMapping("/promocodes/validate")
    public ResponseEntity<Map<String, Object>> validatePromoCode(@RequestParam String code) {
        String sanitized = code != null ? code.trim().replaceAll("['\"]+$|^['\"]+", "").trim() : "";
        return promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue(sanitized)
                .map(pc -> ResponseEntity.ok(Map.<String, Object>of(
                        "valid", true,
                        "code", pc.getCode(),
                        "discountPercentage", pc.getDiscountPercentage()
                )))
                .orElse(ResponseEntity.ok(Map.of("valid", false)));
    }
}