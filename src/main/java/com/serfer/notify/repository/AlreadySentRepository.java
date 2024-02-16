package com.serfer.notify.repository;

import com.serfer.notify.entity.AlreadySent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AlreadySentRepository extends JpaRepository<AlreadySent, Long> {

    Optional<AlreadySent> findByOwnerAndDateTime(String email, LocalDateTime dataTime);

}