package com.bizzan.bc.trc.util;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Sm3Hash;
import org.tron.common.utils.ByteArray;
import org.tron.core.exception.CancelException;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Block;
//import org.tron.protos.Protocol.Transaction;
import org.tron.trident.proto.Chain;
import org.tron.protos.contract.BalanceContract.TransferContract;
import org.tron.walletserver.WalletApi;

import java.util.Arrays;

public class TransactionForTrx {

    public static Chain.Transaction setReference(Chain.Transaction transaction, Block newestBlock) {
        long blockHeight = newestBlock.getBlockHeader().getRawData().getNumber();
        byte[] blockHash = getBlockHash(newestBlock).getBytes();
        byte[] refBlockNum = ByteArray.fromLong(blockHeight);
        Chain.Transaction.raw rawData = transaction.getRawData().toBuilder()
                .setRefBlockHash(ByteString.copyFrom(ByteArray.subArray(blockHash, 8, 16)))
                .setRefBlockBytes(ByteString.copyFrom(ByteArray.subArray(refBlockNum, 6, 8)))
                .build();
        return transaction.toBuilder().setRawData(rawData).build();
    }

    public static Sha256Sm3Hash getBlockHash(Block block) {
        return Sha256Sm3Hash.of(block.getBlockHeader().getRawData().toByteArray());
    }

    public static String getTransactionHash(Chain.Transaction transaction) {
        String txid = ByteArray.toHexString(Sha256Sm3Hash.hash(transaction.getRawData().toByteArray()));
        return txid;
    }

    public static Chain.Transaction createTransaction(byte[] from, byte[] to, long amount) {
        Chain.Transaction.Builder transactionBuilder = Chain.Transaction.newBuilder();
        Block newestBlock = WalletApi.getBlock(-1);

        Chain.Transaction.Contract.Builder contractBuilder = Chain.Transaction.Contract.newBuilder();
        TransferContract.Builder transferContractBuilder = TransferContract.newBuilder();
        transferContractBuilder.setAmount(amount);
        ByteString bsTo = ByteString.copyFrom(to);
        ByteString bsOwner = ByteString.copyFrom(from);
        transferContractBuilder.setToAddress(bsTo);
        transferContractBuilder.setOwnerAddress(bsOwner);
        try {
            Any any = Any.pack(transferContractBuilder.build());
            contractBuilder.setParameter(any);
        } catch (Exception e) {
            return null;
        }
        contractBuilder.setType(Chain.Transaction.Contract.ContractType.TransferContract);
        transactionBuilder.getRawDataBuilder().addContract(contractBuilder)
                .setTimestamp(System.currentTimeMillis())
                .setExpiration(newestBlock.getBlockHeader().getRawData().getTimestamp() + 10 * 60 * 60 * 1000);
        Chain.Transaction transaction = transactionBuilder.build();
        Chain.Transaction refTransaction = setReference(transaction, newestBlock);
        return refTransaction;
    }

    static byte[] signTransaction2Byte(byte[] transaction, byte[] privateKey)
            throws InvalidProtocolBufferException {
        ECKey ecKey = ECKey.fromPrivate(privateKey);
        Chain.Transaction transaction1 = Chain.Transaction.parseFrom(transaction);
        byte[] rawdata = transaction1.getRawData().toByteArray();
        byte[] hash = Sha256Sm3Hash.hash(rawdata);
        byte[] sign = ecKey.sign(hash).toByteArray();
        return transaction1.toBuilder().addSignature(ByteString.copyFrom(sign)).build().toByteArray();
    }



    static boolean broadcast(byte[] transactionBytes) throws InvalidProtocolBufferException {
        return WalletApi.broadcastTransaction(transactionBytes);
    }


}
