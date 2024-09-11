package com.geinforce.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class LoginUserDTO {
    Long user_id;
    String name;
    String entity_type;

}
