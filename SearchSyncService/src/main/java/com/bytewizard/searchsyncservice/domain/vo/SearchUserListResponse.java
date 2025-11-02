package com.bytewizard.searchsyncservice.domain.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class SearchUserListResponse implements Serializable {


    private Long userId;

    private String avatar;

    private String nickname;

    private String description;

    private Integer followers;

    private Integer videoCount;

}