package com.tanhua.commons.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 环信用户信息结构
 */
@Data
@AllArgsConstructor
public class HuanXinUser implements Serializable {

    private String username;
    private String password;
    private String nickname;
}