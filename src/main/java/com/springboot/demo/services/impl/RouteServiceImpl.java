package com.springboot.demo.services.impl;

import com.springboot.demo.dao.RouteMapper;
import com.springboot.demo.entity.Route;
import com.springboot.demo.services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yzn00 on 2019/7/11.
 */
@Service
public class RouteServiceImpl implements RouteService {
    @Autowired
    RouteMapper routeMapper;

    @Override
    public List<Route> getAllRoute() {
        return routeMapper.getAllRoute();
    }
}
