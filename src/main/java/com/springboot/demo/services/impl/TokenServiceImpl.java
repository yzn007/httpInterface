package com.springboot.demo.services.impl;

import com.springboot.demo.dao.TokenMapper;
import com.springboot.demo.entity.Token;
import com.springboot.demo.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yzn00 on 2019/7/11.
 */
@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    TokenMapper tokenMapper;
    @Override
    public List<Token> getAllToken() {
        return tokenMapper.getAllToken();
    }
}
