package com.springboot.demo.services;

import com.springboot.demo.entity.Token;

import java.util.List;

/**
 * Created by yzn00 on 2019/7/11.
 */
public interface TokenService {
    List<Token> getAllToken();
}
