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
import cc.spring.services.BusinessMemberService;
import cc.spring.services.CoolsmsService;

@Controller
@RequestMapping("/businessMember/")
public class BusinessMemberController {
	@Autowired
	private HttpSession session;

	@Autowired
	private BusinessMemberService bms;

	// 濡쒓렇�씤 李쎌쑝濡� �씠�룞
	@RequestMapping("login_form")
	public String login_form() throws Exception {
		return "member/clientLogin";
	}

	// �궗�뾽�옄 濡쒓렇�씤
	@RequestMapping("login")
	public String login(MemberDTO dto, RedirectAttributes redir) throws Exception {

		String loginPw = EncryptionUtils.sha512(dto.getPw());
		dto.setPw(loginPw);

		// �엯�젰�븳 id�� 鍮꾨�踰덊샇媛� �씪移섑븯�뒗 �쉶�썝�씠 �엳�쑝硫� �븘�옒 援щЦ �떎�뻾
		boolean memberCount = bms.existingMember(dto);

		if (memberCount) {
			// 濡쒓렇�씤 �떆 count update
			loginCountDTO ldto = new loginCountDTO(bms.selectBusinessMemberInfo(dto.getBusinessId()).getCode(), 0,
					null);
			boolean result = bms.login(ldto, dto);
			if (result) {
				// �엯�젰�븳 id�� �씪移섑븯�뒗 �쉶�썝�쓽 �젙蹂� dto濡� 媛��졇�삤湲�
				MemberDTO bmd = bms.selectBusinessMemberInfo(dto.getBusinessId());

				session.setAttribute("code", bmd.getCode());
				session.setAttribute("id", bmd.getBusinessId());
				session.setAttribute("companyName", bmd.getCompanyName());
				session.setAttribute("authGradeCode", bmd.getAuthGradeCode());
				return "redirect:/";
			}
		}
		redir.addFlashAttribute("status", "false2");
		return "redirect:/businessMember/login_form";
	}

//		鍮꾨�踰덊샇 李얘린�븷�븣 �룿踰덊샇濡� �븘�씠�뵒媛� 諛쏆븘�삤�뒗 肄붾뱶
	@RequestMapping("getIdByPhone")
	public String getIdByPhone(String phone) {
		String result = bms.getIdByPhone(phone);
		return null;
	}

	// 怨꾩젙李얘린�떆 �씤利앸쾲�샇 �옖�뜡 諛쒖넚
	@ResponseBody
	@RequestMapping(value = "sendSmsLogin", produces = "text/html;charset=utf8")
	public String sendSms2(String phone) throws Exception {
		// �씠誘� 媛��엯�븳 �뿰�씫泥섍� �엳�뒗吏� �솗�씤
		boolean result = bms.phoneCheck(phone);

		// 媛숈� �뿰�씫泥섍� DB�뿉 �뾾�쑝硫� �떎�뻾
		if (result) {
			session.setAttribute("numStr", CoolsmsService.sendCode(phone));
			session.setAttribute("phone", phone);
		}

		return String.valueOf(result);
	}

	// �씤利앸쾲�샇 �엯�젰 �썑 �씤利� 踰꾪듉 �겢由� �떆
	@ResponseBody
	@RequestMapping("certificationLogin")
	public Map<String, Object> certification2(String code) {
		String numStr = (String) session.getAttribute("numStr");

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", false);

		if (code.equals(numStr)) {
			String phone = (String) session.getAttribute("phone");
			String businessId = bms.getIdByPhone(phone);
			result.put("businessId", businessId);
			result.put("success", true);

			session.removeAttribute("phone");
			session.removeAttribute("numStr");
		}
		return result;
	}

	// 鍮꾨�踰덊샇 �옱�꽕�젙
	@ResponseBody
	@RequestMapping("changePw")
	public void changePw(MemberDTO dto) throws Exception {
		String updatePw = EncryptionUtils.sha512(dto.getPw());
		dto.setPw(updatePw);
		bms.updatePwBusiness(dto);
	}

	// 濡쒓렇�븘�썐
	@RequestMapping("logout")
	public String logout() {
		session.invalidate();
		return "redirect:/";
	}

