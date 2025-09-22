package com.tribe.backend.club.web;

import com.tribe.backend.club.dto.ClubCreateRequest;
import com.tribe.backend.club.dto.ClubMembershipResponse;
import com.tribe.backend.club.dto.ClubResponse;
import com.tribe.backend.club.service.ClubService;
import com.tribe.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public ClubResponse createClub(@AuthenticationPrincipal UserPrincipal principal,
                                   @Valid @RequestBody ClubCreateRequest request) {
        return clubService.createClub(principal.getId(), request);
    }

    @PostMapping("/{clubId}/join")
    public ClubMembershipResponse joinClub(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable UUID clubId) {
        return clubService.joinClub(principal.getId(), clubId);
    }

    @GetMapping
    public List<ClubResponse> listByCity(@RequestParam String city) {
        return clubService.listClubsByCity(city);
    }

    @GetMapping("/me")
    public List<ClubMembershipResponse> myMemberships(@AuthenticationPrincipal UserPrincipal principal) {
        return clubService.listMemberships(principal.getId());
    }

    @GetMapping("/{clubId}")
    public ClubResponse getClub(@PathVariable UUID clubId) {
        return clubService.getClub(clubId);
    }
}
