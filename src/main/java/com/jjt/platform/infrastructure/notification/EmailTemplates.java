package com.jjt.platform.infrastructure.notification;

/**
 * Inline HTML email templates. Each method returns a ready-to-send HTML body
 * and a subject. Values are HTML-escaped before substitution to prevent injection.
 */
public final class EmailTemplates {

    private EmailTemplates() {}

    public record EmailContent(String subject, String htmlBody) {}

    public static EmailContent paymentReceived(String sponsorName, String childName,
                                               String amount, String currency,
                                               String month, String orgName) {
        String subject = String.format("[%s] Payment Confirmed – %s for %s", orgName, month, childName);
        String body = base(orgName, String.format("""
                <p>Dear %s,</p>
                <p>Thank you! We have received your sponsorship payment.</p>
                <table style="border-collapse:collapse;width:100%%">
                  <tr><td style="padding:6px;border:1px solid #ddd"><strong>Child</strong></td>
                      <td style="padding:6px;border:1px solid #ddd">%s</td></tr>
                  <tr><td style="padding:6px;border:1px solid #ddd"><strong>Month</strong></td>
                      <td style="padding:6px;border:1px solid #ddd">%s</td></tr>
                  <tr><td style="padding:6px;border:1px solid #ddd"><strong>Amount</strong></td>
                      <td style="padding:6px;border:1px solid #ddd">%s %s</td></tr>
                </table>
                <p>JazakAllah Khair for your continued support.</p>
                """, esc(sponsorName), esc(childName), esc(month), esc(currency), esc(amount)));
        return new EmailContent(subject, body);
    }

    public static EmailContent sponsorshipActivated(String sponsorName, String childName,
                                                    String startMonth, String orgName) {
        String subject = String.format("[%s] Your Sponsorship of %s is Now Active", orgName, childName);
        String body = base(orgName, String.format("""
                <p>Dear %s,</p>
                <p>We are delighted to confirm that your sponsorship of <strong>%s</strong>
                   is now <strong>active</strong> from <strong>%s</strong>.</p>
                <p>Your generous commitment will directly fund this child's education.
                   You will receive monthly updates on their progress.</p>
                <p>JazakAllah Khair for making a difference.</p>
                """, esc(sponsorName), esc(childName), esc(startMonth)));
        return new EmailContent(subject, body);
    }

    public static EmailContent progressUpdate(String sponsorName, String childName,
                                              String month, String summary, String orgName) {
        String subject = String.format("[%s] Progress Update for %s – %s", orgName, childName, month);
        String body = base(orgName, String.format("""
                <p>Dear %s,</p>
                <p>Here is the latest progress update for <strong>%s</strong> (%s):</p>
                <blockquote style="border-left:4px solid #6c63ff;padding:12px;margin:12px 0;color:#444">
                  %s
                </blockquote>
                <p>Thank you for making this education journey possible.</p>
                """, esc(sponsorName), esc(childName), esc(month), esc(summary)));
        return new EmailContent(subject, body);
    }

    public static EmailContent paymentOverdue(String sponsorName, String childName,
                                              String month, String amount, String currency,
                                              String orgName) {
        String subject = String.format("[%s] Payment Reminder – %s for %s", orgName, month, childName);
        String body = base(orgName, String.format("""
                <p>Dear %s,</p>
                <p>This is a friendly reminder that your sponsorship payment for
                   <strong>%s</strong> for <strong>%s</strong> is now overdue.</p>
                <table style="border-collapse:collapse;width:100%%">
                  <tr><td style="padding:6px;border:1px solid #ddd"><strong>Amount Due</strong></td>
                      <td style="padding:6px;border:1px solid #ddd">%s %s</td></tr>
                </table>
                <p>Please arrange payment at your earliest convenience.
                   If you have already sent the payment, please ignore this reminder.</p>
                <p>For any queries, please contact us by replying to this email.</p>
                """, esc(sponsorName), esc(childName), esc(month), esc(currency), esc(amount)));
        return new EmailContent(subject, body);
    }

    public static EmailContent donationReceipt(String donorName, String receiptNumber,
                                               String amount, String currency,
                                               String donationType, String date, String orgName) {
        String subject = String.format("[%s] Donation Receipt %s", orgName, receiptNumber);
        String body = base(orgName, String.format("""
                <p>Dear %s,</p>
                <p>Thank you for your generous donation. Please find your receipt below.</p>
                <table style="border-collapse:collapse;width:100%%">
                  <tr><td style="padding:6px;border:1px solid #ddd"><strong>Receipt No.</strong></td>
                      <td style="padding:6px;border:1px solid #ddd">%s</td></tr>
                  <tr><td style="padding:6px;border:1px solid #ddd"><strong>Date</strong></td>
                      <td style="padding:6px;border:1px solid #ddd">%s</td></tr>
                  <tr><td style="padding:6px;border:1px solid #ddd"><strong>Type</strong></td>
                      <td style="padding:6px;border:1px solid #ddd">%s</td></tr>
                  <tr><td style="padding:6px;border:1px solid #ddd"><strong>Amount</strong></td>
                      <td style="padding:6px;border:1px solid #ddd">%s %s</td></tr>
                </table>
                <p>Please retain this receipt for your records. JazakAllah Khair.</p>
                """, esc(donorName), esc(receiptNumber), esc(date), esc(donationType),
                esc(currency), esc(amount)));
        return new EmailContent(subject, body);
    }

    private static String base(String orgName, String content) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;color:#222;max-width:600px;margin:auto;padding:24px">
                  <div style="border-bottom:3px solid #6c63ff;padding-bottom:12px;margin-bottom:24px">
                    <h2 style="margin:0;color:#6c63ff">%s</h2>
                  </div>
                  %s
                  <div style="border-top:1px solid #eee;margin-top:32px;padding-top:12px;font-size:12px;color:#888">
                    This email was sent by %s. Please do not reply if this was unexpected.
                  </div>
                </body>
                </html>
                """, esc(orgName), content, esc(orgName));
    }

    private static String esc(String raw) {
        if (raw == null) return "";
        return raw.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;");
    }
}
