package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class AnnouncementVo {
    private String id;
    private String title;
    private String description;
    private String createDate;
}