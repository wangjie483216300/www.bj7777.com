package weixinmenu.demo.service.Impl;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.catalina.mapper.MappingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;
import weixinmenu.demo.dao.WeiXinBindingDao;
import weixinmenu.demo.model.PageData;
import weixinmenu.demo.model.WeixinBinding;
import weixinmenu.demo.service.WeinxinnLoginServer;
import weixinmenu.demo.service.WeixinUserMessageServer;
import weixinmenu.demo.util.HttpKit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class WeinxinnLoginServerImpl implements WeinxinnLoginServer {
    /**
     * 1、授权回调地址
     */
    @Value("${weixinurl}")
    private String weixinurl ;
    /**
     * 2、微信获取code地址
     */
    @Value("${getcodeurl}")
    private String getcodeurl ;
    /**
     * 3、微信通过code获取网页授权access_token
     */
    @Value("${getwebaccess_token}")
    private String getweb_access_tokenurl;
    /**
     * 4、通过openid和网页授权token获取用户信息
     */
    @Value("${getuserMessagebytoken}")
    private String getuserMessagebytoken;


    @Autowired
    private WeixinUserMessageServer weixinUserMessageServer;


    @Autowired
    private WeiXinBindingDao weiXinBindingDao;


    @Override
    public String isauth(String bingding_id){
        WeixinBinding weixinBinding = weiXinBindingDao.getWeixinBinding(bingding_id);
        String appid  = weixinBinding.getWx_appid();
        String myurl="";
        weixinurl = weixinurl+"/weixin/verauth";
        try {
            myurl = URLEncoder.encode(weixinurl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String redirectUrl = getcodeurl.replaceAll("APPID",appid).
                                        replaceAll("REDIRECT_URI",weixinurl).
                                        replaceAll("SCOPE","snsapi_userinfo");
        log.info("回调redirectUrl是："+redirectUrl);
        return redirectUrl;
    }


    @Override
    public PageData getwebaccess_token(String code, String bingding_id){
        PageData pageData = new PageData();
        WeixinBinding weixinBinding = weiXinBindingDao.getWeixinBinding(bingding_id);
        String appid  = weixinBinding.getWx_appid();
        String secret = weixinBinding.getWx_appsecret();
        String gettoken = getweb_access_tokenurl.replaceAll("APPID",appid).
                                                replaceAll("SECRET",secret).
                                                replaceAll("CODE",code);
        String result="";
        try {
            result = HttpKit.post(gettoken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSONObject.fromObject(result);
        if (jsonObject.toString().contains("errcode")){
            pageData.put("error",jsonObject);
            return pageData; }else {
            String openid = jsonObject.getString("openid");
            pageData=weixinUserMessageServer.getusermessage(openid,weixinBinding);
        }
        pageData.put("webtokenbycode",result);
        return pageData;
    }

    @Override
    public String getuserMessage(String openid, String webToken) {
        String url = getuserMessagebytoken.replaceAll("ACCESS_TOKEN",webToken).
                                            replaceAll("OPENID",openid);
        String messageResult = "";
        try {
            messageResult = HttpKit.get(url);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return messageResult;
    }

}
