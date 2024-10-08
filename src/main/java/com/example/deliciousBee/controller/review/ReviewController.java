package com.example.deliciousBee.controller.review;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.deliciousBee.model.file.AttachedFile;
import com.example.deliciousBee.model.keyWord.KeyWord;
import com.example.deliciousBee.model.keyWord.ReviewKeyWord;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.menu.Menu;
import com.example.deliciousBee.model.menu.ReviewMenu;
import com.example.deliciousBee.model.review.Review;
import com.example.deliciousBee.model.review.ReviewConverter;
import com.example.deliciousBee.model.review.ReviewUpdateForm;
import com.example.deliciousBee.model.review.ReviewWriteForm;
import com.example.deliciousBee.service.keyWord.ReviewKeyWordService;
import com.example.deliciousBee.service.menu.MenuService;
import com.example.deliciousBee.service.restaurant.RestaurantService;
import com.example.deliciousBee.service.review.ReviewService;
import com.example.deliciousBee.util.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/review")
public class ReviewController {

	@Value("${spring.cloud.gcp.storage.bucket}")
	private String bucketName;

	private final ReviewService reviewService;
	private final FileService fileService;
	private final RestaurantService restaurantService;
	private final MenuService menuService;
	private final ReviewKeyWordService reviewKeyWordService;

