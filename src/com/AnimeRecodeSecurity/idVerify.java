/*
 * ID合法性检测
 * by shengjing19
 * create 2025/07/25
 * last modify
 * v1.0
 * */
package com.AnimeRecodeSecurity;

public class idVerify {
    public boolean verifyID(String ID){

        //检查ID是否为空
        if (ID == null || ID.isEmpty())
        {
            //ID为空
            return false;
        }

        //检查ID类型是否合法
        int id;
        try {
            id = Integer.parseInt(ID);
        } catch (NumberFormatException e) {
            //未知ID类型
            return false;
        }

        //验证通过
        return true;
    }
}
