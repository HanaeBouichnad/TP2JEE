package org.sid.billingservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.billingservice.model.Customer;
import org.sid.billingservice.model.Product;

import java.util.Date;
import java.util.List;

@Entity @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {
    @Id @GeneratedValue
    private Long id;
    private Date billingDate;

    @OneToMany(mappedBy = "bill")
    private List<ProductItem> productItems;

    private Long customerId;
    @Transient
    private Customer customer;
}
