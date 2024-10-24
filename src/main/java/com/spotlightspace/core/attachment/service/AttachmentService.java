package com.spotlightspace.core.attachment.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.core.attachment.domain.Attachment;
import com.spotlightspace.core.attachment.dto.GetAttachmentResponseDto;
import com.spotlightspace.core.attachment.repository.AttachmentRepository;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.review.repository.ReviewRepository;
import com.spotlightspace.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReviewRepository repository;
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 이미 생성 후 나중에 추가 첨부파일 생성할 때 접근
    @Transactional
    public List<GetAttachmentResponseDto> addNewAttachmentList(
            List<MultipartFile> files, Long tableId, TableRole tableRole, AuthUser authUser)
            throws IOException {
        // 해당 사용자가 맞는지 확인
        if (tableRole.getTableRole().equals(TableRole.USER)) {

        }
        if (tableRole.getTableRole().equals(TableRole.EVENT)) {

        }
        if (tableRole.getTableRole().equals(TableRole.REVIEW)) {

        }

        List<GetAttachmentResponseDto> responseDtos = new ArrayList<>();

        for (MultipartFile file : files) {
            String randomName = UUID.randomUUID().toString().substring(0, 8);;
            String fileName = randomName + file.getOriginalFilename();
            String fileUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            // 업로드할 파일의 메타데이터를 저장하는 객체 생성
            ObjectMetadata metadata = new ObjectMetadata();
            // 파일 콘텐츠 타입을 설정
            metadata.setContentType(file.getContentType());
            // 파일 크기 설정 -> S3에 전달할 때 파일 크기를 알 수 있게 함
            metadata.setContentLength(file.getSize());
            metadata.setContentDisposition("inline");

            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

            Attachment attachment = attachmentRepository.save(Attachment.of(fileUrl, tableRole, tableId));
            responseDtos.add(GetAttachmentResponseDto.from(attachment));
        }
        return responseDtos;
    }

    // 이벤트 생성 시 첨부파일도 같이 생성할 때 접근
    @Transactional
    public void addAttachmentList(List<MultipartFile> files, Long id, TableRole tableRole) throws IOException {
        for (MultipartFile file : files) {
            saveAttachment(file, id, tableRole);
        }
    }

    // 유저, review 생성 시 첨부파일도 같이 생성할 때 접근
    @Transactional
    public void addAttachment(MultipartFile file, Long id, TableRole tableRole) throws IOException {
        saveAttachment(file, id, tableRole);
    }

    // 해당 (유저, 리뷰, 이벤트) 삭제시 연관 삭제는 여기로
    @Transactional
    public void deleteAttachmentWithOtherTable(Long tableId, TableRole tableRole) {
        // 해당 테이블 id와 tableRole이 일치하는 첨부파일 찾기
        Attachment attachment = attachmentRepository.findByTargetIdAndTableRoleOrElseThrow(tableId, tableRole);
        String fileName = attachment.getUrl().substring(attachment.getUrl().lastIndexOf("/") + 1);
        amazonS3Client.deleteObject(bucket, fileName);
        attachmentRepository.delete(attachment);
    }

    // 첨부파일만 접근해서 삭제할 때
    @Transactional
    public void deleteAttachment(Long attachementId, Long tableId, AuthUser authUser, TableRole tableRole) {
        // 해당 첨부파일 id값으로 존재하는지 검사
        Attachment attachment = attachmentRepository.findByIdOrElseThrow(attachementId);
        // 어떤 table(유저, 이벤트, 리뷰)에 대한 건지 검사, 관련된 table repository에 접근해서 해당 객체 가져오기 (tableRole 사용)
        if (tableRole.equals(TableRole.EVENT)) {
            // 해당 접근자가 작성한 table의 id값과 authUser값과 일치하는 해당 table의 객체가 존재하는지 검사
            Event event = eventRepository.findByIdAndUserIdOrElseThrow(tableId, authUser.getUserId());
            if(attachment.getTargetId().equals(event.getId())) {
                String fileName = attachment.getUrl().substring(attachment.getUrl().lastIndexOf("/") + 1);
                amazonS3Client.deleteObject(bucket, fileName);
                attachmentRepository.delete(attachment);
            }
        }
        if (tableRole.equals(TableRole.USER)) {
            // 해당 접근자가 작성한 table의 id값과 authUser값과 일치하는 해당 table의 객체가 존재하는지 검사
        }
        if (tableRole.equals(TableRole.REVIEW)) {
            // 해당 접근자가 작성한 table의 id값과 authUser값과 일치하는 해당 table의 객체가 존재하는지 검사
        }
    }

    private void saveAttachment(MultipartFile file, Long id, TableRole tableRole) throws IOException {
        String randomName = UUID.randomUUID().toString().substring(0, 8);;
        String fileName = randomName + file.getOriginalFilename();
        String fileUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

        // 업로드할 파일의 메타데이터를 저장하는 객체 생성
        ObjectMetadata metadata = new ObjectMetadata();
        // 파일 콘텐츠 타입을 설정
        metadata.setContentType(file.getContentType());
        // 파일 크기 설정 -> S3에 전달할 때 파일 크기를 알 수 있게 함
        metadata.setContentLength(file.getSize());
        metadata.setContentDisposition("inline");

        amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);
        attachmentRepository.save(Attachment.of(fileUrl, tableRole, id));
    }

    public String getImageUrl(Long userId, TableRole tableRole) {
        Attachment attachment = attachmentRepository.findByTableRoleAndTargetId(tableRole, userId);
        return attachment.getUrl();
    }
}
