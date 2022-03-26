package com.bizzan.bc.trc.controller;

import com.bizzan.bc.trc.dto.TransactionDto;
import com.bizzan.bc.trc.service.Trc20Service;
import com.bizzan.bc.wallet.util.MessageResult;
import com.bizzan.bc.trc.config.ActivitiConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tron.common.utils.ByteArray;
import org.tron.keystore.Credentials;
import org.tron.keystore.WalletUtils;
import org.tron.trident.core.exceptions.IllegalException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Validated
@Api(tags = {"交易模块"})
@RestController
@RequestMapping("/transaction")
public class TransactionController {


    @ApiOperation(value = "TRC20转账交易接口", notes = "TRC20转账交易接口")
    @RequestMapping("trc20Transaction" )
    private MessageResult trc20Transaction(@RequestBody TransactionDto transactionDto) {
        Trc20Service trc20Service = ActivitiConfig.getBean(Trc20Service.class);
        MessageResult messageResult;
        try {
            String txId = trc20Service.trc20Transaction(transactionDto.getToAddress(),transactionDto.getPrivateKey(),transactionDto.getFromAddress()
                    , transactionDto.getAmount(), transactionDto.getMemo());
            messageResult = MessageResult.success();
            messageResult.setData(txId);
        } catch (Exception e) {
            return MessageResult.error(500, "error:" + e.getMessage());
        }
        return messageResult;
    }


    @ApiOperation(value = "根据地址获取能量带宽", notes = "根据地址获取能量带宽")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromAddress", value = "发起方地址", paramType = "query", dataType = "string", required = true),

    })
    @PostMapping("getAccountSolidity")
    private MessageResult getAccountSolidity(@NotBlank(message = "发起方地址必传") String fromAddress
    ) {
        Trc20Service trc20Service = ActivitiConfig.getBean(Trc20Service.class);
        MessageResult messageResult;
        try {
            Map<String, BigDecimal> map = trc20Service.getAccountSolidity(fromAddress);
            messageResult = MessageResult.success();
            messageResult.setData(map);
        } catch (Exception e) {
            return MessageResult.error(500, "error:" + e.getMessage());
        }
        return messageResult;
    }


    @ApiOperation(value = "冻结TRX转换资源", notes = "冻结TRX转换资源")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ownerAddress", value = "发起方地址", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "frozenBalance", value = "冻结余额", paramType = "query", dataType = "long", required = true),
            @ApiImplicitParam(name = "frozenDuration", value = "冻结时长（'天'为单位;不传默认三天）", paramType = "query", dataType = "long"),
            @ApiImplicitParam(name = "resourceCode", value = "资源类型（0：带宽;1：能量）", paramType = "query", dataType = "int", required = true),
            @ApiImplicitParam(name = "receiveAddress", value = "接收方地址", paramType = "query", dataType = "string"),
    })
    @PostMapping("freezeBalance")
    private MessageResult freezeBalance(@NotBlank(message = "发起方地址必传") String ownerAddress, @NotBlank(message = "接收方地址必传") String receiveAddress
            , @NotNull(message = "冻结额度必传") Long frozenBalance, Long frozenDuration, @NotNull(message = "资源类型必传") Integer resourceCode
    ) {
        Trc20Service trc20Service = ActivitiConfig.getBean(Trc20Service.class);
        frozenDuration = Optional.ofNullable(frozenDuration).orElse(3L);
        MessageResult messageResult;
        try {
            String data = trc20Service.freezeBalance(ownerAddress, frozenBalance, frozenDuration, resourceCode, receiveAddress);
            messageResult = MessageResult.success();
            messageResult.setData(data);
        } catch (IllegalException e) {
            return MessageResult.error(500, "error:" + e.getMessage());
        }
        return messageResult;
    }

    @ApiOperation(value = "解冻冻结TRX", notes = "冻结三天之后才能解冻")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ownerAddress", value = "发起方地址", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "privateKey", value = "私钥", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "resourceCode", value = "资源类型（0：带宽;1：能量）", paramType = "query", dataType = "int", required = true),
    })
    @PostMapping("unfreezeBalance")
    private String unfreezeBalance(@NotBlank(message = "发起方地址必传") String ownerAddress, @NotBlank(message = "私钥必传") String privateKey
            , @NotNull(message = "资源类型必传") Integer resourceCode
    ) throws IllegalException {
        Trc20Service trc20Service = ActivitiConfig.getBean(Trc20Service.class);
        return trc20Service.unfreezeBalance(ownerAddress, privateKey, resourceCode);
    }


    @ApiOperation(value = "测试TRC20转账交易接口", notes = "测试专用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "toAddress", value = "接收方地址", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "amount", value = "币种数量", paramType = "query", dataType = "", required = true),
            @ApiImplicitParam(name = "path", value = "发起方地址", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "memo", value = "备注", paramType = "query", dataType = "string"),
    })
    @PostMapping("testTrc20Transaction")
    private MessageResult testTrc20Transaction(@NotBlank(message = "接收方地址必传") String toAddress, @NotBlank(message = "json文件路径") String path
            , @NotNull(message = "币种数量必传") Long amount,String memo) {
        Trc20Service trc20Service = ActivitiConfig.getBean(Trc20Service.class);
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials("".getBytes(), new File("D://data/tusdt-prod/keystore/"  + path));
        } catch (Exception e) {
            return MessageResult.error(500, "getCredentials_error:" + e.getMessage());
        }
        MessageResult messageResult;
        try {
            String txId = trc20Service.trc20Transaction(toAddress, ByteArray.toHexString(credentials.getPair().getPrivateKey())
                    ,credentials.getAddress(), amount, memo);
            messageResult = MessageResult.success();
            messageResult.setData(txId);
        } catch (Exception e) {
            return MessageResult.error(500, "error:" + e.getMessage());
        }
        return messageResult;
    }
}
