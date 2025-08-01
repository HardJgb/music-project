package org.example.pojo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * @TableName customer
 */
@Data
public class Customer implements Serializable {
    private Integer customerId;

    private String customerName;

    private String customerPhone;

    private String customerAddress;

    private String customerContact;

    private String customerOther;

    private List<Order> orderList;
}