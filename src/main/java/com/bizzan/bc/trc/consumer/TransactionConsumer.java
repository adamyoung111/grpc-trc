//package com.grpc.trc20.consumer;
//
//import com.alibaba.fastjson.JSONObject;
//import com.bizzan.bc.wallet.util.MessageResult;
//import com.grpc.trc20.dto.TransactionDto;
//import com.grpc.trc20.service.Trc20Service;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//import org.springframework.util.Assert;
//
//import java.util.List;
//
//@Log4j2
//@RequiredArgsConstructor
//@Component
//public class TransactionConsumer {
//
//    private final Trc20Service trc20Service;
//
//    /***
//     * 交易监听
//     * @param record
//     */
//    @KafkaListener(topics = {"trc20-create-transaction"})
//    public void createTransaction(ConsumerRecord<String, String> record) {
//        String content = record.value();
//        List<TransactionDto> transactionDtoList = JSONObject.parseArray(content, TransactionDto.class);
//        Assert.notNull(transactionDtoList, "交易信息为空");
//        transactionDtoList.forEach(transactionDto -> {
//            Long energy = trc20Service.getAccountSolidity(transactionDto.getFromAddress(), transactionDto.getPrivateKey());
//            Assert.notNull(energy, "当前能量不足");
//
//
//            String message = trc20Service.trc20Transaction(transactionDto.getToAddress(), transactionDto.getFromAddress()
//                    , transactionDto.getAmount(), transactionDto.getPrivateKey(), transactionDto.getMemo());
//            log.info("withdraw----txId=" + message);
//            MessageResult result = new MessageResult();
//            JSONObject jsonObject = JSONObject.parseObject(message);
//            JSONObject jsonObjectResult = JSONObject.parseObject(jsonObject.getString("result"));
//            if (jsonObjectResult.containsKey("result")) {
//                JSONObject jsonObjectTransaction = JSONObject.parseObject(jsonObject.getString("transaction"));
//                String txId = jsonObjectTransaction.containsKey("txID") ? jsonObjectTransaction.getString("txID") : "";
//                result.setData(txId);
//            }
//        });
//    }
//}
