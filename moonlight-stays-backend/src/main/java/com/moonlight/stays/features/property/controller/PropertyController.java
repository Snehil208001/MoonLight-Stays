package com.moonlight.stays.features.property.controller;

import com.moonlight.stays.exception.MoonlightException;
import com.moonlight.stays.features.property.model.Property;
import com.moonlight.stays.features.property.repository.PropertyRepository;
import com.moonlight.stays.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties Manager")
public class PropertyController {

    private final PropertyRepository propertyRepository;

    @GetMapping
    public ResponseEntity<List<Property>> getAllApprovedProperties() {
        return ResponseEntity.ok(propertyRepository.findByApprovedTrue());
    }

    @PostMapping
    public ResponseEntity<Property> createProperty(
            @RequestBody Property property,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        property.setHost(principal.getUser());
        Property saved = propertyRepository.save(property);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getPropertyById(@PathVariable Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new MoonlightException("Property not found with id " + id, HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(property);
    }
}
