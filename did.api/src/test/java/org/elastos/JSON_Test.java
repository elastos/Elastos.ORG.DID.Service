/**
 * Copyright (c) 2017-2018 The Elastos Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.elastos;

import com.alibaba.fastjson.JSON;
import net.sf.json.JSONObject;
import org.elastos.ela.Ela;
import org.elastos.entity.SetDidInfoEntity;
import org.elastos.util.JsonUtil;
import org.elastos.util.StrKit;
import org.junit.Test;

import java.util.Map;

/**
 * clark
 * <p>
 * 10/17/18
 */
public class JSON_Test {

    @Test
    public void test01(){
        String str ="{\n" +
                "    \"result\":{\n" +
                "        \"vsize\":346,\n" +
                "        \"locktime\":0,\n" +
                "        \"txid\":\"62637968e72b06e4fa1de91542a3b71bd2462ba1d29e9c14c2ecfd042d1937ab\",\n" +
                "        \"confirmations\":6756,\n" +
                "        \"type\":8,\n" +
                "        \"version\":0,\n" +
                "        \"vout\":[\n" +
                "            {\n" +
                "                \"outputlock\":0,\n" +
                "                \"address\":\"XQd1DCi6H62NQdWZQhJCRnrPn7sF9CTjaU\",\n" +
                "                \"assetid\":\"a3d0eaa466df74983b5d7c543de6904f4c9418ead5ffd6d25814234a96db37b0\",\n" +
                "                \"value\":\"0.10010000\",\n" +
                "                \"n\":0\n" +
                "            },\n" +
                "            {\n" +
                "                \"outputlock\":0,\n" +
                "                \"address\":\"EbxU18T3M9ufnrkRY7NLt6sKyckDW4VAsA\",\n" +
                "                \"assetid\":\"a3d0eaa466df74983b5d7c543de6904f4c9418ead5ffd6d25814234a96db37b0\",\n" +
                "                \"value\":\"0.50249300\",\n" +
                "                \"n\":1\n" +
                "            }\n" +
                "        ],\n" +
                "        \"blockhash\":\"4021e5c0ace86221016d3aa2b114adbd84bb03692bb6ddc6034794260834c570\",\n" +
                "        \"size\":346,\n" +
                "        \"blocktime\":1538279155,\n" +
                "        \"payload\":{\n" +
                "            \"CrossChainAddresses\":[\n" +
                "                \"EHLhCEbwViWBPwh1VhpECzYEA7jQHZ4zLv\"\n" +
                "            ],\n" +
                "            \"OutputIndexes\":[\n" +
                "                0\n" +
                "            ],\n" +
                "            \"CrossChainAmounts\":[\n" +
                "                10000000\n" +
                "            ]\n" +
                "        },\n" +
                "        \"vin\":[\n" +
                "            {\n" +
                "                \"sequence\":0,\n" +
                "                \"txid\":\"ba7bd41aae0a1371d9689ad04508f0754bb4a5333386411bccbdec718ce61625\",\n" +
                "                \"vout\":1\n" +
                "            }\n" +
                "        ],\n" +
                "        \"payloadversion\":0,\n" +
                "        \"attributes\":[\n" +
                "            {\n" +
                "                \"data\":\"32323432343239353130383035363838303230\",\n" +
                "                \"usage\":0\n" +
                "            }\n" +
                "        ],\n" +
                "        \"time\":1538279155,\n" +
                "        \"programs\":[\n" +
                "            {\n" +
                "                \"code\":\"21021421976fdbe518ca4e8b91a37f1831ee31e7b4ba62a32dfe2f6562efd57806adac\",\n" +
                "                \"parameter\":\"40cf6b8a18c861fcad1c23816221cc40a0d2e7d43065c070e66905ff7d6c634068542dd2a9b0bbb24de6a5a547b57767f908fc384cd6dc06298de11ebc3338aa79\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"hash\":\"62637968e72b06e4fa1de91542a3b71bd2462ba1d29e9c14c2ecfd042d1937ab\"\n" +
                "    },\n" +
                "    \"status\":200\n" +
                "}";
        Map m = (Map)JSON.parse(str);
        Map mm = (Map)m.get("result");
        Object o = mm.get("blocktime");
        System.out.println();
    }

    @Test
    public void test02(){
        Map val = null;
        String data = "{\"Id\":1,\"Contents\":\"name\"}";
        if(!StrKit.isBlank(data)){
            try {
                val = (Map)JSON.parse(data);
                if(val.containsKey("Id") && val.containsKey("Contents")){
                    System.out.print(1);
                }else{
                    System.out.print(2);
                }
            }catch(Exception ex){
                System.out.print(2);
            }
        }
    }

    @Test
    public void priv2Pub(){
        String publicKey = Ela.getPublicFromPrivate("1615CC0AB02168680354E07048F9CE54B2921847F68453586C4A2DBC23BA2C9D");
        System.out.print(publicKey);
    }
}
