package com.cmpt276.group3.grouproject.models;

import com.cmpt276.group3.grouproject.enums.EOIStream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpressionOfInterestRepository
        extends JpaRepository<ExpressionOfInterest, Long> {

    List<ExpressionOfInterest> findByReceiverOrderByCreatedAtDesc(User receiver);

    Optional<ExpressionOfInterest> findByIdAndReceiver(Long id, User receiver);

    boolean existsBySenderAndReceiverAndStream(
            User sender,
            User receiver,
            EOIStream stream
    );
}
