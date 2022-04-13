package com.tanhua.domain.db;

import lombok.Data;

@Data
public class Announcement extends BasePojo {
    private Integer id;
    private String title;
    private String description;
}