package com.tribe.backend.premium.web;

import com.tribe.backend.premium.dto.SubscriptionRequest;
import com.tribe.backend.premium.dto.SubscriptionResponse;
import com.tribe.backend.premium.service.PremiumService;
import com.tribe.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/premium")
public class PremiumController {

    private final PremiumService premiumService;

    public PremiumController(PremiumService premiumService) {
        this.premiumService = premiumService;
    }

    @PostMapping("/subscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse subscribe(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody SubscriptionRequest request) {
        return premiumService.startSubscription(principal.getId(), request);
    }

    @GetMapping("/subscriptions/current")
    public SubscriptionResponse current(@AuthenticationPrincipal UserPrincipal principal) {
        return premiumService.activeSubscription(principal.getId());
    }

    @DeleteMapping("/subscriptions/current")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UserPrincipal principal) {
        premiumService.cancelSubscription(principal.getId());
    }
}
