package cc.spring.services;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import cc.spring.commons.SmsKey;
import cc.spring.commons.VerificationCode;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;

@Service
public class CoolsmsService{

	public static String sendCode(String to) throws CoolsmsException{
		//랜덤 인증번호 생성
		String veriCode = VerificationCode.makeCode();
		
		//coolsms key
		String coolsmsKey = SmsKey.ACCESS_KEY;
		String coolsmsSecret = SmsKey.SECRET_KEY;
		Message coolsms = new Message(coolsmsKey, coolsmsSecret);
	
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("to", to);    // 수신전화번호 (ajax로 view 화면에서 받아온 값으로 넘김)
	    params.put("from", "01026244607");    // 발신전화번호. 테스트시에는 발신,수신 둘다 본인 번호로 하면 됨
	    params.put("type", "sms"); 
	    params.put("text", "인증번호는 [" + veriCode + "] 입니다.");

	    coolsms.send(params); // 메시지 전송
	
	    return veriCode;
	}
}
