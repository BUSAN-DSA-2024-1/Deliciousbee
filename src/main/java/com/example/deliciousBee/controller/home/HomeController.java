package com.example.deliciousBee.controller.home;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.member.Role;
import com.example.deliciousBee.security.jwt.JwtTokenProvider;
import com.example.deliciousBee.service.member.BeeMemberService;
import com.example.deliciousBee.service.restaurant.RestaurantService;
import com.example.deliciousBee.service.review.ReviewService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.review.Review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
	
	

	private final RestaurantService restaurantService;
	private final JwtTokenProvider jwtTokenProvider;
	private final BeeMemberService beeMemberService;


	@ModelAttribute("auth") // "auth"라는 이름으로 모델에 추가
	public Map<String, Object> authenticationDetails(HttpServletRequest request) {
		String token = extractJwtFromRequest(request); // 요청에서 JWT 추출 (아래 설명 참조)

		Map<String, Object> auth = new HashMap<>();
		auth.put("isAuthenticated", false); // 기본값 false
		auth.put("isAdmin", false); // 기본값 false
		auth.put("username", ""); // 빈 문자열로 초기화

		if (token != null && jwtTokenProvider.validateToken(token)) {
			String memberId = jwtTokenProvider.getMemberIdFromJWT(token);
			BeeMember beeMember = beeMemberService.findMemberById(memberId);

			if (beeMember != null) {
				auth.put("isAuthenticated", true);
				auth.put("isAdmin", beeMember.getRole() == Role.ADMIN); // Enum 직접 비교
				auth.put("username", beeMember.getNickname());
			}
		}
		return auth;
	}

	// 요청에서 JWT 추출 (Authorization 헤더 또는 쿠키)
	private String extractJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}

		// Authorization 헤더에 없으면 쿠키에서 확인
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("Authorization".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}

		return null; // 토큰 없음
	}
	@GetMapping("/")
	public String restaurantlist(Model model) {
		List<Restaurant> restaurantlist = restaurantService.findRandom5Restaurants();
		model.addAttribute("restaurantlist", restaurantlist);
		return "shop/index";
	}

	@GetMapping("/admin")
	public String adminPage() {
		return "admin/adminpage"; //
	}


	@GetMapping("/message")
	public String messagePage() {return "shop/message";}


	@GetMapping("login")
	public String loginForm() {
		return "member/loginForm";
	}


}
