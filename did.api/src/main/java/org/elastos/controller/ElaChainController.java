/**
 * Copyright (c) 2017-2018 The Elastos Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.elastos.controller;

import org.elastos.entity.DidInfoEntity;
import org.elastos.entity.SignDataEntity;
import org.elastos.entity.TransferParamEntity;
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


    @RequestMapping(value = "/did",method = RequestMethod.GET)
    @ResponseBody
    public String createDID(){

        return call(null,null,"createDid",service);
    }

    @RequestMapping(value = "/did/{privateKey}",method = RequestMethod.GET)
    @ResponseBody
    public String retriveDID(@PathVariable("privateKey") String privateKey){

        return call(privateKey,String.class,"retriveDid",service);
    }

    @RequestMapping(value = "/sign",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String sign(@RequestBody String reqBody){

        return call(reqBody,SignDataEntity.class,"sign",service);
    }

    @RequestMapping(value = "/verify",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String verify(@RequestBody String reqBody){

        return call(reqBody,SignDataEntity.class,"verify",service);
    }

    @RequestMapping(value = "/did/transfer",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String didTransfer(@RequestBody String reqBody){

        return call(reqBody,TransferParamEntity.class,"didTransfer",service);
    }

    @RequestMapping(value = "/setDidInfo",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String setDidInfo(@RequestBody String reqBody){

        return call(reqBody,DidInfoEntity.class,"setDidInfo",service);
    }

    @RequestMapping(value = "/getDidInfo",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String getDidInfo(@RequestBody String reqBody){

        return call(reqBody,DidInfoEntity.class,"getDidInfo",service);
    }

    @RequestMapping(value = "/cross/d2m/transfer",method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public String d2mTransfer(@RequestBody String reqBody){

        return call(reqBody,TransferParamEntity.class,"did2MainCrossTransfer",service);
    }


}
