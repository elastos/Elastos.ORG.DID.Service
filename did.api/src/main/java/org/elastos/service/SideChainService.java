/**
 * Copyright (c) 2017-2018 The Elastos Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.elastos.service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import com.alibaba.fastjson.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.elastos.api.SingleSignTransaction;
import org.elastos.conf.BasicConfiguration;
import org.elastos.conf.SidechainConfiguration;
import org.elastos.conf.RetCodeConfiguration;
import org.elastos.ela.ECKey;
import org.elastos.ela.Ela;
import org.elastos.ela.SignTool;
import org.elastos.ela.Util;
import org.elastos.entity.*;
import org.elastos.exception.ApiRequestDataException;
import org.elastos.util.*;
import org.elastos.util.ela.ElaHdSupport;
import org.elastos.util.ela.ElaKit;
import org.elastos.util.ela.ElaSignTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.net.www.protocol.http.HttpURLConnection;

import javax.xml.bind.DatatypeConverter;

/**
 * @author clark
 * <p>
 * Apr 21, 2018 12:45:54 PM
 */
@Service
public class SideChainService {

    private static final String CHARSET = "UTF-8";
    private static final String BURN_ADDRESS = "0000000000000000000000000000000000";
    @Autowired
    private BasicConfiguration basicConfiguration;
    @Autowired
    private RetCodeConfiguration retCodeConfiguration;
    @Autowired
    private SidechainConfiguration sidechainConfiguration;

    private static Logger logger = LoggerFactory.getLogger(SideChainService.class);

    /**
     * create a ela wallet
     * @return
     */
    public String createWallet(){
        JSONObject result = new JSONObject();
        String privateKey = Ela.getPrivateKey();
        String publicKey  = Ela.getPublicFromPrivate(privateKey);
        String publicAddr = Ela.getAddressFromPrivate(privateKey);
        result.put("privateKey",privateKey);
        result.put("publicKey",publicKey);
        result.put("address",publicAddr);
        return JSON.toJSONString(new ReturnMsgEntity().setResult(result).setStatus(retCodeConfiguration.SUCC()));
    }

    public String mnemonic(String type){
        return JSON.toJSONString(new ReturnMsgEntity().setResult(ElaHdSupport.generateMnemonic(type.equals("chinese")?MnemonicType.CHINESE:MnemonicType.ENGLISH)).setStatus(retCodeConfiguration.SUCC()));
    }

    public String genHdWallet(HdWalletEntity entity) throws Exception{
        JSONArray array = new JSONArray();
        String mnemonic = entity.getMnemonic();
        Integer start = entity.getStart();
        Integer end = entity.getEnd();
        Integer index = entity.getIndex();
        if(mnemonic != null && index != null){
            return genHdWallet(mnemonic,index);
        }
        if(mnemonic == null || start < 0 || start > end){
            throw new ApiRequestDataException("invalid param");
        }
        for(int i=start;i<=end;i++){
            array.add(addDidToHdWallet(mnemonic,i));
        }
        return JSON.toJSONString(new ReturnMsgEntity().setResult(array).setStatus(retCodeConfiguration.SUCC()));
    }

    private JSONObject addDidToHdWallet(String mnemonic , int index) throws Exception{
        JSONObject jso = JSONObject.fromObject(ElaHdSupport.generate(mnemonic,index));
        String privKey = (String)jso.get("privateKey");
        jso.put("did",Ela.getIdentityIDFromPrivate(privKey));
        return jso;
    }

    public String genHdWallet(String mnemonic,int index) throws Exception{
        return JSON.toJSONString(new ReturnMsgEntity().setResult(addDidToHdWallet(mnemonic,index)).setStatus(retCodeConfiguration.SUCC()));
    }

    /**
     * genHdTx info
     * @param hdTxEntity info entity
     * @return
     * @throws Exception
     */
    public String genCrossHdTx(HdTxEntity hdTxEntity) throws Exception {

        List<List<Map>> utxoList = remakeHdEntity(hdTxEntity);

        return JSON.toJSONString(new ReturnMsgEntity().setResult(genCrossHdTx(hdTxEntity, utxoList)).setStatus(retCodeConfiguration.SUCC()));

    }

