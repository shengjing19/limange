#!/bin/bash

# 测试路径 /xp/server/tomcat/apache-tomcat-9.0.98/webapps/
# limange(AnimeRecode) 自动化下载与部署脚本
# create in 2025/10/30
# shengjing19
# v1

#init
initVar() {
	echoType='echo -e'
	version='0.0.4(Dev6.1)'				# 软件版本
	URL='https://raw.githubusercontent.com/shengjing19/limange/refs/heads/master/limange_0.0.4Dev6.1.tar.gz'								# 下载链接
	Official='http://hisx.3vfree.vip'	# 官方地址
}
initVar
export LANG=en_US.UTF-8

#the color of font
echoContent() {
	case $1 in
	"red")
		# shellcheck disable=SC2154
		${echoType} "\033[31m${printN}$2 \033[0m"
		;;
	"skyBlue")
		${echoType} "\033[1;36m${printN}$2 \033[0m"
		;;
	"green")
		${echoType} "\033[32m${printN}$2 \033[0m"
		;;
	"white")
		${echoType} "\033[37m${printN}$2 \033[0m"
		;;
	"magenta")
		${echoType} "\033[31m${printN}$2 \033[0m"
		;;
	"yellow")
		${echoType} "\033[33m${printN}$2 \033[0m"
		;;
	esac
}


#start
menu() {
	echoContent red "  ___      " 
	echoContent red "/\_ \    __             "
	echoContent red "\//\ \  /\_\    ___ ___      __      ___      __      __  "
	echoContent red "  \ \ \ \/\ \ /' __\` __\`\  /'__\`\  /' _ \`\  /'_\` \  /'__\`/"
	echoContent red "   \_\ \_\ \ \/\ \/\ \/\ \/\ \L\.\_/\ \/\ \/\ \L\ \/\  __/"
	echoContent red "   /\____\\ \_\ \_\ \_\ \_\ \__/.\_\ \_\ \_\ \____ \ \____\\"
	echoContent red "   \/____/ \/_/\/_/\/_/\/_/\/__/\/_/\/_/\/_/\/___L\ \/____/"
	echoContent red "                                              /\____/      "
	echoContent red "                                               \_/__/       v${version}"
	echoContent green "欢迎使用limange(动漫记录)Web应用程序"
	echoContent green "该自动化脚本(V1)会一键化为您部署\n"
	echoContent green "请确保已安装以下环境"
	echoContent green "1.Java v23及以上"
	echoContent green "2.Mysql v5.7及以上"
	echoContent green "3.Tomcat v9.0.91及以上"
	echoContent green "官方网站:${Official}\n"
	echoContent white "Press 1 : 安装与运行 limange(Anime Recode)"
	echoContent white "Press 0 : 退出"
	echoContent white "----------"

	while [ 1 ]; do
		read -r -p "[!]请选择: " selectMenuType

		case ${selectMenuType} in
		1):
			serverInstall
			;;
		0)
			exitInstall
			;;
		*)
			continue
			;;
		esac
		break
	done
}

serverInstall() {
	cd /opt
	echoContent yellow "[!]正在下载limange文件包"
	if [ $(uname -s) = 'Linux' ] && [ $(uname -m) = 'x86_64' ] && [ $(getconf LONG_BIT) = '64' ]; then
		curl -k ${URL} -o limange-${version}-linux-amd64.tgz
	else
		# No OS version is detected. Please refer to http://hisx.3vfree.vip for manual installation
		echoContent red "[-]未检测到操作系统版本。请访问${Official}进行手动安装\n" && exit 1
	fi

	if [ ! -f "/opt/limange-${version}-linux-amd64.tgz" ]; then
    	
        echoContent red "[-]出现错误:下载失败,请检查curl报错信息"
		exitInstall
	else
		echoContent green "[+]下载成功！!"
	fi

	# 获取tomcat webapps目录路径
	echoContent yellow "[!]请输入tomcat webapps文件夹路径" 
	read -p "[!]请输入目标路径:" path
	if [ ! -d "$path" ]; then
    	echoContent red "[-]错误：路径 $path 不存在或不是一个目录"
    	exitInstall
	fi

	# 获取tomcat服务端口号
	echoContent yellow "[!]请输入tomcat 端口号" 
	echoContent yellow "[!]如不知请在tomcat路径下的conf文件夹下的server.xml中查找,位于<Connector>标签中的port属性" 
	read -p "请输入:" tom_port
	
	# 解压已下载的limange文件包
	echoContent yellow "[!]正在向$path 目录下解包"
	tar -zxf /opt/limange-${version}*.tgz -C $path 
	if [ ! -d "$path/limange" ]; then
    	echoContent red "[-]出现错误:无法检测limange是否安装完成,请前往$path/limange下查看"
	else
		echoContent green "[+]已完成解包"
	fi

	# 移除下载的limange文件包
	echoContent yellow "[!]正在移除下载的limange文件包 limange-${version}-linux-amd64.tgz"
	if [ -f "/opt/limange-${version}-linux-amd64.tgz" ]; then
    	if rm -f "/opt/limange-${version}-linux-amd64.tgz"; then
        	echoContent green "[+]成功删除:limange-${version}-linux-amd64.tgz"
    	else
        	echoContent red "[-]出现错误:移除失败,请手动前往/opt/下删除"
    	fi
	else
    	echoContent yellow "[!]文件不存在，无需删除"
	fi

	successfullyInstall
}

successfullyInstall() {
	echoContent green "[+]已完成自动化部署,请手动重启tomcat服务\n"
	echoContent yellow "[!]如遇“无法访问此网站”请检查端口号是否正确\n"
	ip=$(ip route get 1.2.3.4 | awk '{print $7}' | head -1)
	echoContent green "******************WELCOME!************************"
	echoContent green "初始访问地址:http://$ip:$tom_port/limange/install.jsp"
	echoContent green "后续访问地址:http://$ip:$tom_port/limange/index.jsp"
	echoContent green "*************************************************"
	echoContent green "\n尽情享用吧!!"
	echoContent green "--the shengjing19 "
}

exitInstall() {
	exit 1
}

cd /opt
menu
