package org.mifos.pheevouchermanagementsystem.repository;

import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {
    Boolean existsByInstructionId(String instructionId);
    Boolean existsBySerialNo(String serialNo);
    Boolean existsByVoucherNo(String voucherNo);
    Optional<Voucher> findBySerialNo(String serialNo);
    Optional<Voucher> findByVoucherNo(String voucherNo);
}
