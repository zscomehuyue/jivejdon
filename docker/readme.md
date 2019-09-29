# run docker 

* 创建images 

sudo docker build -t jive:mysql . 
> * 并在当前的目录,并copy mysql/sql文件执行初始化；

## run msyql 并进入os    ubuntu

*  docker run -ti  --name jive1.0   jive:mysql bash 


## docker 操作
* 提交镜像： 

sudo docker commit -m "My network exercise" Exercise net:v1.0
* 说明
    * 把名称为Exercise 保存为新的镜像 net:v1.0 ； 

* 进入docker
docker exec -ti id/name bash


### 安装软件
sudo apt-get update

* apt安装

  * 普通安装： sudo apt-get install 软件名
  * 修复安装： sudo apt-get -f install 软件名
  * 重新安装： sudo apt-get --rreinstall install 软件名
  
* 软件 
  * apt-get install vim
  * apt-get install net-tools
  * apt install iputils-ping 
  * apt install openssh-server
  * apt install openssh-client
  * apt install psmisc
  
  
  
  
  
  
  * apt install apache2
  * apt install apache2-utils
    