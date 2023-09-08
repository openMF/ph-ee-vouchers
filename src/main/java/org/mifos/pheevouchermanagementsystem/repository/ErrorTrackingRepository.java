package org.mifos.pheevouchermanagementsystem.repository;

import org.mifos.pheevouchermanagementsystem.domain.ErrorTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ErrorTrackingRepository extends JpaRepository<ErrorTracking, Long>, JpaSpecificationExecutor<ErrorTracking> {}
