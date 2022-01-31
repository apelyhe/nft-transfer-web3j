package hu.pelyheadam.nft;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
@ExternalTaskSubscription("token-error")
public class LogTokenError implements ExternalTaskHandler {

    private final static Logger LOGGER = Logger.getLogger(LogTokenError.class.getName());

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

        LOGGER.warning("Invalid token ID or token URI does not exists!");

        externalTaskService.complete(externalTask);
    }
}
