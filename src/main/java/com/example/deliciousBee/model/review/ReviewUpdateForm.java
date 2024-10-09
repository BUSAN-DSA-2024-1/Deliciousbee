package com.example.deliciousBee.model.review;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.deliciousBee.model.keyWord.ReviewKeyWord;
import com.example.deliciousBee.model.menu.ReviewMenu;

import lombok.Data;

@Data
public class ReviewUpdateForm {

	private Long reviewId;
	private String reviewContents;
	private Integer rating;
	private Integer tasteRating;
	private Integer priceRating;
	private Integer kindRating;
	private boolean fileRemoved;
	private LocalDate visitDate;
	
	private List<Long> reviewMenuListIds;
	
	// 커스텀관련
	private String customMenuName;
	private String customKeywordName;

}
