package com.tanhua.sso.controller;

import com.tanhua.sso.service.SmsService;
import com.tanhua.sso.vo.ErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("user")
@RestController
public class SmsController {
    @Autowired
    private SmsService smsService;

    public ResponseEntity<Object> sendCheckCode (@RequestBody Map<String,Object> param){
        ErrorResult.ErrorResultBuilder builder = ErrorResult.builder().errCode("000000").errMessage("短信发送失败");
        String phone = String.valueOf(param.get("phone"));
        Map<String, Object> sendCheckCode = this.smsService.sendCheckCode(phone);
        int code = ((Integer)(sendCheckCode.get("code"))).intValue();
        if (code == 3) {
            return ResponseEntity.ok(null);
        } else if (code == 1) {
            String msg = sendCheckCode.get("msg").toString();
            builder.errCode("000001").errMessage(msg);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(builder.build());
    }
}
