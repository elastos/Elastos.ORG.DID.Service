/**
 * Copyright (c) 2017-2019 The Elastos Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.elastos.entity;

import java.util.Map;

/**
 * clark
 * <p>
 * 6/5/19
 */
public class RpcReq {

    private String method ;

    private Map<String,Object> params ;

    public RpcReq(String method){
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public RpcReq setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }
}
