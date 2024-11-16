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
import com.spotlightspace.core.review.repository.ReviewRepository;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static com.spotlightspace.common.exception.ErrorCode.EVENT_NOT_FOUND;
import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;
import static com.spotlightspace.core.data.EventTestData.testEvent;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.spotlightspace.core.data.UserTestData.testAuthUser;
import static com.spotlightspace.core.data.UserTestData.testUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttachmentServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private AmazonS3Client amazonS3Client;

    @Spy
    @InjectMocks
    private AttachmentService attachmentService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Nested
    @DisplayName("첨부파일 등록 시")
    class createAttachments {

        @Test
        @DisplayName("(유저) 첨부파일리스트 등록 성공")
        void createAttachmentForUser_success() throws IOException {

            // given
            AuthUser authUser = testAuthUser();
            User user = testUser();
            ReflectionTestUtils.setField(user, "id", 1L);
            List<MultipartFile> files = List.of(mock(MultipartFile.class));
            TableRole tableRole = TableRole.USER;
            Long tableId = user.getId();

            given(userRepository.findByIdOrElseThrow(tableId)).willReturn(user);

            for (MultipartFile file : files) {
                given(file.getOriginalFilename()).willReturn("test.jpg");
                given(file.getContentType()).willReturn("image/jpeg");
                given(file.getSize()).willReturn(1024L);
                given(file.getInputStream()).willReturn(mock(InputStream.class));
            }

            String randomName = UUID.randomUUID().toString().substring(0, 8);
            String fileName = randomName + "test.jpg";
            String url = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            doAnswer(invocation -> null).when(amazonS3Client)
                    .putObject(eq(bucket), anyString(), any(InputStream.class), any(ObjectMetadata.class));
            Attachment attachment = Attachment.of(url, tableRole, tableId);
            given(attachmentRepository.save(any(Attachment.class))).willReturn(attachment);

            // when
            List<GetAttachmentResponseDto> responseDtos = attachmentService.addNewAttachmentList(files, tableId, tableRole, authUser);

            // then
            assertNotNull(responseDtos);
            assertEquals(1, responseDtos.size());
            assertEquals(attachment.getUrl(), responseDtos.get(0).getUrl());
        }

        @Test
        @DisplayName("(이벤트) 첨부파일 리스트 등록 성공")
        void createAttachmentForEvent_success() throws IOException {

            // given
            AuthUser authUser = testAuthUser();
            Event event = testEvent();
            List<MultipartFile> files = List.of(mock(MultipartFile.class));
            TableRole tableRole = TableRole.EVENT;
            Long tableId = event.getId();

            given(eventRepository.findByIdOrElseThrow(tableId)).willReturn(event);

            for (MultipartFile file : files) {
                given(file.getOriginalFilename()).willReturn("test.jpg");
                given(file.getContentType()).willReturn("image/jpeg");
                given(file.getSize()).willReturn(1024L);
                given(file.getInputStream()).willReturn(mock(InputStream.class));
            }

            String randomName = UUID.randomUUID().toString().substring(0, 8);
            String fileName = randomName + "test.jpg";
            String url = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            doAnswer(invocation -> null).when(amazonS3Client)
                    .putObject(eq(bucket), anyString(), any(InputStream.class), any(ObjectMetadata.class));
            Attachment attachment = Attachment.of(url, tableRole, tableId);
            given(attachmentRepository.save(any(Attachment.class))).willReturn(attachment);

            // when
            List<GetAttachmentResponseDto> responseDtos = attachmentService.addNewAttachmentList(files, tableId, tableRole, authUser);

            // then
            assertNotNull(responseDtos);
            assertEquals(1, responseDtos.size());
            assertEquals(attachment.getUrl(), responseDtos.get(0).getUrl());
        }

        @Test
        @DisplayName("(유저) 권한이 없는자가 첨부파일 등록")
        void createAttachment_NotMatchUser() {

            // given
            AuthUser authUser = testAuthUser();
            User user = testUser();
            ReflectionTestUtils.setField(user, "id", 2L);
            List<MultipartFile> files = List.of(mock(MultipartFile.class));
            TableRole tableRole = TableRole.USER;
            Long tableId = user.getId();

            given(userRepository.findByIdOrElseThrow(tableId)).willReturn(user);

            // when
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                attachmentService.addNewAttachmentList(files, tableId, tableRole, authUser);
            });

            // then
            assertEquals("존재하지 않는 유저입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("(이벤트) 권한이 없는자가 첨부파일 등록")
        void createAttachment_NotMatchEvent() {

            // given
            AuthUser authUser = testAuthUser();
            ReflectionTestUtils.setField(authUser, "userId", 2L);
            Event event = testEvent();
            List<MultipartFile> files = List.of(mock(MultipartFile.class));
            TableRole tableRole = TableRole.EVENT;
            Long tableId = event.getId();

            given(eventRepository.findByIdOrElseThrow(tableId)).willReturn(event);

            // when
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                attachmentService.addNewAttachmentList(files, tableId, tableRole, authUser);
            });

            // then
            assertEquals("해당 사용자가 등록한 이벤트가 아닙니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("첨부파일 수정 시")
    class UpdateAttachments {

        @Test
        @DisplayName("첨부파일 수정 성공")
        void updateAttachment() throws IOException {

            // given
            Long tableId = 1L;
            TableRole tableRole = TableRole.USER;
            AuthUser authUser = testAuthUser();

            Attachment attachment = Attachment.of("existing-url", tableRole, tableId);
            ReflectionTestUtils.setField(attachment, "id", 1L);
            given(attachmentRepository.findByIdOrElseThrow(attachment.getId())).willReturn(attachment);

            MultipartFile file = mock(MultipartFile.class);
            given(file.getOriginalFilename()).willReturn("test.jpg");
            given(file.getContentType()).willReturn("image/jpeg");
            given(file.getSize()).willReturn(1024L);
            given(file.getInputStream()).willReturn(mock(InputStream.class));

            doNothing().when(attachmentService).deleteAttachment(attachment.getId(), tableId, tableRole, authUser);

            String randomName = UUID.randomUUID().toString().substring(0, 8);
            String fileName = randomName + "test.jpg";
            String url = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            doAnswer(invocation -> null).when(amazonS3Client)
                    .putObject(eq(bucket), anyString(), any(InputStream.class), any(ObjectMetadata.class));

            Attachment newAttachment = Attachment.of(url, tableRole, tableId);
            given(attachmentRepository.save(any(Attachment.class))).willReturn(newAttachment);

            // when
            GetAttachmentResponseDto responseDto =
                    attachmentService.updateAttachment(attachment.getId(), file, tableId, tableRole, authUser);

            // then
            assertNotNull(responseDto);
            assertEquals(url, responseDto.getUrl());
        }
    }

    @Nested
    @DisplayName("(이벤트, 리뷰, 유저) 등록 시 첨부파일 연관 등록")
    class createAttachment_withAnotherClass {

        @Test
        @DisplayName("첨부파일 리스트 추가 성공")
        void addAttachmentList_success() throws IOException {

            // given
            Long tableId = 1L;
            TableRole tableRole = TableRole.USER;

            MultipartFile file1 = mock(MultipartFile.class);
            given(file1.getOriginalFilename()).willReturn("test1.jpg");
            given(file1.getContentType()).willReturn("image/jpeg");
            given(file1.getSize()).willReturn(1024L);
            given(file1.getInputStream()).willReturn(mock(InputStream.class));

            MultipartFile file2 = mock(MultipartFile.class);
            given(file2.getOriginalFilename()).willReturn("test2.jpg");
            given(file2.getContentType()).willReturn("image/jpeg");
            given(file2.getSize()).willReturn(2048L);
            given(file2.getInputStream()).willReturn(mock(InputStream.class));

            List<MultipartFile> files = List.of(file1, file2);

            String randomName1 = UUID.randomUUID().toString().substring(0, 8);
            String fileName1 = randomName1 + "test1.jpg";
            String url1 = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName1;

            String randomName2 = UUID.randomUUID().toString().substring(0, 8);
            String fileName2 = randomName2 + "test2.jpg";
            String url2 = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName2;

            doAnswer(invocation -> null).when(amazonS3Client)
                    .putObject(eq(bucket), anyString(), any(InputStream.class), any(ObjectMetadata.class));

            given(attachmentRepository.save(any(Attachment.class)))
                    .willReturn(Attachment.of(url1, tableRole, tableId))
                    .willReturn(Attachment.of(url2, tableRole, tableId));

            // when
            attachmentService.addAttachmentList(files, tableId, tableRole);

            // then
            verify(amazonS3Client, times(2))
                    .putObject(eq(bucket), anyString(), any(InputStream.class), any(ObjectMetadata.class));
            verify(attachmentRepository, times(2)).save(any(Attachment.class));
        }

        @Test
        @DisplayName("첨부파일 추가 성공")
        void addAttachment_success() throws IOException {

            // given
            Long tableId = 1L;
            TableRole tableRole = TableRole.USER;

            MultipartFile file = mock(MultipartFile.class);
            given(file.getOriginalFilename()).willReturn("test3.jpg");
            given(file.getContentType()).willReturn("image/jpeg");
            given(file.getSize()).willReturn(1024L);
            given(file.getInputStream()).willReturn(mock(InputStream.class));


            String randomName = UUID.randomUUID().toString().substring(0, 8);
            String fileName = randomName + "test3.jpg";
            String url = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            doAnswer(invocation -> null).when(amazonS3Client)
                    .putObject(eq(bucket), anyString(), any(InputStream.class), any(ObjectMetadata.class));


            given(attachmentRepository.save(any(Attachment.class))).willReturn(Attachment.of(url, tableRole, tableId));

            // when
            attachmentService.addAttachment(file, tableId, tableRole);

            // then
            verify(amazonS3Client, times(1))
                    .putObject(eq(bucket), anyString(), any(InputStream.class), any(ObjectMetadata.class));
            verify(attachmentRepository, times(1)).save(any(Attachment.class));
        }
    }

    @Nested
    @DisplayName("(이벤트, 유저, 리뷰) 삭제 시 연관 첨부파일 삭제")
    class deleteAttachments_withAnotherClass {

        @Test
        @DisplayName("첨부파일 연관 삭제 성공")
        void deleteAttachment_success() {

            // given
            Long tableId = 1L;
            TableRole tableRole = TableRole.USER;

            String randomName = UUID.randomUUID().toString().substring(0, 8);
            String fileName = randomName + "test4.jpg";
            String url = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            Attachment attachment = Attachment.of(url, tableRole, tableId);

            given(attachmentRepository.findByTargetIdAndTableRole(tableId, tableRole)).willReturn(Optional.of(attachment));

            // when
            attachmentService.deleteAttachmentWithOtherTable(tableId, tableRole);

            // then
            verify(amazonS3Client, times(1)).deleteObject(bucket, fileName);
            verify(attachmentRepository, times(1)).delete(attachment);
        }

        @Test
        @DisplayName("존재하지 않은 첨부파일 삭제 시도")
        void deleteAttachment_notFound() {
            // given
            Long tableId = 1L;
            TableRole tableRole = TableRole.USER;

            // when
            attachmentService.deleteAttachmentWithOtherTable(tableId, tableRole);

            // then
            verify(amazonS3Client, never()).deleteObject(anyString(), anyString());
            verify(attachmentRepository, never()).delete(any(Attachment.class));
        }
    }

    @Nested
    @DisplayName("첨부파일 조회")
    class GetAttachments {

        @Test
        @DisplayName("첨부파일 조회")
        void getAttachmentList_success() {

            // given
            Long tableId = 1L;
            TableRole tableRole = TableRole.USER;

            String randomName1 = UUID.randomUUID().toString().substring(0, 8);
            String fileName1 = randomName1 + "test5.jpg";
            String url1 = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName1;

            String randomName2 = UUID.randomUUID().toString().substring(0, 8);
            String fileName2 = randomName2 + "test6.jpg";
            String url2 = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName2;

            Attachment attachment1 = Attachment.of(url1, tableRole, tableId);
            Attachment attachment2 = Attachment.of(url2, tableRole, tableId);

            List<Attachment> attachments = List.of(attachment1, attachment2);
            given(attachmentRepository.findAllByTargetIdAndTableRole(tableId, tableRole)).willReturn(attachments);

            // when
            List<GetAttachmentResponseDto> result = attachmentService.getAttachmentList(tableId, tableRole);

            // then
            assertNotNull(result);
            assertThat(result).hasSize(2);
            // extracting - GetAttachmentResponseDto 객체에서 url 필드를 추출하여 새로운 리스트로 만든다.
            // continasExactly - 리스트가 정확히 주어진 순서대로 지정된 값들을 가지고 있는지 검증
            assertThat(result).extracting("url")
                    .containsExactly("https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName1,
                            "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName2);
        }

        @Test
        @DisplayName("첨부파일이 없을 때 빈 목록 반환")
        void getAttachmentList_noAttachments() {
            // given
            Long tableId = 1L;
            TableRole tableRole = TableRole.USER;

            given(attachmentRepository.findAllByTargetIdAndTableRole(tableId, tableRole)).willReturn(Collections.emptyList());

            // when
            List<GetAttachmentResponseDto> result = attachmentService.getAttachmentList(tableId, tableRole);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("첨부파일만 삭제 시")
    class deleteAttachments {

        @Test
        @DisplayName("(유저)에 관한 첨부파일만 삭제")
        void deleteAttachment_User() {

            // given
            Long attachmentId = 1L;
            AuthUser authUser = testAuthUser();
            User user = testUser();
            Long tableId = 1L;
            ReflectionTestUtils.setField(user, "id", tableId);
            TableRole tableRole = TableRole.USER;

            String randomName = UUID.randomUUID().toString().substring(0, 8);
            String fileName = randomName + "test.jpg";
            String url = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            Attachment attachment = Attachment.of(url, tableRole, tableId);

            given(attachmentRepository.findByIdOrElseThrow(attachmentId)).willReturn(attachment);
            given(userRepository.findByIdOrElseThrow(user.getId())).willReturn(user);

            // when
            attachmentService.deleteAttachment(attachmentId, tableId, TableRole.USER, authUser);

            verify(amazonS3Client).deleteObject(eq(bucket), eq(fileName));
            verify(attachmentRepository).delete(attachment);
        }

        @Test
        @DisplayName("(유저) 일치하지 않는 사용자가 첨부파일 삭제에 접근")
        void deleteAttachment_notEqualUser() {

            // given
            Long attachmentId = 1L;
            Long tableId = 2L;
            AuthUser authUser = testAuthUser();
            User user = testUser();
            ReflectionTestUtils.setField(user, "id", tableId);
            TableRole tableRole = TableRole.USER;

            String randomName = UUID.randomUUID().toString().substring(0, 8);
            String fileName = randomName + "test.jpg";
            String url = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            Attachment attachment = Attachment.of(url, tableRole, attachmentId);

            given(attachmentRepository.findByIdOrElseThrow(attachmentId)).willReturn(attachment);
            given(userRepository.findByIdOrElseThrow(user.getId())).willThrow(new ApplicationException(USER_NOT_FOUND));

            // when
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                attachmentService.deleteAttachment(attachmentId, tableId, tableRole, authUser);
            });

            // then
            assertEquals("존재하지 않는 유저입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("(이벤트) 관련 첨부파일 삭제")
        void deleteAttachment_Event() {

            // given
            Long attachmentId = 1L;
            Long tableId = 1L;
            AuthUser authUser = testAuthUser();
            Event event = testEvent();
            ReflectionTestUtils.setField(event, "id", tableId);
            TableRole tableRole = TableRole.EVENT;

            String randomName = UUID.randomUUID().toString().substring(0, 8);
            String fileName = randomName + "test.jpg";
            String url = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            Attachment attachment = Attachment.of(url, tableRole, attachmentId);

            given(attachmentRepository.findByIdOrElseThrow(attachmentId)).willReturn(attachment);
            given(eventRepository.findByIdAndUserIdOrElseThrow(event.getId(), authUser.getUserId())).willReturn(event);

            // when
            attachmentService.deleteAttachment(attachmentId, event.getId(), TableRole.EVENT, authUser);

            // then
            verify(amazonS3Client).deleteObject(eq(bucket), eq(fileName));
            verify(attachmentRepository).delete(attachment);
        }

        @Test
        @DisplayName("(이벤트) 일치하지 않은 사용자가 다른 이벤트의 첨부파일 삭제에 접근")
        void deleteAttachment_notEqualEvent() {

            // given
            Long attachmentId = 1L;
            Long tableId = 2L;
            AuthUser authUser = testAuthUser();
            Event event = testEvent();
            ReflectionTestUtils.setField(event, "id", tableId);
            TableRole tableRole = TableRole.EVENT;

            String randomName = UUID.randomUUID().toString().substring(0, 8);
            String fileName = randomName + "test.jpg";
            String url = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            Attachment attachment = Attachment.of(url, tableRole, attachmentId);

            given(attachmentRepository.findByIdOrElseThrow(attachmentId)).willReturn(attachment);
            given(eventRepository.findByIdAndUserIdOrElseThrow(event.getId(), authUser.getUserId()))
                    .willThrow(new ApplicationException(EVENT_NOT_FOUND));

            // when
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                attachmentService.deleteAttachment(attachmentId, tableId, TableRole.EVENT, authUser);
            });

            // then
            assertEquals("존재하지 않는 이벤트입니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("SNS 쪽 관련 첨부파일")
    class Attachment_SNS {

        @Test
        @DisplayName("SNS 첨부파일 URL 추가")
        void createAttachment_SNS() {

            // given
            String url = "https://example.com/image.jpg";
            Long targetId = 1L;
            TableRole tableRole = TableRole.USER;

            Attachment attachment = Attachment.of(url, tableRole, targetId);

            given(attachmentRepository.save(any(Attachment.class))).willReturn(attachment);

            // when
            attachmentService.addAttachmentWithUrl(url, targetId, tableRole);

            // then
            verify(attachmentRepository, times(1)).save(any(Attachment.class));
        }

        @Test
        @DisplayName("SNS 첨부파일 가져오기")
        void getAttachment_SNS() {

            // given
            String url = "https://example.com/image.jpg";
            Long targetId = 1L;
            User user = testUser();
            ReflectionTestUtils.setField(user, "id", targetId);
            TableRole tableRole = TableRole.USER;

            Attachment attachment = Attachment.of(url, tableRole, targetId);

            given(attachmentRepository.findByTableRoleAndTargetId(tableRole, user.getId())).willReturn(attachment);

            // when
            String imageUrl = attachmentService.getImageUrl(user.getId(), tableRole);

            // then
            assertEquals(url, imageUrl);

        }

        @Test
        @DisplayName("SNS 첨부파일 가져오기_실패")
        void getAttachment_SNS_NotExists() {

            // given
            Long targetId = 1L;
            User user = testUser();
            ReflectionTestUtils.setField(user, "id", targetId);
            TableRole tableRole = TableRole.USER;

            given(attachmentRepository.findByTableRoleAndTargetId(tableRole, user.getId())).willReturn(null);

            // when
            String imageUrl = attachmentService.getImageUrl(user.getId(), tableRole);

            // then
            assertNull(imageUrl);
        }
    }
}
