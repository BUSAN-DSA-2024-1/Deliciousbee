package com.example.deliciousBee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.like.RtLike;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.review.Review;


@Repository
public interface LikeRtRepository extends JpaRepository<RtLike, Long> {
	
    boolean existsByBeeMemberAndRestaurant(BeeMember member, Restaurant restaurant);
    Optional<RtLike> findByBeeMemberAndRestaurant(BeeMember member, Restaurant restaurant);
    List<RtLike> findByBeeMember(BeeMember member); // 멤버가 좋아요한 LikeRt 목록 가져오기(황)
    Page<RtLike> findByBeeMember(BeeMember member, Pageable pageable);
    @Query("SELECT l.restaurant FROM RtLike l WHERE l.beeMember = :member")
    Page<Restaurant> findLikedRestaurantsByMember(@Param("member") BeeMember member, Pageable pageable);
     // 좋아요 삭제
 	void deleteByBeeMemberAndRestaurant(BeeMember beeMember, Restaurant restaurant);
}