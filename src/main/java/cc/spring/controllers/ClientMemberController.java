package cc.spring.controllers;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cc.spring.commons.EncryptionUtils;
import cc.spring.dto.MemberDTO;
import cc.spring.dto.loginCountDTO;
import cc.spring.services.AdminMemberService;
import cc.spring.services.ClientMemberService;
import cc.spring.services.CoolsmsService;

@Controller
@RequestMapping("/clientMember/")
public class ClientMemberController {
	@Autowired
	private HttpSession session;
	
	@Autowired
	private ClientMemberService cms;
	
	@Autowired
	private AdminMemberService ams;
	
	@Autowired
	private CoolsmsService sms;
	
	//  로그인 창으로 이동
	@RequestMapping("login_form")
	public String login_form() throws Exception {
		return "member/clientLogin";
	}

	// 클라이언트 로그인
	@RequestMapping("login")
	public String login(MemberDTO dto, RedirectAttributes redir) throws Exception {
		

		// 입력한 id와 비밀번호가 관리자인지 확인
		boolean admin = ams.login(dto.getId(), dto.getPw());
		if(admin) {
			// 입력한 id와 비밀번호 일치하는 관리자 정보 가져오기
			MemberDTO amd = ams.selectAdminMemberInfo(dto.getId(), dto.getPw());
			session.setAttribute("code", amd.getCode());
			session.setAttribute("id", amd.getId());
			session.setAttribute("nickname", amd.getName());
			session.setAttribute("authGradeCode", amd.getAuthGradeCode());
			return "redirect:/";
		}
		
		

		
		String pw = EncryptionUtils.sha512(dto.getPw());
		dto.setPw(pw);
		
		// 입력한 id와 비밀번호가 일치하는 회원이 있으면 아래 구문 실행
		boolean memberCount = cms.existingMember(dto);
		
		if(memberCount) {
			// 로그인시 count update
			loginCountDTO ldto = new loginCountDTO(cms.selectClientMemberInfo(dto.getId()).getCode(), 0, null);
			boolean result = cms.login(ldto,dto);
			if(result) {
				// 입력한 id와 일치하는 회원의 정보 dto로 가져오기
				MemberDTO cmd = cms.selectClientMemberInfo(dto.getId());
				session.setAttribute("code", cmd.getCode());
				session.setAttribute("id",cmd.getId());
				session.setAttribute("nickname", cmd.getNickName());
				session.setAttribute("authGradeCode", cmd.getAuthGradeCode());
				return "redirect:/";
			}
		}
		redir.addFlashAttribute("status", "false");
		return "redirect:/clientMember/login_form";
	}
//	비밀번호 찾기할때 폰번호로 아이디값 받아오는 코드
	@RequestMapping("getIdByPhone")
	public String getIdByPhone(String phone) {
		String result = cms.getIdByPhone(phone);
		return null;
	}
	
	// 로그아웃
	@RequestMapping("logout")
	public String logout() {
		session.invalidate();
		return "redirect:/";
	}
	// 클라이언트 회원가입 창으로 이동
	@RequestMapping("sign_form")
	public String sign_form() throws Exception {
		return "member/clientSign";
	}
	
	// 회원가입 시 중복체크
	@ResponseBody
	@RequestMapping(value="checkSum", produces="text/html;charset=utf8")
	public String checkId(String key, String value) throws Exception {
		if(key.equals("PHONE") || key.equals("EMAIL")) {
			boolean result = cms.phoneAndemailDuplication(key, value);
			return String.valueOf(result);
		}
		else {
			boolean result = cms.isClientMember(key, value);
			return String.valueOf(result);
		}
	}
	
	// 회원가입 시 인증번호 랜덤 발송
	@ResponseBody
	@RequestMapping(value="sendSmsSign", produces="text/html;charset=utf8")
	public String sendSms(String phone) throws Exception {
		// 이미 가입한 연락처가 있는지 확인
		boolean result = cms.phoneCheck(phone);
		
		// 같은 연락처가 DB에 없으면 실행
		if(!result) {
			session.setAttribute("numStr", CoolsmsService.sendCode(phone));
		}
			
		return String.valueOf(result);
	}
	

	// 계정찾기시 인증번호 랜덤 발송
	@ResponseBody
	@RequestMapping(value="sendSmsLogin", produces="text/html;charset=utf8")
	public String sendSms2(String phone) throws Exception {
		// 이미 가입한 연락처가 있는지 확인
		boolean result = cms.phoneCheck(phone);
		
		// 같은 연락처가 DB에 있으면 실행
		if(result) {
			session.setAttribute("numStr", CoolsmsService.sendCode(phone));
			session.setAttribute("phone", phone);
		}
		return String.valueOf(result);
	}
	
	// 인증번호 입력 후 인증 버튼 클릭 시
	@ResponseBody
	@RequestMapping(value="certificationSign", produces="text/html;charset=utf8")
	public String certification(String code) {
		String numStr = (String) session.getAttribute("numStr");
				
		if(numStr.equals(code)) {
			return String.valueOf(true);
		}

		else {
			return String.valueOf(false);
		}	 
	}
	// 인증번호 입력 후 인증 버튼 클릭 시
	@ResponseBody
	@RequestMapping("certificationLogin")
	public Map<String, Object> certification2(String code) {
		String numStr = (String) session.getAttribute("numStr");
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", false);
		
		if(code.equals(numStr)) {
			String phone = (String) session.getAttribute("phone");
			String searchId = cms.getIdByPhone(phone);
			result.put("searchId", searchId);
			result.put("success", true);
			
			session.removeAttribute("phone");
			session.removeAttribute("numStr");
		}
		return result;
	}
	
