package org.mifos.pheevouchermanagementsystem.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidityDTO {
    private String serialNumber;
    private Boolean isValid;
}
