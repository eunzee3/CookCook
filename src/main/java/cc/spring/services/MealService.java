package cc.spring.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cc.spring.dto.ChatDTO;
import cc.spring.provider.ChatGPTProvider;

@Service
public class MealService {
	
	@Autowired
	private ChatGPTProvider provider;
	
	@Autowired
	private Gson gson;
	
	// content 까지는 provider에서 가공해서 가져오고 그뒤는 service에서 각자 가공하기
	// 식단 추출 기능
	public void makeMeal(String sendMsg) throws Exception {
		JsonObject content = provider.makeMeal(sendMsg);
		Map<String, ChatDTO> data = gson.fromJson(content, Map.class);
		
		System.out.println(data.values());
	}
}
