package com.example.deliciousBee.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.deliciousBee.model.board.Restaurant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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


    @Query("SELECT DISTINCT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) AND r.verificationStatus = 'APPROVED'")
    Page<Restaurant> searchByNameOrMenuName(@Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT r FROM Restaurant r WHERE r.verificationStatus = 'APPROVED' ORDER BY " +
            "6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * sin(radians(r.latitude))) ASC")
    Page<Restaurant> findAllSortedByDistance(@Param("userLatitude") Double userLatitude, @Param("userLongitude") Double userLongitude, Pageable pageable);


    @Query("SELECT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) AND r.verificationStatus = 'APPROVED' ORDER BY " +
            "6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * sin(radians(r.latitude))) ASC")
    Page<Restaurant> searchByNameOrMenuNameSortedByDistance(@Param("keyword") String keyword, @Param("userLatitude") Double userLatitude, @Param("userLongitude") Double userLongitude, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) " +
            "AND r.verificationStatus = 'APPROVED' " +
            "AND 6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
            "sin(radians(r.latitude))) < :radius " +
            "ORDER BY 6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
            "sin(radians(r.latitude))) ASC")
    Page<Restaurant> searchByNameOrMenuNameWithinRadiusAsc(@Param("keyword") String keyword,
                                                           @Param("userLatitude") Double userLatitude,
                                                           @Param("userLongitude") Double userLongitude,
                                                           @Param("radius") Double radius,
                                                           Pageable pageable);
    @Query("SELECT DISTINCT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) " +
            "AND r.verificationStatus = 'APPROVED' " +
            "AND 6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
            "sin(radians(r.latitude))) < :radius " +
            "ORDER BY 6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
            "sin(radians(r.latitude)))")
    Page<Restaurant> searchByNameOrMenuNameWithinRadius(@Param("keyword") String keyword,
                                                           @Param("userLatitude") Double userLatitude,
                                                           @Param("userLongitude") Double userLongitude,
                                                           @Param("radius") Double radius,
                                                           Pageable pageable);




    @Query("SELECT DISTINCT r FROM Restaurant r WHERE r.verificationStatus = 'APPROVED' " +
            "AND 6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
            "sin(radians(r.latitude))) < :radius " +  // 반경 필터
            "ORDER BY 6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
            "sin(radians(r.latitude))) ASC")
    Page<Restaurant> findAllWithinRadius(@Param("userLatitude") Double userLatitude,
                                         @Param("userLongitude") Double userLongitude,
                                         @Param("radius") Double radius,
                                         Pageable pageable);
    @Query("SELECT DISTINCT r FROM Restaurant r WHERE r.verificationStatus = 'APPROVED' ORDER BY r.average_rating DESC")
    Page<Restaurant> findAllSortedByRating(Pageable pageable);

    @Query("SELECT DISTINCT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) AND r.verificationStatus = 'APPROVED' ORDER BY r.average_rating DESC")
    Page<Restaurant> searchByNameOrMenuNameSortedByRating(@Param("keyword") String keyword, Pageable pageable);
    
 // 1. 카테고리로 전체 레스토랑 조회
    @Query("SELECT DISTINCT r FROM Restaurant r WHERE r.verificationStatus = 'APPROVED' " +
           "AND (:categories IS NULL OR r.mainCategory IN :categories)")
    Page<Restaurant> findAllByCategory(@Param("categories") List<String> categories, Pageable pageable);

    // 2. 카테고리로 평점순 정렬
    @Query("SELECT DISTINCT r FROM Restaurant r WHERE r.verificationStatus = 'APPROVED' " +
           "AND (:categories IS NULL OR r.mainCategory IN :categories) " +
           "ORDER BY r.average_rating DESC")
    Page<Restaurant> findAllSortedByRatingAndCategory(@Param("categories") List<String> categories, Pageable pageable);

    // 3. 키워드와 카테고리로 검색
    @Query("SELECT DISTINCT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) " +
           "AND r.verificationStatus = 'APPROVED' " +
           "AND (:categories IS NULL OR r.mainCategory IN :categories)")
    Page<Restaurant> searchByNameOrMenuNameWithCategories(@Param("keyword") String keyword,
                                                          @Param("categories") List<String> categories,
                                                          Pageable pageable);

    // 4. 키워드와 카테고리로 평점순 검색
    @Query("SELECT DISTINCT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) " +
           "AND r.verificationStatus = 'APPROVED' " +
           "AND (:categories IS NULL OR r.mainCategory IN :categories) " +
           "ORDER BY r.average_rating DESC")
    Page<Restaurant> searchByNameOrMenuNameSortedByRatingAndCategory(@Param("keyword") String keyword,
                                                                     @Param("categories") List<String> categories,
                                                                     Pageable pageable);

    // 5. 키워드, 카테고리, 위치로 거리순 검색
    @Query("SELECT DISTINCT r FROM Restaurant r WHERE (r.name LIKE %:keyword% OR r.menu_name LIKE %:keyword%) " +
           "AND r.verificationStatus = 'APPROVED' " +
           "AND (:categories IS NULL OR r.mainCategory IN :categories) " +
           "AND 6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
           "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
           "sin(radians(r.latitude))) < :radius " +
           "ORDER BY 6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
           "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
           "sin(radians(r.latitude))) ASC")
    Page<Restaurant> searchByNameOrMenuNameWithinRadiusAndCategory(@Param("keyword") String keyword,
                                                                   @Param("userLatitude") Double userLatitude,
                                                                   @Param("userLongitude") Double userLongitude,
                                                                   @Param("radius") Double radius,
                                                                   @Param("categories") List<String> categories,
                                                                   Pageable pageable);

    // 6. 카테고리와 위치로 거리순 조회
    @Query("SELECT DISTINCT r FROM Restaurant r WHERE r.verificationStatus = 'APPROVED' " +
           "AND (:categories IS NULL OR r.mainCategory IN :categories) " +
           "AND 6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
           "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
           "sin(radians(r.latitude))) < :radius " +
           "ORDER BY 6371000 * acos(cos(radians(:userLatitude)) * cos(radians(r.latitude)) * " +
           "cos(radians(r.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * " +
           "sin(radians(r.latitude))) ASC")
    Page<Restaurant> findAllWithinRadiusAndCategory(@Param("userLatitude") Double userLatitude,
                                                    @Param("userLongitude") Double userLongitude,
                                                    @Param("radius") Double radius,
                                                    @Param("categories") List<String> categories,
                                                    Pageable pageable);

}
