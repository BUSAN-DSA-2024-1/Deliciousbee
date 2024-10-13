package com.example.deliciousBee.service.restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.deliciousBee.dto.report.RestaurantVerificationDto;
import com.example.deliciousBee.dto.restaurant.RestaurantDto;
import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.board.VerificationStatus;
import com.example.deliciousBee.model.file.RestaurantAttachedFile;
import com.example.deliciousBee.model.like.ReviewLike;
import com.example.deliciousBee.model.like.RtLike;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.review.Review;
import com.example.deliciousBee.repository.BeeMemberRepository;
import com.example.deliciousBee.repository.LikeRtRepository;
import com.example.deliciousBee.repository.MenuRepository;
import com.example.deliciousBee.repository.RestaurantRepository;
import com.example.deliciousBee.repository.RtFileRepository;
import com.example.deliciousBee.util.RestaurantFileService;

import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

	private final RestaurantRepository restaurantRepository;
	private final RtFileRepository fileRepository;
	private final RestaurantFileService fileService;
	private final MenuRepository menuRepository;
	private final LikeRtRepository likeRtRepository;
	private final BeeMemberRepository beeMemberRepository;

	public void saveRestaurant(Restaurant restaurant, List<RestaurantAttachedFile> attachedFile) {
		if (attachedFile != null) {
			restaurant.setVerificationStatus(VerificationStatus.PENDING);
			restaurantRepository.save(restaurant);

			fileRepository.saveAll(attachedFile);
		} else {
			log.info("else filev파파파ㅏㅍ {}", attachedFile);
			restaurantRepository.save(restaurant);
		}

	}

	// 내가 수정함
	public Page<Restaurant> findAll(Pageable pageable) {
		return restaurantRepository.findAll(pageable);
	}

	public List<RestaurantVerificationDto> getPendingRestaurantDtos() {
		List<Restaurant> pendingRestaurants = restaurantRepository.findPendingRestaurants();
		return pendingRestaurants.stream().map(RestaurantVerificationDto::new) // Restaurant -> RestaurantReportDto 변환
				.collect(Collectors.toList());
	}

	public void updateApprove(Restaurant restaurant) {
		restaurant.setVerificationStatus(VerificationStatus.APPROVED);
		restaurantRepository.save(restaurant);
	}

	public List<Restaurant> findRandom5Restaurants() {
		return restaurantRepository.findRandom5Restaurants();
	}

	public Restaurant findRestaurant(Long id) {
		Restaurant restaurant = restaurantRepository.findById(id).get();
		return restaurant;
	}

//    public Restaurant findRestaurant(Long id) {
//        return restaurantRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid restaurant Id:" + id));
//    }

