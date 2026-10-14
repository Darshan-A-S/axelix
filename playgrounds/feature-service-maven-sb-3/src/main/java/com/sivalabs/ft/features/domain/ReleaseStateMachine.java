package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.exceptions.BadRequestException;
import com.sivalabs.ft.features.domain.models.ReleaseStatus;
import java.util.EnumSet;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Guards the {@link ReleaseStatus} lifecycle with Spring Statemachine: a release can be published
 * (DRAFT -> RELEASED), but a published release cannot go back to DRAFT.
 */
@Component
class ReleaseStateMachine {

    enum ReleaseEvent {
        PUBLISH,
        REVERT_TO_DRAFT
    }

    void validateTransition(String releaseCode, ReleaseStatus current, ReleaseStatus target) {
        if (target == null || target == current) {
            return;
        }
        StateMachine<ReleaseStatus, ReleaseEvent> machine = build(current);
        machine.startReactively().block();
        try {
            ReleaseEvent event = target == ReleaseStatus.RELEASED ? ReleaseEvent.PUBLISH : ReleaseEvent.REVERT_TO_DRAFT;
            StateMachineEventResult<ReleaseStatus, ReleaseEvent> result = machine.sendEvent(
                            Mono.just(MessageBuilder.withPayload(event).build()))
                    .blockLast();
            if (result == null || result.getResultType() != ResultType.ACCEPTED) {
                throw new BadRequestException(
                        "Release " + releaseCode + " cannot transition from " + current + " to " + target);
            }
        } finally {
            machine.stopReactively().block();
        }
    }

    private static StateMachine<ReleaseStatus, ReleaseEvent> build(ReleaseStatus initial) {
        try {
            StateMachineBuilder.Builder<ReleaseStatus, ReleaseEvent> builder = StateMachineBuilder.builder();
            builder.configureStates().withStates().initial(initial).states(EnumSet.allOf(ReleaseStatus.class));
            builder.configureTransitions()
                    .withExternal()
                    .source(ReleaseStatus.DRAFT)
                    .target(ReleaseStatus.RELEASED)
                    .event(ReleaseEvent.PUBLISH);
            return builder.build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build the release state machine", e);
        }
    }
}
