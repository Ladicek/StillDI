package stilldi.impl;

import java.util.List;

final class PhaseSynthesisResult {
    final List<SyntheticBeanBuilderImpl<?>> syntheticBeans;
    final List<SyntheticObserverBuilderImpl> syntheticObservers;

    PhaseSynthesisResult(List<SyntheticBeanBuilderImpl<?>> syntheticBeans, List<SyntheticObserverBuilderImpl> syntheticObservers) {
        this.syntheticBeans = syntheticBeans;
        this.syntheticObservers = syntheticObservers;
    }
}
