package com.bizzan.bc.wallet;


import com.bizzan.bc.trc.Trc20Application;
import com.bizzan.bc.trc.util.TransactionForTrx;
import com.bizzan.bc.trc.util.TransformUtil;
import com.bizzan.bc.trc.util.TypeDecoder;
import com.bizzan.bc.wallet.entity.Deposit;
import com.bizzan.bc.wallet.service.AccountService;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import org.tron.common.crypto.Sha256Sm3Hash;
import org.tron.common.utils.ByteArray;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Chain;


import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;

import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Contract.TriggerSmartContract;
import org.tron.trident.proto.Response;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionReturn;
import org.tron.trident.utils.Base58Check;
import org.tron.walletserver.WalletApi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Trc20Application.class})
class Trc20ApplicationTests {



    @Test
    void contextLoads() {
//        transferFrom("TYsZwqvbB58YJdofXmvaF1PxLKacvhQL4K","THtba1phs7bAiWxbjvqz1jJ95tt7GQiJ13",
//                1,0,"交易备注",10000000);
//        ApiWrapper wrapper = ApiWrapper.ofMainnet("5c51f727cfa124d32a4bb96ef85cc4fc9a9d69f98b6ff85e97f82f955647286b", "22e0780c-55fc-4dcf-919d-f74d6edbaee0");
//        org.tron.trident.core.contract.Contract contract = wrapper.getContract("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t");
//        Trc20Contract token = new Trc20Contract(contract, "TYsZwqvbB58YJdofXmvaF1PxLKacvhQL4K", wrapper);
//        String result = token.transfer("TXL5A2CXWDF4479rRxXiNSuhVQLRwS6f47", 1, 0, "测试", 10000000);
//        System.out.println(result);
//        AccountService accountService = ActivitiConfig.getBean(AccountService.class);
//        List<Account> accountList = accountService.findByBalanceAndGas(BigDecimal.ZERO, BigDecimal.ZERO);
//        System.err.println(accountList);
//        System.err.println(accountService.count());;
    }

    public static void testSendTrc20Transaction() {
        ApiWrapper client = ApiWrapper.ofMainnet("5c51f727cfa124d32a4bb96ef85cc4fc9a9d69f98b6ff85e97f82f955647286b", "22e0780c-55fc-4dcf-919d-f74d6edbaee0");
        Function trc20Transfer = new Function("transfer", Arrays.asList(new Address("TTeHr8xMVMP3yF22974d3StZCVNKA4cQdo"), new Uint256(1L)), Arrays.asList(new TypeReference<Bool>() {
        }));
        String encodedHex = FunctionEncoder.encode(trc20Transfer);

        TriggerSmartContract trigger = TriggerSmartContract.newBuilder().setOwnerAddress(ApiWrapper.parseAddress("TYsZwqvbB58YJdofXmvaF1PxLKacvhQL4K")).setContractAddress(ApiWrapper.parseAddress("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")).setData(ApiWrapper.parseHex(encodedHex)).build();

        System.out.println("trigger:\n" + trigger);

        TransactionExtention txnExt = client.blockingStub.triggerContract(trigger);
        System.out.println("txn id => " + Hex.toHexString(txnExt.getTxid().toByteArray()));

        Chain.Transaction signedTxn = client.signTransaction(txnExt);
        System.out.println(signedTxn.toString());
        TransactionReturn ret = client.blockingStub.broadcastTransaction(signedTxn);
        System.out.println("======== Result ========\n" + ret.toString());
    }