	// 鍮꾩쫰�땲�뒪 �쉶�썝媛��엯 李쎌쑝濡� �씠�룞
	@RequestMapping("sign_form")
	public String sign_form() throws Exception {
		return "member/businessSign";
	}

	// �쉶�썝媛��엯 �떆 以묐났泥댄겕
	@ResponseBody
	@RequestMapping(value = "checkSum", produces = "text/html;charset=utf8")
	public String checkId(String key, String value) throws Exception {
		// 媛숈� �뿰�씫泥섏� �씠硫붿씪濡� �겢�씪�씠�뼵�듃�� 鍮꾩쫰�땲�뒪 �쉶�썝媛��엯 �븳 踰덉뵫 媛��뒫
		if (key.equals("PHONE") || key.equals("EMAIL")) {
			boolean result = bms.phoneAndemailDuplication(key, value);
			return String.valueOf(result);
		}

		boolean result = bms.isBusinessMember(key, value);
		return String.valueOf(result);
	}

	// �쉶�썝媛��엯 �떆 �씤利앸쾲�샇 �옖�뜡 諛쒖넚
	@ResponseBody
	@RequestMapping(value = "sendSmsSign", produces = "text/html;charset=utf8")
	public String sendSms(String phone) throws Exception {
		// �씠誘� 媛��엯�븳 �뿰�씫泥섍� �엳�뒗吏� �솗�씤
		boolean result = bms.phoneCheck(phone);

		// 媛숈� �뿰�씫泥섍� DB�뿉 �뾾�쑝硫� �떎�뻾
		if (!result) {
			session.setAttribute("numStr", CoolsmsService.sendCode(phone));
		}
		return String.valueOf(result);
	}

	// �씤利앸쾲�샇 �엯�젰 �썑 �씤利� 踰꾪듉 �겢由� �떆
	@ResponseBody
	@RequestMapping(value = "certificationSign", produces = "text/html;charset=utf8")
	public String certification(String code) throws Exception {
		String numStr = (String) session.getAttribute("numStr");

		if (numStr.equals(code)) {
			return String.valueOf(true);
		}

		else {
			return String.valueOf(false);
		}
	}

	// �씤利앸쾲�샇 �떆媛꾩큹怨� �떆 �꽭�뀡�뿉 ���옣�맂 �씤利앸쾲�샇 �궘�젣
	@ResponseBody
	@RequestMapping(value = "removeSession")
	public void removeSession() throws Exception {
		session.removeAttribute("numStr");
	}

	// �쉶�썝媛��엯 �뤌�뿉�꽌 �엯�젰�븳 媛믩뱾 �꽆�뼱�샂
	@RequestMapping("signup")
	public String signup(MemberDTO dto, String member_birth_year, String member_birth_month, String member_birth_day,
			Model m) throws Exception {
		// 諛쏆� �깮�뀈�썡�씪 �빀移섍린
		String birthDate = member_birth_year + member_birth_month + member_birth_day;
		dto.setBirthDate(birthDate);
		// 鍮꾨�踰덊샇 �븫�샇�솕
		String shaPw = EncryptionUtils.sha512(dto.getPw());
		dto.setPw(shaPw);
		// �뙋留ㅼ옄�쉶�썝 媛��엯 �떆 authgradecode 1002 �궫�엯
		dto.setAuthGradeCode(1002);

		int result = 0;
		result = bms.insertBusiness(dto);
		if (result != 0) {
			m.addAttribute("businessName", dto.getName());
			m.addAttribute("status", "complete");
			return "member/businessSign";
		} else {
			return "error";
		}

	}

	// �궡 �젙蹂� 蹂닿린 �겢由� �떆 �럹�씠吏� �씠�룞
	@RequestMapping("businessMyInfo")
	public String myInfo() throws Exception {
		return "/member/businessMyInfo";
	}

