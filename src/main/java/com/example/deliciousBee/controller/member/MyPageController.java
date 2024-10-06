package com.example.deliciousBee.controller.member;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import com.example.deliciousBee.model.member.Role;
import jakarta.servlet.http.Cookie;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import com.example.deliciousBee.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.mypage.MyPage;
import com.example.deliciousBee.model.mypage.MyPageVisit;
import com.example.deliciousBee.model.review.Review;
import com.example.deliciousBee.repository.MyPageFileRepository;
import com.example.deliciousBee.repository.MyPageRepository;
import com.example.deliciousBee.repository.ReviewRepository;
import com.example.deliciousBee.service.member.BeeMemberService;
import com.example.deliciousBee.service.member.FollowService;
import com.example.deliciousBee.service.member.MyPageService;
import com.example.deliciousBee.service.review.ReviewService;
import com.example.deliciousBee.util.MemberFileService;
import com.example.deliciousBee.util.MyPageFileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller // 응답 html
@RequiredArgsConstructor
@RequestMapping("member")
public class MyPageController {

	@Value("${file.upload.path}") // 폴더를 찾아서 uploadPath한테 줌
	private String uploadPath; // c드라이브 어디에저장할지 지정

	  private final ObjectMapper objectMapper; 
	private final MyPageRepository myPageRepository;
	private final MyPageService myPageService;
	private final BeeMemberService beeMemberService;
	private final HttpSession session;
	private final BCryptPasswordEncoder passwordEncoder;
	private final ReviewService reviewService;
	private final FollowService followService;
	private final ReviewRepository reviewRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final MyPageFileRepository myPageFileRepository;
	private final MyPageFileService myPageFileService;

	@Autowired
	private MemberFileService memberFileService; // fileStore 주입 받음.
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
				auth.put("username", beeMember.getUsername());
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




	// **************마이페이지 이동******************
	@GetMapping("myPage")
	public String myPage(@AuthenticationPrincipal BeeMember loginMember,
			@RequestParam(name = "id", required = false) Long id,
			@RequestParam(name = "sort", defaultValue = "createDate") String sort,
			@RequestParam(name = "page", defaultValue = "0") int page,
			Model model) {
			MyPage myPage = null;

		// 로그인한 경우
		if (loginMember != null) {
			if (id == null) {
				myPage = myPageRepository.findMyPageWithVisitsByMemberId(loginMember.getMember_id());
			} else {
				myPage = myPageService.findById(id); // 다른 사용자의 마이페이지
			}
			model.addAttribute("loginMember", loginMember);
		} else {
			// 로그인하지 않은 경우
			if (id != null) {
				myPage = myPageService.findById(id); // 다른 사용자의 마이페이지만 조회 가능
			} else {
				return "redirect:/login"; // 자신의 마이페이지를 보려면 로그인 필요
			}
		}

		handleMyPageAccess(myPage, loginMember, sort, page, model);

		return "member/myPage";
	}


	private void handleMyPageAccess(MyPage myPage, BeeMember loginMember, String sort, int page, Model model) {
		myPageService.increaseHitCount(myPage.getId()); // 조회수 증가
		myPageService.increaseVisitCount(myPage.getId(), loginMember); // 방문자 수 증가
		
		// 오늘 방문자 수 및 방문 기록 모델에 추가
		model.addAttribute("todayVisitCount", myPageService.getTodayVisitCount(myPage.getId()));
		List<MyPageVisit> visits = myPageService.getTodayMyPageVisits(myPage.getId()); // 오늘 방문 기록만 가져오도록 수정
		model.addAttribute("visitors", visits);
		List<MyPage> reviewCount = myPageRepository.findMyPagesOrderByReviewCountDesc(); // MyPageRepository를 직접 사용
		model.addAttribute("reviewCount", reviewCount);
		model.addAttribute("myPage", myPage);
		// 로그인한 사용자의 팔로잉 목록 가져오기
		if (loginMember != null) {
	        List<BeeMember> followingList = followService.getFollowingList(loginMember.getMember_id());
	        model.addAttribute("followingList", followingList);
	    }
		
		 // 로그인한 사용자인지 확인
	    boolean isOwner = loginMember != null && loginMember.getMyPage().getId().equals(myPage.getId());
	    model.addAttribute("isOwner", isOwner); // isOwner 변수를 모델에 추가
	    
	    Pageable pageable = PageRequest.of(page, 6, Sort.by(sort).descending());

	    Page<Review> reviews;
	    if ("visitDate".equals(sort)) {
	        reviews = reviewRepository.findByBeeMemberOrderByVisitDateDesc(myPage.getBeeMember(), pageable);
	    } else {
	        reviews = reviewRepository.findByBeeMemberOrderByCreateDateDesc(myPage.getBeeMember(), pageable);
	    }
	    model.addAttribute("reviews", reviews);
	    model.addAttribute("currentSort", sort); 
	    //평균 별점
	    List<Review> allMemberReviews = reviewRepository.findByBeeMember(myPage.getBeeMember());
	    double averageRating = allMemberReviews.stream()
	            .mapToInt(Review::getRating)
	            .average()
	            .orElse(0.0); // 평균 값 계산

	    model.addAttribute("averageRating", averageRating); 
		

	    
	}

