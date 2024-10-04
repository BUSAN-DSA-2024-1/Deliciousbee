package com.example.deliciousBee.model.mypage;

import java.util.List;

import com.example.deliciousBee.model.file.AttachedFile;
import com.example.deliciousBee.model.file.MemberAttachedFile;
import com.example.deliciousBee.model.member.BeeMember;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MyPageUpdateForm {


	private Long id;

	private String introduce;

	private boolean fileRemoved; // 기본값 false 삭제됬으면 ture 반환, boolea의 getter는 get이아니라 is


	public static MyPage toMyPage(MyPageUpdateForm myPageUpdateForm) {
		MyPage myPage = new MyPage();

		myPage.setId(myPageUpdateForm.getId());
		myPage.setIntroduce(myPageUpdateForm.getIntroduce());

		return myPage;
	}
	
}
