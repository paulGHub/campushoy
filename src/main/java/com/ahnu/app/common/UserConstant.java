package com.ahnu.app.common;

/**
 * 用户需要填的信息
 *
 * @author DamonCheng@ssw.com.au
 * @date 8/6/2020 11:53 AM
 */
public interface UserConstant {
    /**
     *
     * 请求的学校域名；每个学校都不相同
     */
    String DOMAIN = "https://netncepu.campusphere.net"; //NCEPU

    /**
     * 不退出就固定不变的值
     */
    String CPDAILY_EXTENSION = "";//经测试，只在数据提交时才能抓取


    /**
     * 用户的MOD_AUTH_CAS信息
     */
    String COOKIE = "";


}
