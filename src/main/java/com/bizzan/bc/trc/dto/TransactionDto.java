package com.bizzan.bc.trc.dto;

import lombok.Data;

@Data
public class TransactionDto {

    private String toAddress;
    private String fromAddress;
    private Long amount;
    private String privateKey;
    private String memo;
}
