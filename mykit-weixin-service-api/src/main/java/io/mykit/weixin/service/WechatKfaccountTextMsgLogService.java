/**
 * Copyright 2019-2999 the original author or authors.
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

import io.mykit.weixin.params.WechatKfaccountNewsMsgParams;
import io.mykit.weixin.params.WechatKfaccountTextMsgParams;
import io.mykit.weixin.service.base.WechatCacheService;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/8
 * @description 微信客服消息发送记录service
 */
public interface WechatKfaccountTextMsgLogService extends WechatCacheService {

    /**
     * 发送微信客服文本消息
     * @param wechatKfaccountTextMsgParams 接收的客户端参数
     */
    void sendWechatKfaccountTextMsg(WechatKfaccountTextMsgParams wechatKfaccountTextMsgParams) throws Exception;

    /**
     * 发送微信图文消息
     */
    void sendWechatKfaccountNewsMsg(WechatKfaccountNewsMsgParams wechatKfaccountNewsMsgParams) throws Exception;
}
