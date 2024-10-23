//package com.spotlightspace.config.s3;
//
//@RequiredArgsConstructor
//@Component
//@Transactional
//public class S3Service {
//
//    private final AmazonS3Client amazonS3Client;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//
//    public String uploadFile(MultipartFile multipartFile){
//        if (!isMatchingExtension(multipartFile)) {
//            throw new ForbiddenException(ResponseCode.INVALID_EXTENTSION);
//        }
//        return uploadFileToS3(multipartFile);
//    }
//
//    public String updateFile(MultipartFile multipartFile, String existFilePath) {
//        if (existFilePath != null && !existFilePath.isEmpty()) {
//            String existingFileName = existFilePath.substring(
//                    existFilePath.lastIndexOf("/") + 1);
//            amazonS3Client.deleteObject(bucket, existingFileName);
//        }
//        return uploadFileToS3(multipartFile);
//    }
//
//    public void deleteFile(String fileName) {
//        amazonS3Client.deleteObject(bucket, fileName);
//    }
//
//    private String uploadFileToS3(MultipartFile multipartFile) {
//        try {
//            String originalFileName = multipartFile.getOriginalFilename();
//            String fileName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
//
//            ObjectMetadata metadata = new ObjectMetadata();
//            metadata.setContentLength(multipartFile.getSize());
//            metadata.setContentType(multipartFile.getContentType());
//
//            String randomFileName = UUID.randomUUID().toString();
//            String fullFileName = randomFileName + fileName;
//
//            amazonS3Client.putObject(
//                    new PutObjectRequest(bucket, fullFileName,
//                            multipartFile.getInputStream(), metadata)
//                            .withCannedAcl(CannedAccessControlList.PublicRead)
//            );
//            return amazonS3Client.getUrl(bucket, fullFileName).toString();
//        } catch (IOException e) {
//            throw new ForbiddenException(ResponseCode.INVALID_UPLOAD);
//        }
//    }
//}
