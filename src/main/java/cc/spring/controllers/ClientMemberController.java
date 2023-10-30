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
	
	//  �α��� â���� �̵�
	@RequestMapping("login_form")
	public String login_form() throws Exception {
		return "member/clientLogin";
	}

	// Ŭ���̾�Ʈ �α���
	@RequestMapping("login")
	public String login(MemberDTO dto, RedirectAttributes redir) throws Exception {
		

		// �Է��� id�� ��й�ȣ�� ���������� Ȯ��
		boolean admin = ams.login(dto.getId(), dto.getPw());
		if(admin) {
			// �Է��� id�� ��й�ȣ ��ġ�ϴ� ������ ���� ��������
			MemberDTO amd = ams.selectAdminMemberInfo(dto.getId(), dto.getPw());
			session.setAttribute("code", amd.getCode());
			session.setAttribute("id", amd.getId());
			session.setAttribute("nickname", amd.getName());
			session.setAttribute("authGradeCode", amd.getAuthGradeCode());
			return "redirect:/";
		}
		
		

		
		String pw = EncryptionUtils.sha512(dto.getPw());
		dto.setPw(pw);
		
		// �Է��� id�� ��й�ȣ�� ��ġ�ϴ� ȸ���� ������ �Ʒ� ���� ����
		boolean memberCount = cms.existingMember(dto);
		
		if(memberCount) {
			// �α��ν� count update
			loginCountDTO ldto = new loginCountDTO(cms.selectClientMemberInfo(dto.getId()).getCode(), 0, null);
			boolean result = cms.login(ldto,dto);
			if(result) {
				// �Է��� id�� ��ġ�ϴ� ȸ���� ���� dto�� ��������
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
//	��й�ȣ ã���Ҷ� ����ȣ�� ���̵� �޾ƿ��� �ڵ�
	@RequestMapping("getIdByPhone")
	public String getIdByPhone(String phone) {
		String result = cms.getIdByPhone(phone);
		return null;
	}
	
	// �α׾ƿ�
	@RequestMapping("logout")
	public String logout() {
		session.invalidate();
		return "redirect:/";
	}
	// Ŭ���̾�Ʈ ȸ������ â���� �̵�
	@RequestMapping("sign_form")
	public String sign_form() throws Exception {
		return "member/clientSign";
	}
	
	// ȸ������ �� �ߺ�üũ
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
	
	// ȸ������ �� ������ȣ ���� �߼�
	@ResponseBody
	@RequestMapping(value="sendSmsSign", produces="text/html;charset=utf8")
	public String sendSms(String phone) throws Exception {
		// �̹� ������ ����ó�� �ִ��� Ȯ��
		boolean result = cms.phoneCheck(phone);
		
		// ���� ����ó�� DB�� ������ ����
		if(!result) {
			session.setAttribute("numStr", CoolsmsService.sendCode(phone));
		}
			
		return String.valueOf(result);
	}
	

	// ����ã��� ������ȣ ���� �߼�
	@ResponseBody
	@RequestMapping(value="sendSmsLogin", produces="text/html;charset=utf8")
	public String sendSms2(String phone) throws Exception {
		// �̹� ������ ����ó�� �ִ��� Ȯ��
		boolean result = cms.phoneCheck(phone);
		
		// ���� ����ó�� DB�� ������ ����
		if(result) {
			session.setAttribute("numStr", CoolsmsService.sendCode(phone));
			session.setAttribute("phone", phone);
		}
		return String.valueOf(result);
	}
	
	// ������ȣ �Է� �� ���� ��ư Ŭ�� ��
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
	// ������ȣ �Է� �� ���� ��ư Ŭ�� ��
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
	
	// ��й�ȣ �缳��
	@ResponseBody
	@RequestMapping("changePw")
	public void changePw(MemberDTO dto) throws Exception {
		String updatePw = EncryptionUtils.sha512(dto.getPw());
		dto.setPw(updatePw);
		cms.updatePw(dto);
	}
	
	// ������ȣ �ð��ʰ� �� ���ǿ� ����� ������ȣ ����
	@ResponseBody
	@RequestMapping(value="removeSession")
	public void removeSession() {
		session.removeAttribute("numStr");
	}
	
	// ȸ������ ������ �Է��� ���� �Ѿ��
	@RequestMapping("signup")
	public String signup(MemberDTO dto, String member_birth_year, String member_birth_month, String member_birth_day, Model m) throws Exception{
		// ���� ������� ��ġ��
		String birthDate = member_birth_year + member_birth_month + member_birth_day;
		dto.setBirthDate(birthDate);
		// ��й�ȣ ��ȣȭ
		String shaPw = EncryptionUtils.sha512(dto.getPw());
		dto.setPw(shaPw);
		// �Ϲ�ȸ�� ���� �� authgradecode 1003 ����
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
	
	// �� ���� ���� Ŭ�� �� ������ �̵�
	@RequestMapping("clientMyInfo")
	public String myInfo() throws Exception {
		return "/member/clientMyInfo";
	}
	
	// ��й�ȣ �Է� �� �α����� ȸ���� ��й�ȣ�� ��ġ�ϴ��� Ȯ��
	@ResponseBody
	@RequestMapping("checkPw")
	public String checkPw(String pw) throws Exception {
		String enPw = EncryptionUtils.sha512(pw);
		String id = (String) session.getAttribute("id");
		boolean result = cms.checkPw(id, enPw);
		return String.valueOf(result);
	}
	
	// ȸ������ ��������
	@ResponseBody
	@RequestMapping("selectClientMemberInfo")
	public MemberDTO selectClientMemberInfo(String id) throws Exception {
		MemberDTO dto = cms.selectClientMemberInfo(id);
		return dto;
	}
	
	// ȸ������ ������ ������ ������ �̵�
	@RequestMapping("goUpdateInfo")
	public String goUpdateInfo(String id, Model m) throws Exception {
		MemberDTO dto = cms.selectClientMemberInfo(id);
		m.addAttribute("info", dto);
		return "/member/clientInfoUpdate";
	}
	// ȸ������ ���� �� ��� ������ ����ó ������ ���ؼ��� ������....
	// �� �κ��� �α��ε� ȸ���� ����ó�� ��ȸ���� ����ó�� �Ѿ��
	@ResponseBody
	@RequestMapping("sendSmsUpdate")
	public String sendSmsUpdate(String phone) throws Exception {
				
		session.setAttribute("numStr", CoolsmsService.sendCode(phone));
			
		return String.valueOf(true);
	}
	
	// ȸ������(������Ʈ) �Է��� ���� �Ѿ���� �� 
	@RequestMapping("updateMemberInfo")
	public String updateMemberInfo(MemberDTO dto, String member_birth_year, String member_birth_month, String member_birth_day, Model m) throws Exception {
		// ���� ������� ��ġ��
		String birthDate = member_birth_year + member_birth_month + member_birth_day;
		dto.setBirthDate(birthDate);
		// ȸ�� ���� �� where = id���� id���� �ʿ���
		String id = (String) session.getAttribute("id");
		dto.setId(id);
		
		int result = cms.updateMemberInfo(dto);
		if(result == 1) {
			MemberDTO updateDto = cms.selectClientMemberInfo(id);
			// ������Ʈ�� ���� �ٽ� ���ǿ� ���
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
	
	// ȸ��Ż���ϱ�
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