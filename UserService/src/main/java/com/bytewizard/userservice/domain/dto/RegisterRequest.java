package com.bytewizard.userservice.domain.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String account;

    private String password;

    private String code;

    private String nickname;
}
