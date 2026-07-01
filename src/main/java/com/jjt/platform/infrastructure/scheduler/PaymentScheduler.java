package com.jjt.platform.infrastructure.scheduler;

import com.jjt.platform.api.admin.service.AdminDonationService;
import com.jjt.platform.api.admin.service.AdminPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Scheduled jobs for the payment reconciliation lifecycle.
 *
 * Monthly (1st of month at 00:05): generate EXPECTED payment records for all active sponsorships.
 * Daily (02:00):  transition EXPECTED → OVERDUE for payments past the configured due day.
 * Daily (02:10):  generate EXPECTED donation records from active recurring donation schedules.
 */
@Component
public class PaymentScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentScheduler.class);

    private final AdminPaymentService paymentService;
    private final AdminDonationService donationService;
    private final int paymentDueDay;

    public PaymentScheduler(AdminPaymentService paymentService,
                            AdminDonationService donationService,
                            @Value("${jjt.payment.due-day:15}") int paymentDueDay) {
        this.paymentService = paymentService;
        this.donationService = donationService;
        this.paymentDueDay = paymentDueDay;
    }

    /** Runs on the 1st of every month at 00:05 to generate EXPECTED payment records for all orgs. */
    @Scheduled(cron = "0 5 0 1 * *")
    public void generateMonthlyExpectedPayments() {
        YearMonth currentMonth = YearMonth.now();
        log.info("Scheduler: generating expected payments for {}", currentMonth);
        int created = paymentService.generateExpectedPayments(currentMonth, null);
        log.info("Scheduler: created {} expected payments for {}", created, currentMonth);
    }

    /** Runs daily at 02:10. Generates EXPECTED donation records for all due recurring schedules across all orgs. */
    @Scheduled(cron = "0 10 2 * * *")
    public void generateExpectedDonations() {
        int created = donationService.generateExpectedFromRecurring(null);
        if (created > 0) {
            log.info("Scheduler: generated {} EXPECTED donation records from recurring schedules", created);
        }
    }

    /**
     * Runs daily at 02:00. Marks any EXPECTED payment from a month where
     * today is past the payment due day as OVERDUE.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void markOverduePayments() {
        LocalDate today = LocalDate.now();
        if (today.getDayOfMonth() <= paymentDueDay) {
            return;
        }
        // Any EXPECTED payment from the current month or earlier is overdue.
        String cutoffMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        int marked = paymentService.markOverduePayments(cutoffMonth);
        if (marked > 0) {
            log.info("Scheduler: marked {} payments as OVERDUE (cutoff={})", marked, cutoffMonth);
        }
    }
}
