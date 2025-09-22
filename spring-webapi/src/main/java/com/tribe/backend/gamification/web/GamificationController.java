package com.tribe.backend.gamification.web;

import com.tribe.backend.gamification.domain.StreakType;
import com.tribe.backend.gamification.dto.BadgeResponse;
import com.tribe.backend.gamification.dto.LeaderboardEntry;
import com.tribe.backend.gamification.dto.StreakResponse;
import com.tribe.backend.gamification.service.GamificationService;
import com.tribe.backend.security.UserPrincipal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gamification")
public class GamificationController {

    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/badges")
    public List<BadgeResponse> myBadges(@AuthenticationPrincipal UserPrincipal principal) {
        return gamificationService.badgesForUser(principal.getId());
    }

    @GetMapping("/streaks")
    public List<StreakResponse> myStreaks(@AuthenticationPrincipal UserPrincipal principal) {
        return gamificationService.streaksForUser(principal.getId());
    }

    @PostMapping("/streaks/{type}/increment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordInteraction(@AuthenticationPrincipal UserPrincipal principal,
                                  @PathVariable StreakType type) {
        gamificationService.recordInteraction(principal.getId(), type);
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntry> leaderboard(@RequestParam(required = false) String city) {
        return gamificationService.leaderboard(city);
    }
}
