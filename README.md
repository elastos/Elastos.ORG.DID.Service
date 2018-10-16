Elastos.ORG.DID.Service
==============

[Elastos.ORG.DID.Service documentation](https://didservice.readthedocs.io)

This repo provide simple HTTP Restful API for developers to interact with elastos blockchain . you may need to construct your own local node to use some of these API , never provide private key to any third party . 


## Quick Start

Run with `Maven`：

```xml
<dependency>
    <groupId>org.elastos</groupId>
    <artifactId>did.api</artifactId>
    <version>0.0.2</version>
</dependency>
```

or `Gradle`:

```sh
compile 'org.elastos:did.api:0.0.2'
```

Add A Entry Class
```java
@SpringBootApplication
public class MainEntry {

    public static void main(String[] args) {
        SpringApplication.run(org.elastos.Application.class, args);
    }
}
```

Add application.properties in resources
```
## chain restful url
## change to your local DID sidechain node resutful port
node.didPrefix           = http://localhost:20334
node.connectionCount     = /api/v1/node/connectioncount
node.state               = /api/v1/node/state
node.blockTxByHeight     = /api/v1/block/transactions/height/
node.blockByHeight       = /api/v1/block/details/height/
node.blockByhash         = /api/v1/block/details/hash/
node.blockHeight         = /api/v1/block/height
node.blockHash           = /api/v1/block/hash/
node.transaction         = /api/v1/transaction/
node.asset               = /api/v1/asset/
node.balanceByAddr       = /api/v1/asset/balances/
node.balanceByAsset      = /api/v1/asset/balance/
node.utxoByAsset         = /api/v1/asset/utxo/
node.utxoByAddr          = /api/v1/asset/utxos/
node.sendRawTransaction  = /api/v1/transaction
node.transactionPool     = /api/v1/transactionpool
node.restart             = /api/v1/restart

## api return status code
retcode.SUCC             = 200
retcode.BAD_REQUEST      = 400
retcode.NOT_FOUND        = 404
retcode.INTERNAL_ERROR   = 500
retcode.PROCESS_ERROR    = 10001

## basic
basic.ONE_ELA            = 100000000
basic.FEE                = 0.000001
basic.CROSS_CHAIN_FEE    = 0.0001

## application
server.port              = 8091

## log
logging.level.root       =INFO
logging.level.org.elastos=DEBUG

## DID related api
## did setting field output receiving adddress , you can set you own receiving address
did.address              =EbxU18T3M9ufnrkRY7NLt6sKyckDW4VAsA
did.fee                  =0.0001
did.mainChainAddress     =XQd1DCi6H62NQdWZQhJCRnrPn7sF9CTjaU
did.burnAddress          =0000000000000000000000000000000000
```
