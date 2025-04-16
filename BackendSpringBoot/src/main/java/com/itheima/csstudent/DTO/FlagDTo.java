package com.itheima.csstudent.DTO;

import lombok.Data;
import org.springframework.security.core.parameters.P;

@Data
public class FlagDTo {
    private String userId;
    private String imageName;
    private String flag;
}
