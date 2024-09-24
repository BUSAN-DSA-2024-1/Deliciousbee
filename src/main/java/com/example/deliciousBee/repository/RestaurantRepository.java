package com.example.deliciousBee.repository;

import com.example.deliciousBee.model.board.CategoryType;
import com.example.deliciousBee.model.board.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.deliciousBee.model.board.Restaurant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByCategory(String category);
//    List<Restaurant> findByCategoriesContaining(CategoryType category);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Restaurant r WHERE r.id = :id")
    boolean existsById(@Param("id") Long id); 

    @Query("SELECT r FROM Restaurant r WHERE r.verificationStatus = 'PENDING'")
    List<Restaurant> findPendingRestaurants();

    @Query(value = "SELECT * FROM restaurant WHERE verification_status = 'APPROVED' ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<Restaurant> findRandom5Restaurants();

    //검색결과
    Page<Restaurant> findByNameContaining(
            String keyword, Pageable pageable);


    @Query("SELECT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) AND r.verificationStatus = 'APPROVED'")
    Page<Restaurant> searchByNameOrMenuName(@Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT r FROM Restaurant r WHERE r.verificationStatus = 'APPROVED' ORDER BY " +
            "6371 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * sin(radians(r.latitude))) ASC")
    Page<Restaurant> findAllSortedByDistance(@Param("userLatitude") Double userLatitude, @Param("userLongitude") Double userLongitude, Pageable pageable);


    @Query("SELECT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) AND r.verificationStatus = 'APPROVED' ORDER BY " +
            "6371 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * sin(radians(r.latitude))) ASC")
    Page<Restaurant> searchByNameOrMenuNameSortedByDistance(@Param("keyword") String keyword, @Param("userLatitude") Double userLatitude, @Param("userLongitude") Double userLongitude, Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) " +
            "AND r.verificationStatus = 'APPROVED' " +
            "AND 6371 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
            "sin(radians(r.latitude))) < :radius " +
            "ORDER BY 6371 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
            "sin(radians(r.latitude))) ASC")
    Page<Restaurant> searchByNameOrMenuNameWithinRadius(@Param("keyword") String keyword,
                                                        @Param("userLatitude") Double userLatitude,
                                                        @Param("userLongitude") Double userLongitude,
                                                        @Param("radius") Double radius,
                                                        Pageable pageable);



    @Query("SELECT r FROM Restaurant r WHERE r.verificationStatus = 'APPROVED' " +
            "AND 6371 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
            "sin(radians(r.latitude))) < :radius " +  // 반경 필터
            "ORDER BY 6371 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
            "sin(radians(r.latitude))) ASC")
    Page<Restaurant> findAllWithinRadius(@Param("userLatitude") Double userLatitude,
                                         @Param("userLongitude") Double userLongitude,
                                         @Param("radius") Double radius,
                                         Pageable pageable);
    @Query("SELECT r FROM Restaurant r WHERE r.verificationStatus = 'APPROVED' ORDER BY r.average_rating DESC")
    Page<Restaurant> findAllSortedByRating(Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) AND r.verificationStatus = 'APPROVED' ORDER BY r.average_rating DESC")
    Page<Restaurant> searchByNameOrMenuNameSortedByRating(@Param("keyword") String keyword, Pageable pageable);
}
