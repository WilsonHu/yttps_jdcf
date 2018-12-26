package com.eservice.iot.service.impl;

import com.eservice.iot.dao.RecordMapper;
import com.eservice.iot.model.record.Record;
import com.eservice.iot.service.RecordService;
import com.eservice.iot.core.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


/**
* Class Description: xxx
* @author Wilson Hu
* @date 2018/12/26.
*/
@Service
@Transactional
public class RecordServiceImpl extends AbstractService<Record> implements RecordService {
    @Resource
    private RecordMapper recordMapper;

}
