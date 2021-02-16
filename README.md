
自用的spire脚本,用于自动配置ssh服务器

# 使用方法
-  安装[spire](https://github.com/epiccastle/spire)
  
-  创建 ~/.private_conf.edn 配置文件，格式如下:
```clojure
{
 :default-ssh-server :server ;;默认的ssh服务器配置名,必须配置
}
```
  :default-ssh-server 用于指定要操作的ssh服务器名，此配置从\~/.ssh/config文件中读取。
  比如下面的\~/.ssh/config配置:
```conf 
Host server
  HostName server.ip
  Port 22
  User root
  IdentityFile ~/.ssh/id_rsa
```
  使用私钥登录，服务器配置名为:server
  
-  clone本项目:
```sh 
  git clone https://github.com/ntestoc3/ssh-scripts
  
  cd ssh-scripts
```

-  执行对应的脚本文件,对:default-ssh-server指定的服务器进行配置

  下面的所有配置项都保存在\~/.private_conf.edn文件中,完整配置如下:
```clojure
{
 ;; 默认操作的服务器项
 :default-ssh-server :server

 ;; burp collaborator配置
 :email "my@test.com"
 :burp-domain "colla.xyz"
 :burp-metric-path "32i42OHIOUFE"

 ;; norecon nowx配置
 :wxpusher-token "AT_xxx"

 ;; hftp配置
 :hftp-domain "www.hftp.xxx"
 :hftp-port 81
  
 ;; XSS-Catch配置
 :xss-catch-domain "www.xss.xxx"
 }
```

  各个脚本相关的配置与要求见对应部分
  
## norecon 
 安装norecon与所有依赖工具，和browserless docker的配置，需要提供的配置项:
```clojure 
{
 :wxpusher-token "AT_xxx" ;; nowx使用的token
}
```

  安装:
```sh
./norecon.clj 
```
   
  显示已安装工具的版本号:
```sh 
./norecon.clj info
```

## XSS Catcher
  安装并配置XSS Catcher, 添加如下配置:
```clojure 
{
 :xss-catch-domain "XSS Catcher使用的域名"
}
```
  保存域名的证书为\~/keys/domain.crt, 私钥保存为\~/keys/domain.key.

  安装:
```sh 
./xsscatch.clj
```
   支持的参数:
   - 创建并启动(默认): up
   - 重新创建: recreate
   - 同docker-compose: start stop logs ps

## burp collaborator server
  配置私有的burp collaborator服务器，手动步骤参考文章[self-hosted-burp-collaborator-with-custom-domain](https://teamrot.fi/self-hosted-burp-collaborator-with-custom-domain/)
  
- 购买云主机，需要开放的端口:53, 80, 443, 25, 587, 465, 19090, 19443

  这些端口不能被占用。 

  并且云主机的公网ip必须有对应的主机名可以解析(域名的ns记录不能设置成ip), 

  GoDaddy可以直接配置，参考上面的文章。
  
- 购买域名, 修改域名的ns指向

- 添加配置项到 ~/.private_conf.edn
```clojure 
{
 :email "my@test.com" ;;用于注册证书时使用(burp collaborator)
 :burp-domain "colla.xyz" ;; burp collaborator服务器域名
 :burp-metric-path "32i42OHIOUFE" ;; burp collaborator metric路径
}
```

- 安装并启动:
```sh 
./burp_collaborator.clj 
```
   支持的参数:
   - 创建并启动(默认): up
   - 同systemctl: stop start status restart

## hftp
   简单的http服务器，用于提供文件下载,需要提供配置项:
```clojure 
{
 :hftp-domain "htfp 使用的域名"
 :hftp-port 81 ;; hftp使用的端口
}
```
   安装并启动:
```sh 
spire hftp.clj up
```
   支持的参数:
   - 创建并启动(默认): up
   - 重新创建: recreate
   - 同docker-compose: start stop logs ps

   
