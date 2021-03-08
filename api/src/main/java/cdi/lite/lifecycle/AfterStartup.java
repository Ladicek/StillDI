package cdi.lite.lifecycle;

/**
 * Event fired by the CDI container during application startup.
 * <p>
 * In a fully runtime environment (such as one with Portable Extensions), this event
 * corresponds to the {@code @Initialized(ApplicationScoped.class) Object} event.
 *
 * @see javax.enterprise.inject.spi.BeforeShutdown
 */
public interface AfterStartup {
}
