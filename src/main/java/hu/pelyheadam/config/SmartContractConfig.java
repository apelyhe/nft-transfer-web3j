package hu.pelyheadam.config;

import java.math.BigInteger;

public interface SmartContractConfig {
    BigInteger GAS_PRICE = BigInteger.ZERO;
    BigInteger GAS_LIMIT = BigInteger.valueOf(340636L);
}
