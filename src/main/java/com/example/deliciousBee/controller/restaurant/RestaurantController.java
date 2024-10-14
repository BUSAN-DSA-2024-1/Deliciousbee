package com.example.deliciousBee.controller.restaurant;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.keyWord.KeyWord;
import com.example.deliciousBee.model.keyWord.KeywordCategory;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.member.Role;
import com.example.deliciousBee.model.review.Review;
import com.example.deliciousBee.security.jwt.JwtTokenProvider;
import com.example.deliciousBee.service.keyWord.ReviewKeyWordService;
import com.example.deliciousBee.service.member.BeeMemberService;
import com.example.deliciousBee.service.restaurant.RestaurantService;
import com.example.deliciousBee.service.review.ReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("restaurant")
public class RestaurantController {

	@Value("${spring.cloud.gcp.storage.bucket}")
	private String bucketName;

	private String uploadPath = "C:\\upload\\";

	private final RestaurantService restaurantService;
	private final ReviewService reviewService;
	private final ReviewKeyWordService reviewKeyWordService;
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



	@GetMapping("newfile")
	public String newfile(@AuthenticationPrincipal BeeMember loginMember, Model model) {

		return "restaurant/newfile";
	}

	@GetMapping("rtwrite")
	public String rtwriteForm(@AuthenticationPrincipal BeeMember loginMember, Model model) {
		if (loginMember == null) {
			return "redirect:/member/login";
		}
		model.addAttribute("restaurantForm", new Restaurant());
		return "restaurant/rtwrite";
	}

	// RestaurantController.java

	@GetMapping("/{id}/edit")
	public String editRestaurantForm(@AuthenticationPrincipal BeeMember loginMember,
									 @PathVariable Long id,
									 Model model) throws JsonProcessingException {
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		// 맛집 정보를 데이터베이스에서 가져옴
		Restaurant restaurant = restaurantService.findRestaurant(id);



		// 선택된 카테고리 JSON 직렬화


		System.out.println("레스토랑 확인용");
		System.out.println(restaurant.getAddress());
		System.out.println(restaurant.getMenuList());

		// 카테고리 Enum 정의
		Map<String, List<String>> categoryEnum = new HashMap<>();
		categoryEnum.put("한식", Arrays.asList("백반", "죽", "국수", "찌개", "탕", "전골", "족발", "보쌈", "한정식", "분식"));
		categoryEnum.put("일식", Arrays.asList("초밥", "회", "돈가스", "일본식카레", "일본식면요리"));
		categoryEnum.put("중식", Arrays.asList("중식"));
		categoryEnum.put("양식", Arrays.asList("파스타", "스테이크"));
		categoryEnum.put("아시안", Arrays.asList("아시안"));
		categoryEnum.put("패스트푸드", Arrays.asList("피자", "햄버거", "핫도그", "샌드위치"));
		categoryEnum.put("디저트", Arrays.asList("카페", "디저트"));

		// categories를 콤마로 분리하여 리스트로 변환
		List<String> selectedCategories = restaurant.getCategories() != null && !restaurant.getCategories().isEmpty()
				? Arrays.asList(restaurant.getCategories().split(","))
				: new ArrayList<>();

		// selectedCategories를 JSON 문자열로 변환
		ObjectMapper objectMapper = new ObjectMapper();
		String selectedCategoriesJson = objectMapper.writeValueAsString(selectedCategories);



		// 모델에 맛집 정보와 카테고리 Enum 및 선택된 카테고리 목록 추가
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("categoryEnum", categoryEnum);
		model.addAttribute("selectedCategories", selectedCategories);
		model.addAttribute("selectedCategoriesJson", selectedCategoriesJson);
		return "restaurant/rtedit"; // 수정 폼 뷰 이름
	}



	// 검색
	@GetMapping("/search")
	public String searchRestaurants(@RequestParam(value = "keyword", required = false) String keyword,
			@PageableDefault(page = 0, size = 10) Pageable pageable, Model model) {

//		Page<Restaurant> restaurants;
//		if (keyword == null || keyword.isEmpty()) {
//			// 검색어가 없는 경우 전체 레스토랑 목록 조회
//			restaurants = restaurantService.findAll(pageable);
//		} else {
//			restaurants = restaurantService.searchByNameOrMenuName(keyword, pageable);
//		}
//
//		// PageNavigator 객체 생성 및 설정
//		int countPerPage = pageable.getPageSize(); // 페이지당 글 목록 수
//		int pagePerGroup = 5; // 그룹당 페이지 수
//		int currentPage = pageable.getPageNumber() + 1; // 현재 페이지 (Pageable은 0부터 시작)
//		int totalRecordsCount = (int) restaurants.getTotalElements(); // 전체 글 수
//		int totalPageCount = restaurants.getTotalPages(); // 전체 페이지 수
//
//		PageNavigator navi = new PageNavigator(countPerPage, pagePerGroup, currentPage, totalRecordsCount, totalPageCount);
//
//		model.addAttribute("restaurants", restaurants);
		model.addAttribute("keyword", keyword);
//		model.addAttribute("navi", navi); // navi 객체를 모델에 추가

		return "restaurant/rtlist";
	}

