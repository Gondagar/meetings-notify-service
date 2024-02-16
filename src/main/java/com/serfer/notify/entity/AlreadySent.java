package com.serfer.notify.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "alreadySent")
public class AlreadySent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    String owner;

    LocalDateTime dateTime;

}
