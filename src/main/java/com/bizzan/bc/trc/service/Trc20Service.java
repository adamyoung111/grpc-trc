package com.bizzan.bc.trc.service;

import com.bizzan.bc.wallet.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tron.keystore.Credentials;
import org.tron.keystore.WalletUtils;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class Trc20Service {

    @Value("${trc.apiKey}")
    private String apiKey;

    @Value("${trc.contractAddress}")
    private String contractAddress;

    @Value("${trc.feeLimit}")
    private Long feeLimit;
    private final BigDecimal conversion = new BigDecimal("1000000");
    @Value("${trc.privateKey}")
    private String privateKey;

    /***
     * trc20转账
     * @param toAddress
     * @param fromAddress
     * @param amount
     * @return
     */
    public String trc20Transaction(String toAddress, String privateKey, String fromAddress, long amount, String memo) {
        ApiWrapper wrapper = ApiWrapper.ofMainnet(privateKey, apiKey);
        Contract contract = wrapper.getContract(contractAddress);
        Trc20Contract token = new Trc20Contract(contract, fromAddress, wrapper);
        String result = token.transfer(toAddress, amount * 1000000, 0, memo, feeLimit);
        return result;
    }


    /***
     * 查询当前地址资源
     * @param ownerAddress
     * @return
     */
    public Map<String, BigDecimal> getAccountSolidity(String ownerAddress) {
        ApiWrapper wrapper = ApiWrapper.ofMainnet(privateKey, apiKey);
        Contract contract = wrapper.getContract(contractAddress);
        Trc20Contract token = new Trc20Contract(contract, ownerAddress, wrapper);
        Response.Account account = wrapper.getAccountSolidity(ownerAddress);
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("energy", new BigDecimal(account.getAccountResource().getEnergyUsage()));
        map.put("bandWidth", new BigDecimal(account.getFreeNetUsage()));
        map.put("TrxBalance", BigDecimal.valueOf(account.getBalance()).divide(conversion));
        map.put("Trc20Balance", BigDecimal.valueOf(token.balanceOf(ownerAddress).longValue()).divide(conversion));
        map.put("frozenBalance", BigDecimal.valueOf(account.getAccountResource().getFrozenBalanceForEnergy().getFrozenBalance()).divide(conversion));
        return map;
    }

    /***
     * 冻结余额转换能量或带宽
     * @param ownerAddress
     * @param frozenBalance
     * @param frozenDuration
     * @param resourceCode  0("BANDWIDTH") or 1("ENERGY")
     * @return
     * @throws IllegalException
     */
    public String freezeBalance(String ownerAddress, long frozenBalance, long frozenDuration, int resourceCode, String receiveAddress) throws IllegalException {
        ApiWrapper wrapper = ApiWrapper.ofMainnet(privateKey, apiKey);
        BigDecimal freezeAmount = BigDecimal.valueOf(frozenBalance).multiply(conversion);
        Response.TransactionExtention transactionExtention;
        if (null != receiveAddress && StringUtils.isNotBlank(receiveAddress)) {
            transactionExtention = wrapper.freezeBalance(ownerAddress, freezeAmount.longValue(), frozenDuration, resourceCode, receiveAddress);
        } else {
            transactionExtention = wrapper.freezeBalance(ownerAddress, freezeAmount.longValue(), frozenDuration, resourceCode);
        }
        return getBroadcastTransaction(transactionExtention, wrapper);
    }

    /***
     * 解冻余额释放能量或带宽
     * @param ownerAddress
     * @param privateKey
     * @param resourceCode  0("BANDWIDTH") or 1("ENERGY")
     * @return
     * @throws IllegalException
     */
    public String unfreezeBalance(String ownerAddress, String privateKey, int resourceCode) throws IllegalException {
        ApiWrapper wrapper = ApiWrapper.ofMainnet(privateKey, apiKey);
        Response.TransactionExtention transactionExtention = wrapper.unfreezeBalance(ownerAddress, resourceCode);
        BigDecimal bigDecimal = new BigDecimal("100");
        return getBroadcastTransaction(transactionExtention, wrapper);
    }


    /***
     * 签名广播
     * @param transactionExtention
     * @param wrapper
     * @return txid
     */
    public String getBroadcastTransaction(Response.TransactionExtention transactionExtention, ApiWrapper wrapper) {
        Chain.Transaction signedTxn = wrapper.signTransaction(transactionExtention);
        Response.TransactionReturn ret = wrapper.blockingStub.broadcastTransaction(signedTxn);
        log.info("======== Result ========\n" + ret.toString());
        return Hex.toHexString(transactionExtention.getTxid().toByteArray());
    }
}