	@GetMapping("randomReviews")
    @ResponseBody
    public String getRandomReviews() throws JsonProcessingException { // 예외 처리 추가
        List<Review> allReviews = reviewService.findAllReviews();
        List<Map<String, String>> randomReviews = new ArrayList<>(); // JSON에 맞게 변경

        if (!allReviews.isEmpty()) {
            // 이미지를 가진 리뷰만 필터링
            List<Review> reviewsWithImages = allReviews.stream()
                .filter(review -> review.getAttachedFile() != null && !review.getAttachedFile().isEmpty())
                .collect(Collectors.toList());

            // 리뷰 목록을 무작위로 섞음
            Collections.shuffle(reviewsWithImages);

            // 최대 100개의 리뷰를 선택
            List<Review> selectedReviews = reviewsWithImages.subList(0, Math.min(reviewsWithImages.size(), 100));

            // 선택된 리뷰의 데이터를 맵에 저장
            for (Review review : selectedReviews) {
                Map<String, String> reviewData = new HashMap<>();
                
                // 이미지 URL 설정 (이미 isEmpty() 체크를 했으므로 null 체크 필요 없음)
                reviewData.put("imageUrl", "/review/display?filename=" + review.getAttachedFile().get(0).getSaved_filename());

                // 레스토랑 이름, restaurantId, reviewId 추가
                reviewData.put("restaurantName", review.getRestaurant().getName());
                reviewData.put("restaurantId", String.valueOf(review.getRestaurant().getId()));
                reviewData.put("reviewId", String.valueOf(review.getId()));

                randomReviews.add(reviewData);
            }
        }


        return objectMapper.writeValueAsString(randomReviews);
    }
	
	// **********************마이페이지 수정하기 페이지 이동****************************
	@GetMapping("updateMyPage")
	public String goUpdateMyPage(@AuthenticationPrincipal BeeMember loginMember,
			@RequestParam(name = "sort", defaultValue = "createDate") String sort,
			@RequestParam(name = "page", defaultValue = "0") int page,
			Model model) {
		if (loginMember == null) {
			return "redirect:/member/login";
		}
		MyPage myPage = loginMember.getMyPage(); // MyPage 객체를 가져옵니다.
		model.addAttribute("myPage", myPage); // myPage를 모델에 추가합니다.
		model.addAttribute("loginMember", loginMember);
		
		Pageable pageable = PageRequest.of(page, 6, Sort.by(sort).descending());

	    Page<Review> reviews;
	    if ("visitDate".equals(sort)) {
	        reviews = reviewRepository.findByBeeMemberOrderByVisitDateDesc(myPage.getBeeMember(), pageable);
	    } else {
	        reviews = reviewRepository.findByBeeMemberOrderByCreateDateDesc(myPage.getBeeMember(), pageable);
	    }
	    model.addAttribute("reviews", reviews);
	    model.addAttribute("currentSort", sort);
		handleMyPageAccess(myPage, loginMember, sort, page, model); // "createDate" 또는 원하는 sort 값 전달
		 return "member/updateMyPage";
	}

	// **********************마이페이지에서 수정하기*********************
	@PostMapping("updateMyPage")
    public String updateMyPage(@AuthenticationPrincipal BeeMember loginMember,
                               @RequestParam(name = "introduce") String introduce,
                               @RequestParam(name = "isFileRemoved", required = false, defaultValue = "false") boolean isFileRemoved,
                               @RequestParam(name = "file", required = false) MultipartFile file,
                               RedirectAttributes redirectAttributes) {

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        try {
            myPageService.updateMyPage(introduce, isFileRemoved, file, loginMember);
            redirectAttributes.addFlashAttribute("message", "마이페이지가 성공적으로 수정되었습니다.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
        }
        return "redirect:/member/myPage";
    }
	// *******************사람들 마이페이지 리스트 **********************
	@GetMapping("myPageList")
	public String myPageList(@RequestParam(name = "searchText", defaultValue = "") String searchText,
			@PageableDefault(size = 10, sort = "id", direction = Direction.DESC) Pageable pageable, Model model) {

		Page<MyPage> myPageList = myPageService.findAll(pageable);
		model.addAttribute("myPageList", myPageList);
		int totalRecoardsCount = (int) myPageList.getTotalElements();

		List<Review> allReviews = reviewService.findAllReviews(); // 모든 리뷰 가져오기
		Random random = new Random();

		if (!allReviews.isEmpty()) {
			int randomIndex = random.nextInt(allReviews.size());
			Review randomReview = allReviews.get(randomIndex);
			model.addAttribute("randomReview", randomReview);
		}

		// 랜텀 카테고리
//		Map<CategoryType, List<Review>> randomReviewsByCategory = new HashMap<>();
//		for (CategoryType category : CategoryType.values()) {
//			List<Review> randomReviews = reviewService.getRandomReviewsByCategory(category);
//			randomReviewsByCategory.put(category, randomReviews);
//		}
//		model.addAttribute("randomReviewsByCategory", randomReviewsByCategory);

		// 리뷰 순 정리
		List<MyPage> reviewCount = myPageRepository.findMyPagesOrderByReviewCountDesc(); // MyPageRepository를 직접 사용
		model.addAttribute("reviewCount", reviewCount);
		return "member/MyPageList";

	}
	
	
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
	          auth.put("username", beeMember.getUsername());
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

}
