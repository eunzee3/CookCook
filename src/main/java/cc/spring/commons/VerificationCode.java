package cc.spring.commons;

import java.util.Random;

public class VerificationCode {

	//5�ڸ� ���� ������ȣ ����
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
