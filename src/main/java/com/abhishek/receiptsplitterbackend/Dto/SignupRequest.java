package com.abhishek.receiptsplitterbackend.Dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String name;
}
