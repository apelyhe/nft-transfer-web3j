package hu.pelyheadam.nft;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
@ExternalTaskSubscription("address-error")
public class LogAddressError implements ExternalTaskHandler {

    private final static Logger LOGGER = Logger.getLogger(LogAddressError.class.getName());

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

        LOGGER.warning("Invalid address(es)!");

        externalTaskService.complete(externalTask);
    }
}
