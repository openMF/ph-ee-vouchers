package org.mifos.pheevouchermanagementsystem.service;

import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherRequestDTO;
import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherResponseDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RedeemVoucherService {
    @Async("asyncExecutor")
    public RedeemVoucherResponseDTO redeemVoucher(RedeemVoucherRequestDTO redeemVoucherRequestDTO){

        return null;
    }
}
