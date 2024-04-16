package org.mifos.pheevouchermanagementsystem.data;

import static org.mifos.connector.common.exception.PaymentHubError.ExtValidationError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.mifos.connector.common.channel.dto.PhErrorDTO;
import org.mifos.connector.common.exception.PaymentHubErrorCategory;
import org.mifos.connector.common.validation.ValidatorBuilder;
import org.mifos.pheevouchermanagementsystem.util.VoucherValidatorsEnum;
import org.mifos.pheevouchermanagementsystem.util.VouchersDTOConstant;
import org.springframework.stereotype.Component;

@Component
public class VoucherValidator {

    private static final String resource = "voucherValidator";
    private static final String requestId = "requestId";
    private static final int expectedRequestIdLength = 12;
    private static final String batchId = "batchId";
    private static final int expectedBatchIdLength = 12;
    private static final String voucherInstructions = "voucherInstructions";
    private static final String instructionID = "instructionId";
    private static final int expectedInstructionIdLength = 16;
    private static final String groupCode = "groupCode";
    private static final int expectedGroupCodeLength = 3;
    private static final String currency = "currency";
    private static final int expectedCurrencyLength = 3;
    private static final String amount = "amount";
    private static final String payeeFunctionalID = "payeeFunctionalId";
    private static final int expectedPayeeFunctionalIDLength = 20;
    private static final String narration = "narration";
    private static final int maximumNarrationLength = 50;
    private static final String serialNumber = "serialNumber";
    private static final int expectedSerialNumberLength = 20;
    private static final String agentID = "agentId";
    private static final int expectedAgentIDLength = 10;
    private static final String voucherSerialNumber = "voucherSerialNumber";
    private static final String voucherSecretNumber = "voucherSecretNumber";
    private static final int expectedVoucherSecretNumberLength = 6;

    public UnsupportedParameterValidation unsupportedParameterValidation = new UnsupportedParameterValidation();

    public PhErrorDTO validateCreateVoucher(RequestDTO request) {
        final ValidatorBuilder validatorBuilder = new ValidatorBuilder();
        unsupportedParameterValidation.handleUnsupportedParameterValidation(request.getAdditionalProperties(), validatorBuilder);

        // Checks for requestID
        validatorBuilder.reset().resource(resource).parameter(requestId).value(request.getRequestID())
                .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_REQUEST_ID).validateFieldMaxLengthWithFailureCodeAndErrorParams(
                        expectedRequestIdLength, VoucherValidatorsEnum.INVALID_REQUEST_ID_LENGTH);

