package com.up72.server.mina.main;

import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.tcp.MinaTCPServer;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.DataLoader;
import com.up72.server.mina.utils.TaskUtil;
import com.up72.server.mina.utils.redis.MyRedis;

public class MinaServerManager {

    public static MinaTCPServer tcpServer;
//    protected MinaHttpServer httpServer;

    public MinaServerManager() {
        tcpServer = new MinaTCPServer();
//        httpServer = new MinaHttpServer();
    }

    public void startMinaTCPServer() {
        tcpServer.startServer();
    }

    public void startMinaHTTPServer() {
        //httpServer.startServer();
    }

    public void stopMinaTCPServer() {
        tcpServer.stopServer();
    }

    public void stopMinaHTTPServer() {
        //httpServer.stopServer();
    }

    public void broadcastMessage2TCPClient(Object message) {
        tcpServer.broadcast(message);
    }

    public void startMinaServer() {
        DataLoader.initMybatis();
        //DataLoader.loadInitData();

        //初始化redis
        MyRedis.initRedis();
      	//清理回放文件，避免服务器停服之后，json文件不完整
        
        //初始化俱乐部数据
        TCPGameFunctions.initClubList();
        //初始化支付订单号
//        TCPGameFunctions.initPayList();
        
        //删除所有回放文件
      	BackFileUtil.deleteAllRecord();
        
        tcpServer.startServer();
        //启动定时任务
        TaskUtil.initTaskSchdual();
//        httpServer.startServer();
    }

    public void stopMinaServer() {
        tcpServer.stopServer();
        //httpServer.startServer();
    }

    public static void main(String[] args) {
        MinaServerManager minaManager = new MinaServerManager();
        minaManager.startMinaServer();
    }
    
}
