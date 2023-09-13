package org.mifos.pheevouchermanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@EnableCaching
@SpringBootApplication
@ComponentScan("org.mifos.pheevouchermanagementsystem")
public final class PhEeVoucherManagementSystemApplication {

    private PhEeVoucherManagementSystemApplication() {}

    public static void main(String[] args) {
        SpringApplication.run(PhEeVoucherManagementSystemApplication.class, args);
    }

}
