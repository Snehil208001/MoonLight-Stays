package com.moonlight.project.airBnbApp.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long id;
    private Integer rating;
    private String content;
    private Long hotelId;
    private Long userId;
}