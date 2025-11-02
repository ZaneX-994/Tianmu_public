package com.bytewizard.videoactionservice.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryListResponse implements Serializable {

    private int categoryId;

    private String categoryName;
}