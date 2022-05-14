package com.gmail.scanner.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EfoodOrder {

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime orderDate;
  private long orderNumber;
  private String price;

  public LocalDateTime getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDateTime orderDate) {
    this.orderDate = orderDate;
  }

  public long getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(long orderNumber) {
    this.orderNumber = orderNumber;
  }

  public String getPrice() {
    return price;
  }

  public void setPrice(String price) {
    this.price = price;
  }
}
