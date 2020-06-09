package weixinmenu.demo.service.Impl;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import weixinmenu.demo.dao.WeiXinBindingDao;
import weixinmenu.demo.model.WeixinBinding;
import weixinmenu.demo.model.WorkManWeixinMenu;
import weixinmenu.demo.service.MenuServer;
import weixinmenu.demo.util.HttpKit;
import weixinmenu.demo.util.WeixinUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MenuServerImpl implements MenuServer {
    @Autowired
    WeiXinBindingDao weiXinBindingDao;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public  String  createMenuFoWorkMan(String binding_id )throws Exception{
        List<String> openidList = weiXinBindingDao.getWorkManOpenids(binding_id);
        WeixinBinding weixinBinding = weiXinBindingDao.getWeixinBinding(binding_id);
        String ACCESS_TOKEN = WeixinUtil.getAccessToken(weixinBinding.getWx_appid(),  weixinBinding.getWx_appsecret(), redisTemplate);
        //获取用户已经创建的标签 并检查是否有对应标签
        String url = "https://api.weixin.qq.com/cgi-bin/tags/get?access_token="+ACCESS_TOKEN;
        //get请求获取信息，获取 获取公众号已创建的标签
        String targs = HttpKit.get(url);
        JSONObject jsonObject=JSONObject.fromObject(targs);
        JSONArray jsonArray=jsonObject.getJSONArray("tags");
        int workmanId = 0;
        for (int i = 0; i < jsonArray.size() ; i++ ){
            String tag = jsonArray.get(i).toString();
            JSONObject jsonObject1 = JSONObject.fromObject(tag);
            //判断公众号是否创建了“工作人员”这个标签
            if (jsonObject1.get("name").equals("工作人员")){
                workmanId = (int) jsonObject1.get("id");
                break;
            }
        }
        String resultTag = "";
        //公众还没有创建,为该社区创建标签
        if (workmanId==0){
            JSONObject json=JSONObject.fromObject("{\"tag\":{\"name\":\""+"工作人员"+"\"} }");
            String tagPost="https://api.weixin.qq.com/cgi-bin/tags/create?access_token="+ACCESS_TOKEN; //创建标签接口
            String createTagResult = HttpKit.post(tagPost,json.toString());
            JSONObject createTagResultjson = JSONObject.fromObject(createTagResult);
            resultTag = createTagResultjson.get("tag").toString();
            System.out.println(resultTag);
            }
            Map map = new HashMap<>();
            map.put("openid_list",openidList);
            map.put("tagid",workmanId);
            //用户批量绑定标签接口
            String tagUserPost="https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token="+ACCESS_TOKEN;
            JSONObject fans = JSONObject.fromObject(map);
            //post请求获取绑定返回信息
            String tagUserResult=HttpKit.post(tagUserPost,fans.toString());
            if (JSONObject.fromObject(tagUserResult).get("errmsg").equals("ok")){
                List<WorkManWeixinMenu> workManWeixinMenus = weiXinBindingDao.getWorkManMenu();
                for(WorkManWeixinMenu workManWeixinMenu:workManWeixinMenus){
                    if(null != workManWeixinMenu.getUrl() && !"".equals(workManWeixinMenu.getUrl())){
                        if(workManWeixinMenu.getUrl().contains("OtherUrl")){
                            String url1 = workManWeixinMenu.getUrl().split(",")[1];
                            workManWeixinMenu.setUrl("http://wwg.bjdch.gov.cn"+url+weixinBinding.getBinding_id());
                        }else if(workManWeixinMenu.getUrl().contains("ExternalLinks")){
                            String url1 = workManWeixinMenu.getUrl().split(",")[1];
                            workManWeixinMenu.setUrl(url);
                        }else {
                            workManWeixinMenu.setUrl("http://wwg.bjdch.gov.cn" + "/weixin/" + workManWeixinMenu.getUrl() + "?bingding_id=" + binding_id);
                        }
                    }
                }
                Map map1 = new HashMap();
                Map matchrule = new HashMap();
                matchrule.put("tag_id",workmanId);
                //要生成的菜单列表
                map1.put("button",workManWeixinMenus);
                //用户标签的id
                map1.put("matchrule",matchrule);
                log.info("map1：",map1.get("button").toString()+map1.get("matchrule").toString());
                //创建个性化菜单
                String postUrl="https://api.weixin.qq.com/cgi-bin/menu/addconditional?access_token="+ACCESS_TOKEN;
                String createresult = HttpKit.post(postUrl,JSONObject.fromObject(map1).toString());
                if (createresult.contains("menuid")){
                    return "ok";
            }
        }
        return "false";
    }

}