        // Checks for batchID
        validatorBuilder.reset().resource(resource).parameter(batchId).value(request.getBatchID())
                .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_BATCH_ID)
                .validateFieldMaxLengthWithFailureCodeAndErrorParams(expectedBatchIdLength, VoucherValidatorsEnum.INVALID_BATCH_ID_LENGTH);

        // Check for voucherInstructions
        validatorBuilder.reset().resource(resource).parameter(voucherInstructions).value(request.getVoucherInstructions())
                .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_VOUCHER_INSTRUCTIONS);
        if (request.getVoucherInstructions() == null) {
            request.setVoucherInstructions(new ArrayList<>(Arrays.asList(new VoucherInstruction())));
        }

        request.getVoucherInstructions().forEach(voucherInstruction -> {
            // check for unsupported parameters in voucherInstruction
            unsupportedParameterValidation.handleUnsupportedParameterValidation(voucherInstruction.getAdditionalProperties(),
                    validatorBuilder);

            // Check for instructionID
            validatorBuilder.reset().resource(resource).parameter(instructionID).value(voucherInstruction.getInstructionID())
                    .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_INSTRUCTION_ID)
                    .validateFieldMaxLengthWithFailureCodeAndErrorParams(expectedInstructionIdLength,
                            VoucherValidatorsEnum.INVALID_INSTRUCTION_ID_LENGTH);

            // Check for groupCode
            validatorBuilder.reset().resource(resource).parameter(groupCode).value(voucherInstruction.getGroupCode())
                    .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_GROUP_CODE)
                    .validateFieldNotBlankAndLengthWithFailureCodeAndErrorParams(expectedGroupCodeLength,
                            VoucherValidatorsEnum.INVALID_GROUP_CODE_LENGTH);

            // Check for currency
            validatorBuilder.reset().resource(resource).parameter(currency).value(voucherInstruction.getCurrency())
                    .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_CURRENCY)
                    .validateFieldNotBlankAndLengthWithFailureCodeAndErrorParams(expectedCurrencyLength,
                            VoucherValidatorsEnum.INVALID_CURRENCY_LENGTH);

            // Check for amount
            validatorBuilder.reset().resource(resource).parameter(amount).value(voucherInstruction.getAmount())
                    .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_AMOUNT)
                    .validateBigDecimalFieldNotNegativeWithFailureCode(VoucherValidatorsEnum.INVALID_NEGATIVE_AMOUNT);

            // Check for payeeFunctionalID
            validatorBuilder.reset().resource(resource).parameter(payeeFunctionalID).value(voucherInstruction.getPayeeFunctionalID())
                    .ignoreIfNull().validateFieldMaxLengthWithFailureCodeAndErrorParams(expectedPayeeFunctionalIDLength,
                            VoucherValidatorsEnum.INVALID_PAYEE_FUNCTIONAL_ID_LENGTH);

            // Check for narration
            validatorBuilder.reset().resource(resource).parameter(narration).value(voucherInstruction.getNarration()).ignoreIfNull()
                    .validateFieldMaxLengthWithFailureCodeAndErrorParams(maximumNarrationLength,
                            VoucherValidatorsEnum.INVALID_NARRATION_LENGTH);
        });

        // If errors exist, build and return PhErrorDTO
        if (validatorBuilder.hasError()) {
            validatorBuilder.errorCategory(PaymentHubErrorCategory.Validation.toString())
                    .errorCode(VoucherValidatorsEnum.VOUCHER_SCHEMA_VALIDATION_ERROR.getCode())
                    .errorDescription(VoucherValidatorsEnum.VOUCHER_SCHEMA_VALIDATION_ERROR.getMessage())
                    .developerMessage(VoucherValidatorsEnum.VOUCHER_SCHEMA_VALIDATION_ERROR.getMessage())
                    .defaultUserMessage(VoucherValidatorsEnum.VOUCHER_SCHEMA_VALIDATION_ERROR.getMessage());

            PhErrorDTO.PhErrorDTOBuilder phErrorDTOBuilder = new PhErrorDTO.PhErrorDTOBuilder(ExtValidationError.getErrorCode());
            phErrorDTOBuilder.fromValidatorBuilder(validatorBuilder);
            return phErrorDTOBuilder.build();
        }

        return null;
    }

    public PhErrorDTO validateVoucherLifecycle(RequestDTO request) {
        final ValidatorBuilder validatorBuilder = new ValidatorBuilder();

        // check for unsupported parameter
        unsupportedParameterValidation.handleUnsupportedParameterValidation(request.getAdditionalProperties(), validatorBuilder);

        // Checks for requestID
        validatorBuilder.reset().resource(resource).parameter(requestId).value(request.getRequestID())
                .validateFieldMaxLengthWithFailureCodeAndErrorParams(expectedRequestIdLength,
                        VoucherValidatorsEnum.INVALID_REQUEST_ID_LENGTH);

        // Checks for batchID
        validatorBuilder.reset().resource(resource).parameter(batchId).value(request.getBatchID())
                .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_BATCH_ID)
                .validateFieldMaxLengthWithFailureCodeAndErrorParams(expectedBatchIdLength, VoucherValidatorsEnum.INVALID_BATCH_ID_LENGTH);

        // Check for voucherInstructions
        validatorBuilder.reset().resource(resource).parameter(voucherInstructions).value(request.getVoucherInstructions())
                .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_VOUCHER_INSTRUCTIONS);
        if (request.getVoucherInstructions() == null) {
            request.setVoucherInstructions(new ArrayList<>(Arrays.asList(new VoucherInstruction())));
        }

        Set<String> requiredFields = new HashSet<>();
        requiredFields.add(VouchersDTOConstant.serialNumber);
        requiredFields.add(VouchersDTOConstant.status);

        request.getVoucherInstructions().forEach(voucherInstruction -> {
            // check for unsupported parameters in voucherInstruction
            unsupportedParameterValidation.handleUnsupportedParameterValidation(voucherInstruction.getAdditionalProperties(),
                    validatorBuilder);

            // check for only required fields needed from the voucherInstructionDTO
            try {
                unsupportedParameterValidation.handleRequiredParameterValidation(voucherInstruction.getNonNullFieldNames(), requiredFields,
                        validatorBuilder);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // // Check for serialNumber
            // validatorBuilder.reset().resource(resource).parameter(serialNumber).value(voucherInstruction.getSerialNumber())
            // .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_SERIAL_NUMBER).validateFieldMaxLengthWithFailureCodeAndErrorParams(
            // expectedSerialNumberLength, VoucherValidatorsEnum.INVALID_SERIAL_NUMBER_LENGTH);
        });

        // If errors exist, build and return PhErrorDTO
        if (validatorBuilder.hasError()) {
            validatorBuilder.errorCategory(PaymentHubErrorCategory.Validation.toString())
                    .errorCode(VoucherValidatorsEnum.VOUCHER_LIFECYCLE_VALIDATION_ERROR.getCode())
                    .errorDescription(VoucherValidatorsEnum.VOUCHER_LIFECYCLE_VALIDATION_ERROR.getMessage())
                    .developerMessage(VoucherValidatorsEnum.VOUCHER_LIFECYCLE_VALIDATION_ERROR.getMessage())
                    .defaultUserMessage(VoucherValidatorsEnum.VOUCHER_LIFECYCLE_VALIDATION_ERROR.getMessage());

            PhErrorDTO.PhErrorDTOBuilder phErrorDTOBuilder = new PhErrorDTO.PhErrorDTOBuilder(ExtValidationError.getErrorCode());
            phErrorDTOBuilder.fromValidatorBuilder(validatorBuilder);
            return phErrorDTOBuilder.build();
        }

        return null;
    }

    public PhErrorDTO validateRedeemVoucher(RedeemVoucherRequestDTO request) {
        final ValidatorBuilder validatorBuilder = new ValidatorBuilder();

        // check for unsupported parameter
        unsupportedParameterValidation.handleUnsupportedParameterValidation(request.getAdditionalProperties(), validatorBuilder);

        // Check for requestID
        validatorBuilder.reset().resource(resource).parameter(requestId).value(request.getRequestId())
                .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_REQUEST_ID).validateFieldMaxLengthWithFailureCodeAndErrorParams(
                        expectedRequestIdLength, VoucherValidatorsEnum.INVALID_REQUEST_ID_LENGTH);

        // Check for agentID
        validatorBuilder.reset().resource(resource).parameter(agentID).value(request.getAgentId())
                .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_AGENT_ID)
                .validateFieldMaxLengthWithFailureCodeAndErrorParams(expectedAgentIDLength, VoucherValidatorsEnum.INVALID_AGENT_ID_LENGTH);

        // // Check for voucherSecretNumber
        // validatorBuilder.reset().resource(resource).parameter(voucherSecretNumber).value(request.getVoucherSecretNumber())
        // .isNullWithFailureCode(VoucherValidatorsEnum.INVALID_VOUCHER_SECRET_NUMBER)
        // .validateFieldNotBlankAndLengthWithFailureCodeAndErrorParams(expectedVoucherSecretNumberLength,
        // VoucherValidatorsEnum.INVALID_VOUCHER_SECRET_NUMBER_LENGTH);

        // If errors exist, build and return PhErrorDTO
        if (validatorBuilder.hasError()) {
            validatorBuilder.errorCategory(PaymentHubErrorCategory.Validation.toString())
                    .errorCode(VoucherValidatorsEnum.REDEEM_VOUCHER_VALIDATION_ERROR.getCode())
                    .errorDescription(VoucherValidatorsEnum.REDEEM_VOUCHER_VALIDATION_ERROR.getMessage())
                    .developerMessage(VoucherValidatorsEnum.REDEEM_VOUCHER_VALIDATION_ERROR.getMessage())
                    .defaultUserMessage(VoucherValidatorsEnum.REDEEM_VOUCHER_VALIDATION_ERROR.getMessage());

            PhErrorDTO.PhErrorDTOBuilder phErrorDTOBuilder = new PhErrorDTO.PhErrorDTOBuilder(ExtValidationError.getErrorCode());
            phErrorDTOBuilder.fromValidatorBuilder(validatorBuilder);
            return phErrorDTOBuilder.build();
        }

        return null;
    }
}
