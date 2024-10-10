package com.example.deliciousBee.model.review;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.deliciousBee.model.file.AttachedFile;
import com.example.deliciousBee.service.keyWord.ReviewKeyWordService;
import com.example.deliciousBee.service.menu.MenuService;
import com.example.deliciousBee.service.restaurant.RestaurantService;
import com.example.deliciousBee.service.review.ReviewService;
import com.example.deliciousBee.util.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class ReviewConverter {

	private final FileService fileService;

	public static Review reviewWriteFormToReview(ReviewWriteForm writeForm) {
		Review review = new Review();
		review.setReviewContents(writeForm.getReviewContents());
		review.setCreateDate(LocalDate.now());
		review.setVisitDate(writeForm.getVisitDate());
		review.setLikeCount(0);
		review.setAttachedFile(writeForm.getAttachedFile());

		// 평점관련
		review.setRating(writeForm.getRating());
		review.setTasteRating(writeForm.getTasteRating());
		review.setPriceRating(writeForm.getPriceRating());
		review.setKindRating(writeForm.getKindRating());
//		review.setReviewMenuList(writeForm.getReviewMenuList());
		review.setReviewMenuList(new ArrayList<>());
		return review;
	}

	// reviewUpdateForm -> review
	@Transactional
	public static Review reviewUpdateFormToReview(ReviewUpdateForm reviewUpdateForm) {
		Review review = new Review();
		review.setId(reviewUpdateForm.getReviewId());
		review.setReviewContents(reviewUpdateForm.getReviewContents());
		review.setRating(reviewUpdateForm.getRating());
		review.setTasteRating(reviewUpdateForm.getTasteRating());
		review.setPriceRating(reviewUpdateForm.getPriceRating());
		review.setKindRating(reviewUpdateForm.getKindRating());
		review.setVisitDate(reviewUpdateForm.getVisitDate());
		return review;
	}

	// review -> reviewUpdateForm
	public static ReviewUpdateForm reviewToReviewUpdateForm(Review review) {
		ReviewUpdateForm reviewUpdateForm = new ReviewUpdateForm();
		reviewUpdateForm.setReviewId(review.getId());
		reviewUpdateForm.setReviewContents(reviewUpdateForm.getReviewContents());
		reviewUpdateForm.setRating(review.getRating());
		reviewUpdateForm.setTasteRating(review.getTasteRating());
		reviewUpdateForm.setPriceRating(review.getPriceRating());
		reviewUpdateForm.setKindRating(review.getKindRating());
		reviewUpdateForm.setVisitDate(review.getVisitDate());
		return reviewUpdateForm;
	}

}
