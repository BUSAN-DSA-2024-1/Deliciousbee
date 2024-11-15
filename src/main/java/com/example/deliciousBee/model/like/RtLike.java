package com.example.deliciousBee.model.like;

import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.review.Review;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RtLike {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "bee_member_id")
	private BeeMember beeMember;

	@ManyToOne
	@JoinColumn(name = "restaurant_id")
	private Restaurant restaurant;

	public RtLike(BeeMember beeMember, Restaurant restaurant) {
		this.beeMember = beeMember;
		this.restaurant = restaurant;
	}
}
