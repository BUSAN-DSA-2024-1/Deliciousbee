package com.example.deliciousBee.model.menu;

import com.example.deliciousBee.model.review.Review;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "menu", "review" })
public class ReviewMenu {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "review_id")
	@JsonBackReference
	private Review review;

	@ManyToOne
	@JoinColumn(name = "menu_id")
	private Menu menu;

	@Column(name = "custom_menu_name")
	private String customMenuName;

}
