package hu.pelyheadam.nft;


import hu.pelyheadam.config.AddressConfig;
import hu.pelyheadam.contract.TestNFT;
import hu.pelyheadam.contract.seats.SeatsToken;
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
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import javax.crypto.KeyGenerator;
import java.math.BigInteger;
import java.util.Random;
import java.util.logging.Logger;

import static hu.pelyheadam.config.SmartContractConfig.GAS_LIMIT;
import static hu.pelyheadam.config.SmartContractConfig.GAS_PRICE;

@Component
@ExternalTaskSubscription("manage-token")
public class ManageTokenWorker implements ExternalTaskHandler {

    private final static Logger LOGGER = Logger.getLogger(ManageContractWorker.class.getName());
    private static final Web3j web3 = Web3j.build(new HttpService("http://vm.niif.cloud.bme.hu:5601"));
    private static final AddressConfig addressPaths = new AddressConfig();

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

        String from = externalTask.getVariable("_from");
        String tokenIdString = externalTask.getVariable("_tokenId");
        BigInteger tokenId = null;
        if (tokenIdString != null)
            tokenId = new BigInteger(tokenIdString);
        String contractAddress = externalTask.getVariable("_contractAddress");
        String tokenUri = externalTask.getVariable("_tokenUri");

        // the account from which the contract will be deployed
        Credentials credentials;

        // set the gas price and gas limit
        ContractGasProvider contractGasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
        BigInteger newTokenId = null;
        boolean tokenIdKnown = false;
        try {
            // get the path of sender account
            String fromAddress = addressPaths.getPathFromAddress(from);
            // load the credentials of sender account
            credentials = WalletUtils.loadCredentials("", fromAddress);

            // if the token id has not been sent, mint one
            if (tokenId == null && tokenUri != null) {

                SeatsToken contract = SeatsToken.load(contractAddress,web3,credentials,contractGasProvider);

                BigInteger before = contract.totalSupply().send();
                //LOGGER.info("Before: " + before.toString());
                //TransactionReceipt receipt = contract.mint(from, randomTokenId, tokenUri).send();
                TransactionReceipt receipt = contract.createToken(tokenUri).send();
                newTokenId = contract.balanceOf(from).send();
                //LOGGER.info("After: " + newTokenId.toString());
                LOGGER.info("Token minted! Transaction hash: " + receipt.getTransactionHash());
                tokenIdKnown = true;
            }
            else if (tokenId != null)
                tokenIdKnown = true;

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (!tokenIdKnown) {
            externalTaskService.handleBpmnError(externalTask, "Token_Error");
            //throwTokenError(externalTask.getId());
        } else {
            VariableMap variables = Variables.createVariables();
            if (newTokenId != null)
                variables.put("_tokenId", newTokenId.toString());

            externalTaskService.complete(externalTask, variables);
        }


    }

  /*  private void throwTokenError(String id) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost("http://localhost:8080/engine-rest/external-task/" + id + "/bpmnError");
            StringEntity params = new StringEntity("    {\n" +
                    "      \"workerId\": \"" + id + "\",\n" +
                    "      \"errorCode\": \"Token_Error\",\n" +
                    "      \"errorMessage\": \"Invalid address(es).\",\n" +
                    "       }");
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            //System.out.println("STATUS CODE:   " + response.getStatusLine().getStatusCode());
            //System.out.println("REASON:   " + response.getStatusLine().toString());
        } catch (Exception ignored) {
        }
    }*/
}
