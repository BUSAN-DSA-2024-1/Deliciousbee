package com.example.deliciousBee.service.member;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.deliciousBee.model.file.AttachedFile;
import com.example.deliciousBee.model.file.MemberAttachedFile;
import com.example.deliciousBee.model.file.MyPageAttachedFile;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.model.mypage.MyPage;
import com.example.deliciousBee.model.mypage.MyPageUpdateForm;
import com.example.deliciousBee.model.mypage.MyPageVisit;
import com.example.deliciousBee.model.review.Review;
import com.example.deliciousBee.repository.BeeMemberRepository;
import com.example.deliciousBee.repository.MemberFileRepository;
import com.example.deliciousBee.repository.MyPageFileRepository;
import com.example.deliciousBee.repository.MyPageRepository;
import com.example.deliciousBee.repository.MyPageVisitRepository;
import com.example.deliciousBee.util.CookieUtils;
import com.example.deliciousBee.util.MemberFileService;
import com.example.deliciousBee.util.MyPageFileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MyPageService implements UserDetailsService {

	@Value("${file.upload.path}")
	private String uploadPath;

	private final BeeMemberRepository beeMemberRepository;
	private final MyPageFileRepository myPageFileRepository;
	private final MyPageRepository myPageRepository;
	private final MyPageVisitRepository myPageVisitRepository;
	private final HttpSession session;
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final MyPageFileService myPageFileService;
	

	// *****************마이페이지 생성***************************
	@Transactional
	public void saveMyPage(MyPage myPage, MyPageAttachedFile attachedFile) {
		myPageRepository.save(myPage);

	}
	//마이페이지 수정
	@Transactional
    public void updateMyPage(String introduce, boolean isFileRemoved, MultipartFile file, BeeMember loginMember) throws IOException {
        MyPage myPage = loginMember.getMyPage();
        myPage.setIntroduce(introduce);
        log.info("확인용: {}", myPage);
        if (isFileRemoved) {
            if (myPage.getMainImage() != null) {
                myPageFileService.deleteFile(myPage.getMainImage().getSaved_filename());
                myPageFileRepository.delete(myPage.getMainImage());
                myPage.setMainImage(null);
            }
        }

        if (file != null && !file.isEmpty()) {
            // 기존 파일 삭제
            if (myPage.getMainImage() != null) {
                myPageFileService.deleteFile(myPage.getMainImage().getSaved_filename());
                myPageFileRepository.delete(myPage.getMainImage());
            }
            MyPageAttachedFile newFile = myPageFileService.saveFile(file);
            myPage.setMainImage(newFile);
            newFile.setMyPage(myPage);
            log.info("확인용: {}", myPage);
            log.info("확인용: {}", newFile);
        }

        myPageRepository.save(myPage);
    }

		private void removeExistingFile(MyPage myPage) {
		    if (myPage.getMainImage() != null) {
		        myPageFileService.deleteFile(myPage.getMainImage().getSaved_filename());
		        myPageFileRepository.delete(myPage.getMainImage());
		        myPage.setMainImage(null);
		    }
		}

    // 필요시 파일 저장 로직을 처리하는 메서드 추가
    private void saveFile(MultipartFile file) {
        // 파일 저장 로직 구현
    }
	// 사진찾기
			private MyPageAttachedFile findFileById(MyPage myPage) {
				// 쿼리 메서드
				MyPageAttachedFile attachedFile = myPageFileRepository.findByMyPage(myPage); // findBy로 시작해야 쿼리만들어줌
				
				log.info("확인용: {}", attachedFile);
				return attachedFile;
			}
			
			//첨부파일 삭제하는 메서드
				public void removeFile(MyPageAttachedFile attachedFile) {
					//첨부파일 삭제(서버, DB 둘다 삭제), 파일삭제를 요청했거나 새로운 파일이 업로드되면 기존파일삭제
					//1)DB에서 삭제
					myPageFileRepository.deleteById(attachedFile.getMainImage_id()); 
					//2)서버(로컬)에서 삭제
					String fullPath = uploadPath + "/" + attachedFile.getSaved_filename();
				}
//******************************************************
	// V4(페이징 추가) 게시물 전체 목록 *(Map->List) 변환해서 리턴
	public Page<MyPage> findAll(Pageable pageable) { // 페이징 List->Page
		Page<MyPage> page = myPageRepository.findAll(pageable);
		return page;
	}

	public MyPage findById(Long myPage_id) {
		Optional<MyPage> myPage = myPageRepository.findById(myPage_id);
		return myPage.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 마이페이지입니다."));
	}

	// **V4 첨부파일 찾기 attachedfile_id가아니라 Board_id로 찾기
//	public MemberAttachedFile findFileByMyPageId(MyPage myPage) {
//		// 쿼리 메서드
//		MemberAttachedFile memberAttachedFile = memberFileRepository.findByMyPage(myPage);
//		return memberAttachedFile;
//	}

	@Override
	public UserDetails loadUserByUsername(String member_id) throws UsernameNotFoundException {
		Optional<BeeMember> beeMember = beeMemberRepository.findByMemberid(member_id);
		if (beeMember.isPresent()) {
			return beeMember.get();
		} else {
			throw new UsernameNotFoundException("User not found with username: " + member_id);
		}
	}

	public List<MyPage> findMyPagesOrderByReviewCountDesc() {
		return null;
	}

	// *****************조회수********************
	@Transactional
	public void increaseHitCount(Long myPageId) { // loginMember 매개변수 제거
	    String HIT_COUNT_KEY = "visitedMyPage_" + myPageId;

	    // 쿠키 사용
	    String cookieValue = CookieUtils.getCookieValue(request, HIT_COUNT_KEY);
	    if (cookieValue == null) {
	        updateHitCount(myPageId);
	        CookieUtils.createCookie(response, HIT_COUNT_KEY, "visited", 60); // 1분
	    }
	}

	// 조회수 업데이트
	private void updateHitCount(Long myPageId) {
		MyPage myPage = myPageRepository.findById(myPageId)
				.orElseThrow(() -> new IllegalArgumentException("해당 마이페이지가 존재하지 않습니다: " + myPageId));
		myPage.setHit(myPage.getHit() + 1);
		myPageRepository.save(myPage);
	}

	// *****************방문자(오늘) ****************
	@Transactional
	public void increaseVisitCount(Long myPageId, BeeMember loginMember) {
		if (loginMember != null) { // 로그인한 사용자만
			MyPage myPage = myPageRepository.findById(myPageId)
					.orElseThrow(() -> new IllegalArgumentException("해당 마이페이지가 존재하지 않습니다: " + myPageId));

			// 1. 로그인한 사용자 ID와 마이페이지 주인 ID가 다를 경우에만 진행
			if (!loginMember.getMember_id().equals(myPage.getBeeMember().getMember_id())) {
				// 오늘 날짜의 방문 기록 확인 (트랜잭션 내부에서 확인)
				boolean alreadyVisitedToday = myPageVisitRepository.existsByMyPageAndVisitorAndVisitDate(myPage,
						loginMember, LocalDate.now());

				if (!alreadyVisitedToday) { // 오늘 이미 방문한 기록이 없으면
					MyPageVisit visit = new MyPageVisit(myPage, loginMember);
					myPageVisitRepository.save(visit);
					myPage.getVisits().add(visit);
				}
			}
		}
	}

	// ************** 특정 마이페이지의 오늘 방문 기록 조회 *********************
	public List<MyPageVisit> getTodayMyPageVisits(Long myPageId) {
		return myPageVisitRepository.findByMyPageIdAndVisitDate(myPageId, LocalDate.now());
	}

	// ************** 특정 마이페이지의 오늘 방문자 수 조회 *********************
	public long getTodayVisitCount(Long myPageId) {
		return myPageVisitRepository.countDistinctVisitorByMyPageIdAndVisitDate(myPageId, LocalDate.now());
	}

}
