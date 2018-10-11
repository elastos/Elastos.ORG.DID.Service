/**
 * Copyright (c) 2017-2018 The Elastos Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.elastos.controller;

import com.alibaba.fastjson.JSON;
import org.elastos.conf.RetCodeConfiguration;
import org.elastos.entity.ReturnMsgEntity;
import org.elastos.exception.ApiInternalException;
import org.elastos.util.JsonUtil;
import org.elastos.util.StrKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * clark
 * <p>
 * 9/3/18
 */
@RestController
@RequestMapping("/api/1")
public class BaseController {
    @Autowired
    private RetCodeConfiguration retCodeConfiguration;

    private Logger logger = LoggerFactory.getLogger(BaseController.class);

    /**
     * POST/GET request
     *
     * @param reqParam    request body
     * @param clazz      Entity class name
     * @param methodName invoked method name
     * @param service    requested service
     * @param <T>
     * @return
     */
    protected <T> String call(String reqParam, Class<T> clazz, String methodName, Object service) {
        try {
            if (reqParam != null){

                T entity = JsonUtil.jsonStr2Entity((String)reqParam, clazz);

                Method m = service.getClass().getDeclaredMethod(methodName, clazz);

                return (String) m.invoke(service, entity);

            }else{

                Method m = service.getClass().getDeclaredMethod(methodName);

                return (String) m.invoke(service);

            }

        } catch (Exception ex) {

            return handleError(ex, methodName);
        }
    }

    private String handleError(Exception ex, String methodName) {

        ex.printStackTrace();

        logger.error("Hd {} error : {}", methodName, ex);

        long status = 0;

        if (ex instanceof ApiInternalException) {
            status = retCodeConfiguration.NTERNAL_ERROR();
        } else {
            status = retCodeConfiguration.BAD_REQUEST();
        }
        String msg = ex.getMessage();
        if (ex instanceof  InvocationTargetException){
            msg = ((InvocationTargetException)ex).getTargetException().getMessage();
        }
        if (StrKit.isBlank(msg)){
            msg = ex.getCause().getMessage();
        }
        if (StrKit.isBlank(msg)){
            msg = ex.toString();
        }
        return JSON.toJSONString(new ReturnMsgEntity().setResult(msg).setStatus(status));

    }
}