//    @Transactional
//    public void updateRestaurant(Restaurant updateRestaurant, List<RestaurantAttachedFile> attachedFiles) {
//
//        Restaurant findRestaurant = findRestaurant(updateRestaurant.getId());
//
//        findRestaurant.setName(updateRestaurant.getName());
//        findRestaurant.setAddress(updateRestaurant.getAddress());
//        findRestaurant.setPhone_number(updateRestaurant.getPhone_number());
//        findRestaurant.setOpening_hours(updateRestaurant.getOpening_hours());
//        findRestaurant.setMenu_name(updateRestaurant.getMenu_name());
//        findRestaurant.setPrice_range(updateRestaurant.getPrice_range());
//        findRestaurant.setHomepage_url(updateRestaurant.getHomepage_url());
//        findRestaurant.setDescription(updateRestaurant.getDescription());
//        findRestaurant.setLongitude(updateRestaurant.getLongitude());
//        findRestaurant.setLatitude(updateRestaurant.getLatitude());
//        findRestaurant.setUpdated_at(updateRestaurant.getUpdated_at());
//        findRestaurant.setCategory(updateRestaurant.getCategory());
//        findRestaurant.setMainCategory(updateRestaurant.getMainCategory());
//
//        restaurantRepository.save(findRestaurant);
//
//        if (attachedFiles != null && !attachedFiles.isEmpty()) {
//            for (RestaurantAttachedFile attachedFile : attachedFiles) {
//                attachedFile.setRestaurant(findRestaurant); // Set the restaurant reference
//            }
//            fileRepository.saveAll(attachedFiles);
//        }
//
//    }

	@Transactional
	public void deleteRestaurant(Long id) {
		// 레스토랑 조회
		Restaurant restaurant = restaurantRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid restaurant Id:" + id));

		// 레스토랑 ID로 연관된 파일 목록 조회
		List<RestaurantAttachedFile> attachedFiles = fileRepository.findFilesByRestaurantId(id);

		// GCS에서 파일 삭제
		for (RestaurantAttachedFile attachedFile : attachedFiles) {
			try {
				// FileService의 deleteFile 메서드를 이용해 GCS에서 파일 삭제
				boolean deleted = fileService.deleteFile(attachedFile.getSaved_filename());
				if (!deleted) {
					throw new RuntimeException("Failed to delete file from GCS: " + attachedFile.getSaved_filename());
				}
			} catch (IOException e) {
				// 파일 삭제 실패 시 예외 처리
				e.printStackTrace();
				throw new RuntimeException("Failed to delete file from GCS: " + attachedFile.getSaved_filename(), e);
			}
		}

		// 데이터베이스에서 파일 기록 삭제
		fileRepository.deleteAll(attachedFiles);

		// 레스토랑 삭제
		restaurantRepository.delete(restaurant);
	}

	// 카테고리
	public List<Restaurant> findByCategory(String category) {
		return restaurantRepository.findByCategory(category);
	}

	public RestaurantAttachedFile findFileByRestaurantId(Restaurant restaurant) {
		RestaurantAttachedFile attachedFile = fileRepository.findByRestaurant(restaurant);
		return attachedFile;
	}

	public RestaurantAttachedFile findFileByRestaurantAttachedFileId(Long id) {
		Optional<RestaurantAttachedFile> attachedFile = fileRepository.findById(id);
		return attachedFile.orElse(null);
	}

	public boolean existsById(Long id) {
		return restaurantRepository.existsById(id);
	}

	@Transactional
	public Page<Restaurant> findByNameContaining(String keyword, Pageable pageable) {
		return restaurantRepository.findByNameContaining(keyword, pageable);
	}

	public Page<Restaurant> searchByNameOrMenuName(String keyword, Pageable pageable) {
		return restaurantRepository.searchByNameOrMenuName(keyword, pageable);
	}

	public Page<RestaurantDto> searchRestaurants(String keyword, Pageable pageable, String sortBy, Double userLatitude,
			Double userLongitude, Double radius) {
		Page<Restaurant> restaurants = null;

		// radius가 null이면 기본값 설정
		if (radius == null) {
			radius = 1500.0; // 기본 반경 값
		}

		if (keyword == null || keyword.isEmpty()) {
			if ("distance".equals(sortBy) && userLatitude != null && userLongitude != null) {
				restaurants = restaurantRepository.findAllWithinRadius(userLatitude, userLongitude, radius, pageable);
			} else if ("rating".equals(sortBy)) {
				restaurants = restaurantRepository.findAllSortedByRating(pageable);
			} else {
				restaurants = restaurantRepository.findAll(pageable);
			}
		} else {
			if ("distance".equals(sortBy) && userLatitude != null && userLongitude != null) {
				restaurants = restaurantRepository.searchByNameOrMenuNameWithinRadiusAsc(keyword, userLatitude,
						userLongitude, radius, pageable);
			} else if ("rating".equals(sortBy)) {
				restaurants = restaurantRepository.searchByNameOrMenuNameSortedByRating(keyword, pageable);
			} else {
				restaurants = restaurantRepository.searchByNameOrMenuNameWithinRadius(keyword, userLatitude,
						userLongitude, radius, pageable);
			}
		}

		// 결과가 없을 경우 빈 페이지 반환
		if (restaurants == null || restaurants.getContent().isEmpty()) {
			log.info("검색 결과가 없습니다.");
			return Page.empty(pageable);
		}

		return restaurants.map(restaurant -> {
			RestaurantDto dto = new RestaurantDto(restaurant);
			if (userLatitude != null && userLongitude != null) {
				double distance = calculateDistance(userLatitude, userLongitude, restaurant.getLatitude(),
						restaurant.getLongitude());
				dto.setDistance(distance);
			}
			return dto;
		});
	}

	public Page<RestaurantDto> searchRestaurantsByCategory(String keyword, Pageable pageable, String sortBy,
			Double userLatitude, Double userLongitude, Double radius, List<String> categories) {
		Page<Restaurant> restaurants = null;

// radius가 null이면 기본값 설정
		if (radius == null) {
			radius = 1500.0; // 기본 반경 값
		}

		if (keyword == null || keyword.isEmpty()) {
			if ("distance".equals(sortBy) && userLatitude != null && userLongitude != null) {
				restaurants = restaurantRepository.findAllWithinRadiusAndCategory(userLatitude, userLongitude, radius,
						categories, pageable);
			} else if ("rating".equals(sortBy)) {
				restaurants = restaurantRepository.findAllSortedByRatingAndCategory(categories, pageable);
			} else {
				restaurants = restaurantRepository.findAllByCategory(categories, pageable);
			}
		} else {
			if ("distance".equals(sortBy) && userLatitude != null && userLongitude != null) {
				restaurants = restaurantRepository.searchByNameOrMenuNameWithinRadiusAndCategory(keyword, userLatitude,
						userLongitude, radius, categories, pageable);
			} else if ("rating".equals(sortBy)) {
				restaurants = restaurantRepository.searchByNameOrMenuNameSortedByRatingAndCategory(keyword, categories,
						pageable);
			} else {
				restaurants = restaurantRepository.searchByNameOrMenuNameWithCategories(keyword, categories, pageable);
			}
		}

// 결과가 없을 경우 빈 페이지 반환
		if (restaurants == null || restaurants.getContent().isEmpty()) {
			log.info("검색 결과가 없습니다.");
			return Page.empty(pageable);
		}

		return restaurants.map(restaurant -> {
			RestaurantDto dto = new RestaurantDto(restaurant);
			if (userLatitude != null && userLongitude != null) {
				double distance = calculateDistance(userLatitude, userLongitude, restaurant.getLatitude(),
						restaurant.getLongitude());
				dto.setDistance(distance);
			}
			return dto;
		});
	}

	private Double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		double R = 6371; // 지구의 반지름 (단위: km)
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distanceKm = R * c; // 거리 (단위: km)

		return distanceKm * 1000; // 거리 (단위: m)
	}

	public List<RestaurantAttachedFile> saveFiles(List<MultipartFile> files, Restaurant restaurant) throws IOException {
		List<RestaurantAttachedFile> attachedFiles = new ArrayList<>();

		for (MultipartFile file : files) {
			// 단일 파일을 저장하고 리스트에 추가
			RestaurantAttachedFile attachedFile = fileService.saveFile(file, restaurant);
			if (attachedFile != null) {
				attachedFiles.add(attachedFile);
			}
		}

		return attachedFiles; // List<RestaurantAttachedFile> 반환
	}

