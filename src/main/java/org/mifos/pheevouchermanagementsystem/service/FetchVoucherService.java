package org.mifos.pheevouchermanagementsystem.service;

import org.mifos.pheevouchermanagementsystem.data.FetchVoucherResponseDTO;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.VoucherNotFoundException;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FetchVoucherService {

    private final VoucherRepository voucherRepository;

    @Autowired
    public FetchVoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    public FetchVoucherResponseDTO fetchVoucher(String serialNumber, String registeringInstitutionId) {
        Voucher voucher = voucherRepository.findBySerialNo(serialNumber)
                .orElseThrow(() -> VoucherNotFoundException.voucherNotFound(serialNumber));
        return new FetchVoucherResponseDTO(serialNumber, voucher.getCreatedDate(), registeringInstitutionId, voucher.getStatus(),
                voucher.getPayeeFunctionalId());
    }

    public Page<FetchVoucherResponseDTO> fetchAllVouchers(String registeringInstitutionId, Integer page, Integer size) {
        Pageable pageRequest = PageRequest.of(page, size);
        Page<Voucher> voucherPage = voucherRepository.findAll(pageRequest);

        return voucherPage.map(voucher -> new FetchVoucherResponseDTO(voucher.getSerialNo(), voucher.getCreatedDate(),
                registeringInstitutionId, voucher.getStatus(), voucher.getPayeeFunctionalId()));
    }
}
