package com.udigo.hotel.auth.controller;

import com.udigo.hotel.member.model.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    /** ✅ 로그인 페이지 이동 */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            HttpServletRequest request,
                            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔹 로그인 상태 체크 (이미 로그인된 경우)
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            model.addAttribute("successMessage", "이미 로그인되어 있습니다.");
            return "main/main"; // ✅ Flash Attribute 대신 Model을 사용하여 로그인 성공 메시지 전달
        }

        // 🔹 로그인 실패 시 에러 메시지 전달
        if (error != null) {
            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 잘못되었습니다.");
        }


        // 🔹 로그인 전 페이지 저장 (Referer 활용)
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.contains("/auth/login")) {
            request.getSession().setAttribute("prevPage", referer);
        }

        return "auth/login";
    }

    /** ✅ 로그인 성공 후 메인 페이지 이동 */
    @GetMapping("/")
    public String mainPage(HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔹 로그인 상태 확인 후 처리
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            model.addAttribute("memberId", auth.getName());
            model.addAttribute("successMessage", "로그인에 성공했습니다!"); // ✅ Flash Attribute 대신 Model 사용

            // 🔹 로그인 전 페이지로 이동
            String prevPage = (String) request.getSession().getAttribute("prevPage");
            if (prevPage != null) {
                request.getSession().removeAttribute("prevPage");
                return "redirect:" + prevPage;
            }

            return "main/main"; // ✅ Flash Attribute를 없애고 Model을 직접 사용
        }
        return "redirect:/auth/login";
    }

    @Autowired
    private MemberService memberService;

    // 아이디 찾기 페이지
    @GetMapping("/findid")
    public String findIdPage() {
        return "auth/findid";
    }

    @PostMapping("/findid")
    public String findId(@RequestParam("email") String email, Model model) {
        String findId = String.valueOf(memberService.findIdByEmail(email));

        // 🔥 디버깅 로그 추가 (서버 콘솔에서 확인)
        System.out.println("찾은 아이디: " + findId);

        if (findId != null && !findId.isEmpty()) {
            model.addAttribute("findId", findId); // ✅ Thymeleaf에 전달
        } else {
            model.addAttribute("error", "등록된 이메일이 없습니다.");
        }

        return "auth/findid"; // ✅ 같은 페이지로 이동하여 모달창 띄우기
    }

    /** ✅ 비밀번호 찾기 페이지 */
    @GetMapping("/findpass")
    public String showFindPasswordPage() {
        return "auth/findpass";
    }

    /** ✅ 비밀번호 찾기 요청 처리 */
    @PostMapping("/findpass")
    public String processFindPassword(@RequestParam("memberId") String memberId,
                                      @RequestParam("email") String email, Model model) {
        String tempPassword = memberService.findPassword(memberId, email);

        if (tempPassword == null || tempPassword.isEmpty()) {
            model.addAttribute("error", "존재하지 않는 회원 정보입니다.");
        } else {
            model.addAttribute("success", "임시 비밀번호가 이메일로 발송되었습니다.");
        }

        return "auth/findpass";
    }


}
