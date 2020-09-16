package com.ahnu.app.service;

import com.ahnu.app.bean.FieldItem;
import com.ahnu.app.bean.Item;
import com.ahnu.app.bean.SubmitForm;
import com.ahnu.app.common.ApiConstant;
import com.ahnu.app.common.EmailConstant;
import com.ahnu.app.common.UserConstant;
import com.ahnu.app.dao.RequestDao;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 每天被启动的任务
 *
 * @author DamonCheng@ssw.com.au
 * @date 8/6/2020 11:39 AM
 */
public class Task {


    /**
     * 成功
     */
    private static final String SUCCESS = "SUCCESS";

    private static final String RESP_LOGIN_KEY = "WEC-REDIRECTURL";

    /**
     * 开始方法
     */
    public void start() {
        try {
            //需要提交的表单数据
            SubmitForm submitForm = new SubmitForm();
            //查询今日是否有表单可以提交
            JSONObject datas = getFormList();
            //判断是否跳转到登录页面
            if (datas.containsKey(RESP_LOGIN_KEY)) {
                System.out.println("登录过期");
            }
            JSONArray formList = datas.getJSONArray("rows");
            if (formList == null) {
                return;
            }
            for (int i = 0; i < formList.size(); i++) {
                JSONObject jsonRow = formList.getJSONObject(i);
                Integer isHandled = jsonRow.getInteger("isHandled");
                int priority=jsonRow.getIntValue("priority");
                //查看是否已经提交，并看是否是极重要任务
                if (!isHandled.equals(0)|| (priority-5)<0) {
                    continue;
                }
                //这个 wid =collectorWid;
                String wid = jsonRow.getString("wid");
                String formWid = jsonRow.getString("formWid");
                submitForm.setFormWid(formWid);
                submitForm.setAddress("河北保定莲池区");
                submitForm.setCollectWid(wid);
                String schoolTaskWid = getSchoolTaskWid(wid);
                submitForm.setSchoolTaskWid(schoolTaskWid);
                List<Item> form = getForm(formWid, wid);
                submitForm.setForm(form);
                if (submit(submitForm)) {
                    System.out.println("提交成功");

                    return;
                } else {
                    System.out.println("提交失败！");
                    return;
                }

            }

        } catch (Exception ex) {
            System.out.println("异常信息："+ex.getMessage());
        }


    }

    /**
     * 获取今日最新未填写的表单集合
     *
     * @return 表单信息
     * @throws IOException
     */
    public JSONObject getFormList() {
        return RequestDao.request(UserConstant.DOMAIN + ApiConstant.FORMLIST, "{\"pageSize\":6,\"pageNumber\":1}").getJSONObject("datas");
    }


    /**
     * 传入collectorWid 获取schoolTaskWid
     *
     * @param collectorWid 就是表单的Wid
     * @return
     */
    public String getSchoolTaskWid(String collectorWid) {
        return RequestDao.request(UserConstant.DOMAIN + ApiConstant.SCHOOLTASKWID, "{\"collectorWid\":\"" + collectorWid + "\"}").getJSONObject("datas").getJSONObject("collector").getString("schoolTaskWid");
    }


    /**
     * 获取表单，并填写表单
     *
     * @param formWid
     * @param collectorWid 就是表单的Wid
     * @return 返回已经填写好的表单
     */
    public List<Item> getForm(String formWid, String collectorWid) {
        JSONArray jsonArray = RequestDao.request(UserConstant.DOMAIN + ApiConstant.FORMDETAIl, "{\"pageSize\":20,\"pageNumber\":1,\"formWid\":\"" + formWid + "\",\"collectorWid\":\"" + collectorWid + "\"}").getJSONObject("datas").getJSONArray("rows");
        List<Item> form = new ArrayList<Item>(4);
        for (int j = 0; j < jsonArray.size(); j++) {
            Item item = jsonArray.getObject(j, Item.class);
            String ind=item.getSort();
            //为每一项填上需要的值
            if (ind.equals("1")||ind.equals("5")) {//选择项填写
                item.setFormWid(formWid);
                if (ind.equals("1"))
                        item.setValue("小于");
                else if(ind.equals("5"))
                        item.setValue("身体健康，正常");
                List<FieldItem> fieldItems = new ArrayList<FieldItem>();
                for (FieldItem fieldItem : item.getFieldItems()) {
                    if (fieldItem.getContent().contains("健康")||fieldItem.getContent().equals("小于")) {
                        fieldItem.setIsSelected(null);
                        fieldItems.add(fieldItem);
                    }
                }
                item.setFieldItems(fieldItems);
                form.add(item);
            } else if (ind.equals("2")||ind.equals("3")||ind.equals("4")) {//填空项填写
                item.setFormWid(formWid);
                item.setValue("36.6");
                form.add(item);
            }

        }
        System.out.println(JSON.toJSONString(form));
        return form;
    }

    /**
     * 提交信息
     *
     * @param submitForm 表单信息
     * @return
     */
    public boolean submit(SubmitForm submitForm) {
        JSONObject request = RequestDao.request(UserConstant.DOMAIN + ApiConstant.SUBMITFORM, JSONObject.toJSONString(submitForm, SerializerFeature.WriteMapNullValue));
        String message = request.getString("message");
        if (message == null || !SUCCESS.equals(message)) {
            return false;
        } else {
            return true;
        }
    }

    public static void stop(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
