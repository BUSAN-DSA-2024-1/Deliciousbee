package com.example.deliciousBee.dto.restaurant;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

public class RestaurantSearchResponse {
    private PagedModel<EntityModel<RestaurantDto>> restaurants;
    private Double radius;

    public RestaurantSearchResponse(PagedModel<EntityModel<RestaurantDto>> restaurants, Double radius) {
        this.restaurants = restaurants;
        this.radius = radius;
    }

    public PagedModel<EntityModel<RestaurantDto>> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(PagedModel<EntityModel<RestaurantDto>> restaurants) {
        this.restaurants = restaurants;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }
}