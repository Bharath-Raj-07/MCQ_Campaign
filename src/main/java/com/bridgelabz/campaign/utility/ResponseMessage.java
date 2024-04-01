package com.bridgelabz.campaign.utility;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseMessage {

    private int code;
    private String message;
    private Object data;

}
