package stilldi.impl;

import java.util.List;

final class PhaseProcessingResult {
    final List<ProcessingAction> actions;

    PhaseProcessingResult(List<ProcessingAction> actions) {
        this.actions = actions;
    }
}
