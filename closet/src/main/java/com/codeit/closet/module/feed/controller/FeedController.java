package com.codeit.closet.module.feed.controller;

import com.codeit.closet.module.feed.dto.FeedCreateRequest;
import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.feed.dto.FeedUpdateRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

  // 기본 틀만 잡고 차후에 Service 연동 할 예정

  @GetMapping
  public ResponseEntity<FeedDTOCursorResponse> getFeeds(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam Integer limit,
      @RequestParam String sortBy,
      @RequestParam String sortDirection,
      @RequestParam(required = false) String keywordLike,
//      @RequestParam(required = false) SkyStatus skyStatusEqual,
//      @RequestParam(required = false) PrecipitationType precipitationTypeEqual,
      @RequestParam(required = false) UUID authorIdEqual) {

    return null;
  }

  @PostMapping
  public ResponseEntity<FeedDTO> createFeed(
      @RequestBody FeedCreateRequest request) {
    return null;
  }

  @DeleteMapping("/{feedId}")
  public ResponseEntity<Void> deleteFeed(@PathVariable("feedId") UUID id) {
    return null;
  }

  @PatchMapping("/{feedId}")
  public ResponseEntity<FeedDTO> updateFeed(
      @PathVariable(name = "feedId") UUID id,
    @RequestBody FeedUpdateRequest request) {
    return null;
  }
}
