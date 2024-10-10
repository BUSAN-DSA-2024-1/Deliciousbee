package com.example.deliciousBee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.like.RtLike;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.review.Review;

@Repository
public interface LikeRtRepository extends JpaRepository<RtLike, Long> {
    boolean existsByBeeMemberAndRestaurant(BeeMember member, Restaurant restaurant);
    Optional<RtLike> findByBeeMemberAndRestaurant(BeeMember member, Restaurant restaurant);
    List<RtLike> findByBeeMember(BeeMember member); // 멤버가 좋아요한 LikeRt 목록 가져오기
     // 좋아요 삭제
 	void deleteByBeeMemberAndRestaurant(BeeMember beeMember, Restaurant restaurant);
}