	@GetMapping("/rtread/{restaurant_id}")
	public String read(@AuthenticationPrincipal BeeMember loginMember,
			@PathVariable("restaurant_id") Long restaurant_id, Model model,
			@RequestParam(name = "sortBy", defaultValue = "modifiedAt") String sortBy,
			@RequestParam(name = "page", defaultValue = "0") int page) { // 페이징 처리를 위한 page 파라미터 추가

		//if (loginMember == null) {
		//	return "redirect:/member/login";
		//}

		// 레스토랑 정보 가져오기
		Restaurant restaurant = restaurantService.findRestaurant(restaurant_id);
		if (restaurant == null) {
			return "redirect:/shop/index";
		}
		model.addAttribute("restaurant", restaurant);
		
		// 로그인 여부 확인
	    boolean isLoggedIn = loginMember != null;
	    model.addAttribute("isLoggedIn", isLoggedIn);

	    // 사용자가 해당 레스토랑을 좋아요 했는지 여부 확인
	    boolean isLiked = false;
	    if (isLoggedIn) {
	        isLiked = restaurantService.isRestaurantLikedByUser(loginMember, restaurant_id);
	    }
	    model.addAttribute("isLiked", isLiked);
	    
//		// 리뷰 정보 가져오기
//		String memberId = loginMember.getMember_id();
//		Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
//		Pageable pageable = PageRequest.of(page, 5, sort);
//		Page<Review> reviewsByRestaurant = reviewService.sortReview(restaurant_id, memberId, pageable, sortBy);
//		model.addAttribute("reviewsByRestaurant", reviewsByRestaurant.getContent());
//		model.addAttribute("currentPage", page);
//		model.addAttribute("totalPages", reviewsByRestaurant.getTotalPages());

		// 카테고리 가져오기
		Map<KeywordCategory, List<KeyWord>> keywordsByCategory = reviewKeyWordService.getKeywordsByCategory();
		model.addAttribute("keywordsByCategory", keywordsByCategory);
		return "restaurant/rtread";
	}


	// **수정 폼을 보여주는 GET 메서드**
	@GetMapping("/rtedit/{restaurant_id}")
	public String editForm(@AuthenticationPrincipal BeeMember loginMember,
						   @PathVariable("restaurant_id") Long restaurant_id, Model model) {

		if (loginMember == null) {
			return "redirect:/member/login";
		}

		// 레스토랑 정보 가져오기
		Restaurant restaurant = restaurantService.findRestaurant(restaurant_id);
		if (restaurant == null) {
			return "redirect:/shop/index";
		}



		model.addAttribute("restaurant", restaurant);
		return "restaurant/rtedit"; // 수정 폼 뷰 이름
	}






		@GetMapping("rtdelete")
		public String remove(@AuthenticationPrincipal BeeMember loginMember,
				@RequestParam(name = "id", required = false) Long id) {

			Restaurant restaurant = restaurantService.findRestaurant(id);

			if (restaurant == null || !restaurant.getMember().getMember_id().equals(loginMember.getMember_id())) {
				return "redirect:/";
			}

			restaurantService.deleteRestaurant(restaurant.getId());

			return "redirect:/";
		}

	@GetMapping("rtupdate")
	public String rtUpdate(@AuthenticationPrincipal BeeMember loginMember, @RequestParam(name = "id") Long id,
			Model model) {
		log.info("{}", loginMember);
		if (loginMember == null) {
			return "redirect:/member/login";
		}
		Restaurant findRestaurant = restaurantService.findRestaurant(id);
//		if(findRestaurant == null || !findRestaurant.getMember().getMember_id().equals(loginMember.getMember_id())) {
//
//			return "redirect:/shop/index";
//		}

		model.addAttribute("restaurantForm", findRestaurant);
		return "restaurant/rtupdate";
	}

//	@PostMapping("rtupdates")
//	public String update(@Validated @ModelAttribute("restaurantForm") Restaurant update
//			,BindingResult result
//	) {
//
//		if(result.hasErrors()) {
//			return "restaurant/rtupdate";
//		}
//		//수정
//		Restaurant updateRestaurant = Restaurant.toRestaurant(update);
//
//		restaurantService.updateRestaurant(updateRestaurant);
//
//		//수정되면
//		return "redirect:/";
//	}

	@GetMapping("/display")
	public ResponseEntity<Resource> display(@RequestParam("filename") String filename) {
		try {
			// Google Cloud Storage 키 파일 설정
			String keyFileName = "deliciousbee-8dc8626d1aad.json"; // GCP 서비스 계정 키 파일명
			InputStream keyFile = getClass().getResourceAsStream("/" + keyFileName);

			// Google Cloud Storage 클라이언트 생성
			Storage storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(keyFile)).build()
					.getService();

			// 파일을 GCS에서 가져오기
			Blob blob = storage.get(bucketName, filename);

			if (blob == null || !blob.exists()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

			// Blob의 데이터를 ByteArrayResource로 변환
			Resource resource = new ByteArrayResource(blob.getContent());

			// 헤더 설정
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", blob.getContentType());

			return new ResponseEntity<>(resource, headers, HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
			//레스토랑 좋아요(황)
			@PostMapping("/{restaurantId}/like")
			@ResponseBody
			public ResponseEntity<Map<Object, Object>> likeRt(@PathVariable(name = "restaurantId") Long restaurantId,
					@AuthenticationPrincipal BeeMember loginMember) {
				Map<Object, Object> response = new HashMap<>();
				long likeCount = restaurantService.likeRt(loginMember, restaurantId);
				response.put("success", true);
				response.put("likeCount", likeCount);
				return ResponseEntity.ok(response);
			}

			@PostMapping("/{restaurantId}/unlike")
			@ResponseBody
			public ResponseEntity<Map<String, Object>> unlikeReview(@PathVariable(name = "restaurantId") Long restaurantId,
					@AuthenticationPrincipal BeeMember loginMember) {
				Map<String, Object> response = new HashMap<>();
				int likeCount = restaurantService.unlikeRt(loginMember, restaurantId);
				response.put("success", true);
				response.put("likeCount", likeCount);
				return ResponseEntity.ok(response);
			}

}