    public void transferFrom(String fromAddr, String destAddr, long amount, int power, String memo, long feeLimit) {
        ApiWrapper client = ApiWrapper.ofMainnet("5c51f727cfa124d32a4bb96ef85cc4fc9a9d69f98b6ff85e97f82f955647286b", "22e0780c-55fc-4dcf-919d-f74d6edbaee0");

        Function transferFrom = new Function("transferFrom", Arrays.asList(new Address(fromAddr), new Address(destAddr), new Uint256(BigInteger.valueOf(amount).multiply(BigInteger.valueOf(10).pow(power)))), Arrays.asList(new TypeReference<Bool>() {
        }));

        TransactionBuilder builder = client.triggerCall(Base58Check.bytesToBase58(ApiWrapper.parseAddress(fromAddr).toByteArray()), Base58Check.bytesToBase58(ApiWrapper.parseAddress("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t").toByteArray()), transferFrom);

        builder.setFeeLimit(feeLimit);
        builder.setMemo(memo);

        Chain.Transaction signedTxn = client.signTransaction(builder.build());

        System.out.println(client.broadcastTransaction(signedTxn));
        ;
    }

//    public  void addDeposits(List<Deposit> deposits, String fromAddress, String toAddress, String txId,
//                                   long finalBlockHeight, BigDecimal amount) {
//        //如果是本系统生成的地址
//        if (!accountService.isAddressExist(fromAddress) && accountService.isAddressExist(toAddress)
//        ) {
//            Deposit deposit = new Deposit();
//            Date time = new Date(System.currentTimeMillis());
//            deposit.setTime(time);
//            deposit.setBlockHash(txId);
//            deposit.setTxid(txId);
//            deposit.setBlockHeight(finalBlockHeight);
//            deposit.setAddress(toAddress);
//            deposit.setAmount(amount);
//            deposit.setStatus(2);
//            deposits.add(deposit);
//        }
//    }
    public static void main(String[] args) throws IllegalException {
        ApiWrapper wrapper = ApiWrapper.ofMainnet("5c51f727cfa124d32a4bb96ef85cc4fc9a9d69f98b6ff85e97f82f955647286b", "22e0780c-55fc-4dcf-919d-f74d6edbaee0");
        Chain.Block block = wrapper.getBlockByNum(39338640);
        List<Chain.Transaction> transactionList = block.getTransactionsList();
        List<Deposit> deposits = new ArrayList<>();
        transactionList.forEach(transaction -> {
            Any parameter = transaction.getRawData().getContract(0).getParameter();
            String type = transaction.getRawData().getContract(0).getType().toString();
            try {
                if (type.equalsIgnoreCase("TriggerSmartContract")) {
                    TriggerSmartContract contract = parameter.unpack(TriggerSmartContract.class);
                    String data = TransformUtil.bytesToHexString(contract.getData().toByteArray());
                    if (data.length() == 136) {
                        String txid = ByteArray.toHexString(Sha256Sm3Hash.hash(transaction.getRawData().toByteArray()));
                        System.out.println("txn id => " + txid);
                    }
                } else if (type.equalsIgnoreCase("TransferContract")) {
                    Contract.TransferContract contract = parameter.unpack(Contract.TransferContract.class);
                    System.err.println(TransformUtil.bytesToHexString(contract.getToAddress().toByteArray()));
                    System.err.println(TransformUtil.bytesToHexString(contract.getOwnerAddress().toByteArray())) ;
                    System.err.println(TypeDecoder.decodeAddress("0000000000000000000000"+TransformUtil.bytesToHexString(contract.getOwnerAddress().toByteArray())));
                    System.err.println(contract.getAmount());
                    String txId = TransactionForTrx.getTransactionHash(transaction);
                    System.out.println("txn id => " + txId);
                    Deposit deposit = new Deposit();
                    Date time = new Date(System.currentTimeMillis());
                    deposit.setTime(time);
                    deposit.setBlockHash(txId);
                    deposit.setTxid(txId);
                    deposit.setBlockHeight(123L);
//                    deposit.setAddress(toAddress);
//                    deposit.setAmount(amount);
                    deposit.setStatus(2);
                    deposits.add(deposit);
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        });
        Chain.Transaction transaction = wrapper.getTransactionById("1f3557df7b270a118f4d09812bd84b7ef6c20b504111e909e44328261493fdf0");
//        String data=ApiWrapper.toHex(transaction.getRawDataOrBuilder().getContract(0).getParameter().getValue().toByteArray());
//        System.err.println(data);
//        Contract contract = wrapper.getContract("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t");
//        Trc20Contract token = new Trc20Contract(contract, "TMJ2RneQuvGUQQB27xUhtawXP7NQiiAyJ8", wrapper);
//        String result= token.transfer("TYsZwqvbB58YJdofXmvaF1PxLKacvhQL4K",1,0,"测试",1000000);
//        System.err.println(result);
////        BigInteger bigInteger= token.balanceOf("TYsZwqvbB58YJdofXmvaF1PxLKacvhQL4K");
////        TransactionExtention transactionExtention= wrapper.unfreezeBalance("TYsZwqvbB58YJdofXmvaF1PxLKacvhQL4K",0);
//        Response.Account account = wrapper.getAccount("TYsZwqvbB58YJdofXmvaF1PxLKacvhQL4K");
//        System.out.println(account.getBalance());
//        System.out.println(account.getAccountResource().getEnergyUsage());
//        System.out.println(account.getFreeNetUsage());
//        System.err.println(account.getAccountResource().getFrozenBalanceForEnergy().getFrozenBalance());
    }

}
