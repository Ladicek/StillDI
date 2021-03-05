package stilldi.impl;

import java.util.List;
import java.util.Set;

final class PhaseDiscoveryResult {
    final Set<String> additionalClasses;
    final List<ContextBuilderImpl> contexts;
    final List<MetaAnnotationsHelper.StereotypeConfigurator<?>> stereotypes;

    PhaseDiscoveryResult(Set<String> additionalClasses, List<ContextBuilderImpl> contexts,
            List<MetaAnnotationsHelper.StereotypeConfigurator<?>> stereotypes) {
        this.additionalClasses = additionalClasses;
        this.contexts = contexts;
        this.stereotypes = stereotypes;
    }
}
