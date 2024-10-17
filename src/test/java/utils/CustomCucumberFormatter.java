package utils;

import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.*;

public class CustomCucumberFormatter implements Plugin {

    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestStepStarted.class, this::handleTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        // Implementation
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        // Implementation
    }

    private void handleTestStepStarted(TestStepStarted event) {
        // Implementation
    }

    private void handleTestStepFinished(TestStepFinished event) {
        // Implementation
    }
}
