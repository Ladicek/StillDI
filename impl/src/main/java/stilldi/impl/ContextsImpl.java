package stilldi.impl;

import cdi.lite.extension.phases.discovery.ContextBuilder;
import cdi.lite.extension.phases.discovery.Contexts;

import java.util.List;

class ContextsImpl implements Contexts {
    final List<ContextBuilderImpl> builders;

    ContextsImpl(List<ContextBuilderImpl> builders) {
        this.builders = builders;
    }

    @Override
    public ContextBuilder add() {
        ContextBuilderImpl builder = new ContextBuilderImpl();
        builders.add(builder);
        return builder;
    }
}
