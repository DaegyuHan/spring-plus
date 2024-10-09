package org.example.expert.domain.manager.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.manager.dto.request.ManagerRegistLogEvent;
import org.example.expert.domain.manager.entity.ManagerRegistLog;
import org.example.expert.domain.manager.repository.ManagerRegistLogRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ManagerRegistLogService {

    private final ManagerRegistLogRepository managerRegistLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void ManagerRegistLog(ManagerRegistLogEvent managerRegistLogEvent) {

        ManagerRegistLog managerRegistLog = ManagerRegistLog.createManagerRegistLog(
                managerRegistLogEvent.getTodoTitle(),
                managerRegistLogEvent.getUserEmail(),
                managerRegistLogEvent.getManagerEmail(),
                managerRegistLogEvent.getManagerRegistLogEnum()
        );
        managerRegistLogRepository.save(managerRegistLog);
    }

}