	// 鍮꾨�踰덊샇 �엯�젰 �떆 濡쒓렇�씤�븳 �쉶�썝�쓽 鍮꾨�踰덊샇�� �씪移섑븯�뒗吏� �솗�씤
	@ResponseBody
	@RequestMapping("checkPw")
	public String checkPw(String pw) throws Exception {
		String enPw = EncryptionUtils.sha512(pw);
		String id = (String) session.getAttribute("id");
		boolean result = bms.checkPw(id, enPw);
		return String.valueOf(result);
	}

	// �쉶�썝�젙蹂� 媛��졇�삤湲�
	@ResponseBody
	@RequestMapping("selectBusinessMemberInfo")
	public MemberDTO selectClientMemberInfo(String id) throws Exception {
		MemberDTO dto = bms.selectBusinessMemberInfo(id);
		return dto;
	}

	// �쉶�썝�젙蹂� �닔�젙�씠 媛��뒫�븳 �뤌�쑝濡� �씠�룞
	@RequestMapping("goUpdateInfo")
	public String goUpdateInfo(String id, Model m) throws Exception {
		MemberDTO dto = bms.selectBusinessMemberInfo(id);
		m.addAttribute("info", dto);
		return "/member/businessInfoUpdate";
	}

	// �쉶�썝�젙蹂� �닔�젙 �떆 紐⑤뱺 蹂�寃쎌� �뿰�씫泥� �씤利앹쓣 �넻�빐�꽌留� 媛��뒫�빐....
	// �씠 遺�遺꾩� 濡쒓렇�씤�맂 �쉶�썝�쓽 �뿰�씫泥섏� 鍮꾪쉶�썝�쓽 �뿰�씫泥섎쭔 �꽆�뼱�샂
	@ResponseBody
	@RequestMapping("sendSmsUpdate")
	public String sendSmsUpdate(String phone) throws Exception {

		session.setAttribute("numStr", CoolsmsService.sendCode(phone));
		
		return String.valueOf(true);
	}

	// �쉶�썝�젙蹂�(�뾽�뜲�씠�듃) �엯�젰�븳 �궡�슜 �꽆�뼱�삤�뒗 怨�
	@RequestMapping("updateMemberInfo")
	public String updateMemberInfo(MemberDTO dto, String member_birth_year, String member_birth_month,
			String member_birth_day, Model m) throws Exception {
		// 諛쏆� �깮�뀈�썡�씪 �빀移섍린
		String birthDate = member_birth_year + member_birth_month + member_birth_day;
		dto.setBirthDate(birthDate);
		// �쉶�썝 �닔�젙 �떆 where = id�뿉�꽌 id媛믪씠 �븘�슂�븿
		String id = (String) session.getAttribute("id");
		dto.setBusinessId(id);

		int result = bms.updateMemberInfo(dto);
		if (result == 1) {
			MemberDTO updateDto = bms.selectBusinessMemberInfo(id);
			// �뾽�뜲�씠�듃�맂 �젙蹂� �떎�떆 �꽭�뀡�뿉 �떞湲�
			session.setAttribute("code", updateDto.getCode());
			session.setAttribute("id", updateDto.getBusinessId());
			session.setAttribute("companyName", updateDto.getCompanyName());
			session.setAttribute("authGradeCode", updateDto.getAuthGradeCode());
			m.addAttribute("status", "complete");
			m.addAttribute("dto", updateDto);
			return "/member/businessMyInfo";
		} else {
			return "error";
		}
	}

	// �쉶�썝�깉�눜�븯湲�
	@ResponseBody
	@RequestMapping("deleteMember")
	public boolean deleteMember() throws Exception {
		int code = (int) session.getAttribute("code");

		// �뙋留ㅼ옄媛� �벑濡앺븳 怨듦뎄媛� �엳�쑝硫� 怨꾩젙 �궘�젣瑜� 留됱쓣嫄곗뿉�슂.
		boolean result1 = bms.checkGroupBuying(code);
		if (result1) {
			return true;
		} else {
			int result2 = bms.deleteMember(code);
			if (result2 == 1) {
				session.invalidate();
			}
			return false;
		}
	}
	@ExceptionHandler(Exception.class)
	public String exceptionHandler(Exception e) {
		e.printStackTrace();
		return "redirect:?/error";
	}

}
