package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.gotson.komga.domain.model.PageHash
import org.gotson.komga.domain.model.PageHashUnknown
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.persistence.PageHashRepository
import org.gotson.komga.domain.service.PageHashLifecycle
import org.gotson.komga.infrastructure.swagger.PageableAsQueryParam
import org.gotson.komga.infrastructure.web.getMediaTypeOrDefault
import org.gotson.komga.interfaces.api.rest.dto.PageHashDto
import org.gotson.komga.interfaces.api.rest.dto.PageHashMatchDto
import org.gotson.komga.interfaces.api.rest.dto.PageHashUnknownDto
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("api/v1/page-hashes", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('$ROLE_ADMIN')")
class PageHashController(
  private val pageHashRepository: PageHashRepository,
  private val pageHashLifecycle: PageHashLifecycle,
) {

  @GetMapping
  @PageableAsQueryParam
  fun getPageHashes(
    @RequestParam(name = "action", required = false) actions: List<PageHash.Action>?,
    @Parameter(hidden = true) page: Pageable,
  ): Page<PageHashDto> =
    pageHashRepository.findAllKnown(actions, page).map { it.toDto() }

  @GetMapping("/{hash}/thumbnail", produces = [MediaType.IMAGE_JPEG_VALUE])
  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  fun getPageHashThumbnail(@PathVariable hash: String): ByteArray =
    pageHashRepository.getKnownThumbnail(hash) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("/unknown")
  @PageableAsQueryParam
  fun getUnknownPageHashes(
    @Parameter(hidden = true) page: Pageable,
  ): Page<PageHashUnknownDto> =
    pageHashRepository.findAllUnknown(page).map { it.toDto() }

  @GetMapping("unknown/{pageHash}")
  @PageableAsQueryParam
  fun getUnknownPageHashMatches(
    @PathVariable pageHash: String,
    @RequestParam("media_type") mediaType: String,
    @RequestParam("file_size") size: Long,
    @Parameter(hidden = true) page: Pageable,
  ): Page<PageHashMatchDto> =
    pageHashRepository.findMatchesByHash(
      PageHashUnknown(
        hash = pageHash,
        mediaType = mediaType,
        size = if (size < 0) null else size,
      ),
      page,
    ).map { it.toDto() }

  @GetMapping("unknown/{pageHash}/thumbnail", produces = [MediaType.IMAGE_JPEG_VALUE])
  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  fun getUnknownPageHashThumbnail(
    @PathVariable pageHash: String,
    @RequestParam("media_type") mediaType: String,
    @RequestParam("file_size") size: Long,
    @RequestParam("resize") resize: Int? = null,
  ): ResponseEntity<ByteArray> =
    pageHashLifecycle.getPage(
      PageHashUnknown(
        hash = pageHash,
        mediaType = mediaType,
        size = if (size < 0) null else size,
      ),
      resize,
    )?.let {
      ResponseEntity.ok()
        .contentType(getMediaTypeOrDefault(it.mediaType))
        .body(it.content)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PutMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun updatePageHash() {
    TODO()
  }
}
