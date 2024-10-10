package com.example.deliciousBee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.deliciousBee.model.keyWord.ReviewKeyWord;
import com.example.deliciousBee.model.review.Review;

public interface ReviewKeyWordRepository extends JpaRepository<ReviewKeyWord, Long>{
	
	// 리뷰를 통하여 리뷰키워드 삭제
	void deleteAllByIdIn(List<Long> ids);

}
