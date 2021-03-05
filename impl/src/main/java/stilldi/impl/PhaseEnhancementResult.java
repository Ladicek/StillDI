package stilldi.impl;

import java.util.List;
import java.util.Set;

final class PhaseEnhancementResult {
    final List<EnhancementAction> actions;

    PhaseEnhancementResult(List<EnhancementAction> actions) {
        this.actions = actions;
    }
}
