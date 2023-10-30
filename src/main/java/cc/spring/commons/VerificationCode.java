package cc.spring.commons;

import java.util.Random;

public class VerificationCode {

	//5자리 랜덤 인증번호 생성
	public static String makeCode(){ 
		Random rand = new Random(); 
		String numStr = "";
		for(int i=0; i<5; i++) {
			String ran = Integer.toString(rand.nextInt(10));
			numStr+=ran;
		}
		return numStr;
	}	
}