	@PostMapping("write/{restaurant_id}")
	public String postWriteReview(@Validated @ModelAttribute("writeForm") ReviewWriteForm reviewWriteForm,
			BindingResult result, @RequestParam(name = "file", required = false) MultipartFile[] files,
			@RequestParam(value = "reviewMenuList", required = false) List<Long> reviewMenuListIds,
			@AuthenticationPrincipal BeeMember loginMember, @PathVariable(name = "restaurant_id") Long restaurant_id,
			@RequestParam(value = "keywords", required = false) List<Long> selectedKeywords) {

		if (loginMember == null) {
			return "redirect:/login";
		}
		Review review = ReviewConverter.reviewWriteFormToReview(reviewWriteForm);
		review.setBeeMember(loginMember);
		review.setUserName(loginMember.getName());
		review.setRestaurant(restaurantService.findRestaurant(restaurant_id));

		// 첨부 파일 처리
		List<AttachedFile> attachedFiles = new ArrayList<>();
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					try {
						AttachedFile attachedFile = fileService.saveFile(file);
						attachedFile.setReview(review);
						attachedFiles.add(attachedFile);
					} catch (IOException e) {
						e.printStackTrace();
						return "redirect:/error";
					}
				}
			}
		}

		// 리뷰 메뉴 등록
		List<ReviewMenu> reviewMenus = new ArrayList<>();
		// 기존 메뉴 목록 처리
		if (reviewMenuListIds != null) {
			reviewMenus.addAll(reviewMenuListIds.stream().map(menuId -> {
				Menu menu = menuService.findMenuById(menuId);
				ReviewMenu reviewMenu = new ReviewMenu();
				reviewMenu.setReview(review);
				reviewMenu.setMenu(menu);
				return reviewMenu;
			}).collect(Collectors.toList()));
		}

		// 커스텀 메뉴 처리
		if (reviewWriteForm.getCustomMenuName() != null && !reviewWriteForm.getCustomMenuName().isEmpty()) {
			ReviewMenu reviewMenu = new ReviewMenu();
			reviewMenu.setReview(review);
			reviewMenu.setCustomMenuName(reviewWriteForm.getCustomMenuName());
			reviewMenus.add(reviewMenu);
		}

		review.setReviewMenuList(reviewMenus);
		reviewService.saveReview(review, attachedFiles);

		// 카테고리 처리
		if (selectedKeywords != null) {
			selectedKeywords.forEach(keywordId -> {
				KeyWord keyword = reviewKeyWordService.findById(keywordId);
				ReviewKeyWord reviewKeyWord = new ReviewKeyWord(review, keyword, null);
				reviewKeyWordService.save(reviewKeyWord);

				List<ReviewKeyWord> existingkeywords = review.getKeywords();
				existingkeywords.add(reviewKeyWord);
				review.setKeywords(existingkeywords);
			});
		}

		if (reviewWriteForm.getCustomKeywordName() != null && !reviewWriteForm.getCustomKeywordName().isEmpty()) {

			ReviewKeyWord reviewKeyWord = new ReviewKeyWord(review, null, reviewWriteForm.getCustomKeywordName());
			reviewKeyWordService.save(reviewKeyWord);

			List<ReviewKeyWord> existingKeywords = review.getKeywords();
			existingKeywords.add(reviewKeyWord);
			review.setKeywords(existingKeywords);

		}

		return "redirect:/restaurant/rtread/" + restaurant_id;
	}

	@GetMapping("/display")
	@ResponseBody
	public ResponseEntity<Resource> display(@RequestParam("filename") String filename) {
		try {
			// Google Cloud Storage 키 파일 설정
			String keyFileName = "deliciousbee-acb114448e3c.json"; // GCP 서비스 계정 키 파일명
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

	// 리뷰 좋아요 처리
	@PostMapping("/{reviewId}/like")
	@ResponseBody
	public ResponseEntity<Map<Object, Object>> likeReview(@PathVariable(name = "reviewId") Long reviewId,
			@AuthenticationPrincipal BeeMember loginMember) {
		Map<Object, Object> response = new HashMap<>();
		long likeCount = reviewService.likeReview(loginMember, reviewId);
		response.put("success", true);
		response.put("likeCount", likeCount);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{reviewId}/unlike")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> unlikeReview(@PathVariable(name = "reviewId") Long reviewId,
			@AuthenticationPrincipal BeeMember loginMember) {
		Map<String, Object> response = new HashMap<>();
		int likeCount = reviewService.unlikeReview(loginMember, reviewId);
		response.put("success", true);
		response.put("likeCount", likeCount);
		return ResponseEntity.ok(response);
	}

	// 리뷰삭제
	@PostMapping("/delete/{reviewId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable("reviewId") Long reviewId) {
		Map<String, Object> response = new HashMap<>();
		boolean success = reviewService.deleteReview(reviewId);
		response.put("success", success);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/update/{reviewId}")
	public String getUpdateReview(@AuthenticationPrincipal BeeMember loginMember,
			@PathVariable("reviewId") Long reviewId, Model model) {
		Review findReview = reviewService.findReview(reviewId);
		if (findReview == null || !findReview.getBeeMember().getMember_id().equals(loginMember.getMember_id())) {
			log.info("허용 되지 않는 접근 방식입니다");
			return "redirect:/review/allreview/";
		}

		ReviewUpdateForm reviewUpdateForm = ReviewConverter.reviewToReviewUpdateForm(findReview);
		model.addAttribute("reviewUpdateForm", reviewUpdateForm);

		List<AttachedFile> attachedFile = reviewService.findFilesByReviewId(findReview.getId());
		if (attachedFile != null) {
			model.addAttribute("file", attachedFile);
		}

		return "/review/update";
	}

	@GetMapping("/get/{reviewId}")
	@ResponseBody
	public ResponseEntity<Review> getReview(@PathVariable("reviewId") Long reviewId) {
		Review review = reviewService.findReview(reviewId);
		if (review == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(review);
	}

	// 리뷰 수정
	@PostMapping("/update/{reviewId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> postUpdateReview(@ModelAttribute ReviewUpdateForm reviewUpdateForm,
			@RequestPart(name = "updateReviewAttachedFile", required = false) MultipartFile[] updateReviewAttachedFile) {

		Review findReview = ReviewConverter.reviewUpdateFormToReview(reviewUpdateForm);
		
		// 첨부파일 처리
		List<AttachedFile> attachedFiles = new ArrayList<>();
		if (updateReviewAttachedFile != null && updateReviewAttachedFile.length > 0) {
			for (MultipartFile file : updateReviewAttachedFile) {
				if (!file.isEmpty()) {
					try {
						AttachedFile attachedFile = fileService.saveFile(file);
						attachedFile.setReview(findReview);
						attachedFiles.add(attachedFile);
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
		}

		// 메뉴 수정 처리
		List<ReviewMenu> reviewMenus = new ArrayList<>();
		List<Long> updateReviewMenuListIds = reviewUpdateForm.getReviewMenuListIds();
		if (updateReviewMenuListIds != null) {
			reviewMenus.addAll(updateReviewMenuListIds.stream().map(menuId -> {
				Menu menu = menuService.findMenuById(menuId);
				ReviewMenu reviewMenu = new ReviewMenu();
				reviewMenu.setReview(findReview);
				reviewMenu.setMenu(menu);
				return reviewMenu;
			}).collect(Collectors.toList()));
		}
		log.info("**************************************reviewMenus:{}", reviewMenus);
		log.info("**************************************FrontRindReview:{}", findReview);
		findReview.setReviewMenuList(reviewMenus);
		log.info("**************************************BackFindReview:{}", findReview);
				
		// 커스텀 키워드 처리
		if (reviewUpdateForm.getCustomKeywordName() != null && !reviewUpdateForm.getCustomKeywordName().isEmpty()) {
			ReviewKeyWord reviewKeyWord = new ReviewKeyWord(findReview, null, reviewUpdateForm.getCustomKeywordName());
			reviewKeyWordService.save(reviewKeyWord);
			List<ReviewKeyWord> existingKeywords = findReview.getKeywords();
			existingKeywords.add(reviewKeyWord);
			findReview.setKeywords(existingKeywords);
		}

		reviewService.updateReview(findReview, attachedFiles);
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		return ResponseEntity.ok(response);
	}

	// 수정시 첨부파일 삭제
	@PostMapping("/update/remove/{attachedFileId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> deleteAttachedFile(@PathVariable("attachedFileId") Long attachedFileId) {
		Map<String, Object> response = new HashMap<>();
		boolean success = reviewService.deleteAttachedFile(attachedFileId);
		response.put("success", success);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/allreview/{restaurant_id}/sort")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getSortAllReview(@PathVariable("restaurant_id") Long restaurant_id,
			@RequestParam(required = false, defaultValue = "modifiedAt", value = "sortBy") String sortBy,
			@AuthenticationPrincipal BeeMember loginMember,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		try {
			String memberId = loginMember.getMember_id();
			PageRequest pageble = PageRequest.of(page - 1, 5);
			Page<Review> reviews = reviewService.sortReview(restaurant_id, memberId, pageble, sortBy);
			
			Map<String, Object> response = new HashMap<>();
			response.put("reviews", reviews.getContent());
			response.put("currentPage", reviews.getNumber() + 1);
			response.put("totalPages", reviews.getTotalPages());
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("서버 처리 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

}