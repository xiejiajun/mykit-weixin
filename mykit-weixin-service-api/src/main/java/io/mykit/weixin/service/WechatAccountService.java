/**
 * Copyright 2018-2118 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mykit.weixin.service;

import io.mykit.weixin.entity.WechatAccount;
import io.mykit.weixin.service.base.WechatCacheService;

import java.util.List;

/**
 * @author liuyazhuang
 * @date 2018/10/8 13:34
 * @description 微信账户Service接口
 * @version 1.0.0
 */
public interface WechatAccountService extends WechatCacheService {

    /**
     * 获取所有的账户信息
     * @param status 状态
     * @return 微信账户列表
     */
    List<WechatAccount> getAllWechatAccount(Integer ... status);

    /**
     * 根据id和system获取微信账户信息
     * @param foreignId foreignId
     * @param foreignSystem foreignSystem
     * @return 微信账户信息
     */
    WechatAccount getWechatAccountByForeignIdAndSystem(String foreignId, String foreignSystem);

    /**
     * 获取微信开发者账号表id
     * @param foreignId  foreignId
     * @param foreignSystem foreignSystem
     * @return 微信账户id
     */
    String getIdByForeignIdAndSystem(String foreignId, String foreignSystem);
}
