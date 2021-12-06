package hu.pelyheadam.nft;

import hu.pelyheadam.config.AddressConfig;
import hu.pelyheadam.contract.TestNFT;
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
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;


import java.util.logging.Logger;

import static hu.pelyheadam.config.SmartContractConfig.GAS_LIMIT;
import static hu.pelyheadam.config.SmartContractConfig.GAS_PRICE;

/*
    checks if _contractAddress has been sent, if not deploy one
 */

@Component
@ExternalTaskSubscription("manage-contract")
public class ManageContractWorker implements ExternalTaskHandler {

    private final static Logger LOGGER = Logger.getLogger(ManageContractWorker.class.getName());
    private static final Web3j web3 = Web3j.build(new HttpService("http://vm.niif.cloud.bme.hu:5601"));
    private static final AddressConfig addressPaths = new AddressConfig();

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

        String from = externalTask.getVariable("_from");
        String contractAddress = externalTask.getVariable("_contractAddress");

        // the account from which the contract will be deployed
        Credentials credentials;

        // set the gas price and gas limit
        ContractGasProvider contractGasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);

        try {
            // get the path of sender account
            String fromAddress = addressPaths.getPathFromAddress(from);
            // load the credentials of sender account
            credentials = WalletUtils.loadCredentials("", fromAddress);

            // if the contract address jas not been sent, deploy a new one
            if (contractAddress == null) {

                TestNFT contract = TestNFT.deploy(web3,
                        credentials,
                        contractGasProvider,
                        "smart contract", "TNFT").send();
                contractAddress = contract.getContractAddress();
                LOGGER.info("Successfully deployed! Address: " + contractAddress);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // add exists to the variables
        VariableMap variables = Variables.createVariables();
        variables.put("_contractAddress", contractAddress);

        // complete external task
        externalTaskService.complete(externalTask, variables);

    }
}
