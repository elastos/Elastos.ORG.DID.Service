/**
 * Copyright (c) 2017-2018 The Elastos Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.elastos.controller;

import org.elastos.entity.*;
import org.elastos.service.ElaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * clark
 * <p>
 * 9/20/18
 */
@RestController
public class ElaChainController extends BaseController{

    @Autowired
    private ElaService service;

    @RequestMapping(value = "/createWallet",method = RequestMethod.GET)
    @ResponseBody
    public String createWallet(){
        return call(null,null,"createWallet",service);
    }

    @RequestMapping(value = "/cn/mnemonic",method = RequestMethod.GET)
    @ResponseBody
    public String mnemonicCn(){
        return call("chinese",String.class,"mnemonic",service);
    }

    @RequestMapping(value = "/eng/mnemonic",method = RequestMethod.GET)
    @ResponseBody
    public String mnemonicEng(){
        return call("english",String.class,"mnemonic",service);
    }

    @RequestMapping(value = "/hd",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String genHdWallet(@RequestAttribute String reqBody){
        return call(reqBody,HdWalletEntity.class,"genHdWallet",service);
    }
    @RequestMapping(value = "/currHeight",method = RequestMethod.GET)
    @ResponseBody
    public String getCurrentHeight(){

        return call(null,null,"getCurrentHeight",service);
    }

    @RequestMapping(value = "/txs/{height}",method = RequestMethod.GET)
    @ResponseBody
    public String getBlockTxsByHeight(@PathVariable("height") String height){

        return call( height ,String.class,"getBlockTxsByHeight",service);
    }

    @RequestMapping(value = "/block/height/{height}",method = RequestMethod.GET)
    @ResponseBody
    public String getBlockByHeight(@PathVariable("height") String height){

        return call( height ,String.class,"getBlockByHeight",service);
    }

    @RequestMapping(value = "/block/hash/{hash}",method = RequestMethod.GET)
    @ResponseBody
    public String getBlockByHash(@PathVariable("hash") String hash){

        return call( hash ,String.class,"getBlockByHash",service);
    }

    @RequestMapping(value = "/block/transaction/{hash}",method = RequestMethod.GET)
    @ResponseBody
    public String getTransactionByHash(@PathVariable("hash") String hash){

        return call( hash ,String.class,"getTransactionByHash",service);
    }

    @RequestMapping(value = "/balance/{address}",method = RequestMethod.GET)
    @ResponseBody
    public String getBalance(@PathVariable("address") String address){

        return call( address ,String.class,"getBalance",service);
    }

    @RequestMapping(value = "/utxos/{address}",method = RequestMethod.GET)
    @ResponseBody
    public String getUtxos(@PathVariable("address") String address){

        return call( address ,String.class,"getUtxos",service);
    }


    @RequestMapping(value = "/gen/did",method = RequestMethod.GET)
    @ResponseBody
    public String createDID(){

        return call(null,null,"createDid",service);
    }

    @RequestMapping(value = "/sign",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String sign(@RequestAttribute String reqBody){

        return call(reqBody,SignDataEntity.class,"sign",service);
    }

    @RequestMapping(value = "/verify",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String verify(@RequestAttribute String reqBody){

        return call(reqBody,SignDataEntity.class,"verify",service);
    }

    @RequestMapping(value = "/transfer",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String didTransfer(@RequestAttribute String reqBody){

        return call(reqBody,TransferParamEntity.class,"didTransfer",service);
    }

    @RequestMapping(value = "/setDidInfo",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String setDidInfo(@RequestAttribute String reqBody){

        return call(reqBody,SetDidInfoEntity.class,"setDidInfo",service);
    }

    @RequestMapping(value = "/getDidInfo",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String getDidInfo(@RequestAttribute String reqBody){

        return call(reqBody,GetDidInfoEntity.class,"getDidInfo",service);
    }

    @RequestMapping(value = "/setDidPayload",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String getDidPayload(@RequestAttribute String reqBody){

        return call(reqBody,SetDidInfoEntity.class,"setDidPayload",service);
    }

    @RequestMapping(value = "/cross/d2m/transfer",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String d2mTransfer(@RequestAttribute String reqBody){

        return call(reqBody,TransferParamEntity.class,"did2MainCrossTransfer",service);
    }

    @RequestMapping(value = "/tx/{txid}",method = RequestMethod.GET)
    @ResponseBody
    public String getTxByTxId(@PathVariable("txid") String txid){

        return call(txid,String.class,"getTxByTxId",service);
    }

}
