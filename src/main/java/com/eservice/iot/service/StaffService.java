package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.*;
import com.eservice.iot.model.record.Record;
import com.eservice.iot.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author HT
 */
@Component
public class StaffService {

    private final static Logger logger = LoggerFactory.getLogger(StaffService.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Value("${attendance_begin_time}")
    private int ATTENDANCE_BEGIN_TIME;

    @Value("${attendance_end_time}")
    private int ATTENDANCE_END_TIME;

    @Value("${morning_afternoon_time}")
    private int MORNING_AFTERNOON_TIME;

    @Value("${afternoon_evening_time}")
    private int AFTERNOON_EVENING_TIME;

    @Autowired
    private RestTemplate restTemplate;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Token
     */
    private String token;
    /**
     * 员工列表
     */
    private ArrayList<Staff> staffList = new ArrayList<>();

    /**
     * 早上签到
     */
    private ArrayList<VisitRecord> morningSignList = new ArrayList<>();

    /**
     * 中午签到
     */
    private ArrayList<VisitRecord> afternoonSignList = new ArrayList<>();

    /**
     * 晚上签到
     */
    private ArrayList<VisitRecord> eveningSignList = new ArrayList<>();

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TagService tagService;

    @Resource
    private PolicyService policyService;

    @Resource
    private RecordService recordService;

    private ThreadPoolTaskExecutor mExecutor;

    /**
     * 查询开始时间,单位为秒
     */
    private Long queryStartTime = 0L;


    public StaffService() {
        //准备初始数据，此时获取到考勤列表后不去通知钉钉，初始化开始查询时间
        queryStartTime = Util.getDateStartTime().getTime() / 1000;
    }

    /**
     * 每秒查询一次考勤信息
     */
    @Scheduled(initialDelay = 5000, fixedRate = 1000)
    public void fetchSignInScheduled() {
        ///当员工列表数为0，或者已全部签核完成,以及当前处于程序初始化状态情况下，可以跳过不再去获取考勤数据
        boolean skip = staffList.size() <= 0 || tagService == null || !tagService.isTagInitialFinished();
        if (skip) {
            return;
        }
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            querySignInStaff(queryStartTime);
        }
    }

