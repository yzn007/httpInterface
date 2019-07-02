package com.springboot.demo.services;

import com.springboot.demo.entity.Person;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yzn00 on 2019/6/19.
 */

public interface PersonService {
    public int insertPerson();
    public int updatePerson();
    public List<Person>selectAllPerson();
}
