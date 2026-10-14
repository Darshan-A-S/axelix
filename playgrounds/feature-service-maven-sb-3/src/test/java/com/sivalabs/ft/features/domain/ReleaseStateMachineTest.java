package com.sivalabs.ft.features.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sivalabs.ft.features.domain.exceptions.BadRequestException;
import com.sivalabs.ft.features.domain.models.ReleaseStatus;
import org.junit.jupiter.api.Test;

class ReleaseStateMachineTest {

    private final ReleaseStateMachine releaseStateMachine = new ReleaseStateMachine();

    @Test
    void shouldAllowPublishingDraftRelease() {
        assertThatCode(() -> releaseStateMachine.validateTransition(
                        "IDEA-2025.1", ReleaseStatus.DRAFT, ReleaseStatus.RELEASED))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowKeepingTheSameStatus() {
        assertThatCode(() -> releaseStateMachine.validateTransition(
                        "IDEA-2025.1", ReleaseStatus.RELEASED, ReleaseStatus.RELEASED))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectRevertingReleasedReleaseToDraft() {
        assertThatThrownBy(() -> releaseStateMachine.validateTransition(
                        "IDEA-2025.1", ReleaseStatus.RELEASED, ReleaseStatus.DRAFT))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("RELEASED to DRAFT");
    }
}
