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
		//���� ������ȣ ����
		String veriCode = VerificationCode.makeCode();
		
		//coolsms key
		String coolsmsKey = SmsKey.ACCESS_KEY;
		String coolsmsSecret = SmsKey.SECRET_KEY;
		Message coolsms = new Message(coolsmsKey, coolsmsSecret);
	
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("to", to);    // ������ȭ��ȣ (ajax�� view ȭ�鿡�� �޾ƿ� ������ �ѱ�)
	    params.put("from", "01026244607");    // �߽���ȭ��ȣ. �׽�Ʈ�ÿ��� �߽�,���� �Ѵ� ���� ��ȣ�� �ϸ� ��
	    params.put("type", "sms"); 
	    params.put("text", "������ȣ�� [" + veriCode + "] �Դϴ�.");

	    coolsms.send(params); // �޽��� ����
	
	    return veriCode;
	}
}
