package weixinmenu.demo.service.Impl;


import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import weixinmenu.demo.model.PageData;
import weixinmenu.demo.model.WeixinBinding;
import weixinmenu.demo.service.WeixinUserMessageServer;
import weixinmenu.demo.util.HttpKit;
import weixinmenu.demo.util.WeixinUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarEntry;


@Service
public class WeixinUserMessageServerImpl implements WeixinUserMessageServer {
    @Value("${getusermessage}")
    private String getusermessageurl;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 通过openid获取用户个人信息
     * @param openid
     * @param weixinBinding
     * @return
     */
    @Override
    public PageData getusermessage(String openid, WeixinBinding weixinBinding) {
        PageData pd = new PageData();
        String Access_token = "";
        String getresult = "";
        try {
             Access_token = WeixinUtil.getAccessToken(weixinBinding.getWx_appid(),weixinBinding.getWx_appsecret(),redisTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = getusermessageurl.replaceAll("ACCESS_TOKEN",Access_token).replaceAll("OPENID",openid);
        try {
            getresult = HttpKit.get(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSONObject.fromObject(getresult);
        pd.put("usermessagebyopenid",jsonObject);
        return pd;
    }
}
