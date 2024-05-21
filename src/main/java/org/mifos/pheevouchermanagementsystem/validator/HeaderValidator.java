package org.mifos.pheevouchermanagementsystem.validator;

import static org.mifos.connector.common.exception.PaymentHubError.ExtValidationError;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.mifos.connector.common.channel.dto.PhErrorDTO;
import org.mifos.connector.common.exception.PaymentHubErrorCategory;
import org.mifos.connector.common.validation.ValidatorBuilder;
import org.mifos.pheevouchermanagementsystem.data.UnsupportedParameterValidation;
import org.mifos.pheevouchermanagementsystem.util.HeaderConstants;
import org.mifos.pheevouchermanagementsystem.util.VoucherValidatorsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HeaderValidator {

    @Autowired
    private UnsupportedParameterValidation unsupportedParameterValidator;

    @Value("#{'${default_headers}'.split(',')}")
    private List<String> defaultHeader;

    private static final String resource = "voucherValidator";

    public PhErrorDTO validateCreateVoucher(Set<String> requiredHeaders, HttpServletRequest request) {
        final ValidatorBuilder validatorBuilder = new ValidatorBuilder();

        List<String> headers = getHeaderList(request);

        unsupportedParameterValidator.handleRequiredParameterValidation(headers, requiredHeaders, validatorBuilder);

        // Checks for X-Program-Id
        validatorBuilder.validateFieldIgnoreNullAndMaxLengthWithFailureCode(resource, HeaderConstants.X_PROGRAM_ID,
                request.getHeader(HeaderConstants.X_PROGRAM_ID), 20, VoucherValidatorsEnum.INVALID_PROGRAM_ID_LENGTH);

        // Checks for X-Registering-Institution-ID
        validatorBuilder.validateFieldIgnoreNullAndMaxLengthWithFailureCode(resource, HeaderConstants.X_REGISTERING_INSTITUTION_ID,
                request.getHeader(HeaderConstants.X_REGISTERING_INSTITUTION_ID), 20,
                VoucherValidatorsEnum.INVALID_REGISTERING_INSTITUTION_ID_LENGTH);

        // Checks for X-Callback-URL
        validatorBuilder.validateFieldIsNullAndMaxLengthWithFailureCode(resource, HeaderConstants.X_CALLBACKURL,
                request.getHeader(HeaderConstants.X_CALLBACKURL), VoucherValidatorsEnum.INVALID_CALLBACK_URL, 100,
                VoucherValidatorsEnum.INVALID_CALLBACK_URL_LENGTH);

        return handleValidationErrors(validatorBuilder);
    }

    public PhErrorDTO validateVoucherLifecycle(Set<String> requiredHeaders, HttpServletRequest request) {

        final ValidatorBuilder validatorBuilder = new ValidatorBuilder();

        List<String> headers = getHeaderList(request);

        unsupportedParameterValidator.handleRequiredParameterValidation(headers, requiredHeaders, validatorBuilder);

        // Checks for X-Program-Id
        validatorBuilder.validateFieldIgnoreNullAndMaxLengthWithFailureCode(resource, HeaderConstants.X_PROGRAM_ID,
                request.getHeader(HeaderConstants.X_PROGRAM_ID), 20, VoucherValidatorsEnum.INVALID_PROGRAM_ID_LENGTH);

        // Checks for X-Registering-Institution-ID
        validatorBuilder.validateFieldIsNullAndMaxLengthWithFailureCode(resource, HeaderConstants.X_REGISTERING_INSTITUTION_ID,
                request.getHeader(HeaderConstants.X_REGISTERING_INSTITUTION_ID), VoucherValidatorsEnum.INVALID_REGISTERING_INSTITUTION_ID,
                20, VoucherValidatorsEnum.INVALID_REGISTERING_INSTITUTION_ID_LENGTH);

        // Checks for X-Callback-URL
        validatorBuilder.validateFieldIsNullAndMaxLengthWithFailureCode(resource, HeaderConstants.X_CALLBACKURL,
                request.getHeader(HeaderConstants.X_CALLBACKURL), VoucherValidatorsEnum.INVALID_CALLBACK_URL, 100,
                VoucherValidatorsEnum.INVALID_CALLBACK_URL_LENGTH);

        return handleValidationErrors(validatorBuilder);
    }

    public PhErrorDTO validateCancelOrRedeemVoucher(Set<String> requiredHeaders, HttpServletRequest request) {

        final ValidatorBuilder validatorBuilder = new ValidatorBuilder();

        List<String> headers = getHeaderList(request);

        String command = request.getParameter("command");
        if (command.equals("redeem")) {
            headers.remove(HeaderConstants.X_CALLBACKURL);
            headers.remove(HeaderConstants.X_PROGRAM_ID);

            unsupportedParameterValidator.handleRequiredParameterValidation(headers, requiredHeaders, validatorBuilder);

            // Checks for X-Registering-Institution-ID
            validatorBuilder.validateFieldIsNullAndMaxLengthWithFailureCode(resource, HeaderConstants.X_REGISTERING_INSTITUTION_ID,
                    request.getHeader(HeaderConstants.X_REGISTERING_INSTITUTION_ID),
                    VoucherValidatorsEnum.INVALID_REGISTERING_INSTITUTION_ID, 20,
                    VoucherValidatorsEnum.INVALID_REGISTERING_INSTITUTION_ID_LENGTH);
        } else {
            return validateVoucherLifecycle(requiredHeaders, request);
        }

        return handleValidationErrors(validatorBuilder);
    }

    public PhErrorDTO validateForRegisteringInstitutionID(Set<String> requiredHeaders, HttpServletRequest request) {
        final ValidatorBuilder validatorBuilder = new ValidatorBuilder();

        List<String> headers = getHeaderList(request);

        unsupportedParameterValidator.handleRequiredParameterValidation(headers, requiredHeaders, validatorBuilder);

        // Checks for X-Registering-Institution-ID
        validatorBuilder.validateFieldIsNullAndMaxLengthWithFailureCode(resource, HeaderConstants.X_REGISTERING_INSTITUTION_ID,
                request.getHeader(HeaderConstants.X_REGISTERING_INSTITUTION_ID), VoucherValidatorsEnum.INVALID_REGISTERING_INSTITUTION_ID,
                20, VoucherValidatorsEnum.INVALID_REGISTERING_INSTITUTION_ID_LENGTH);

        return handleValidationErrors(validatorBuilder);

    }

    private PhErrorDTO handleValidationErrors(ValidatorBuilder validatorBuilder) {
        if (validatorBuilder.hasError()) {
            validatorBuilder.errorCategory(PaymentHubErrorCategory.Validation.toString())
                    .errorCode(VoucherValidatorsEnum.HEADER_VALIDATION_ERROR.getCode())
                    .errorDescription(VoucherValidatorsEnum.HEADER_VALIDATION_ERROR.getMessage())
                    .developerMessage(VoucherValidatorsEnum.HEADER_VALIDATION_ERROR.getMessage())
                    .defaultUserMessage(VoucherValidatorsEnum.HEADER_VALIDATION_ERROR.getMessage());

            PhErrorDTO.PhErrorDTOBuilder phErrorDTOBuilder = new PhErrorDTO.PhErrorDTOBuilder(ExtValidationError.getErrorCode());
            phErrorDTOBuilder.fromValidatorBuilder(validatorBuilder);
            return phErrorDTOBuilder.build();
        }
        return null;
    }

    public List<String> getHeaderList(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaderNames();
        return Collections.list(request.getHeaderNames());
    }
}