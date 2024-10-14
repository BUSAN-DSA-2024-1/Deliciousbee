package com.example.deliciousBee.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.deliciousBee.model.file.MemberAttachedFile;
import com.example.deliciousBee.model.member.BeeMember;
import com.example.deliciousBee.repository.MemberFileRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;


@Service
public class MemberFileService {
	@Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

	
    private final ResourceLoader resourceLoader;
    

    public MemberFileService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public MemberAttachedFile saveFile(MultipartFile mfile) throws IOException {
        if (mfile == null || mfile.isEmpty()) {
            return null;
        }

        // Google Cloud Storage 키 파일 설정
        String keyFileName = "deliciousbee-8dc8626d1aad.json";  // GCP 서비스 계정 키 파일명
        Resource resource = resourceLoader.getResource("classpath:" + keyFileName);
        InputStream keyFile = resource.getInputStream();

        // Google Cloud Storage 클라이언트 생성
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build()
                .getService();

        // 원본 파일명
        String originalFilename = mfile.getOriginalFilename();

        // GCS에 저장할 고유 파일명 생성
        String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // GCS에 파일 업로드
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, savedFilename)
                .setContentType(mfile.getContentType())
                .build();

        Blob blob = storage.create(blobInfo, mfile.getInputStream());

        // 업로드된 파일의 정보를 AttachedFile 객체로 반환
        return new MemberAttachedFile(originalFilename, savedFilename, mfile.getSize());
    }

    public boolean deleteFile(String savedFilename) {
        try {
            // Google Cloud Storage 클라이언트 생성 (같은 방식으로 생성)
            String keyFileName = "deliciousbee-8dc8626d1aad.json";  // GCP 서비스 계정 키 파일명
            Resource resource = resourceLoader.getResource("classpath:" + keyFileName);
            InputStream keyFile = resource.getInputStream();

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(keyFile))
                    .build()
                    .getService();

            // GCS에서 파일 삭제
            boolean deleted = storage.delete(bucketName, savedFilename);
            return deleted;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Autowired
    private MemberFileRepository memberFileRepository;
	public MemberAttachedFile findFileByMemberId(BeeMember findMember) {
		return memberFileRepository.findByBeeMember(findMember);
	}

	public void removeFile(MemberAttachedFile profileImage) {
	    // 1. Google Cloud Storage에서 파일 삭제
	    String filename = profileImage.getSaved_filename(); // 저장된 파일 이름
	    String bucketName = "your-bucket-name"; // Google Cloud Storage의 버킷 이름

	    try {
	        Storage storage = StorageOptions.getDefaultInstance().getService();
	        BlobId blobId = BlobId.of(bucketName, filename);
	        storage.delete(blobId); // 파일 삭제
	    } catch (Exception e) {
	    }

	    // 2. DB에서 파일 정보 삭제
	    memberFileRepository.deleteById(profileImage.getProfileImage_id());
	}
}

