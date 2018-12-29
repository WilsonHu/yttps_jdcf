package com.eservice.iot.web;

import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.record.Record;
import com.eservice.iot.model.record.Statistic;
import com.eservice.iot.service.impl.RecordServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.poi.hssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
* Class Description: xxx
* @author Wilson Hu
* @date 2018/12/27.
*/
@RestController
@RequestMapping("/record")
public class RecordController {
    private final static Logger logger = LoggerFactory.getLogger(RecordController.class);

    @Value("${excel_path}")
    private String EXCEL_PATH;
    @Resource
    private RecordServiceImpl recordService;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    @PostMapping("/statistic")
    public Result statistic(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size,
                         String name, String department,String query_start_time, String query_finish_time) {
        PageHelper.startPage(page, size);
        List<Statistic> list = recordService.statistic(name, department,query_start_time, query_finish_time);
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(pageInfo);
    }

    @PostMapping("/exportStatistic")
    public Result exportStatistic(String name, String department,String query_start_time, String query_finish_time) {
        List<Statistic> list = recordService.statistic(name, department,query_start_time, query_finish_time);
        if (list.size() > 0) {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("刷卡统计");

            ///设置要导出的文件的名字
            String fileName = "statistic_" + formatter.format(new Date()) + ".xls";
            //新增数据行，并且设置单元格数据
            insertDataInSheet(sheet, list);

            try {
                File dir = new File(EXCEL_PATH);
                if(!dir.exists()) {
                    if(dir.mkdirs()) {
                        logger.info("excel目录创建成功！");
                    }
                }
                FileOutputStream out = new FileOutputStream(EXCEL_PATH + fileName);
                workbook.write(out);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResultGenerator.genSuccessResult(EXCEL_PATH + fileName);
        } else {
            return ResultGenerator.genFailResult("刷卡记录数为0");
        }
    }

    @PostMapping("/exportRecord")
    public Result exportRecord(String name, String department,String query_start_time, String query_finish_time) {
        List<Record> list = recordService.searchRecord(name, department,query_start_time, query_finish_time);
        if (list.size() > 0) {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("刷脸记录");

            ///设置要导出的文件的名字
            String fileName = "records_" + formatter.format(new Date()) + ".xls";
            //新增数据行，并且设置单元格数据
            insertRecordDataInSheet(sheet, list);

            try {
                File dir = new File(EXCEL_PATH);
                if(!dir.exists()) {
                    if(dir.mkdirs()) {
                        logger.info("excel目录创建成功！");
                    }
                }
                FileOutputStream out = new FileOutputStream(EXCEL_PATH + fileName);
                workbook.write(out);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResultGenerator.genSuccessResult(EXCEL_PATH + fileName);
        } else {
            return ResultGenerator.genFailResult("刷卡记录数为0");
        }
    }

    private void insertDataInSheet(HSSFSheet sheet, List<Statistic> list) {
        String[] excelHeaders = {"姓名", "员工号","部门", "刷卡次数"};
        //headers表示excel表中第一行的表头
        HSSFRow row3 = sheet.createRow(0);
        //在excel表中添加表头
        for (int i = 0; i < excelHeaders.length; i++) {
            HSSFCell cell = row3.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(excelHeaders[i]);
            cell.setCellValue(text);
        }
        //在表中存放查询到的数据放入对应的列
        int rowNum = 1;
        for (Statistic record : list) {
            HSSFRow row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(record.getName());
            row.createCell(1).setCellValue(record.getStaffId());
            row.createCell(2).setCellValue(record.getDepartment());
            row.createCell(3).setCellValue(record.getNumber());
            rowNum++;
        }
    }

    private void insertRecordDataInSheet(HSSFSheet sheet, List<Record> list) {
        String[] excelHeaders = {"姓名", "员工号","部门", "刷卡时间"};
        //headers表示excel表中第一行的表头
        HSSFRow row3 = sheet.createRow(0);
        //在excel表中添加表头
        for (int i = 0; i < excelHeaders.length; i++) {
            HSSFCell cell = row3.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(excelHeaders[i]);
            cell.setCellValue(text);
        }
        //在表中存放查询到的数据放入对应的列
        int rowNum = 1;
        for (Record record : list) {
            HSSFRow row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(record.getName());
            row.createCell(1).setCellValue(record.getStaffId());
            row.createCell(2).setCellValue(record.getDepartment());
            row.createCell(3).setCellValue(formatter2.format(record.getRecordTime()));
            rowNum++;
        }
    }
}
