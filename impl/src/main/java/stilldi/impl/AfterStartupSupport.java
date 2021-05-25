package stilldi.impl;

import cdi.lite.lifecycle.AfterStartup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AfterStartupSupport {
    @Inject
    private Event<AfterStartup> afterStartup;

    public void onStartup(@Observes @Initialized(ApplicationScoped.class) Object event) {
        afterStartup.fire(new AfterStartupImpl());
    }
}
