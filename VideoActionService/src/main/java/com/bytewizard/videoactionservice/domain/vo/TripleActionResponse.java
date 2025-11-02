package com.bytewizard.videoactionservice.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TripleActionResponse implements Serializable {

    private Long LikeId;

    private boolean coin;

    private Long favoriteId;
}
