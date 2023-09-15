package Ecommerce.model;

import java.io.Serializable;

public class Product implements Serializable {

    private String productId;
    private String name;
    private String inventoryStatus;
    private double mrpPrice;
    private double discount;
    private Integer maxQuantity;

    // Constructors, getters, setters, and other methods

    public Product(String productId, String name, String inventoryStatus, double mrpPrice, double discount, Integer maxQuantity) {
        this.productId = productId;
        this.name = name;
        this.inventoryStatus = inventoryStatus;
        this.mrpPrice = mrpPrice;
        this.discount = discount;
        this.maxQuantity = maxQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInventoryStatus() {
        return inventoryStatus;
    }

    public void setInventoryStatus(String inventoryStatus) {
        this.inventoryStatus = inventoryStatus;
    }

    public double getMrpPrice() {
        return mrpPrice;
    }

    public void setMrpPrice(double mrpPrice) {
        this.mrpPrice = mrpPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }
}
