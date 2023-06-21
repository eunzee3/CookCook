package cc.spring.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cc.spring.dto.FileDTO;
import cc.spring.dto.RequestListDTO;
import cc.spring.dto.ShopDTO;
import cc.spring.dto.ShopListDTO;
import cc.spring.dto.ShopReplyAnswerDTO;
import cc.spring.dto.ShopReplyAskDTO;
import cc.spring.services.ShopReplyService;
import cc.spring.services.ShopService;

@Controller
@RequestMapping("/shop/")
public class ShopController {

	@Autowired
	private ShopService shopService;
	
	@Autowired
	private ShopReplyService shopReplyService;

	@Autowired
	private HttpSession session;


	//김은지 Part	
	// 공구샵 등록 폼으로 이동
	@RequestMapping("toShopRegister")
	public String toShopRegister(Model model) {
		// 세션에서 ID 받아오게 수정
		String loginId = "1112254";
		int authgradeCode = 1002;
		
		// 판매자인지 체크
		if(authgradeCode != 1002) {
			return "redirect:/";
		}
		// 판매자인 경우 businessCode 구해오기
		int businessCode = 1; // 세션에서 받도록 수정
		
		model.addAttribute("businessCode", businessCode);
		return "/shop/shopRegister";
	}

	// 공구샵 신청 폼으로 이동
	@RequestMapping("toShopApply")
	public String toShopApply(int code, Model model) {
		// 세션에서 ID 받아오게 수정
		String loginId = "1112254";
		int authgradeCode = 1002;
		//String loginId = "aaa";
		//int authgradeCode = 1003;
		
		// 판매자인 경우 해당 ID의 회원코드 가져오기
		if(authgradeCode == 1002) {
			int businessCode = 1; // 세션에서 받도록 수정
			model.addAttribute("businessCode", businessCode);
		}else {
			model.addAttribute("businessCode", 0);
		}
		
		// 일반 사용자인 경우 해당 ID의 회원코드 가져오기
		if(authgradeCode == 1003) {
			int clientCode = 1; // 세션에서 받도록 수정
			model.addAttribute("clientCode", clientCode);
		}else {
			model.addAttribute("clientCode", 0);			
		}
		
		// 선택한 공구샵 정보 가져오기
		ShopDTO shopDTO = shopService.selectShopInfo(code);
		
		// 선택한 공구샵 이미지 가져오기
		List<FileDTO> fileDTO = shopService.selectShopImg(code);
		
		// 선택한 공구샵 댓글 목록 가져오기
		List<ShopReplyAskDTO> shopReplyAskDTO = shopReplyService.selectShopReply(code);
		
		// 선택한 공구샵 답글 목록 가져오기
		List<ShopReplyAnswerDTO> shopReplyAnswerDTO = shopReplyService.selectShopReplyAnswer(code);
		
		model.addAttribute("loginId", loginId);
		model.addAttribute("shopDTO", shopDTO);
		model.addAttribute("fileDTO", fileDTO);
		model.addAttribute("shopReplyAskDTO", shopReplyAskDTO);
		model.addAttribute("shopReplyAnswerDTO", shopReplyAnswerDTO);
		model.addAttribute("authgradeCode", authgradeCode);
		return "/shop/shopApply";
	}

	// 공구샵 등록시 DB에 insert
	@RequestMapping("insertShop")
	public String insertShop(ShopDTO dto, MultipartFile[] files) throws Exception {

		// realPath - 폴더가 없다면 만들기
		String realPath = session.getServletContext().getRealPath("/resources/shopImg");
		shopService.insertShop(dto, files, realPath);

		return "redirect:/";
	}

	
//최은지 Part
 	
