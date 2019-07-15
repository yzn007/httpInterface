package com.springboot.demo.dao;

import com.springboot.demo.entity.Route;
import com.springboot.demo.entity.Token;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yzn00 on 2019/6/19.
 */
@Repository
@Mapper
public interface RouteMapper {
     List<Route> getAllRoute();
}
