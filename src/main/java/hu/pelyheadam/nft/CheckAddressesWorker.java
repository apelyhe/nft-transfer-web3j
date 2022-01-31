package hu.pelyheadam.nft;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import java.util.concurrent.ExecutionException;

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
        //String price = externalTask.getVariable("_price");
        //System.out.println("\n\n\n" + price + "\n\n\n");

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


        if (!toExists || !fromExists) {
            /*try {
                externalTaskService.handleBpmnError(externalTask, "Address_Error");
                //throwAddressError(externalTask.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            externalTaskService.handleBpmnError(externalTask, "Address_Error");
        } else {
            // complete external task
            externalTaskService.complete(externalTask);
        }
    }

    //source: https://stackoverflow.com/questions/7181534/http-post-using-json-in-java
    private void throwAddressError(String id) throws IOException {


        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost("http://localhost:8080/engine-rest/external-task/" + id + "/bpmnError");
            StringEntity params = new StringEntity("    {\n" +
                    "      \"workerId\": \"" + id + "\",\n" +
                    "      \"errorCode\": \"Address_Error\",\n" +
                    "      \"errorMessage\": \"Invalid address(es).\",\n" +
                    "       }");
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            //System.out.println("STATUS CODE:   " + response.getStatusLine().getStatusCode());
            //System.out.println("REASON:   " + response.getStatusLine().toString());
        } catch (Exception ignored) {
        }

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
