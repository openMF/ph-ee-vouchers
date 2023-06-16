package org.mifos.pheevouchermanagementsystem.repository;

import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {
    Boolean existsByInstructionId(String instructionId);
}
