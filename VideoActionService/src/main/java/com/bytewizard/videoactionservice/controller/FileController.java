package com.bytewizard.videoactionservice.controller;

import com.bytewizard.common.constants.ErrorCode;
import com.bytewizard.common.domain.BaseResponse;
import com.bytewizard.common.utils.ResultUtils;
import com.bytewizard.videoactionservice.domain.dto.InitUploadRequest;
import com.bytewizard.videoactionservice.domain.dto.MergeChunkRequest;
import com.bytewizard.videoactionservice.service.FileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/video/file")
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping("/check")
    public BaseResponse<String> checkFileExistence(@RequestParam(value = "fileHash") String fileHash) {
        System.out.println("check file existence");
        String fileUrl = fileService.checkFileExistence(fileHash);
        if (fileUrl == null) {
            return ResultUtils.error(ErrorCode.VIDEO_NOT_FOUND_ERROR);
        }
        return ResultUtils.success(fileUrl);
    }

    @PostMapping("/get/upload/urls")
    public BaseResponse<List<String>> getUploadUrls(@Valid @RequestBody InitUploadRequest initUploadRequest) {
        return ResultUtils.success(fileService.getUploadUrls(initUploadRequest));
    }

    @GetMapping("/get/upload/progress")
    public BaseResponse<Set<Integer>> getUploadProgress(@Valid @RequestParam(value = "fileHash") String fileHash) {
        return ResultUtils.success(fileService.getUploadProgress(fileHash));
    }

    @PostMapping("/merge/chunk")
    public BaseResponse<String> mergeChunk(@Valid @RequestBody MergeChunkRequest mergeChunkRequest) {
        return ResultUtils.success(fileService.mergeChunk(mergeChunkRequest));
    }
}
