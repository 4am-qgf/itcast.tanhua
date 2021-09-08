package com.tanhua.sso.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsService.class);

    @Autowired
    private RestTemplate restTemplate;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 发送验证码
     * @param mobile
     * @return
     */
    public Map<String,Object> sendCheckCode(String mobile){
        HashMap<String, Object> result = new HashMap<>(2);
        try{
            String redisKey = "CHECK_CODE_" + mobile;
            String value = this.redisTemplate.opsForValue().get(redisKey);
            if(StringUtils.isNoneEmpty(value)){
                result.put("code",1);
                result.put("msg","上一次发送的验证码还没失效");
                return result;
            }
            String code = this.sendSms(mobile);
            if(null == code){
                result.put("code",2);
                result.put("msg","发送验证码失败");
                return result;
            }
            result.put("code",3);
            result.put("msg","OK");
            this.redisTemplate.opsForValue().set(redisKey, code , Duration.ofMinutes(2));
            return result;
        }catch (Exception e){
            LOGGER.error("发送验证码出错"+mobile,e);
            result.put("code",4);
            result.put("msg","发送验证码异常");
            return result;
        }
    }
    /**
     * 发送验证码短信
     *
     * @param mobile
     */
    public String sendSms(String mobile) {
        String url = "https://open.ucpaas.com/ol/sms/sendsms";
        Map<String, Object> params = new HashMap<>();
        params.put("sid", "c0ab20637cd14885c9ab9354c95d5a2e");
        params.put("token", "ea33e0a57006d0ef612e2189f259e1fd");
        params.put("appid", "dcfc0381955f452892fd696addc3e54a");
        params.put("templateid", "487656");
        params.put("mobile", mobile);
        // 生成6位数验证
        params.put("param", RandomUtils.nextInt(100000, 999999));
        ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(url, params, String.class);

        String body = responseEntity.getBody();

        try {
            JsonNode jsonNode = MAPPER.readTree(body);
            //000000 表示发送成功
            if (StringUtils.equals(jsonNode.get("code").textValue(), "000000")) {
                return String.valueOf(params.get("param"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }
}
