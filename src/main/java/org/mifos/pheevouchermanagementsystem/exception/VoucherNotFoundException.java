package org.mifos.pheevouchermanagementsystem.exception;

import java.text.MessageFormat;

public class VoucherNotFoundException extends RuntimeException{
    public VoucherNotFoundException(String message) {
        super(message);
    }

    public static InstructionIdException voucherNotFound(final String voucherSerialNo) {
        String stringWithPlaceHolder = "Voucher with serial number {0} already exist!";
        return new InstructionIdException(MessageFormat.format(stringWithPlaceHolder,voucherSerialNo));
    }
}
