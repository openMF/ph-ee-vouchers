package org.mifos.pheevouchermanagementsystem.exception;

import java.text.MessageFormat;

public class InstructionIdException extends RuntimeException{
    public InstructionIdException(String message) {
        super(message);
    }

    public static InstructionIdException instructionIdNotFound(final String instructionId) {
        String stringWithPlaceHolder = "Instruction ID with {0} already exist!";
        return new InstructionIdException(MessageFormat.format(stringWithPlaceHolder,instructionId));
    }
}
