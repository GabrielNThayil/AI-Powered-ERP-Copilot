package com.example.erp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse {

    private Object data;
    private String message;
    private int status;
    private long timestamp = System.currentTimeMillis();
}