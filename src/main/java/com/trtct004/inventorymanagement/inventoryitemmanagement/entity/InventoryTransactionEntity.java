package com.trtct004.inventorymanagement.inventoryitemmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "invtrn", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "erseq")
    private Long sequenceId;

    @Column(name = "trtrid", nullable = false)
    private Long transactionId;

    @Column(name = "tritmid", length = 8, nullable = false)
    private String itemId;

    @Column(name = "trtype", length = 2, nullable = false)
    private String transactionType;

    @Column(name = "trqty", nullable = false)
    private Integer quantity;

    @Column(name = "trdate", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "truser", length = 10)
    private String userId;

    @Column(name = "trref", length = 20)
    private String reference;
}
