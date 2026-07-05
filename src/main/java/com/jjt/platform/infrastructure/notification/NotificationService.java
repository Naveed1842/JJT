package com.jjt.platform.infrastructure.notification;

import com.jjt.platform.core.domain.entity.NotificationTemplate;
import com.jjt.platform.infrastructure.persistence.entity.EmailNotificationEntity;
import com.jjt.platform.infrastructure.persistence.repository.EmailNotificationJpaRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Logs every outbound notification to email_notifications, then attempts SMTP delivery.
 * If NOTIFICATIONS_ENABLED=false (default for dev) the email is logged but not sent.
 * Failures are recorded in the log row without rolling back the caller's transaction.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailNotificationJpaRepository notificationRepo;
    private final JavaMailSender mailSender;

    @Value("${jjt.notifications.enabled:false}")
    private boolean enabled;

    @Value("${jjt.notifications.from-email:noreply@sponsorone.app}")
    private String fromEmail;

    @Value("${jjt.notifications.from-name:Junior Jinnah Trust}")
    private String fromName;

    public NotificationService(EmailNotificationJpaRepository notificationRepo,
                               JavaMailSender mailSender) {
        this.notificationRepo = notificationRepo;
        this.mailSender = mailSender;
    }

    /**
     * Sends a payment-received confirmation to the sponsor.
     * Runs in a new transaction so a send failure never rolls back the payment record.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendPaymentReceived(UUID orgId, String toEmail, String toName,
                                    String sponsorName, String childName,
                                    String amount, String currency, String month,
                                    String orgName, UUID paymentId) {
        EmailTemplates.EmailContent content = EmailTemplates.paymentReceived(
                sponsorName, childName, amount, currency, month, orgName);
        send(orgId, toEmail, toName, NotificationTemplate.PAYMENT_RECEIVED,
                content, paymentId, "SponsorPayment");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendSponsorshipActivated(UUID orgId, String toEmail, String toName,
                                         String sponsorName, String childName,
                                         String startMonth, String orgName, UUID sponsorshipId) {
        EmailTemplates.EmailContent content = EmailTemplates.sponsorshipActivated(
                sponsorName, childName, startMonth, orgName);
        send(orgId, toEmail, toName, NotificationTemplate.SPONSORSHIP_ACTIVATED,
                content, sponsorshipId, "Sponsorship");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendProgressUpdate(UUID orgId, String toEmail, String toName,
                                   String sponsorName, String childName,
                                   String month, String summary, String orgName, UUID progressId) {
        EmailTemplates.EmailContent content = EmailTemplates.progressUpdate(
                sponsorName, childName, month, summary, orgName);
        send(orgId, toEmail, toName, NotificationTemplate.PROGRESS_UPDATE,
                content, progressId, "ProgressUpdate");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendPaymentOverdue(UUID orgId, String toEmail, String toName,
                                   String sponsorName, String childName,
                                   String month, String amount, String currency,
                                   String orgName, UUID paymentId) {
        EmailTemplates.EmailContent content = EmailTemplates.paymentOverdue(
                sponsorName, childName, month, amount, currency, orgName);
        send(orgId, toEmail, toName, NotificationTemplate.PAYMENT_OVERDUE,
                content, paymentId, "SponsorPayment");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendDonationReceipt(UUID orgId, String toEmail, String toName,
                                    String donorName, String receiptNumber,
                                    String amount, String currency, String donationType,
                                    String date, String orgName, UUID donationId) {
        EmailTemplates.EmailContent content = EmailTemplates.donationReceipt(
                donorName, receiptNumber, amount, currency, donationType, date, orgName);
        send(orgId, toEmail, toName, NotificationTemplate.DONATION_RECEIPT,
                content, donationId, "Donation");
    }

    private void send(UUID orgId, String toEmail, String toName,
                      NotificationTemplate template, EmailTemplates.EmailContent content,
                      UUID relatedEntityId, String relatedEntityType) {
        EmailNotificationEntity record = new EmailNotificationEntity(
                UUID.randomUUID(), orgId, toEmail, toName, template,
                content.subject(), "QUEUED", relatedEntityId, relatedEntityType);
        notificationRepo.save(record);

        if (!enabled) {
            log.info("Notifications disabled — skipping send to {} ({})", toEmail, template);
            record.markSent();
            notificationRepo.save(record);
            return;
        }

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(content.subject());
            helper.setText(content.htmlBody(), true);
            mailSender.send(msg);
            record.markSent();
            log.info("Email sent: {} → {} ({})", template, toEmail, record.getId());
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            record.markFailed(e.getMessage());
            log.error("Email send failed: {} → {} — {}", template, toEmail, e.getMessage());
        }
        notificationRepo.save(record);
    }
}
