package stilldi.impl;

import java.util.List;

final class PhaseEnhancementResult {
    final List<EnhancementAction> actions;

    PhaseEnhancementResult(List<EnhancementAction> actions) {
        this.actions = actions;
    }
}