	// 비밀번호 재설정
	@ResponseBody
	@RequestMapping("changePw")
	public void changePw(MemberDTO dto) throws Exception {
		String updatePw = EncryptionUtils.sha512(dto.getPw());
		dto.setPw(updatePw);
		cms.updatePw(dto);
	}
	
	// 인증번호 시간초과 시 세션에 저장된 인증번호 삭제
	@ResponseBody
	@RequestMapping(value="removeSession")
	public void removeSession() {
		session.removeAttribute("numStr");
	}
	
	// 회원가입 폼에서 입력한 값들 넘어옴
	@RequestMapping("signup")
	public String signup(MemberDTO dto, String member_birth_year, String member_birth_month, String member_birth_day, Model m) throws Exception{
		// 받은 생년월일 합치기
		String birthDate = member_birth_year + member_birth_month + member_birth_day;
		dto.setBirthDate(birthDate);
		// 비밀번호 암호화
		String shaPw = EncryptionUtils.sha512(dto.getPw());
		dto.setPw(shaPw);
		// 일반회원 가입 시 authgradecode 1003 삽입
		dto.setAuthGradeCode(1003);
		
		int result = 0;
		result = cms.insertClient(dto);
		if(result != 0) {
			m.addAttribute("clientName", dto.getName());
			m.addAttribute("status", "complete");
			return "/member/clientSign";
		}
		else {
			return "error";
		}
	}
	
	// 내 정보 보기 클릭 시 페이지 이동
	@RequestMapping("clientMyInfo")
	public String myInfo() throws Exception {
		return "/member/clientMyInfo";
	}
	
	// 비밀번호 입력 시 로그인한 회원의 비밀번호와 일치하는지 확인
	@ResponseBody
	@RequestMapping("checkPw")
	public String checkPw(String pw) throws Exception {
		String enPw = EncryptionUtils.sha512(pw);
		String id = (String) session.getAttribute("id");
		boolean result = cms.checkPw(id, enPw);
		return String.valueOf(result);
	}
	
	// 회원정보 가져오기
	@ResponseBody
	@RequestMapping("selectClientMemberInfo")
	public MemberDTO selectClientMemberInfo(String id) throws Exception {
		MemberDTO dto = cms.selectClientMemberInfo(id);
		return dto;
	}
	
	// 회원정보 수정이 가능한 폼으로 이동
	@RequestMapping("goUpdateInfo")
	public String goUpdateInfo(String id, Model m) throws Exception {
		MemberDTO dto = cms.selectClientMemberInfo(id);
		m.addAttribute("info", dto);
		return "/member/clientInfoUpdate";
	}
	// 회원정보 수정 시 모든 변경은 연락처 인증을 통해서만 가능해....
	// 이 부분은 로그인된 회원의 연락처와 비회원의 연락처만 넘어옴
	@ResponseBody
	@RequestMapping("sendSmsUpdate")
	public String sendSmsUpdate(String phone) throws Exception {
				
		session.setAttribute("numStr", CoolsmsService.sendCode(phone));
			
		return String.valueOf(true);
	}
	
	// 회원정보(업데이트) 입력한 내용 넘어오는 곳 
	@RequestMapping("updateMemberInfo")
	public String updateMemberInfo(MemberDTO dto, String member_birth_year, String member_birth_month, String member_birth_day, Model m) throws Exception {
		// 받은 생년월일 합치기
		String birthDate = member_birth_year + member_birth_month + member_birth_day;
		dto.setBirthDate(birthDate);
		// 회원 수정 시 where = id에서 id값이 필요함
		String id = (String) session.getAttribute("id");
		dto.setId(id);
		
		int result = cms.updateMemberInfo(dto);
		if(result == 1) {
			MemberDTO updateDto = cms.selectClientMemberInfo(id);
			// 업데이트된 정보 다시 세션에 담기
			session.setAttribute("code", updateDto.getCode());
			session.setAttribute("id", updateDto.getId());
			session.setAttribute("nickname", updateDto.getNickName());
			session.setAttribute("authGradeCode", updateDto.getAuthGradeCode());
			
			m.addAttribute("status", "complete");
			m.addAttribute("dto", updateDto);
			return "/member/clientMyInfo";
		}
		else {
			return "error";
		}
	}
	
	// 회원탈퇴하기
	@ResponseBody
	@RequestMapping("deleteMember")
	public String deleteMember() throws Exception {
		int code = (int) session.getAttribute("code");
		int result = cms.deleteMember(code);
		
		if(result == 1) {
			session.invalidate();
		}
		
		
		return String.valueOf(result);
	}

	@ExceptionHandler(Exception.class)
	public String exceptionHandler(Exception e) {
		e.printStackTrace();
		return "redirect:/error";
	}

}