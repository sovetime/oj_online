package org.example.ojsystem.controller;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 15:53
 */
@Data
public class LoginResult {

    private int code; //1-成功 0-失败
    private String msg;
}