//    public void deleteMenusByRestaurantId(Long restaurantId) {
//        menuRepository.deleteByRestaurantId(restaurantId);
//    }

	public void updateRestaurant(Restaurant restaurant, List<RestaurantAttachedFile> attachedFiles) {
		// 레스토랑 정보 업데이트
		restaurantRepository.save(restaurant);

		// 첨부 파일 저장
		if (attachedFiles != null && !attachedFiles.isEmpty()) {
			for (RestaurantAttachedFile file : attachedFiles) {
				fileRepository.save(file);
			}
		}
	}

	// 좋아요 로직
	public long likeRt(BeeMember beeMember, Long restaurantId) {
		Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow();
		if (!likeRtRepository.existsByBeeMemberAndRestaurant(beeMember, restaurant)) {
			RtLike like = new RtLike(beeMember, restaurant);
			likeRtRepository.save(like);
			restaurant.setLikeCount(restaurant.getLikeCount() + 1);
			restaurantRepository.save(restaurant);
		}
		return restaurant.getLikeCount();
	}

	// 좋아요 취소 로직
	@Transactional
	public int unlikeRt(BeeMember beeMember, Long restaurantId) {
		Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow();
		if (likeRtRepository.existsByBeeMemberAndRestaurant(beeMember, restaurant)) {
			likeRtRepository.deleteByBeeMemberAndRestaurant(beeMember, restaurant);
			restaurant.setLikeCount(restaurant.getLikeCount() - 1);
			restaurantRepository.save(restaurant);
		}
		return restaurant.getLikeCount();
	}

	public List<Restaurant> findAllRestaurants() {
		return restaurantRepository.findAll();
	}
	
	public boolean isRestaurantLikedByUser(BeeMember beeMember, Long restaurantId) {
	    Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
	    if (restaurant == null || beeMember == null) {
	        return false;
	    }
	    return likeRtRepository.existsByBeeMemberAndRestaurant(beeMember, restaurant);
	}


}