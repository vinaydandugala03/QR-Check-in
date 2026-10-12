package com.eventticketing.dto;

import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class BulkSeatCreationRequest {

    @NotNull(message = "Venue ID is required")
    private Long venueId;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Row start (rowFrom) is required")
    @Pattern(regexp = "^[a-zA-Z]$", message = "rowFrom must be a single letter from A to Z")
    private String rowFrom;

    @NotBlank(message = "Row end (rowTo) is required")
    @Pattern(regexp = "^[a-zA-Z]$", message = "rowTo must be a single letter from A to Z")
    private String rowTo;

    @NotNull(message = "Seats per row is required")
    @Positive(message = "seatsPerRow must be greater than zero")
    private Integer seatsPerRow;

    @NotNull(message = "Price is required")
    @Positive(message = "price must be greater than zero")
    private Double price;

    public BulkSeatCreationRequest() {
    }

    public BulkSeatCreationRequest(Long venueId, String category, String rowFrom, String rowTo, Integer seatsPerRow, Double price) {
        this.venueId = venueId;
        this.category = category;
        this.rowFrom = rowFrom;
        this.rowTo = rowTo;
        this.seatsPerRow = seatsPerRow;
        this.price = price;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRowFrom() {
        return rowFrom;
    }

    public void setRowFrom(String rowFrom) {
        this.rowFrom = rowFrom;
    }

    public String getRowTo() {
        return rowTo;
    }

    public void setRowTo(String rowTo) {
        this.rowTo = rowTo;
    }

    public Integer getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(Integer seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulkSeatCreationRequest that = (BulkSeatCreationRequest) o;
        return Objects.equals(venueId, that.venueId) &&
                Objects.equals(category, that.category) &&
                Objects.equals(rowFrom, that.rowFrom) &&
                Objects.equals(rowTo, that.rowTo) &&
                Objects.equals(seatsPerRow, that.seatsPerRow) &&
                Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(venueId, category, rowFrom, rowTo, seatsPerRow, price);
    }
}