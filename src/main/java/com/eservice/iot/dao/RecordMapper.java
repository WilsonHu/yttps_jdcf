package com.eservice.iot.dao;

import com.eservice.iot.core.Mapper;
import com.eservice.iot.model.record.Record;
import com.eservice.iot.model.record.Statistic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RecordMapper extends Mapper<Record> {
    List<Record> searchRecord(@Param("name") String name,
                              @Param("department") String department,
                              @Param("query_start_time") String query_start_time,
                              @Param("query_finish_time") String query_finish_time);

    List<Statistic> statistic(@Param("name") String name,
                                 @Param("department") String department,
                                 @Param("query_start_time") String query_start_time,
                                 @Param("query_finish_time") String query_finish_time);
}