/**
 * Copyright (c) 2017-2018 The Elastos Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.elastos.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * clark
 * <p>
 * 9/27/18
 */
@Component
@ConfigurationProperties("sidechain")
public class SidechainConfiguration {
    private String did_prefix;
    private String did_rpc_user;
    private String did_rpc_pass;

    public String getDid_prefix() {
        return did_prefix;
    }

    public String getDid_rpc_user() {
        return did_rpc_user;
    }

    public void setDid_rpc_user(String did_rpc_user) {
        this.did_rpc_user = did_rpc_user;
    }

    public String getDid_rpc_pass() {
        return did_rpc_pass;
    }

    public void setDid_rpc_pass(String did_rpc_pass) {
        this.did_rpc_pass = did_rpc_pass;
    }

    public void setDid_prefix(String did_prefix) {
        this.did_prefix = did_prefix;
    }
}
