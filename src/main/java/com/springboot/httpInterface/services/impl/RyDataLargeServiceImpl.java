package com.springboot.httpInterface.services.impl;

import com.springboot.httpInterface.dao.RyDataLargeMapper;
import com.springboot.httpInterface.entity.RyDataLarge;
import com.springboot.httpInterface.services.RyDataLargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yzn00 on 2019/8/1.
 */
@Service
public class RyDataLargeServiceImpl implements RyDataLargeService {


    @Autowired
    RyDataLargeMapper ryDataLargeMapper;
    @Override
    public List<RyDataLarge> getRyDataById(Map Id) {
        return ryDataLargeMapper.getRyDataById(Id);
    }

    @Override
    public List<Map> getAllRoute(Map map) {
        return ryDataLargeMapper.getAllRoute(map);
    }

    @Override
    public List<Map> getAllPark(Map map) {
        return ryDataLargeMapper.getAllPark(map);
    }

    @Override
    public List<Map> getWebServiceData(Map m) {
        return ryDataLargeMapper.getWebServiceData(m);
    }

    @Override
    public void updateWebService(Map m) {
        ryDataLargeMapper.updateWebService(m);
    }
}
