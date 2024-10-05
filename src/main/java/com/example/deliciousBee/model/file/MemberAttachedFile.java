package com.example.deliciousBee.model.file;


import com.example.deliciousBee.model.member.BeeMember;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class MemberAttachedFile {

	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long profileImage_id;


	
	  @OneToOne(mappedBy = "profileImage", fetch = FetchType.EAGER)
	  @JoinColumn(name = "member_id")
	    private BeeMember beeMember;
	
	private String original_filename;  //원본 파일이름
	private String saved_filename;     //저장할 파일이름
	private Long file_size;            //파일용량
	
	public MemberAttachedFile(String original_filename, String saved_filename, Long file_size) {
		this.original_filename = original_filename;
		this.saved_filename = saved_filename;
		this.file_size = file_size;
	}
}
