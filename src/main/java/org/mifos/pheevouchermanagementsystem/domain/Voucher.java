package org.mifos.pheevouchermanagementsystem.domain;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_no", unique = true)
    private String serialNo;

    @Column(name = "voucher_no", unique = true)
    private String voucherNo;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "group_code")
    private String groupCode;

    @Column(name = "status")
    private String status;

    @Column(name = "expiry_date")
    private Date expiryDate;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "activated_date")
    private LocalDateTime activatedDate;

    @Column(name = "payee_functional_id")
    private String payeeFunctionalId;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "instruction_id", unique = true)
    private String instructionId;

    @Column(name = "request_id", nullable = false)
    private String requestId;
    @Column(name = "registering_institution_id", nullable = false)
    private String registeringInstitutionId;

    public Voucher(String serialNo, String voucherNo, BigDecimal amount, String currency, String groupCode, String status, Date expiryDate,
            LocalDateTime createdDate, LocalDateTime activatedDate, String payeeFunctionalId, String batchId, String instructionId,
            String requestId, String registeringInstitutionId) {
        this.serialNo = serialNo;
        this.voucherNo = voucherNo;
        this.amount = amount;
        this.currency = currency;
        this.groupCode = groupCode;
        this.status = status;
        this.expiryDate = expiryDate;
        this.createdDate = createdDate;
        this.activatedDate = activatedDate;
        this.payeeFunctionalId = payeeFunctionalId;
        this.batchId = batchId;
        this.instructionId = instructionId;
        this.requestId = requestId;
        this.registeringInstitutionId = registeringInstitutionId;
    }

    public Voucher() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getActivatedDate() {
        return activatedDate;
    }

    public void setActivatedDate(LocalDateTime activatedDate) {
        this.activatedDate = activatedDate;
    }

    public String getPayeeFunctionalId() {
        return payeeFunctionalId;
    }

    public void setPayeeFunctionalId(String payeeFunctionalId) {
        this.payeeFunctionalId = payeeFunctionalId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRegisteringInstitutionId() {
        return registeringInstitutionId;
    }

    public void setRegisteringInstitutionId(String registeringInstitutionId) {
        this.registeringInstitutionId = registeringInstitutionId;
    }
}
