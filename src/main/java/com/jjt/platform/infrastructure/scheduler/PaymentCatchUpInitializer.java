package com.jjt.platform.infrastructure.scheduler;

import com.jjt.platform.api.admin.service.AdminPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

/**
 * Catch-up for the cron-based payment lifecycle jobs in {@link PaymentScheduler}.
 *
 * The monthly EXPECTED-generation cron (1st, 00:05) and the daily overdue-marking cron
 * (02:00) only fire while the application is running. If the app was down at those
 * moments (common in dev, and possible after a production restart or dyno sleep),
 * payments are never generated or marked overdue — and PAYMENT_OVERDUE alerts are
 * never raised. This runner replays both jobs once on startup; both operations are
 * idempotent, so running them again alongside the crons is safe.
 */
@Component
@Order(10)
public class PaymentCatchUpInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PaymentCatchUpInitializer.class);

    private final AdminPaymentService paymentService;
    private final PaymentScheduler scheduler;

    public PaymentCatchUpInitializer(AdminPaymentService paymentService, PaymentScheduler scheduler) {
        this.paymentService = paymentService;
        this.scheduler = scheduler;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int created = paymentService.generateExpectedPayments(YearMonth.now(), null);
            if (created > 0) {
                log.info("Startup catch-up: generated {} expected payments for {}", created, YearMonth.now());
            }
            String cutoff = scheduler.overdueCutoffMonth();
            int marked = paymentService.markOverduePayments(cutoff);
            if (marked > 0) {
                log.info("Startup catch-up: marked {} payments as OVERDUE (cutoff={})", marked, cutoff);
            }
        } catch (Exception e) {
            log.error("Startup payment catch-up failed (will retry via scheduled crons): {}", e.getMessage(), e);
        }
    }
}
