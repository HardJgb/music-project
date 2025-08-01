package org.example.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * @TableName product
 */
@Data
public class Product implements Serializable {
    private Integer productId;

    private String productName;

    private String productImage;

    private String productType;

    private Double productPrice;

    private String productDEsc;

    private static final long serialVersionUID = 1L;
}