 	//공구 목록으로 이동
 	 	@RequestMapping("toShopList")
 		public String toShopList(@RequestParam(name="status",required=false,defaultValue="") String status, Model model) throws Exception{
 	 		List<ShopListDTO> list = new ArrayList<ShopListDTO>();
 	 			
 	 		if(status.equals("closed")){
 	 			//마감된 공구 list
 	 			list = shopService.getStatusList(status);
 	 		}else if(status.equals("open")){
 	 			//진행중인 공구 list
 	 	 		list = shopService.getStatusList(status);
 	 		}else {
 	 			//전체 공구 list
 	 			list = shopService.shopList();
 	 		}
 	 		
 	 		//마감일 디데이 계산
 	 		Map< ShopListDTO, Integer> dDayMap = new HashMap<>();
 	 		SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
 	 		for(ShopListDTO d : list) {
 	 			String deadLineFmt = dateFmt.format(d.getDeadLine());
 	 			String todayFmt = dateFmt.format(new Date(System.currentTimeMillis()));
 	 			
 	 			Date deadLine = new Date(dateFmt.parse(deadLineFmt).getTime());
 	 			Date today = new Date(dateFmt.parse(todayFmt).getTime());
 	 			
 	 			long calculate = deadLine.getTime() - today.getTime();
 	 			int dDay = (int)(calculate / (24*60*60*1000));
 	 			
 	 			d.setdDay(dDay);
 	 			dDayMap.put(d, dDay);
 	 		}
 	 		//상품정보, 이미지정보, 디데이 전송
 	 		model.addAttribute("list",list);
 	 		
 	 		//사업자회원 공구등록 버튼 유무
 	 		int authGradeCode = (Integer)session.getAttribute("authGradeCode");;
 	 		model.addAttribute("authGradeCode",authGradeCode);
 	 		
 			return "/shop/shopList";
 		}
 	 
 	 //공구 목록 검색
 	 	@ResponseBody
 	 	@RequestMapping("searchByKeyword")
 	 	public List<ShopListDTO> searchByKeyword(String category, String keyword) throws Exception{
 	 		List<ShopListDTO> searchList = shopService.searchByKeyword(category,keyword);
 	 		
 	 		Map< ShopListDTO, Integer> dDayMap = new HashMap<>();
 	 		SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
 	 		for(ShopListDTO d : searchList) {
 	 			String deadLineFmt = dateFmt.format(d.getDeadLine());
 	 			String todayFmt = dateFmt.format(new Date(System.currentTimeMillis()));
 	 			
 	 			Date deadLine = new Date(dateFmt.parse(deadLineFmt).getTime());
 	 			Date today = new Date(dateFmt.parse(todayFmt).getTime());
 	 			
 	 			long calculate = deadLine.getTime() - today.getTime();
 	 			int dDay = (int)(calculate / (24*60*60*1000));
 	 			
 	 			d.setdDay(dDay);
 	 			dDayMap.put(d, dDay);
 	 		}
 	 		System.out.println("겨얼과는:" + searchList);
 			return searchList;
 	 	}
 	 	

 	 	
 	 	

	// 공구샵 수정
	@RequestMapping("updateShop")
	public String updateShop(ShopDTO dto, MultipartFile[] files) throws Exception {
		// realPath - 폴더가 없다면 만들기
		String realPath = session.getServletContext().getRealPath("/resources/shopImg");
		shopService.updateShop(dto, files, realPath);
		return "redirect:/shop/toShopApply?code="+dto.getCode();
	}

	// 공구샵 삭제
	@RequestMapping("deleteShop")
	public String deleteShop(int code) {
		shopService.deleteShop(code);
		return "redirect:/shop/toShopList";
	}

	// 공구샵 신청시 DB에 insert
	@RequestMapping("insertShopRequest")
	public String insertShopRequest(int clientCode, int quantity, int parentCode) {
		shopService.insertShopRequest(new RequestListDTO(clientCode,quantity,parentCode));
		return "redirect:/shop/toShopList";
	}




	//예외처리
	@ExceptionHandler(Exception.class)
	public String exceptionHandler(Exception e) {
		e.printStackTrace();
		return "redirect:/error";
	}

}
