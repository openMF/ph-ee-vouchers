package org.mifos.pheevouchermanagementsystem.repository;

import java.util.Optional;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {

    Boolean existsByRequestId(String requestId);

    Boolean existsByBatchId(String batchId);

    Boolean existsByInstructionId(String instructionId);

    Boolean existsBySerialNo(String serialNo);

    Boolean existsByVoucherNo(String voucherNo);

    Optional<Voucher> findBySerialNo(String serialNo);

    Optional<Voucher> findByVoucherNo(String voucherNo);

    Page<Voucher> findByRegisteringInstitutionId(String registeringInstitutionId, Pageable pageable);
}
