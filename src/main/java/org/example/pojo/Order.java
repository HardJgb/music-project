package org.example.pojo;

import lombok.Data;

@Data
public class Order {
    private Integer orderId;
    private String orderStatus;
    private double orderPrice;
    private String orderDate;
    private String orderDesc;
    private int customerId;
    private Customer customer;

}
