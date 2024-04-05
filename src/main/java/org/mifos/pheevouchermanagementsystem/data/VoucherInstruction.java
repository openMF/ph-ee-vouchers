package org.mifos.pheevouchermanagementsystem.data;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherInstruction {

    private String instructionID;
    private String groupCode;
    private String currency;
    private BigDecimal amount;
    private String payeeFunctionalID;
    private String narration;
    private String voucherNumber;
    private String serialNumber;
    private String status;

    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }

    public List<String> getNonNullFieldNames() throws IllegalAccessException {
        List<String> nonNullFields = new ArrayList<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value != null && !field.getName().equals("additionalProperties")) {
                    nonNullFields.add(field.getName());
                }
            } catch (IllegalAccessException e) {
                throw e;
            }
        }
        return nonNullFields;
    }
}
