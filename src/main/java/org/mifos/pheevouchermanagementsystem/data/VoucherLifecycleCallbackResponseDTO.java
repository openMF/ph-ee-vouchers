package org.mifos.pheevouchermanagementsystem.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherLifecycleCallbackResponseDTO {
    private String requestId;
    private String registerRequestId;
    private Integer numberFailedCases;
    private List<FailedCaseDTO> failedCases;
}
