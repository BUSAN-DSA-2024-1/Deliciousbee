package com.example.deliciousBee.service.review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.*;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.deliciousBee.model.board.CategoryType;
import com.example.deliciousBee.model.file.AttachedFile;
import com.example.deliciousBee.model.review.Review;
import com.example.deliciousBee.repository.FileRepository;
import com.example.deliciousBee.repository.ReportRepository;
import com.example.deliciousBee.repository.ReviewRepository;
import com.example.deliciousBee.util.FileService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {
	private String uploadPath = "C:\\upload\\";

	private final ReviewRepository reviewRepository;
	private final FileRepository fileRepository;
	private final FileService fileService;
	private final ReportRepository reportRepository;

	public void saveReview(Review review, List<AttachedFile> attachedFiles) {
		if (attachedFiles != null) {
			for (AttachedFile attachedFile : attachedFiles) {
				attachedFile.setReview(review);
			}
			reviewRepository.save(review);
			fileRepository.saveAll(attachedFiles);
		} else {
			reviewRepository.save(review);
		}
	}

	public List<Review> getReviewsByRestaurantIdWithFiles(Long restaurantId, String memberId) {

		List<Review> reviews = reviewRepository.findByRestaurantId(restaurantId);
		List<Long> reportedReviewIds = reportRepository.findReportedReviewId(memberId);

		// 신고된 리뷰를 제외한 리뷰를 필터링합니다.
		List<Review> filteredReviews = reviews.stream().filter(review -> !reportedReviewIds.contains(review.getId()))
				.collect(Collectors.toList());

		for (Review review : filteredReviews) {
			List<AttachedFile> files = fileRepository.findFilesByReviewId(review.getId());
			review.setAttachedFile(files);
		}
		return filteredReviews;
	}

	// 좋아요 로직
	public int likeReview(Long reviewId) {
		Review review = reviewRepository.findById(reviewId).get();
		review.setLikeCount(review.getLikeCount() + 1);
		reviewRepository.save(review);
		return review.getLikeCount();
	}

	@Transactional
	public boolean deleteReview(Long reviewId) {
		try {
			fileRepository.deleteByReviewId(reviewId);
			reviewRepository.deleteById(reviewId);
			return true;
		} catch (Exception e) {
			log.error("Error deleting review with id: " + reviewId, e);
			return false;
		}
	}

	// 리뷰아이디 -> 리뷰
	@Transactional
	public Review findReview(Long reviewId) {
		Optional<Review> review = reviewRepository.findById(reviewId);
		return review.orElse(null);
	}

	public List<AttachedFile> findFilesByReviewId(Long reviewId) {
		return fileRepository.findFilesByReviewId(reviewId);
	}

	// 리뷰 업데이트
	public void updateReview(Review updateReview, boolean fileRemoved, MultipartFile[] files) {

		Review findReview = findReview(updateReview.getId());
		findReview.setReviewContents(updateReview.getReviewContents());
		findReview.setRating(updateReview.getRating());
		findReview.setRecommendItems(updateReview.getRecommendItems());

		// 기존 파일 가져오기
		List<AttachedFile> attachedFiles = findFilesByReviewId(findReview.getId());

		if (attachedFiles != null && fileRemoved) {
			removeFiles(attachedFiles);
			attachedFiles = null;
		}

		// 새로운 파일처리
		if (files != null && files.length > 0) {
			attachedFiles = new ArrayList<>();
			for (MultipartFile file : files) {
				if (file.isEmpty()) {
					continue;
				}
				AttachedFile savedFile = fileService.saveFile(file);
				if (savedFile != null) {
					savedFile.setReview(findReview);
					attachedFiles.add(savedFile);
				}
			}
		}

		if (attachedFiles != null && !attachedFiles.isEmpty()) {
			saveReview(findReview, attachedFiles);
		}

		reviewRepository.save(findReview);

	}

	// 첨부파일 삭제
	public void removeFiles(List<AttachedFile> attachedFiles) {
		for (AttachedFile attachedFile : attachedFiles) {
			fileRepository.deleteById(attachedFile.getAttachedFile_id());
			String fullPath = uploadPath + "/" + attachedFile.getSaved_filename();
			fileService.deleteFile(fullPath);
		}
	}

	// 수정시 첨부파일 삭제
	public boolean deleteAttachedFile(Long attachedFileId) {
		try {
			fileRepository.deleteById(attachedFileId);
			return true;
		} catch (Exception e) {
			log.error("Error deleting File with id: " + attachedFileId, e);
			return false;
		}
	}

	// 리뷰 정렬 로직
	public List<Review> sortReview(String sort, Long restaurantId, String memberId) {
		List<Review> reviews = switch (sort) {
		case "rating" -> reviewRepository.findAllByRestaurant_IdOrderByRatingDesc(restaurantId); // 특정 식당 ID로 필터링
		case "visitDate" -> reviewRepository.findAllByRestaurant_IdOrderByVisitDateDesc(restaurantId);
		case "likeCount" -> reviewRepository.findAllByRestaurant_IdOrderByLikeCountDesc(restaurantId);
		default -> getReviewsByRestaurantIdWithFiles(restaurantId, memberId);
		};
		
		for (Review review : reviews) {
			review.setAttachedFile(fileRepository.findAllByReview(review));
			review.setCanEdit(memberId.equals(review.getBeeMember().getMember_id()));
		}
		return reviews;
	}

	public List<Review> findAllReviews() {
		return reviewRepository.findAll();
	}

	// 랜덤 카테고리 
	public List<Review> getRandomReviewsByCategory(CategoryType category) {
		// 1. 해당 카테고리의 모든 리뷰를 가져옵니다.
		List<Review> reviews = reviewRepository.findByRestaurant_Category(category);

	    // 2. 리뷰가 없는 경우 빈 리스트를 반환합니다.
	    if (reviews.isEmpty()) {
	        return Collections.emptyList();
	    }

	    // 3. 랜덤 리뷰를 여러 개 선택합니다. (예: 3개)
	    Collections.shuffle(reviews); // 리스트를 랜덤하게 섞습니다.
	    int numberOfReviews = Math.min(3, reviews.size()); // 최대 3개 또는 리뷰 개수만큼 선택합니다.
	    List<Review> randomReviews = reviews.subList(0, numberOfReviews);

	    // 4. 랜덤 리뷰를 반환합니다.
	    return randomReviews;
	}

}