    private List<List<Map>> remakeHdEntity(HdTxEntity hdTxEntity){

        String[] inputAddrs = hdTxEntity.getInputs();

        List<List<Map>> utxoList = new ArrayList<>();

        List<String> inputs = new ArrayList<>();

        for (int i = 0; i < inputAddrs.length; i++) {

            List<String> utxoStr = getUtxoByAddr(inputAddrs[i],ChainType.DID_SIDECHAIN);

            List<Map> utxo = stripUtxo(utxoStr.get(0));

            if(utxo != null){
                inputs.add(inputAddrs[i]);
                utxoList.add(utxo);
            }

        }
        hdTxEntity.setInputs(inputs.toArray(new String[inputs.size()]));
        return utxoList;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public Map<String, Object> genCrossHdTx(HdTxEntity hdTxEntity, List<List<Map>> utxoTotal) throws Exception {

        HdTxEntity.Output[] outputs =  hdTxEntity.getOutputs();
        String[] sdrAddrsArr = hdTxEntity.getInputs();
        if (outputs.length == 0 || sdrAddrsArr.length == 0){
            throw new ApiRequestDataException("outputs or inputs can not be blank");
        }
        double smAmt = 0.0;
        List<String> sdrAddrs = Arrays.asList(sdrAddrsArr);
        List<String> addrs = new ArrayList<>();
        List<Double> amts = new ArrayList<>();
        ChainType type = ChainType.DID_MAIN_CROSS_CHAIN;
        for(int i=0;i<outputs.length;i++){
            HdTxEntity.Output output = outputs[i];
            double amt = output.getAmt() * 1.0/basicConfiguration.ONE_ELA();
            smAmt += amt;
            addrs.add(output.getAddr());
            amts.add(amt);
        }
        Map<String,Object> paraListMap = new HashMap<>();
        List txList = new ArrayList<>();
        paraListMap.put("Transactions", txList);
        Map<String,Object> txListMap = new HashMap<>();
        txList.add(txListMap);

        int index = -1;
        double spendMoney = 0.0;
        boolean hasEnoughFee = false;
        int utxoIndex = -1;
        out :for(int z= 0 ;z < utxoTotal.size();z++){
            List<Map> utxolm = utxoTotal.get(z);
            utxoIndex = z;
            for( int i=0; i<utxolm.size(); i++) {
                index = i;
                spendMoney += Double.valueOf(utxolm.get(i).get("amount")+"");
                if( Math.round(spendMoney * basicConfiguration.ONE_ELA()) >= Math.round((smAmt + (basicConfiguration.CROSS_CHAIN_FEE() * 2)) * basicConfiguration.ONE_ELA())) {
                    hasEnoughFee = true;
                    break out;
                }
            }
        }


        if(!hasEnoughFee) {
            throw new ApiRequestDataException(Errors.NOT_ENOUGH_UTXO.val());
        }

        List utxoInputsArray = new ArrayList<>();
        txListMap.put("UTXOInputs", utxoInputsArray);
        for(int z=0;z<=utxoIndex;z++){
            List<Map> utxolm = utxoTotal.get(z);
            String addr = sdrAddrs.get(z);
            int subIndex = utxolm.size() - 1;
            if(z == utxoIndex){
                subIndex = index;
            }
            for(int i=0;i<=subIndex;i++) {
                Map<String,Object> utxoInputsDetail = new HashMap<>();
                Map<String,Object> utxoM = utxolm.get(i);
                double utxoVal = Double.valueOf(utxoM.get("amount")+"");
                if (utxoVal == 0){
                    continue;
                }
                utxoInputsDetail.put("txid",  utxoM.get("txid"));
                utxoInputsDetail.put("index",  utxoM.get("vout"));
                utxoInputsDetail.put("address",  addr);
                utxoInputsArray.add(utxoInputsDetail);
            }
        }
        List utxoOutputsArray = new ArrayList<>();
        txListMap.put("Outputs", utxoOutputsArray);
        Map<String,Object> brokerOutputs = new HashMap<>();
        brokerOutputs.put("address", BURN_ADDRESS);
        brokerOutputs.put("amount", Math.round((smAmt+basicConfiguration.CROSS_CHAIN_FEE()) * basicConfiguration.ONE_ELA()));
        utxoOutputsArray.add(brokerOutputs);

        double leftMoney = (spendMoney - ((basicConfiguration.CROSS_CHAIN_FEE() * 2) + smAmt));
        String changeAddr = sdrAddrs.get(0);
        Map<String,Object> utxoOutputsDetail = new HashMap<>();
        utxoOutputsDetail.put("address", changeAddr);
        utxoOutputsDetail.put("amount",Math.round(leftMoney * basicConfiguration.ONE_ELA()));
        utxoOutputsArray.add(utxoOutputsDetail);

        List crossOutputsArray = new ArrayList<>();
        txListMap.put("CrossChainAsset",crossOutputsArray);

        for(int i=0;i<addrs.size();i++) {
            utxoOutputsDetail = new HashMap<>();
            utxoOutputsDetail.put("address", addrs.get(i));
            utxoOutputsDetail.put("amount", Math.round(amts.get(i) * basicConfiguration.ONE_ELA()));
            crossOutputsArray.add(utxoOutputsDetail);
        }
        txListMap.put("Fee",basicConfiguration.CROSS_CHAIN_FEE() * basicConfiguration.ONE_ELA() * 2);

        return paraListMap;
    }


    /**
     * get transaction by transaction id
     * @param txid
     * @return
     */
    public String getTxByTxId(String txid){
        return callRpc(sidechainConfiguration.getDid_prefix(),JSON.toJSONString(new RpcReq("getrawtransaction").setParams(new HashMap<String,Object>() {
            {
                this.put("txid",txid);
                this.put("verbose",true);
            }
        })),sidechainConfiguration.getDid_rpc_user(),sidechainConfiguration.getDid_rpc_pass());
    }

    public String sendRawTx(RawTxEntity rawTxEntity){
        ChainType type = rawTxEntity.getType();
        ReturnMsgEntity.ELAReturnMsg msg = JsonUtil.jsonStr2Entity(callRpc(sidechainConfiguration.getDid_prefix(),JSON.toJSONString(new RpcReq("sendrawtransaction").setParams(new HashMap<String,Object>() {
            {
                this.put("data",rawTxEntity.getData());
            }
        })),sidechainConfiguration.getDid_rpc_user(),sidechainConfiguration.getDid_rpc_pass()),ReturnMsgEntity.ELAReturnMsg.class);
        long status = 0;
        Object rst = "";
        if(msg.getError() == null){
            status = retCodeConfiguration.SUCC();
            rst = msg.getResult();
        }else{
            status = retCodeConfiguration.PROCESS_ERROR();
            rst = msg.getError().getMessage();
        }
        return JSON.toJSONString(new ReturnMsgEntity().setResult(rst).setStatus(status));
    }

    /**
     * genHdTx info
     * @param hdTxEntity info entity
     * @return
     * @throws Exception
     */
    public String genHdTx(HdTxEntity hdTxEntity) throws Exception {

        List<List<Map>> utxoList = remakeHdEntity(hdTxEntity);

        return JSON.toJSONString(new ReturnMsgEntity().setResult(genHdTx(hdTxEntity, utxoList)).setStatus(retCodeConfiguration.SUCC()));

    }

    /**
     * get the current height of blockchain
     * @return
     */
    public String getCurrentHeight(){

        return callRpc(sidechainConfiguration.getDid_prefix(),JSON.toJSONString(new RpcReq("getcurrentheight")),sidechainConfiguration.getDid_rpc_user(),sidechainConfiguration.getDid_rpc_pass());
    }

    /**
     * get block by height
     * @param height
     * @return
     */
    public String getBlockByHeight(String height){
        return callRpc(sidechainConfiguration.getDid_prefix(),JSON.toJSONString(new RpcReq("getblockbyheight").setParams(new HashMap<String,Object>() {
            {
                this.put("height",Integer.valueOf(height));
            }
        })),sidechainConfiguration.getDid_rpc_user(),sidechainConfiguration.getDid_rpc_pass());
    }

    /**
     * get block by hash
     * @param hash
     * @return
     */
    public String getBlockByHash(String hash){

        return callRpc(sidechainConfiguration.getDid_prefix(),JSON.toJSONString(new RpcReq("getblock").setParams(new HashMap<String,Object>() {
            {
                this.put("blockhash",hash);
                this.put("verbosity",2);
            }
        })),sidechainConfiguration.getDid_rpc_user(),sidechainConfiguration.getDid_rpc_pass());
    }

    /**
     * get address balance
     * @param address
     * @return
     */
    public String getBalance(String address){

        checkAddr(address);

        String result = callRpc(sidechainConfiguration.getDid_prefix(),JSON.toJSONString(new RpcReq("listunspent").setParams(new HashMap<String,Object>() {
            {
                this.put("addresses",new String[]{address});
            }
        })),sidechainConfiguration.getDid_rpc_user(),sidechainConfiguration.getDid_rpc_pass());

        Map<String,Object>  resultMap = (Map<String,Object>) JSON.parse(result);

        List<Map<String,Object>> resObj = (List)resultMap.get("result");

        if (resObj == null || StrKit.isBlank(resObj+"") || (resObj +"").equalsIgnoreCase("null")){
            return JSON.toJSONString(new ReturnMsgEntity().setResult("0.0").setStatus(retCodeConfiguration.SUCC()));
        }

        BigDecimal total = new BigDecimal("0.0");
        for(int i=0;i<resObj.size();i++){
            Map md = resObj.get(i);
            BigDecimal v = new BigDecimal((String) md.get("amount"));
            total = total.add(v);
        }

        return JSON.toJSONString(new ReturnMsgEntity().setResult(total.setScale(8, RoundingMode.HALF_UP).toString()).setStatus(retCodeConfiguration.SUCC()));
    }

    /**
     * get address utxos
     * @param address
     * @return
     */
    public String getUtxos(String address){

        checkAddr(address);

        return callRpc(sidechainConfiguration.getDid_prefix(),JSON.toJSONString(new RpcReq("listunspent").setParams(new HashMap<String,Object>() {
            {
                this.put("addresses",new String[]{address});
            }
        })),sidechainConfiguration.getDid_rpc_user(),sidechainConfiguration.getDid_rpc_pass());
    }

    /**
     * check address
     * @param address
     */
    private void checkAddr(String address){
        if (!ElaKit.checkAddress(address)){
            throw new ApiRequestDataException(Errors.ELA_ADDRESS_INVALID.val() + ":" + address);
        }
    }


    /**
     * create did
     * @return
     * @throws Exception
     */
    public String createDid() throws Exception{
        JSONObject result = new JSONObject();
        String privKey = Ela.getPrivateKey();
        String did = Ela.getIdentityIDFromPrivate(privKey);
        result.put("privateKey",privKey);
        String publicKey = Ela.getPublicFromPrivate(privKey);
        result.put("publicKey",publicKey);
        String publicAddr = Ela.getAddressFromPrivate(privKey);
        result.put("publicAddr",publicAddr);
        result.put("did",did);
        return JSON.toJSONString(new ReturnMsgEntity().setResult(result).setStatus(retCodeConfiguration.SUCC()));
    }

    /**
     * using privateKey sign data
     * @param entity
     * @return
     * @throws Exception
     */
    public String sign(SignDataEntity entity)  throws Exception{
        JSONObject result = new JSONObject();
        String msg = entity.getMsg();
        String privateKey = entity.getPrivateKey();
        ECKey ec = ECKey.fromPrivate(DatatypeConverter.parseHexBinary(privateKey));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.write(msg.getBytes(CHARSET));
        byte[] signature = SignTool.doSign(baos.toByteArray(), DatatypeConverter.parseHexBinary(privateKey));
        byte[] code = new byte[33];
        System.arraycopy(Util.CreateSingleSignatureRedeemScript(ec.getPubBytes(),1), 1,code,0,code.length);
        result.put("msg",DatatypeConverter.printHexBinary(msg.getBytes(CHARSET)));
        result.put("pub",DatatypeConverter.printHexBinary(code));
        result.put("sig",DatatypeConverter.printHexBinary(signature));
        return JSON.toJSONString(new ReturnMsgEntity().setResult(result).setStatus(retCodeConfiguration.SUCC()));
    }

    /**
     * verify if message is signed by a public key
     * @param entity
     * @return
     */
    public String verify(SignDataEntity entity){
        String hexMsg = entity.getMsg();
        String hexSig = entity.getSig();
        String hexPub = entity.getPub();
        byte[] msg = DatatypeConverter.parseHexBinary(hexMsg);
        byte[] sig = DatatypeConverter.parseHexBinary(hexSig);
        byte[] pub = DatatypeConverter.parseHexBinary(hexPub);
        boolean isVerify = ElaSignTool.verify(msg,sig,pub);
        return JSON.toJSONString(new ReturnMsgEntity().setResult(isVerify).setStatus(retCodeConfiguration.SUCC()));
    }

    /**
     * retrive did
     * @param privateKey
     * @return
     * @throws Exception
     */
    public String retriveDid(String privateKey) throws Exception {

        String did = Ela.getIdentityIDFromPrivate(privateKey);

        return JSON.toJSONString(new ReturnMsgEntity().setResult(did).setStatus(retCodeConfiguration.SUCC()));

    }


    /**
     *
     * @param prefix
     * @param data
     * @return
     */
    private String callRpc(String prefix,String data,String user , String pass){

        Map<String,String> header = new HashMap<>();

        header.put("Authorization","Basic " + Base64.getEncoder().encodeToString((user +":" + pass).getBytes()));

        header.put("Content-Type","application/json");

        HttpURLConnection connection ;

        String result = HttpKit.post(prefix,data,header);

        ReturnMsgEntity.ELAReturnMsg resultMap = JSON.parseObject(result,ReturnMsgEntity.ELAReturnMsg.class);

        Object rst = resultMap.getResult();

        if(rst == null){
            rst = resultMap.getError().getMessage();
        }

        return JSON.toJSONString(new ReturnMsgEntity().setResult(rst).setStatus(retCodeConfiguration.SUCC()));

    }
    /**
     * genHdTx info
     * @param hdTxEntity info entity
     * @param utxoList addrs utxo list
     * @return
     * @throws Exception
     */
    private Map<String, Object> genHdTx(HdTxEntity hdTxEntity, List<List<Map>> utxoList) throws Exception {

        HdTxEntity.Output[] outputs = hdTxEntity.getOutputs();
        double smAmt = 0;
        for (int i = 0; i < outputs.length; i++) {
            smAmt += outputs[i].getAmt()/(basicConfiguration.ONE_ELA() * 1.0);
        }
        Map<String, Object> paraListMap = new HashMap<>();
        List txList = new ArrayList<>();
        paraListMap.put("Transactions", txList);
        Map txListMap = new HashMap();
        txList.add(txListMap);
        int index = -1;
        double spendMoney = 0.0;
        boolean hasEnoughFee = false;
        List utxoInputsArray = new ArrayList<>();
        txListMap.put("UTXOInputs", utxoInputsArray);
        for (int j = 0; j < utxoList.size(); j++) {
            List<Map> utxolm = utxoList.get(j);
            String addr = hdTxEntity.getInputs()[j];
            for (int i = 0; i < utxolm.size(); i++) {
                index = i;
                spendMoney += Double.valueOf(utxolm.get(i).get("Value") + "");
                if (Math.round(spendMoney * basicConfiguration.ONE_ELA()) >= Math.round((smAmt + basicConfiguration.FEE()) * basicConfiguration.ONE_ELA())) {
                    hasEnoughFee = true;
                    break;
                }
            }
            for (int i = 0; i <= index; i++) {
                Map<String, Object> utxoInputsDetail = new HashMap<>();
                Map<String, Object> utxoM = utxolm.get(i);
                double utxoVal = Double.valueOf(utxoM.get("amount")+"");
                if (utxoVal == 0){
                    continue;
                }
                utxoInputsDetail.put("txid", utxoM.get("txid"));
                utxoInputsDetail.put("index", utxoM.get("vout"));
                utxoInputsDetail.put("address", addr);
                utxoInputsArray.add(utxoInputsDetail);
            }
            if (hasEnoughFee) {
                break;
            }
        }

        if (!hasEnoughFee) {
            throw new ApiRequestDataException("Not Enough UTXO");
        }
        List utxoOutputsArray = new ArrayList<>();
        txListMap.put("Outputs", utxoOutputsArray);
        for (int i = 0; i < outputs.length; i++) {
            Map<String, Object> utxoOutputsDetail = new HashMap<>();
            utxoOutputsDetail.put("address", outputs[i].getAddr());
            utxoOutputsDetail.put("amount", outputs[i].getAmt());
            utxoOutputsArray.add(utxoOutputsDetail);
        }
        double leftMoney = (spendMoney - (basicConfiguration.FEE() + smAmt));
        Map<String, Object> utxoOutputsDetail = new HashMap<>();
        utxoOutputsDetail.put("address", hdTxEntity.getInputs()[0]);
        utxoOutputsDetail.put("amount", Math.round(leftMoney * basicConfiguration.ONE_ELA()));
        utxoOutputsArray.add(utxoOutputsDetail);

        txListMap.put("Fee",basicConfiguration.FEE() * basicConfiguration.ONE_ELA());
        return paraListMap;
    }

    /**
     * @param result
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Map> stripUtxo(String result) {

        Map m = JsonUtil.jsonToMap(JSONObject.fromObject(result));
        List<Map> lm = null;
        try {
            lm = (List<Map>)m.get("result");
            return lm;
        } catch (Exception ex) {
            logger.warn(" address has no utxo yet .");
            return null;
        }
    }


    private List<String> getUtxoByAddr(List<String> addrs,ChainType type) {
        List<String> rstlist = new ArrayList<>();
        for(int i=0;i<addrs.size();i++){
            String addr = addrs.get(i);
            checkAddr(addr);
            String result = callRpc(sidechainConfiguration.getDid_prefix(),JSON.toJSONString(new RpcReq("listunspent").setParams(new HashMap<String,Object>() {
                {
                    this.put("addresses",new String[]{addr});
                }
            })),sidechainConfiguration.getDid_rpc_user(),sidechainConfiguration.getDid_rpc_pass());
            rstlist.add(result);
        }
        return rstlist;
    }

    private List<String> getUtxoByAddr(String addr,ChainType type) {
        List<String> addrLst = new ArrayList<>();
        addrLst.add(addr);
        return getUtxoByAddr(addrLst,type);
    }

    @SuppressWarnings("unchecked")
    public String transfer(TransferParamEntity param) throws Exception {

        List<LinkedHashMap> rcv = (List<LinkedHashMap>) param.getReceiver();
        List<Map> sdr = (List<Map>) param.getSender();
        List<String> addrList = new ArrayList<>();
        List<Double> valList = new ArrayList<>();
        Double totalAmt = 0.0;
        for(int i=0;i<rcv.size();i++){
            Map m = rcv.get(i);
            addrList.add((String)m.get("address"));
            Double tmpAmt = Double.valueOf((String)m.get("amount"));
            valList.add(tmpAmt);
            totalAmt += tmpAmt;
        }
        List<String> sdrAddrs = new ArrayList<>();
        List<String> sdrPrivs = new ArrayList<>();
        for(int i=0;i<sdr.size();i++){
            Map m = sdr.get(i);
            String address = (String) m.get("address");
            String privKey = (String) m.get("privateKey");
            sdrAddrs.add(address);
            sdrPrivs.add(privKey);
        }
        String memo = param.getMemo();
        ChainType type = param.getType();
        String response = gen(totalAmt, sdrPrivs , sdrAddrs,
                addrList, valList, memo,type);
        Object orst =((Map<String, Object>) JSON.parse(response)).get("Result");
        if ((orst instanceof Map) == false){
            throw new ApiRequestDataException("Not valid request Data");
        }
        Map<String,Object> rawM = (Map<String, Object>)orst;
        String rawTx = (String) rawM.get("rawTx");
        String txHash = (String) rawM.get("txHash");
        logger.info("rawTx:" + rawTx + ", txHash :" + txHash);

        return sendTx(rawTx,type);
    }


    /**
     * did asset transfer
     * @param entity
     * @return
     * @throws Exception
     */
    public String didTransfer(TransferParamEntity entity) throws Exception{
        entity.setType(ChainType.DID_SIDECHAIN);
        return transfer(entity);
    }

    /**
     * send a transaction to blockchain.
     * @param smAmt
     * @param addrs
     * @param amts
     * @param data
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public String gen(double smAmt , List<String> prvKeys , List<String> sdrAddrs ,List<String> addrs , List<Double> amts , String data,ChainType type) throws Exception {

        List<String> utxoStrLst = getUtxoByAddr(sdrAddrs,type);
        List<List<Map>> utxoTotal = new ArrayList<>();
        for(int i=0;i<utxoStrLst.size();i++){
            String utxoStr = utxoStrLst.get(i);
            List<Map> utxo = stripUtxo(utxoStr);
            if(utxo == null){
                continue;
            }
            utxoTotal.add(utxo);
        }

        if(utxoTotal.size() == 0){
            throw new ApiRequestDataException(Errors.NOT_ENOUGH_UTXO.val());
        }

        if(type == ChainType.MAIN_DID_CROSS_CHAIN || type == ChainType.DID_MAIN_CROSS_CHAIN) {
            return genCrossTx(smAmt,utxoTotal,prvKeys, sdrAddrs, addrs, amts, data,type);
        }

        return genTx(smAmt, utxoTotal, prvKeys, sdrAddrs, addrs, amts, data);
    }

    /**
     *
     * @param smAmt
     * @param utxoTotal
     * @param prvKeys
     * @param sdrAddrs
     * @param addrs
     * @param amts
     * @param data
     * @return
     * @throws Exception
     */
    public String genCrossTx(double smAmt , List<List<Map>> utxoTotal , List<String> prvKeys , List<String> sdrAddrs ,List<String> addrs ,
                             List<Double> amts , String data,ChainType type) throws Exception {

        if(addrs == null || addrs.size() == 0) {
            throw new RuntimeException("output can not be blank");
        }

        Map<String,Object> paraListMap = new HashMap<>();
        List txList = new ArrayList<>();
        paraListMap.put("Transactions", txList);
        Map<String,Object> txListMap = new HashMap<>();
        txList.add(txListMap);

        int index = -1;
        double spendMoney = 0.0;
        boolean hasEnoughFee = false;
        int utxoIndex = -1;
        out :for(int z= 0 ;z < utxoTotal.size();z++){
            List<Map> utxolm = utxoTotal.get(z);
            utxoIndex = z;
            for( int i=0; i<utxolm.size(); i++) {
                index = i;
                spendMoney += Double.valueOf(utxolm.get(i).get("amount")+"");
                if( Math.round(spendMoney * basicConfiguration.ONE_ELA()) >= Math.round((smAmt + (basicConfiguration.CROSS_CHAIN_FEE() * 2)) * basicConfiguration.ONE_ELA())) {
                    hasEnoughFee = true;
                    break out;
                }
            }
        }


        if(!hasEnoughFee) {
            throw new ApiRequestDataException(Errors.NOT_ENOUGH_UTXO.val());
        }

        List utxoInputsArray = new ArrayList<>();
        txListMap.put("UTXOInputs", utxoInputsArray);
        List privsArray = new ArrayList<>();
        for(int z=0;z<=utxoIndex;z++){
            List<Map> utxolm = utxoTotal.get(z);
            String privateKey = prvKeys.get(z);
            String addr = sdrAddrs.get(z);
            int subIndex = utxolm.size() - 1;
            if(z == utxoIndex){
                subIndex = index;
            }
            for(int i=0;i<=subIndex;i++) {
                Map<String,Object> utxoInputsDetail = new HashMap<>();
                Map<String,Object> utxoM = utxolm.get(i);
                Map<String,Object> privM = new HashMap<>();
                double utxoVal = Double.valueOf(utxoM.get("amount")+"");
                if (utxoVal == 0){
                    continue;
                }
                utxoInputsDetail.put("txid",  utxoM.get("txid"));
                utxoInputsDetail.put("index",  utxoM.get("vout"));
                utxoInputsDetail.put("address",  addr);
                privM.put("privateKey",  privateKey);
                utxoInputsArray.add(utxoInputsDetail);
                privsArray.add(privM);
            }
        }
        List utxoOutputsArray = new ArrayList<>();
        txListMap.put("Outputs", utxoOutputsArray);
        Map<String,Object> brokerOutputs = new HashMap<>();
        brokerOutputs.put("address", BURN_ADDRESS);
        brokerOutputs.put("amount", Math.round((smAmt+basicConfiguration.CROSS_CHAIN_FEE()) * basicConfiguration.ONE_ELA()));
        utxoOutputsArray.add(brokerOutputs);

        double leftMoney = (spendMoney - ((basicConfiguration.CROSS_CHAIN_FEE() * 2) + smAmt));
        String changeAddr = sdrAddrs.get(0);
        Map<String,Object> utxoOutputsDetail = new HashMap<>();
        utxoOutputsDetail.put("address", changeAddr);
        utxoOutputsDetail.put("amount",Math.round(leftMoney * basicConfiguration.ONE_ELA()));
        utxoOutputsArray.add(utxoOutputsDetail);

        txListMap.put("PrivateKeySign",privsArray);
        List crossOutputsArray = new ArrayList<>();
        txListMap.put("CrossChainAsset",crossOutputsArray);
        for(int i=0;i<addrs.size();i++) {
            utxoOutputsDetail = new HashMap<>();
            utxoOutputsDetail.put("address", addrs.get(i));
            utxoOutputsDetail.put("amount", Math.round(amts.get(i) * basicConfiguration.ONE_ELA()));
            crossOutputsArray.add(utxoOutputsDetail);
        }

        JSONObject par = new JSONObject();
        par.accumulateAll(paraListMap);
        logger.info("sending : " + par);
        String rawTx = null ;
        rawTx = SingleSignTransaction.genCrossChainRawTransaction(par);
        logger.info("receiving : " + rawTx);
        return rawTx;
    }

    /**
     * generate raw transaction.
     * @param smAmt the total spend money
     * @param addrs receiver addresses
     * @param amts receiver output money
     * @param data memo data
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked", "static-access" })
    public String genTx(double smAmt , List<List<Map>> utxoTotal , List<String> prvKeys , List<String> sdrAddrs ,List<String> addrs , List<Double> amts , String data) throws Exception {

        if(addrs == null || addrs.size() == 0) {
            throw new RuntimeException("output can not be blank");
        }

        Map<String,Object> paraListMap = new HashMap<>();
        List txList = new ArrayList<>();
        paraListMap.put("Transactions", txList);
        Map<String,Object> txListMap = new HashMap<>();
        txList.add(txListMap);
        boolean isPayload = false;
        Map val = null;
        if(!StrKit.isBlank(data)){
            try {
                val = (Map)JSON.parse(data);
                if(val.containsKey("Id") && val.containsKey("Contents")){
                    txListMap.put("Payload",JSONObject.fromObject(data));
                    isPayload = true;
                }else{
                    txListMap.put("Memo", data);
                }
            }catch(Exception ex){
                txListMap.put("Memo", data);
            }
        }

        int index = -1;
        double spendMoney = 0.0;
        boolean hasEnoughFee = false;
        int utxoIndex = -1;
        out :for(int z= 0 ;z < utxoTotal.size();z++){
            List<Map> utxolm = utxoTotal.get(z);
            utxoIndex = z;
            for( int i=0; i<utxolm.size(); i++) {
                index = i;
                spendMoney += Double.valueOf(utxolm.get(i).get("amount")+"");
                if( Math.round(spendMoney * basicConfiguration.ONE_ELA()) >= Math.round((smAmt + basicConfiguration.FEE()) * basicConfiguration.ONE_ELA())) {
                    hasEnoughFee = true;
                    break out;
                }
            }
        }


        if(!hasEnoughFee) {
            throw new ApiRequestDataException(Errors.NOT_ENOUGH_UTXO.val());
        }

        List utxoInputsArray = new ArrayList<>();
        txListMap.put("UTXOInputs", utxoInputsArray);
        for(int z=0;z<=utxoIndex;z++){
            List<Map> utxolm = utxoTotal.get(z);
            String privateKey = prvKeys.get(z);
            String addr = sdrAddrs.get(z);
            int subIndex = utxolm.size() - 1;
            if(z == utxoIndex){
                subIndex = index;
            }
            for(int i=0;i<=subIndex;i++) {
                Map<String,Object> utxoInputsDetail = new HashMap<>();
                Map<String,Object> utxoM = utxolm.get(i);
                double utxoVal = Double.valueOf(utxoM.get("amount")+"");
                if (utxoVal == 0){
                    continue;
                }
                utxoInputsDetail.put("txid",  utxoM.get("txid"));
                utxoInputsDetail.put("index",  utxoM.get("vout"));
                utxoInputsDetail.put("privateKey",  privateKey);
                utxoInputsDetail.put("address",  addr);
                utxoInputsArray.add(utxoInputsDetail);
            }
        }
        List utxoOutputsArray = new ArrayList<>();
        txListMap.put("Outputs", utxoOutputsArray);
        for(int i=0;i<addrs.size();i++) {
            Map<String,Object> utxoOutputsDetail = new HashMap<>();
            utxoOutputsDetail.put("address", addrs.get(i));
            utxoOutputsDetail.put("amount", Math.round(amts.get(i) * basicConfiguration.ONE_ELA()));
            utxoOutputsArray.add(utxoOutputsDetail);
        }
        if(isPayload){
            Map<String,Object> utxoOutputsDetail = new HashMap<>();
            utxoOutputsDetail.put("address", ((Map)(val)).get("Id"));
            utxoOutputsDetail.put("amount", 0);
            utxoOutputsArray.add(utxoOutputsDetail);
        }
        double leftMoney = (spendMoney - (basicConfiguration.FEE() + smAmt));
        String changeAddr = sdrAddrs.get(0);
        Map<String,Object> utxoOutputsDetail = new HashMap<>();
        utxoOutputsDetail.put("address", changeAddr);
        utxoOutputsDetail.put("amount",Math.round(leftMoney * basicConfiguration.ONE_ELA()));
        utxoOutputsArray.add(utxoOutputsDetail);
        JSONObject par = new JSONObject();
        par.accumulateAll(paraListMap);
        logger.info("sending : " + par);
        String rawTx = null ;
        rawTx = ElaKit.genRawTransaction(par);
        logger.info("receiving : " + rawTx);
        return rawTx;
    }

    @SuppressWarnings("static-access")
    public String sendTx(String rawData,ChainType type) {
        RawTxEntity entity = new RawTxEntity();
        entity.setData(rawData);
        entity.setType(type);
        return sendRawTx(entity);
    }

    /**
     * set did info into memo
     * @param info
     * @return
     * @throws Exception
     */
    public String setDidInfo(SetDidInfoEntity info) throws Exception{
        String data = null;
        SetDidInfoEntity.Setting setting = info.getSettings();
        try {
            data = JSON.toJSONString(setting.getInfo());
        }catch (Exception ex){
            throw new ApiRequestDataException("DID info must be a json object");
        }
        // using to sign did setting info
        String privateKey = setting.getPrivateKey();
        String recevAddr = Ela.getAddressFromPrivate(privateKey);
        String fee = "0";
        TransferParamEntity transferParamEntity = new TransferParamEntity();
        SignDataEntity signDataEntity = new SignDataEntity();
        signDataEntity.setPrivateKey(privateKey);
        signDataEntity.setMsg(data);
        String response = sign(signDataEntity);
        Map respMap = (Map)JSON.parse(response);
        String rawMemo = JSON.toJSONString(respMap.get("result"));
        logger.debug("rawMemo:{}",rawMemo);
        transferParamEntity.setMemo(rawMemo);
        String payPrivKey = info.getPrivateKey();
        String addr = Ela.getAddressFromPrivate(payPrivKey);
        List<Map> lstMap = new ArrayList<>();
        Map sm = new HashMap();
        sm.put("address",addr);
        sm.put("privateKey",payPrivKey);
        lstMap.add(sm);
        transferParamEntity.setSender(lstMap);
        List<Map> receiverList = new ArrayList<>();
        Map receivMap = new HashMap();
        receivMap.put("address",recevAddr);
        receivMap.put("amount",fee);
        receiverList.add(receivMap);
        transferParamEntity.setReceiver(receiverList);
        transferParamEntity.setType(ChainType.DID_SIDECHAIN);
        return transfer(transferParamEntity);
    }

    /**
     * make did to mainchain asset transfer
     * @param entity
     * @return
     * @throws Exception
     */
    public String did2MainCrossTransfer(TransferParamEntity entity) throws Exception {
        entity.setType(ChainType.DID_MAIN_CROSS_CHAIN);
        return transfer(entity);
    }
}
