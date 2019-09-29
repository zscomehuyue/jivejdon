# run docker 

* 创建images 

sudo docker build -t jive:mysql . 
> * 并在当前的目录,并copy mysql/sql文件执行初始化；

## run msyql 并进入os    ubuntu

*  docker run -ti  --name jive1.0   jive:mysql bash 

### 安装软件
sudo apt-get update

* apt安装

  * 普通安装： sudo apt-get install 软件名
  * 修复安装： sudo apt-get -f install 软件名
  * 重新安装： sudo apt-get --rreinstall install 软件名
  
  