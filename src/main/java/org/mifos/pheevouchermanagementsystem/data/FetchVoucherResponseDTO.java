package org.mifos.pheevouchermanagementsystem.data;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FetchVoucherResponseDTO {

    private String serialNumber;
    private LocalDateTime createdDate;
    private String registeringInstitutionId;
    private String status;
    private String payeeFunctionalID;
}
