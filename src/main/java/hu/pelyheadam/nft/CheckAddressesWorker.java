package hu.pelyheadam.nft;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Component
@ExternalTaskSubscription("check-addresses")
public class CheckAddressesWorker implements ExternalTaskHandler {

    private static final Web3j web3 = Web3j.build(new HttpService("http://vm.niif.cloud.bme.hu:5601"));

    public CheckAddressesWorker() throws IOException {
    }

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

        boolean addressesExist = false, toExists = false, fromExists = false;

        // get variables
        String fromInString = externalTask.getVariable("_from");
        String toInString = externalTask.getVariable("_to");

        // get all the accounts in network
        EthAccounts accounts = null;
        try {
            accounts = getEthAccounts();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        // if an account from a network equals with the input parameter "_to"
        // return with true
        for (String account : accounts.getAccounts()) {
            if (account.toLowerCase().compareTo(fromInString.toLowerCase()) == 0) {
                fromExists = true;
            }
            if (account.toLowerCase().compareTo(toInString.toLowerCase()) == 0) {
                toExists = true;
            }
            if (fromExists && toExists) {
                addressesExist = true;
                break;
            }
        }


        // add exists to the variables
        VariableMap variables = Variables.createVariables();
        variables.put("_addressesExist", addressesExist);
        
        // complete external task
        externalTaskService.complete(externalTask, variables);
    }

    // a function in order to get ethereum accounts
    public EthAccounts getEthAccounts() throws ExecutionException, InterruptedException {
        EthAccounts result = new EthAccounts();
        result = web3.ethAccounts()
                .sendAsync()
                .get();
        return result;
    }
}