    /**
     * 每分钟获取一次员工信息
     */
    @Scheduled(initialDelay = 3000, fixedRate = 1000 * 60)
    public void fetchStaffScheduled() {
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            try {
                String url = PARK_BASE_URL + "/staffs?";
                for (String tagId : policyService.getmPolicyTags()) {

                    url += "tag_id_list=" + tagId + "&";
                }
                url += "page=0&size=0";
                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        processStaffResponse(body);
                    } else {
                        fetchStaffScheduled();
                    }
                }
            } catch (HttpClientErrorException exception) {
                if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    if (token != null) {
                        fetchStaffScheduled();
                    }
                }
            }
        }
    }

    /**
     * 凌晨1点清除签到记录
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void resetStaffDataScheduled() {
        logger.info("每天凌晨一点清除前一天签到记录：{}", formatter.format(new Date()));

        logger.info("================ 上午签到记录 ===============");
        if (morningSignList != null && morningSignList.size() > 0) {
            for (VisitRecord item : morningSignList) {
                logger.info("姓名：{}，工号：{}， 时间：{}",
                        item.getPerson().getPerson_information().getName(),
                        item.getPerson().getPerson_information().getId(),
                        formatter.format(new Date((long) item.getTimestamp() * 1000)));
            }
            morningSignList.clear();
        }

        logger.info("================ 中午签到记录 ===============");
        if (afternoonSignList != null && afternoonSignList.size() > 0) {
            for (VisitRecord item : afternoonSignList) {
                logger.info("姓名：{}，工号：{}， 时间：{}",
                        item.getPerson().getPerson_information().getName(),
                        item.getPerson().getPerson_information().getId(),
                        formatter.format(new Date((long) item.getTimestamp() * 1000)));
            }
            afternoonSignList.clear();
        }

        logger.info("================ 傍晚签到记录 ===============");
        if (eveningSignList != null && eveningSignList.size() > 0) {
            for (VisitRecord item : eveningSignList) {
                logger.info("姓名：{}，工号：{}， 时间：{}",
                        item.getPerson().getPerson_information().getName(),
                        item.getPerson().getPerson_information().getId(),
                        formatter.format(new Date((long) item.getTimestamp() * 1000)));
            }
            eveningSignList.clear();
        }
    }

    private void processStaffResponse(String body) {
        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
        if (responseModel != null && responseModel.getResult() != null) {
            ArrayList<Staff> tmpList = (ArrayList<Staff>) JSONArray.parseArray(responseModel.getResult(), Staff.class);
            if (tmpList != null && tmpList.size() != 0) {
                if (!staffList.equals(tmpList)) {
                    logger.info("The number of staff：{} ==> {}", staffList.size(), tmpList.size());
                    staffList = tmpList;
                }
            }
        }
    }

    private void processStaffSignInResponse(ArrayList<VisitRecord> records, boolean initial) {
        Collections.reverse(records);
        for (VisitRecord visitRecord : records) {
            if (((long) visitRecord.getTimestamp() * 1000) >= Util.formatAttendanceTime(ATTENDANCE_BEGIN_TIME).getTime()
                    && ((long) visitRecord.getTimestamp() * 1000) <= Util.formatAttendanceTime(ATTENDANCE_END_TIME).getTime()
                    && visitRecord.isIs_pass()) {
                boolean exist = false;
                if (visitRecord.getTimestamp() * 1000 < Util.formatAttendanceTime(MORNING_AFTERNOON_TIME).getTime()) {
                    for (int i = morningSignList.size() - 1; i >= 0; i--) {
                        PersonInformation personInformation = morningSignList.get(i).getPerson().getPerson_information();
                        if (personInformation.getName().equals(visitRecord.getPerson().getPerson_information().getName())
                            && personInformation.getId().equals(visitRecord.getPerson().getPerson_information().getId())) {
                            exist = true;
                            break;
                        }
                    }
                } else if (visitRecord.getTimestamp() * 1000 >= Util.formatAttendanceTime(MORNING_AFTERNOON_TIME).getTime()
                        && visitRecord.getTimestamp() * 1000 < Util.formatAttendanceTime(AFTERNOON_EVENING_TIME).getTime()) {
                    for (int i = afternoonSignList.size() - 1; i >= 0; i--) {
                        PersonInformation personInformation = afternoonSignList.get(i).getPerson().getPerson_information();
                        if (personInformation.getName().equals(visitRecord.getPerson().getPerson_information().getName())
                                && personInformation.getId().equals(visitRecord.getPerson().getPerson_information().getId())) {
                            exist = true;
                            break;
                        }
                    }
                } else {
                    for (int i = eveningSignList.size() - 1; i >= 0; i--) {
                        PersonInformation personInformation = eveningSignList.get(i).getPerson().getPerson_information();
                        if (personInformation.getName().equals(visitRecord.getPerson().getPerson_information().getName())
                                && personInformation.getId().equals(visitRecord.getPerson().getPerson_information().getId())) {
                            exist = true;
                            break;
                        }
                    }
                }
                if(!exist) {
                    Record record = new Record();
                    record.setName(visitRecord.getPerson().getPerson_information().getName());
                    record.setDepartment(getDepartmentName(visitRecord));
                    record.setStaffId(visitRecord.getPerson().getPerson_information().getId());
                    record.setRecordTime(new Date(visitRecord.getTimestamp() * 1000L));
                    record.setCreateTime(new Date());
                    recordService.save(record);
                    if(visitRecord.getTimestamp() * 1000 < Util.formatAttendanceTime(MORNING_AFTERNOON_TIME).getTime()) {
                        morningSignList.add(visitRecord);
                    } else if(visitRecord.getTimestamp() * 1000 >= Util.formatAttendanceTime(MORNING_AFTERNOON_TIME).getTime()
                            && visitRecord.getTimestamp() * 1000 < Util.formatAttendanceTime(AFTERNOON_EVENING_TIME).getTime()) {
                        afternoonSignList.add(visitRecord);
                    } else {
                        eveningSignList.add(visitRecord);
                    }
                }

            }
        }
    }

    private void querySignInStaff(Long startTime) {
        if (token == null) {
            token = tokenService.getToken();
        }
        HashMap<String, Object> postParameters = new HashMap<>();
//        ///考勤记录查询开始时间
        postParameters.put("start_timestamp", startTime);
//        ///考勤记录查询结束时间
        Long queryEndTime = System.currentTimeMillis() / 1000;
        //重启状态
        if (startTime == Util.getDateStartTime().getTime() / 1000) {
            if (queryEndTime > Util.formatAttendanceTime(ATTENDANCE_END_TIME).getTime() / 1000) {
                queryEndTime = Util.formatAttendanceTime(ATTENDANCE_END_TIME).getTime() / 1000;
            }
        }
        postParameters.put("end_timestamp", queryEndTime);
        //只获取员工数据
//        ArrayList<String> identity = new ArrayList<>();
//        identity.add("VISITOR");
//        postParameters.put("identity_list", identity);
        //只获取指定考勤设备的过人记录
        postParameters.put("device_id_list", policyService.getmPolicyDevices());
        ///只获取指定考勤tag的过人记录
        postParameters.put("tag_id_list", policyService.getmPolicyTags());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, token);
        HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/access/record", httpEntity, String.class);
            if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                String body = responseEntity.getBody();
                if (body != null) {
                    ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                    if (responseModel != null && responseModel.getResult() != null) {
                        ArrayList<VisitRecord> tempList = (ArrayList<VisitRecord>) JSONArray.parseArray(responseModel.getResult(), VisitRecord.class);
                        if (tempList != null && tempList.size() > 0) {
                            processStaffSignInResponse(tempList, startTime.equals(Util.getDateStartTime().getTime() / 1000));
                            //query成功后用上一次查询的结束时间作为下一次开始时间，减去1秒形成闭区间，
                            // 这里的时间是服务器时间，所以跟门禁或者抓拍机不一定是一个时间，容易遗漏
                            queryStartTime = queryEndTime - 1;
                        }
                    }
                }
            }
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                //token失效,重新获取token后再进行数据请求
                token = tokenService.getToken();
                querySignInStaff(startTime);
            }
        }
    }

    public boolean deleteStaff(String id) {
        boolean success = false;
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/staffs/" + id, HttpMethod.DELETE, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                        if(responseModel != null && responseModel.getRtn() == 0) {
                            success = true;
                        }
                    }
                }
            } catch (HttpClientErrorException exception) {
                if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    if (token != null) {
                        deleteStaff(id);
                    }
                }
            }
        }
        return success;
    }

    private void initExecutor() {
        mExecutor = new ThreadPoolTaskExecutor();
        mExecutor.setCorePoolSize(10);
        mExecutor.setMaxPoolSize(100);
        mExecutor.setThreadNamePrefix("YTTPS-");
        mExecutor.initialize();
    }

    public ArrayList<Staff> getStaffList() {
        return staffList;
    }

    private String getDepartmentName(VisitRecord record) {
        String tagName = "";
        //返回第一个签到的tag名称
        if (record != null && record.getPerson().getTag_id_list().size() > 0) {
            for (String id : record.getPerson().getTag_id_list()) {
                if (policyService.getmPolicyTags().contains(id)) {
                   tagName = tagService.getTagNameById(id);
                   break;
                }
            }
        }
        return tagName;
    }
}
