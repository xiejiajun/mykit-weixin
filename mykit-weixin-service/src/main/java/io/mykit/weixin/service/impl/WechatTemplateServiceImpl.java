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
package io.mykit.weixin.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.mykit.wechat.mp.beans.json.template.WxTemplateDataItemSend;
import io.mykit.wechat.mp.beans.json.template.send.WxTemplateDataSend;
import io.mykit.wechat.mp.beans.json.template.send.WxTemplateSend;
import io.mykit.wechat.mp.http.handler.template.send.WxTemplateSendHandler;
import io.mykit.wechat.utils.common.StringUtils;
import io.mykit.wechat.utils.json.JsonUtils;
import io.mykit.weixin.constants.code.MobileHttpCode;
import io.mykit.weixin.constants.wechat.WechatConstants;
import io.mykit.weixin.entity.WechatAccount;
import io.mykit.weixin.entity.WechatTemplate;
import io.mykit.weixin.entity.WechatTemplateMsgLog;
import io.mykit.weixin.entity.WechatUserInfo;
import io.mykit.weixin.mapper.WechatTemplateMapper;
import io.mykit.weixin.mapper.WechatTemplateMsgLogMapper;
import io.mykit.weixin.mapper.WechatUserInfoMapper;
import io.mykit.weixin.params.WechatTemplateParams;
import io.mykit.weixin.service.WechatAccountService;
import io.mykit.weixin.service.WechatTemplateService;
import io.mykit.weixin.service.WechatUserInfoService;
import io.mykit.weixin.service.impl.base.WechatCacheServiceImpl;
import io.mykit.weixin.utils.exception.MyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author liuyazhuang
 * @date 2018/10/30 20:17
 * @description
 * @version 1.0.0
 */
@Service
public class WechatTemplateServiceImpl extends WechatCacheServiceImpl implements WechatTemplateService {
    private final Logger logger = LoggerFactory.getLogger(WechatTemplateServiceImpl.class);
    @Resource
    private WechatTemplateMapper wechatTemplateMapper;
    @Resource
    private WechatTemplateMsgLogMapper wechatTemplateMsgLogMapper;
    @Resource
    private WechatAccountService wechatAccountService;
    @Resource
    private WechatUserInfoService wechatUserInfoService;
    @Resource
    private WechatUserInfoMapper wechatUserInfoMapper;

