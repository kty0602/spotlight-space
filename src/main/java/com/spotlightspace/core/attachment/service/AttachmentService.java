package com.spotlightspace.core.attachment.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.attachment.domain.Attachment;
import com.spotlightspace.core.attachment.dto.response.GetAttachmentResponseDto;
import com.spotlightspace.core.attachment.repository.AttachmentRepository;
import com.spotlightspace.core.event.domain.Event;
import com.spotlightspace.core.event.repository.EventRepository;
import com.spotlightspace.core.review.domain.Review;
import com.spotlightspace.core.review.repository.ReviewRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.spotlightspace.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReviewRepository reviewRepository;
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 이미(이벤트, 유저, 리뷰) 생성 후 나중에 추가 첨부파일 생성할 때 접근
    @Transactional
    public List<GetAttachmentResponseDto> createNewAttachmentList(
            List<MultipartFile> files, long tableId, TableRole tableRole, AuthUser authUser) throws IOException {
        // 해당 사용자가 맞는지 확인
        validateTableAccess(tableRole, tableId, authUser);

        List<GetAttachmentResponseDto> responseDtos = new ArrayList<>();

        for (MultipartFile file : files) {
            Attachment attachment = uploadAttachment(file, tableId, tableRole);
            responseDtos.add(GetAttachmentResponseDto.from(attachment));
        }
        return responseDtos;
    }

    // 첨부파일 하나 수정
    // 첨부파일 번호 확인 -> 올바른 신청자인지 확인 -> 삭제 -> 새로운거 생성 -> url 해당에 수정 완
    @Transactional
    public GetAttachmentResponseDto updateAttachment(
            long attachmentId, MultipartFile file, Long tableId, TableRole tableRole, AuthUser authUser
    ) throws IOException {
        // 기존 파일 존재 확인
        attachmentRepository.findByIdOrElseThrow(attachmentId);
        // 기존 파일 삭제 작업 처리
        deleteAttachment(attachmentId, tableId, tableRole, authUser);
        Attachment newAttachment = uploadAttachment(file, tableId, tableRole);
        return GetAttachmentResponseDto.from(newAttachment);
    }

    // 이벤트 생성 시 첨부파일도 같이 생성할 때 접근
    @Transactional
    public void addAttachmentList(List<MultipartFile> files, long id, TableRole tableRole) throws IOException {
        for (MultipartFile file : files) {
            saveAttachment(file, id, tableRole);
        }
    }

    // 유저, review 생성 시 첨부파일도 같이 생성할 때 접근
    @Transactional
    public void addAttachment(MultipartFile file, long id, TableRole tableRole) throws IOException {
        saveAttachment(file, id, tableRole);
    }

    //sns로그인은 image를 url로 받아옴 그것을 처리하기위한 메서드.
    @Transactional
    public void addAttachmentWithUrl(String image, long id, TableRole tableRole) {
        attachmentRepository.save(Attachment.create(image, tableRole, id));
    }

    // 해당 (유저, 리뷰, 이벤트) 삭제시 연관 삭제는 여기로
    @Transactional
    public void deleteAttachmentWithOtherTable(long tableId, TableRole tableRole) {
        // 해당 테이블 id와 tableRole이 일치하는 첨부파일 찾기
        attachmentRepository.findByTargetIdAndTableRole(tableId, tableRole)
                .ifPresent(attachment -> {
                    deleteAttachmentWithName(attachment);
                });
    }

    // 첨부파일만 접근해서 삭제할 때
    @Transactional
    public void deleteAttachment(long attachmentId, long tableId, TableRole tableRole, AuthUser authUser) {
        // 해당 첨부파일 id값으로 존재하는지 검사
        Attachment attachment = attachmentRepository.findByIdOrElseThrow(attachmentId);
        // 각 tableRole에 맞는 삭제 검증 로직 호출
        switch (tableRole) {
            case EVENT:
                validateAndDeleteAttachmentForEvent(tableId, attachment, authUser);
                break;
            case USER:
                validateAndDeleteAttachmentForUser(tableId, attachment, authUser);
                break;
            case REVIEW:
                validateAndDeleteAttachmentForReview(tableId, attachment, authUser);
                break;
            default:
                throw new ApplicationException(WRONG_TABLEROLE);
        }
    }

    // 해당 (이벤트, 리뷰, 유저)에 속한 첨부파일 리스트 가져오기
    public List<GetAttachmentResponseDto> getAttachmentList(long tableId, TableRole tableRole) {
        List<Attachment> attachments = attachmentRepository.findAllByTargetIdAndTableRole(tableId, tableRole);

        return attachments.stream()
                .map(GetAttachmentResponseDto::from)
                .collect(Collectors.toList());
    }

    // deleteAttachment메서드에서 사용
    private void validateAndDeleteAttachmentForEvent(long tableId, Attachment attachment, AuthUser authUser) {
        Event event = eventRepository.findByIdAndUserIdOrElseThrow(tableId, authUser.getUserId());
        if (attachment.getTableRole().equals(TableRole.EVENT) && attachment.getTargetId().equals(event.getId())) {
            deleteAttachmentWithName(attachment);
        } else {
            throw new ApplicationException(EVENT_NOT_FOUND);
        }
    }

    // deleteAttachment메서드에서 사용
    private void validateAndDeleteAttachmentForUser(long tableId, Attachment attachment, AuthUser authUser) {
        User user = userRepository.findByIdOrElseThrow(tableId);
        if (attachment.getTableRole().equals(TableRole.USER) && attachment.getTargetId().equals(user.getId())) {
            deleteAttachmentWithName(attachment);
        } else {
            throw new ApplicationException(USER_NOT_FOUND);
        }
    }

    // deleteAttachment메서드에서 사용
    private void validateAndDeleteAttachmentForReview(long tableId, Attachment attachment, AuthUser authUser) {
        Review review = reviewRepository.findByIdAndUserIdOrElseThrow(tableId, authUser.getUserId());
        if (attachment.getTableRole().equals(TableRole.REVIEW) && attachment.getTargetId().equals(review.getId())) {
            deleteAttachmentWithName(attachment);
        } else {
            throw new ApplicationException(REVIEW_NOT_FOUND);
        }
    }

    // file 삭제 -> url에서 이름만 분리하여 S3와 DB에서 첨부파일 삭제
    private void deleteAttachmentWithName(Attachment attachment) {
        String fileName = attachment.getUrl().substring(attachment.getUrl().lastIndexOf("/") + 1);
        amazonS3Client.deleteObject(bucket, fileName);
        attachmentRepository.delete(attachment);
    }

    // 해당 사용자가 맞는지 확인 with createNewAttachmentList 메소드
    private void validateTableAccess(TableRole tableRole, long tableId, AuthUser authUser) {
        switch (tableRole) {
            case USER -> {
                User user = userRepository.findByIdOrElseThrow(tableId);
                if (!user.getId().equals(authUser.getUserId())) {
                    throw new ApplicationException(USER_NOT_FOUND);
                }
            }
            case EVENT -> {
                Event event = eventRepository.findByIdOrElseThrow(tableId);
                if (!event.getUser().getId().equals(authUser.getUserId())) {
                    throw new ApplicationException(USER_NOT_ACCESS_EVENT);
                }
            }
            case REVIEW -> {
                Review review = reviewRepository.findByIdOrElseThrow(tableId);
                if (!review.getUser().getId().equals(authUser.getUserId())) {
                    throw new ApplicationException(USER_NOT_ACCESS_REVIEW);
                }
            }
        }
    }

    // file만 따로 생성할 때 여기 로직 타게되어있음
    private Attachment uploadAttachment(MultipartFile file, long tableId, TableRole tableRole) throws IOException {
        String randomName = UUID.randomUUID().toString().substring(0, 8);
        String fileName = randomName + file.getOriginalFilename();
        String fileUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

        // 업로드할 파일의 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        metadata.setContentDisposition("inline");

        // S3에 파일 업로드
        amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

        return attachmentRepository.save(Attachment.create(fileUrl, tableRole, tableId));
    }

    // 이벤트, 리뷰, 유저 생성할 때 같이 file 데이터도 생성 시 여기 로직 타게되어있음
    private void saveAttachment(MultipartFile file, long tableId, TableRole tableRole) throws IOException {
        String randomName = UUID.randomUUID().toString().substring(0, 8);
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
        attachmentRepository.save(Attachment.create(fileUrl, tableRole, tableId));
    }

    public String getImageUrl(Long userId, TableRole tableRole) {
        return Optional.ofNullable(attachmentRepository.findByTableRoleAndTargetId(tableRole, userId))
                .map(Attachment::getUrl)
                .orElse(null);
    }
}
