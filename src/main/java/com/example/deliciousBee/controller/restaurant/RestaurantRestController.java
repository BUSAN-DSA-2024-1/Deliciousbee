
package com.example.deliciousBee.controller.restaurant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.deliciousBee.dto.restaurant.RestaurantDto;
import com.example.deliciousBee.model.board.Restaurant;
import com.example.deliciousBee.model.file.AttachedFile;
import com.example.deliciousBee.model.file.RestaurantAttachedFile;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.menu.Menu;
import com.example.deliciousBee.service.member.BeeMemberService;
import com.example.deliciousBee.service.restaurant.RestaurantService;
import com.example.deliciousBee.util.FileService;
import com.example.deliciousBee.util.RestaurantFileService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantRestController {

    private final RestaurantService restaurantService;
    private final BeeMemberService beeMemberService;
    private final FileService fileService;
    private final RestaurantFileService restaurantFileService;


    @GetMapping("search")
    public ResponseEntity<PagedModel<EntityModel<RestaurantDto>>> getRestaurants(
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable, // @RequestParam으로 Pageable 객체를 받음
            @RequestParam(value = "sortBy", required = false, defaultValue = "default") String sortBy,
            @RequestParam(value = "latitude", required = false) Double userLatitude,
            @RequestParam(value = "longitude", required = false) Double userLongitude,
            @RequestParam(value = "radius", required = false, defaultValue = "500") Double radius,
            @RequestParam(value = "page", defaultValue = "0") int page,
            PagedResourcesAssembler<RestaurantDto> assembler) {

        // Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sort)); // 제거

        // page 파라미터를 사용하여 Pageable 객체 수정 (필요한 경우)
        if (page > 0) {
            pageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());
        }

        Page<RestaurantDto> restaurants = restaurantService.searchRestaurants(keyword, pageable, sortBy, userLatitude, userLongitude, radius);

        return ResponseEntity.ok(assembler.toModel(restaurants));
    }


    @PostMapping("create")
    public ResponseEntity<Restaurant> createRestaurant(@AuthenticationPrincipal BeeMember loginMember,
                                                       @RequestParam("name") String name,
                                                       @RequestParam("true-address") String address,
                                                       @RequestParam("phone_number") String phoneNumber,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("categories") String categories,
                                                       @RequestParam("mainCategory") String mainCategory,
                                                       @RequestParam("latitude") Double latitude,
                                                       @RequestParam("longitude") Double longitude,
                                                       @RequestParam("menu_name[]") List<String> menuNames,
                                                       @RequestParam("price_range[]") List<String> priceRanges,
                                                       @RequestPart(name = "file", required = false) MultipartFile[] files) {

        System.out.println("달러다럴");
        // 로그인 여부 확인
        if (loginMember == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // BeeMember를 데이터베이스에서 찾음
        BeeMember findMember = beeMemberService.findMemberById(loginMember.getMember_id());
        System.out.println("Credentials path: " + this.getClass().getClassLoader().getResource("deliciousbee-acb114448e3c.json"));

        // 레스토랑 객체 생성 및 속성 설정
        Restaurant restaurant = new Restaurant();

        restaurant.setCategories(categories);
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setPhone_number(phoneNumber);
        restaurant.setDescription(description);
        restaurant.setMainCategory(mainCategory);
        restaurant.setLatitude(latitude);
        restaurant.setLongitude(longitude);
        restaurant.setMember(findMember);

        // 메뉴 추가 로직
        List<Menu> menus = new ArrayList<>();
        for (int i = 0; i < menuNames.size(); i++) {
            Menu menu = new Menu();
            menu.setName(menuNames.get(i));
            menu.setPrice(priceRanges.get(i));
            menu.setRestaurant(restaurant);
            menus.add(menu);
        }
        restaurant.setMenuList(menus);



        // 업로드한 파일 정보를 저장할 리스트
        List<RestaurantAttachedFile> attachedFiles = new ArrayList<>();

        // 파일 업로드 처리 (Google Cloud Storage)
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        // GCS에 파일 저장
                        AttachedFile uploadedFile = fileService.saveFile(file);

                        // 업로드된 파일 정보를 RestaurantAttachedFile에 매핑
                        if (uploadedFile != null) {
                            RestaurantAttachedFile attachedFile = new RestaurantAttachedFile();
                            attachedFile.setRestaurant(restaurant);
                            attachedFile.setOriginal_filename(uploadedFile.getOriginal_filename());
                            attachedFile.setSaved_filename(uploadedFile.getSaved_filename());
                            attachedFile.setFile_size(uploadedFile.getFile_size());
                            attachedFiles.add(attachedFile);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);  // 파일 저장 실패 시
                    }
                }
            }
        }

        // 레스토랑과 첨부 파일 정보를 데이터베이스에 저장
        restaurantService.saveRestaurant(restaurant, attachedFiles);

        return new ResponseEntity<>(restaurant, HttpStatus.CREATED);
    }



    @PostMapping("/{id}/update")
    public ResponseEntity<Restaurant> updateRestaurant(@AuthenticationPrincipal BeeMember loginMember,
                                                       @PathVariable Long id,
                                                       @RequestParam("name") String name,
                                                       @RequestParam("true-address") String address,
                                                       @RequestParam("phone_number") String phoneNumber,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("categories") String categories,
                                                       @RequestParam("mainCategory") String mainCategory,
                                                       @RequestParam("latitude") Double latitude,
                                                       @RequestParam("longitude") Double longitude,
                                                       @RequestParam("menu_name[]") List<String> menuNames,
                                                       @RequestParam("price_range[]") List<String> priceRanges,
                                                       @RequestParam(name = "delete_file_ids", required = false) List<Long> deleteFileIds,
                                                       @RequestPart(name = "file", required = false) MultipartFile[] files) {

        // 로그인 여부 확인
        if (loginMember == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 기존 레스토랑 정보 가져오기
        Restaurant restaurant = restaurantService.findRestaurant(id);



        // 레스토랑 정보 업데이트
        restaurant.setCategories(categories);
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setPhone_number(phoneNumber);
        restaurant.setDescription(description);
        restaurant.setMainCategory(mainCategory);
        restaurant.setLatitude(latitude);
        restaurant.setLongitude(longitude);

        // 메뉴 업데이트 로직
        // 기존 메뉴 삭제 후 새로 추가하거나, 기존 메뉴를 업데이트하는 방식 중 선택
        // 여기서는 간단히 기존 메뉴를 모두 삭제하고 새로 추가하는 방식 사용

        // 기존 메뉴 삭제
//        restaurantService.deleteMenusByRestaurantId(id);
        System.out.println("확인용");
        System.out.println(menuNames);
        // 새로운 메뉴 추가
        restaurant.getMenuList().clear(); // 기존 메뉴 삭제
        System.out.println(menuNames);
        // 새로운 메뉴 추가
        for (int i = 0; i < menuNames.size(); i++) {
            Menu menu = new Menu();
            menu.setName(menuNames.get(i));
            menu.setPrice(priceRanges.get(i));
            menu.setRestaurant(restaurant); // 양방향 관계 설정
            restaurant.getMenuList().add(menu);
        }
        // 업로드한 파일 정보를 저장할 리스트
        List<RestaurantAttachedFile> attachedFiles = new ArrayList<>();

        // 파일 업로드 처리 (Google Cloud Storage)
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        // GCS에 파일 저장
                        AttachedFile uploadedFile = fileService.saveFile(file);

                        // 업로드된 파일 정보를 RestaurantAttachedFile에 매핑
                        if (uploadedFile != null) {
                            RestaurantAttachedFile attachedFile = new RestaurantAttachedFile();
                            attachedFile.setRestaurant(restaurant);
                            attachedFile.setOriginal_filename(uploadedFile.getOriginal_filename());
                            attachedFile.setSaved_filename(uploadedFile.getSaved_filename());
                            attachedFile.setFile_size(uploadedFile.getFile_size());
                            attachedFiles.add(attachedFile);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);  // 파일 저장 실패 시
                    }
                }
            }
        }

        // 레스토랑과 첨부 파일 정보를 데이터베이스에 저장
        restaurantService.updateRestaurant(restaurant, attachedFiles);

        return new ResponseEntity<>(restaurant, HttpStatus.OK);
    }





}