    @Override
    public WechatTemplate getWechatTemplateByType(String type, String accountId) {
        String key = type.concat(accountId).concat("getWechatTemplateByType");
        WechatTemplate wechatTemplate = null;
        String result = getJedisCluster().get(key);
        if(StringUtils.isEmpty(result) || WechatConstants.NULL_STRING.equalsIgnoreCase(result)){
            wechatTemplate = wechatTemplateMapper.getWechatTemplateByType(type, accountId);
            getJedisCluster().setex(key, WechatConstants.WECHAT_CACHE_TIME, JsonUtils.bean2Json(wechatTemplate));
        }else{
            wechatTemplate = JsonUtils.json2Bean(result, WechatTemplate.class);
        }
        return wechatTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sendWechatTemplateMessage(WechatTemplateParams wechatTemplateParams) throws MyException {
        WechatAccount wechatAccount = wechatAccountService.getWechatAccountByForeignIdAndSystem(wechatTemplateParams.getForeignSystemId(), wechatTemplateParams.getForeignSystem());
        if(wechatAccount == null){
            logger.info("未获取到微信开发者账号信息....");
            throw new MyException("未获取到微信开发者账号信息", MobileHttpCode.HTTP_NOT_GET_WECHAT_ACCOUNT);
        }
        //是否有权限发送模板消息,没有权限发送，直接返回状态码
        if(StringUtils.isEmpty(wechatAccount.getSendTemplate()) || WechatConstants.SEND_NO.equals(wechatAccount.getSendTemplate())){
            logger.info("是否有权限发送模板消息,没有权限发送");
            throw new MyException("没有权限发送模板消息", MobileHttpCode.HTTP_NO_LIMIT_TO_SEND_TEMPLATE);
        }
        //获取微信模板消息
        WechatTemplate wechatTemplate = this.getWechatTemplateByType(wechatTemplateParams.getTemplatetType(), wechatAccount.getId());
        if(wechatTemplate == null){
            logger.info("未获取到微信消息模板....");
            throw new MyException("未获取到微信消息模板", MobileHttpCode.HTTP_NOT_GET_WECHAT_TEMPLATE);
        }
        String openId = "";
        if(StringUtils.isEmpty(wechatTemplateParams.getOpenId())){
            openId = wechatUserInfoService.getOpenId(wechatTemplateParams.getForeignSystemId(), wechatTemplateParams.getForeignSystem(), wechatTemplateParams.getForeignId(), wechatTemplateParams.getForeignType());
        }else{
            openId = wechatTemplateParams.getOpenId();
            //String foreignSystemId, @Param("foreignSystem") String foreignSystem, @Param("openId") String openId, @Param("foreignType") String foreignType
            String id = wechatUserInfoMapper.getId(wechatTemplateParams.getForeignSystemId(), wechatTemplateParams.getForeignSystem(), wechatTemplateParams.getOpenId(), wechatTemplateParams.getForeignType());
            //数据为空,保存数据
            if(StringUtils.isEmpty(id)){
                WechatUserInfo wechatUserInfo = new WechatUserInfo();
                wechatUserInfo.setForeignSystemId(wechatTemplateParams.getForeignSystemId());
                wechatUserInfo.setForeignSystem(wechatTemplateParams.getForeignSystem());
                wechatUserInfo.setSlaveUser(wechatAccount.getSlaveUser());
                wechatUserInfo.setOpenId(openId);
                wechatUserInfo.setForeignId(wechatTemplateParams.getForeignId());
                wechatUserInfo.setForeignType(wechatTemplateParams.getForeignType());
                wechatUserInfo.setNickname("");
                wechatUserInfo.setSex(0);
                wechatUserInfo.setProvince("");
                wechatUserInfo.setCity("");
                wechatUserInfo.setCountry("");
                wechatUserInfo.setHeadimgurl("");
                wechatUserInfo.setPrivilege("");
                wechatUserInfo.setUnionid("");
                wechatUserInfoMapper.saveWechatUserInfo(wechatUserInfo);
            }else{
                wechatUserInfoMapper.updateForeignId(wechatTemplateParams.getForeignId(), id) ;
            }
        }
        if(StringUtils.isEmpty(openId)){
            logger.info("未获取到微信openid....");
            throw new MyException("未获取到微信openid", MobileHttpCode.HTTP_NOT_GET_WECHAT_OPEN_ID);
        }
        WxTemplateDataSend wxTemplateDataSend = new WxTemplateDataSend();
        if(!StringUtils.isEmpty(wechatTemplateParams.getFirst())){
            wxTemplateDataSend.setFirst(new WxTemplateDataItemSend(wechatTemplateParams.getFirst(), "#173177"));
        }
        if(wechatTemplateParams.getKeywordCount() >= 1){
            wxTemplateDataSend.setKeyword1(new WxTemplateDataItemSend(wechatTemplateParams.getKeyword1(), "#173177"));
        }

        if(wechatTemplateParams.getKeywordCount() >= 2){
            wxTemplateDataSend.setKeyword2(new WxTemplateDataItemSend(wechatTemplateParams.getKeyword2(), "#173177"));
        }

        if(wechatTemplateParams.getKeywordCount() >= 3){
            wxTemplateDataSend.setKeyword3(new WxTemplateDataItemSend(wechatTemplateParams.getKeyword3(), "#173177"));
        }

        if(wechatTemplateParams.getKeywordCount() >= 4){
            wxTemplateDataSend.setKeyword4(new WxTemplateDataItemSend(wechatTemplateParams.getKeyword4(), "#173177"));
        }

        if(wechatTemplateParams.getKeywordCount() >= 5){
            wxTemplateDataSend.setKeyword5(new WxTemplateDataItemSend(wechatTemplateParams.getKeyword5(), "#173177"));
        }

        if(wechatTemplateParams.getKeywordCount() >= 6){
            wxTemplateDataSend.setKeyword6(new WxTemplateDataItemSend(wechatTemplateParams.getKeyword6(), "#173177"));
        }

        if(wechatTemplateParams.getKeywordCount() >= 7){
            wxTemplateDataSend.setKeyword7(new WxTemplateDataItemSend(wechatTemplateParams.getKeyword7(), "#173177"));
        }

        if(wechatTemplateParams.getKeywordCount() >= 8){
            wxTemplateDataSend.setKeyword8(new WxTemplateDataItemSend(wechatTemplateParams.getKeyword8(), "#173177"));
        }

        if(wechatTemplateParams.getKeywordCount() >= 9){
            wxTemplateDataSend.setKeyword9(new WxTemplateDataItemSend(wechatTemplateParams.getKeyword9(), "#173177"));
        }

        if(wechatTemplateParams.getKeywordCount() >= 10){
            wxTemplateDataSend.setKeyword10(new WxTemplateDataItemSend(wechatTemplateParams.getKeyword10(), "#173177"));
        }
        if(!StringUtils.isEmpty(wechatTemplateParams.getRemark())){
            wxTemplateDataSend.setRemark(new WxTemplateDataItemSend(wechatTemplateParams.getRemark(), "#173177"));
        }

        WxTemplateSend wxTemplateSend = new WxTemplateSend();
        wxTemplateSend.setData(wxTemplateDataSend);
        wxTemplateSend.setTemplate_id(wechatTemplate.getWechatTemplateId());
        wxTemplateSend.setTouser(openId);
        if(!StringUtils.isEmpty(wechatTemplateParams.getUrl())){
            wxTemplateSend.setUrl(wechatTemplateParams.getUrl());
        }
        //发送模板消息
        String result = "";
        try{
            result = WxTemplateSendHandler.sendTemplate(wechatAccount.getAppId(), wechatAccount.getAppSecret(), wxTemplateSend);
        }catch (Exception e) {
            e.printStackTrace();
            throw new MyException("服务端异常", MobileHttpCode.HTTP_SERVER_EXCEPTION);
        }
        if(StringUtils.isEmpty(result)){
            throw new MyException("发送微信模板消息失败", MobileHttpCode.HTTP_NOT_GET_WECHAT_TEMPLATE_SEND_FAILED);
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (!jsonObject.containsKey(WechatConstants.WEHCAT_ERROR_CODE)){
            throw new MyException("发送微信模板消息失败", MobileHttpCode.HTTP_NOT_GET_WECHAT_TEMPLATE_SEND_FAILED);
        }
        Integer wechatCode = jsonObject.getInteger(WechatConstants.WEHCAT_ERROR_CODE);
        if(wechatCode == null || wechatCode != WechatConstants.WECHAT_CODE_NORMAL){
            throw new MyException("发送微信模板消息失败", MobileHttpCode.HTTP_NOT_GET_WECHAT_TEMPLATE_SEND_FAILED);
        }
        WechatTemplateMsgLog wechatTemplateMsgLog = new WechatTemplateMsgLog();
        wechatTemplateMsgLog.setAccountId(wechatAccount.getId());
        wechatTemplateMsgLog.setTemplateId(wechatTemplate.getId());
        wechatTemplateMsgLog.setType(wechatTemplate.getType());
        wechatTemplateMsgLog.setWechatTemplateId(wechatTemplate.getWechatTemplateId());
        wechatTemplateMsgLog.setParameter(JsonUtils.bean2Json(wechatTemplateParams));
        wechatTemplateMsgLog.setTitle(wechatTemplate.getTitle());
        wechatTemplateMsgLog.setContent(wechatTemplate.getContent());
        wechatTemplateMsgLog.setOpenId(openId);
        wechatTemplateMsgLog.setResult(result);
        wechatTemplateMsgLog.setWxParameter(JsonUtils.bean2Json(wxTemplateSend));
        wechatTemplateMsgLog.setRetry(wechatTemplateParams.getRetry());
        return wechatTemplateMsgLogMapper.saveWechatTemplateMsgLog(wechatTemplateMsgLog);
    }
}
