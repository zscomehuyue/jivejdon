# run docker 

* run centos for mysql
   * docker run  -ti --name m4 -e MYSQL_USER=test -e MYSQL_PASSWORD=test -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=db  -p 3306:3306 centos/mysql-57-centos7

* run and rm when stop 
   * docker run --rm -ti --name m4 -e MYSQL_USER=test -e MYSQL_PASSWORD=test -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=db  -p 3306:3306 centos/mysql-57-centos7

* run tomcat8 
   * docker run --rm  -ti -v /worker/java:/tools -p8080:8080 cos7 bash 
   * docker run --rm  -ti -v /worker/java:/tools -p8080:8080 centos7 bash 
   * 通过本机ip地址访问：localhost:8080 







# 容器root权限

## root login 
第一步：查看容器的CONTAINER ID
docker ps
第二步：获取root权限，例如需要进入的CONTAINER ID为4650e8d1bcca
docker exec -ti -u root 4650e8d1bcca bash

## root/zscome

