package tech.mogami.facilitator.verifier;

import jakarta.validation.ConstraintViolation;

/**
 * Utility class for verifiers.
 */
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor", "unused"})
public abstract class VerifierUtil {

    /**
     * Returns the error message for a given constraint violation.
     *
     * @param constraintViolation the constraint violation
     * @return the error message
     */
    protected String getErrorMessage(final ConstraintViolation<?> constraintViolation) {
        if (constraintViolation.getInvalidValue() != null) {
            return constraintViolation.getMessage()
                    + " (Your value: " + constraintViolation.getInvalidValue() + ")";
        } else {
            return constraintViolation.getMessage();
        }
    }

}
