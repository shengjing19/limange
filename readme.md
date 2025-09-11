# limange(Anime Recode)

limange是一个用于记录你所看完的动漫的信息的Web应用程序


## 展示图片

|                           1                            |                           2                           |
| :----------------------------------------------------: | :---------------------------------------------------: |
| <img width="1624" alt="Light Theme" src="./IMG/1.png"> | <img width="1624" alt="Dark Theme" src="./IMG/2.png"> |
| <img width="1624" alt="Light Theme" src="./IMG/3.png"> | <img width="1624" alt="Dark Theme" src="./IMG/4.png"> |
| <img width="1624" alt="Light Theme" src="./IMG/5.png"> | <img width="1624" alt="Light Theme" src="./IMG/6.png">|

## 结构及部分介绍

1. 目录结构

   ```
   .
   └── limange/
       ├── .idea
       ├── src
           └── com
               └── AnimeRecode
                   └── dao
                   └── model
                   └── Server
                   └── servlet
               └── AnimeRecodeConfig
               └── AnimeRecodeSecurity
                   └── session
                   └── upload
               └── google
     ├── web
         └── css
         └── fontawesome6.4.0
         └── img
         └── js
         └── WEB-INF
         └── admin.jsp
         └── index.jsp
         └── install.jsp
         └── login.jsp
         └── noaccess.html
         └── recovery.jsp
   ```

2. AnimeRecodeSecurity提供了一些基本的安全服务，如授权访问检测(未登录情况下禁止任何接口调用形式)、图片上传检测、id参值检测

3. 后端： tomcat+servlet
   前端： html+css+js


## 注意

1.本Web程序暂没有提供注册也没有做针对某个用户进行sql查询动漫信息(后续可能会做这个分支)，目前是单用户
2.本Web程序的前端UI界面仅支持电脑端，移动端未进行前端适配
3.本Web程序的前端交互与接口调用依赖于js，使用AJAX进行前后端分离，除部分页面逻辑需要Java控制外，其余主要数据获取与修改均已前后端分离(install.jsp除外，install.jsp完全独立)
4.使用IDEA编写，提供的编译版本请使用JDK-23版本运行

