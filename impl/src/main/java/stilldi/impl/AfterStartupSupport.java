package stilldi.impl;

import cdi.lite.lifecycle.AfterStartup;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AfterStartupSupport {
    @Inject
    private Event<AfterStartup> afterStartup;

    public void onStartup(@Observes @Initialized(ApplicationScoped.class) Object event) {
        afterStartup.fire(new AfterStartupImpl());
    }
}
