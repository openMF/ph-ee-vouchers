package org.mifos.pheevouchermanagementsystem.data;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationRequest {

    private String batchId;

    private String payerIdentifier;

    private String currency;

    private BigDecimal amount;
}
