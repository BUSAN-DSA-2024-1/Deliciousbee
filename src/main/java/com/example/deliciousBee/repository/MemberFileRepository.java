package com.example.deliciousBee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.deliciousBee.model.file.MemberAttachedFile;
import com.example.deliciousBee.model.member.BeeMember;

public interface MemberFileRepository extends JpaRepository<MemberAttachedFile, Long> {


	MemberAttachedFile findByBeeMember(BeeMember beeMember);
}
