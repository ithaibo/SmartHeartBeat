# SmartHeartBeat" 

基于[《Android微信智能心跳方案》](https://mp.weixin.qq.com/s?__biz=MzAwNDY1ODY2OQ==&mid=207243549&idx=1&sn=4ebe4beb8123f1b5ab58810ac8bc5994)的实现。

## 简介
在App开发中，如果需要使用TCP协议进行数据传输时，由于各种网络不稳定性，通常会造成TCP连接断开。
解决该问题的通用方案就是建立心跳机制（即，在业务传输之余，每隔一定时间向连接的另一方发送约定的短数据）。

然而，移动设备4G、3G、2G，Wifi等各种网络，可能会出现随时切换；同时，考虑到移动设备的耗电问题，
需要能够在不同的网络状态下找出尽可能的心跳interval。

该项目使用了[Apatch mina](http://mina.apache.org/)。

心跳Interval试探实现在[NatIntervalService](mina_push/src/main/java/com/andy/mina_push/nat/NatIntervalService.java)

## Skill stack
 - Service
 - AlarmManager
 - Apache MINA

## 业务简介
 - 使用Apache MINA建立TCP通道
    Apache MINA基于NIO实现，封装良好
    
 - 设计了心跳间隔探测算法
    通过AlarmManager设置定时任务，启动NatIntervalService执行心跳最大间隔探测算法。