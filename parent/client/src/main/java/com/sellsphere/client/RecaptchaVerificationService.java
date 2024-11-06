package com.sellsphere.client;

import com.sellsphere.client.customer.RecaptchaVerificationFailed;
import lombok.Getter;

import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.CreateAssessmentRequest;
import com.google.recaptchaenterprise.v1.Event;
import com.google.recaptchaenterprise.v1.ProjectName;
import com.google.recaptchaenterprise.v1.RiskAnalysis.ClassificationReason;
import java.io.IOException;
import java.util.List;

public class RecaptchaVerificationService {

    private final String projectID;
    private final String recaptchaSiteKey;

    public RecaptchaVerificationService() {
        this.projectID = System.getenv("GOOGLE_PROJECT_ID");
        this.recaptchaSiteKey = System.getenv("RECAPTCHA_SITE_KEY");
    }

    /**
     * Verifies the reCAPTCHA token and returns the assessment result.
     *
     * @param token         : The reCAPTCHA token obtained from the client.
     * @param recaptchaAction : The expected action associated with the token.
     * @return a VerificationResult object containing the validation outcome and risk score.
     * @throws IOException if there is an issue with the reCAPTCHA API call.
     */
    public VerificationResult verifyToken(String token) throws IOException {
        // Create a client to interact with reCAPTCHA Enterprise.
        try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

            // Create an event with the reCAPTCHA key and token.
            Event event = Event.newBuilder().setSiteKey(recaptchaSiteKey).setToken(token).build();

            // Create a request to assess the token.
            CreateAssessmentRequest request = CreateAssessmentRequest.newBuilder()
                    .setParent(ProjectName.of(projectID).toString())
                    .setAssessment(Assessment.newBuilder().setEvent(event).build())
                    .build();

            // Send the request and get the response.
            Assessment response = client.createAssessment(request);

            // Check if the token is valid.
            if (!response.getTokenProperties().getValid()) {
                return new VerificationResult(false, 0.0f,
                                              "Invalid token: " + response.getTokenProperties().getInvalidReason().name());
            }

            // Get the risk score and reasons for classification.
            float score = response.getRiskAnalysis().getScore();
            List<ClassificationReason> reasons = response.getRiskAnalysis().getReasonsList();

            return new VerificationResult(true, score, reasons);
        }
    }

    public void validate(VerificationResult result) throws RecaptchaVerificationFailed {
        if (!result.isValid()) {
            throw new RecaptchaVerificationFailed("/customer", "Recaptcha verification failed");
        }
    }

    // Inner class to represent the verification result.
    @Getter
    public static class VerificationResult {
        private final boolean valid;
        private final float score;
        private final String message;
        private final List<ClassificationReason> reasons;

        public VerificationResult(boolean valid, float score, String message) {
            this.valid = valid;
            this.score = score;
            this.message = message;
            this.reasons = null;
        }

        public VerificationResult(boolean valid, float score, List<ClassificationReason> reasons) {
            this.valid = valid;
            this.score = score;
            this.message = null;
            this.reasons = reasons;
        }

        public boolean isValid()  {
            return valid;
        }
    }
}
