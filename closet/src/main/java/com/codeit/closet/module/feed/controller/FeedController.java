package com.codeit.closet.module.feed.controller;

import com.codeit.closet.module.feed.dto.FeedCreateRequest;
import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.feed.dto.FeedUpdateRequest;
import com.codeit.closet.module.feed.service.FeedService;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

  private final FeedService feedService;

  @GetMapping
  public ResponseEntity<FeedDTOCursorResponse> getFeeds(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam Integer limit,
      @RequestParam String sortBy,
      @RequestParam String sortDirection,
      @RequestParam(required = false) String keywordLike,
      @RequestParam(required = false) SkyStatus skyStatusEqual,
      @RequestParam(required = false) PrecipitationType precipitationTypeEqual,
      @RequestParam(required = false) UUID authorIdEqual) {
    FeedDTOCursorResponse results = feedService.findFeeds(cursor, idAfter, limit, sortBy,
        sortDirection, keywordLike,
        skyStatusEqual, precipitationTypeEqual, authorIdEqual);
    return ResponseEntity.status(HttpStatus.OK).body(results);
  }

  @PostMapping
  public ResponseEntity<FeedDTO> createFeed(@RequestBody FeedCreateRequest request) {
    FeedDTO result = feedService.createFeed(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @DeleteMapping("/{feedId}")
  public ResponseEntity<Void> deleteFeed(@PathVariable("feedId") UUID id) {
    feedService.deleteFeed(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PatchMapping("/{feedId}")
  public ResponseEntity<FeedDTO> updateFeed(
      @PathVariable(name = "feedId") UUID id,
      @RequestBody FeedUpdateRequest request) {
    FeedDTO result = feedService.updateFeed(id, request);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }
}
