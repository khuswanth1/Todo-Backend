package com.todo.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import com.todo.app.entity.PushSubscription;
import com.todo.app.entity.User;
import java.util.List;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUser(User user);

    @Transactional
    void deleteByEndpoint(String endpoint);
}
