package com.liudehuang.controller;

import com.alibaba.fastjson.JSONObject;
import com.liudehuang.base.BaseApiService;
import com.liudehuang.util.HttpClientUtils;
import com.liudehuang.util.WeiXinUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author liudehuang
 * @date 2019/2/14 8:56
 */
@Controller
public class OauthController extends BaseApiService {
    @Autowired
    private WeiXinUtils weiXinUtils;

    private String errorPage = "errorPage";

    //1、生成授权链接，重定向到微信开放平台
    @RequestMapping("/authorizedUrl")
    public String authorizedUrl(){
        return "redirect:" + weiXinUtils.getAuthorizedUrl();
    }
    /**
     * 2、用户回调地址，返回code码
     * 3、微信重定向到回调地址，带上code码
     */
    @RequestMapping("/callback")
    public String callBack(String code, HttpServletRequest request){
        //1、根据appId、appSecret以及code码换取accessToken以及openid
        String accessTokenUrl = weiXinUtils.getAccessTokenUrl(code);
        JSONObject accessTokenResult = HttpClientUtils.httpGet(accessTokenUrl);
        boolean containKey = accessTokenResult.containsKey("errcode");
        if(containKey){
            request.setAttribute("errorMsg","系统错误!");
            return errorPage;
        }
        //2、使用access_token换取用户信息
        String accessToken = accessTokenResult.getString("access_token");
        String openId = accessTokenResult.getString("openid");
        //3、获取用户信息(需scope为 snsapi_userinfo)
        String userInfoUrl = weiXinUtils.getUserInfo(accessToken, openId);
        JSONObject userInfoResult = HttpClientUtils.httpGet(userInfoUrl);
        System.out.println("userInfoResult:" + userInfoResult);
        request.setAttribute("nickname", userInfoResult.getString("nickname"));
        request.setAttribute("city", userInfoResult.getString("city"));
        request.setAttribute("headimgurl", userInfoResult.getString("headimgurl"));
        return "info";

    }
}
