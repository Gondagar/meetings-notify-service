package com.serfer.notify.tranfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Email {

    @JsonProperty("Message")
    String message;

    @JsonProperty("Subject")
    String subject;

    @JsonProperty("EmailTo")
    String emailTo;

}
