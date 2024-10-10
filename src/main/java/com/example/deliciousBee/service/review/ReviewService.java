package com.example.deliciousBee.service.review;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.deliciousBee.model.board.CategoryType;
import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.file.AttachedFile;
import com.example.deliciousBee.model.keyWord.ReviewKeyWord;
import com.example.deliciousBee.model.like.ReviewLike;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.menu.ReviewMenu;
import com.example.deliciousBee.model.review.Review;
import com.example.deliciousBee.model.review.ReviewConverter;
import com.example.deliciousBee.repository.FileRepository;
import com.example.deliciousBee.repository.ReportRepository;
import com.example.deliciousBee.repository.RestaurantRepository;
import com.example.deliciousBee.repository.ReviewLikeRepository;
import com.example.deliciousBee.repository.ReviewMenuRepository;
import com.example.deliciousBee.repository.ReviewRepository;
import com.example.deliciousBee.service.keyWord.ReviewKeyWordService;
import com.example.deliciousBee.util.FileService;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
	private final ReviewLikeRepository likeRepository;
	private final RestaurantRepository restaurantRepository;
	private final ReviewMenuRepository reviewMenuRepository;
	private final ReviewKeyWordService reviewKeyWordService;

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
		updateRestaurantRatingAndReviewCount(review.getRestaurant());
	}

	private void updateRestaurantRatingAndReviewCount(Restaurant restaurant) {
		// 해당 레스토랑의 모든 리뷰를 조회
		List<Review> reviews = reviewRepository.findByRestaurant(restaurant);

		if (reviews.isEmpty()) {
			restaurant.setAverage_rating(0.0);
			restaurant.setReview_count(0L);
		} else {
			// 평균 평점 계산
			double averageRating = reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);

			// 리뷰 수 계산
			long reviewCount = reviews.size();

			restaurant.setAverage_rating(averageRating);
			restaurant.setReview_count(reviewCount);
		}

		// 레스토랑 정보를 저장하여 업데이트 적용
		restaurantRepository.save(restaurant);
	}

	public Page<Review> getReviewsByRestaurantIdWithFiles(Long restaurantId, String memberId, Pageable pageable,
			String sortBy) {

		Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

		Page<Review> reviews = reviewRepository.findByRestaurantId(restaurantId, sortedPageable);
		List<Long> reportedReviewIds = reportRepository.findReportedReviewId(memberId);

		List<Review> filteredReviews = reviews.stream().filter(review -> !reportedReviewIds.contains(review.getId()))
				.collect(Collectors.toList());

		for (Review review : filteredReviews) {
			List<AttachedFile> files = fileRepository.findFilesByReviewId(review.getId());
			review.setAttachedFile(files);
		}

		// filteredReviews를 Page<Review>로 변환
		Page<Review> pageFilteredReviews = new PageImpl<>(filteredReviews, pageable, reviews.getTotalElements());

		return pageFilteredReviews;
	}

	// 좋아요 로직
	public long likeReview(BeeMember beeMember, Long reviewId) {
		Review review = reviewRepository.findById(reviewId).orElseThrow();
		if (!likeRepository.existsByBeeMemberAndReview(beeMember, review)) {
			ReviewLike like = new ReviewLike(beeMember, review);
			likeRepository.save(like);
			review.setLikeCount(review.getLikeCount() + 1);
			reviewRepository.save(review);
		}
		return review.getLikeCount();
	}

	// 좋아요 취소 로직
	@Transactional
	public int unlikeReview(BeeMember beeMember, Long reviewId) {
		Review review = reviewRepository.findById(reviewId).orElseThrow();
		if (likeRepository.existsByBeeMemberAndReview(beeMember, review)) {
			likeRepository.deleteByBeeMemberAndReview(beeMember, review);
			review.setLikeCount(review.getLikeCount() - 1);
			reviewRepository.save(review);
		}
		return review.getLikeCount();
	}

	@Transactional
	public boolean deleteReview(Long reviewId) {
		try {
			if (reportRepository.existsById(reviewId)) {
				reportRepository.deleteById(reviewId);
			}
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
	@Transactional
	public void updateReview(Review updateReview, List<AttachedFile> attachedFiles) {

		Review findReview = findReview(updateReview.getId());
		updateReview.setCreateDate(findReview.getCreateDate());
		updateReview.setLikeCount(findReview.getLikeCount());
		updateReview.setUserName(findReview.getUserName());
		updateReview.setRestaurant(findReview.getRestaurant());
		updateReview.setBeeMember(findReview.getBeeMember());
		
		reviewRepository.save(updateReview);
		
		// 첨부파일 처리
		if (attachedFiles != null) {
			for (AttachedFile attachedFile : attachedFiles) {
				attachedFile.setReview(updateReview);
				fileRepository.saveAll(attachedFiles);
			}
		}

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
	public Page<Review> sortReview(Long restaurantId, String memberId, Pageable pageable, String sortBy) {
		List<Long> reportedReviewIds = reportRepository.findReportedReviewId(memberId);
		Page<Review> reviews = switch (sortBy) {
		case "rating" -> reviewRepository.findAllByRestaurant_IdOrderByRatingDesc(restaurantId, pageable); // 특정 식당 ID로
		// 필터링
		case "visitDate" -> reviewRepository.findAllByRestaurant_IdOrderByVisitDateDesc(restaurantId, pageable);
		case "likeCount" -> reviewRepository.findAllByRestaurant_IdOrderByLikeCountDesc(restaurantId, pageable);
		default -> getReviewsByRestaurantIdWithFiles(restaurantId, memberId, pageable, sortBy);
		};

		List<Review> filteredReviews = reviews.stream().filter(review -> !reportedReviewIds.contains(review.getId()))
				.collect(Collectors.toList());

		for (Review review : filteredReviews) {
			review.setAttachedFile(fileRepository.findAllByReview(review));
			review.setCanEdit(memberId.equals(review.getBeeMember().getMember_id()));
		}

		Page<Review> pageFilteredReviews = new PageImpl<>(filteredReviews, pageable, reviews.getTotalElements());
		return pageFilteredReviews;
	}

	public List<Review> findAllReviews() {
		return reviewRepository.findAll();
	}

	public Page<Review> findAll(Pageable pageable) {
		return reviewRepository.findAll(pageable);
	}

}