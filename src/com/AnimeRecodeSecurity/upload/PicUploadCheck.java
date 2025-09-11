/*
* 图片上传安全检测
* by shengjing19
* create 2025/07/25
* last modify
* v1.0
* */
package com.AnimeRecodeSecurity.upload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PicUploadCheck {

    //允许的文件扩展名--白名单
    private static final Set<String> allowed_extensions = new HashSet<String>(Arrays.asList(
            "jpg","png"
    ));

    //允许的文件头签名
    private static final byte[][] allowed_Signature ={
            // JPEG: FF D8 FF
            {(byte) 0xFF, (byte) 0xD8 ,(byte) 0xFF},
            // PNG: 89 50 4E 47 0D 0A 1A 0A
            {(byte) 0x89, (byte) 0x50 ,(byte) 0x4E,(byte) 0x47,(byte) 0x0D,(byte) 0x0A,(byte) 0x1A ,(byte) 0x0A}
    };

    //最大文件大小
    private static final long max_file_size=2 * 1024 * 1024;

    /**
     * 检查上传的图片文件安全性
     * @param picfile 上传文件的完整路径
     * @return 是否通过安全检查
     */
    public boolean checkpic(String picfile) throws IOException {
        File file = new File(picfile);

        //基本检测 文件存在与文件大小
        if(!file.exists()){ return false; }
        if(file.length() > max_file_size){ return false; }

        //扩展名检查
        String extension =getFileExtension(file.getName());
        if(!is_allowed_extensions(extension)){ return false; }

        //为FileInputStream加一个数据缓冲，为以后如果还需要读取文件其他部分做预留，现在仅仅读取文件头，不能体现出数据缓冲作用
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            // 文件头签名验证（防止伪扩展名）
            if (!verifySignature(bis)) return false;
            //if(containsScriptTags(bis)) return false;
        }

        return true;
    }

    //获取文件后缀名
    private String getFileExtension(String filename){
        int i = filename.lastIndexOf('.'); //获取‘.’最后出现在什么位置 从0开始
        if(i>0&&i<filename.length()-1)
        {
            return filename.substring(i+1).toLowerCase(); //返回‘.’后面剩余的 并转小写
        }
        return null;
    }

    //检查是否属于白名单
    private boolean is_allowed_extensions(String extension){
        return allowed_extensions.contains(extension); //判断字符串中是否包含白名单中的文件后缀
    }

    //验证文件头签名
    private boolean verifySignature(BufferedInputStream sig) throws IOException {
        // 在流当前位置(即文件开头)设置标记，支持最多8字节的回滚
        sig.mark(8);

        // 读取文件头
        byte[] header = new byte[8];// 创建8字节的字节数组，用于存放被读文件的"前8字节"的文件头
        int read = sig.read(header, 0, 8);
        if (read < 3) return false; //最小长度检查

        // 重置流位置
        sig.reset();

        // 检查所有允许的16进制数
        for (byte[] a_s : allowed_Signature)
        {
            boolean sign = true;
            for (int i = 0; i < a_s.length; i++)
            {
                /*
                * 判断逻辑
                * 先判断通配符，在这里FF为-1
                * 不是通配符再进行逐个匹配*/
                if (a_s[i] != -1 && a_s[i] != header[i])
                {
                    sign = false;
                    break;
                }
            }
            if (sign) return true;
        }

        return false;
    }

    //验证是否包含脚本标签 ---针对非法上传php图片木马
    private boolean containsScriptTags(BufferedInputStream sig) throws IOException{
        byte[] header = new byte[4096];
        int read;
        while ((read=sig.read(header,0,4096))!=-1)
        {
            String contains=new String(header,0,read, StandardCharsets.ISO_8859_1);
            if(contains.contains("<?php")||
            contains.contains("<")||
            contains.contains("<?=")||
            contains.contains("<script")||
            contains.contains("eval("))
            {
                return true;
            }
        }
        return false;
    }
}
