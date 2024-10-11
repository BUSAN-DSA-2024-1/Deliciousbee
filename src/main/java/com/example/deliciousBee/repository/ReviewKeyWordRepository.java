package com.example.deliciousBee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.deliciousBee.model.keyWord.ReviewKeyWord;
import com.example.deliciousBee.model.review.Review;

public interface ReviewKeyWordRepository extends JpaRepository<ReviewKeyWord, Long>{
	
	@Modifying
	@Query("DELETE FROM ReviewKeyWord rk WHERE rk.review.id = :reviewId")
	void deleteReviewKeywordsByReviewId(@Param("reviewId") Long reviewId);

}
