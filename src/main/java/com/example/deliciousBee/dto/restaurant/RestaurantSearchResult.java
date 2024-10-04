package com.example.deliciousBee.dto.restaurant;

import org.springframework.data.domain.Page;

// RestaurantSearchResult.java
public class RestaurantSearchResult {
    private Page<RestaurantDto> restaurants;
    private Double finalRadius;

    public RestaurantSearchResult(Page<RestaurantDto> restaurants, Double finalRadius) {
        this.restaurants = restaurants;
        this.finalRadius = finalRadius;
    }

    public Page<RestaurantDto> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Page<RestaurantDto> restaurants) {
        this.restaurants = restaurants;
    }

    public Double getFinalRadius() {
        return finalRadius;
    }

    public void setFinalRadius(Double finalRadius) {
        this.finalRadius = finalRadius;
    }
}

