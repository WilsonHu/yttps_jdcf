package com.eservice.iot.web;
import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.record.Record;
import com.eservice.iot.service.RecordService;
import com.eservice.iot.service.impl.RecordServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
* Class Description: xxx
* @author Wilson Hu
* @date 2018/12/27.
*/
@RestController
@RequestMapping("/record")
public class RecordController {
    @Resource
    private RecordServiceImpl recordService;

    @PostMapping("/add")
    public Result add(Record record) {
        recordService.save(record);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        recordService.deleteById(id);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/update")
    public Result update(Record record) {
        recordService.update(record);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/detail")
    public Result detail(@RequestParam Integer id) {
        Record record = recordService.findById(id);
        return ResultGenerator.genSuccessResult(record);
    }

    @PostMapping("/list")
    public Result list(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size) {
        PageHelper.startPage(page, size);
        List<Record> list = recordService.findAll();
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(pageInfo);
    }

    @PostMapping("/search")
    public Result search(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size,
                         String name, String department,String query_start_time, String query_finish_time) {
        PageHelper.startPage(page, size);
        List<Record> list = recordService.searchRecord(name, department,query_start_time, query_finish_time);
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(pageInfo);
    }
}
