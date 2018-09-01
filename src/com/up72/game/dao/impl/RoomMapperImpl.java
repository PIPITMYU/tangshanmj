package com.up72.game.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dao.ClubInfoMapper;
import com.up72.game.dao.RoomMapper;
import com.up72.game.dao.UserMapper;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.model.Room;
import com.up72.server.mina.utils.MyBatisUtils;

/**
 * Created by admin on 2017/6/23.
 */
public class RoomMapperImpl implements RoomMapper {

    @Override
    public void insert(Room entity) {
        System.out.println("保存房间信息");
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = RoomMapper.class.getName() + ".insert";
                session.insert(sqlName, entity);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            System.out.println("insert room数据库操作出错！");
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

   

    @Override
    public List<Map<String, Object>> getMyCreateRoom(Long userId,Integer start,Integer limit,Integer roomType) {
    	 List<Map<String, Object>> result = new ArrayList<>();
         SqlSession session = MyBatisUtils.getSession();
         try {
             if (session != null){
                 String sqlName = RoomMapper.class.getName()+".getMyCreateRoom";
                 System.out.println("sql name ==>>" + sqlName);
                 Map<Object, Object> map =new HashMap<>();
                 map.put("userId",userId);
                 map.put("start",start);
                 map.put("limit",limit);
                 map.put("roomType",roomType);
                 result = session.selectList(sqlName,map);
                 session.close();
             }
         }catch (Exception e){
             System.out.println("getMyCreateRoom数据库操作出错！");
             e.printStackTrace();
         }finally {
             session.close();
         }
         return result;
    }
    
    @Override
    public Integer getMyCreateRoomTotal(Long userId, Integer start,
    		Integer limit, Integer roomType) {
    	Integer result = 0;
    	SqlSession session = MyBatisUtils.getSession();
            try {
                if (session != null){
                    String sqlName = RoomMapper.class.getName()+".getMyCreateRoomTotal";
                    System.out.println("sql name ==>>" + sqlName);
                    Map<Object, Object> map =new HashMap<>();
                    map.put("userId",userId);
                    map.put("start",start);
                    map.put("limit",limit);
                    map.put("roomType",roomType);
                    result = session.selectOne(sqlName,map);
                    session.close();
                }
            }catch (Exception e){
                System.out.println("getMyCreateRoomTotal数据库操作出错！");
                e.printStackTrace();
            }finally {
                session.close();
            }
            return result;
    }

	@Override
	public HashMap<String, Object> findServerIpAndXiaoJu(Integer roomId,Long time) {
		HashMap<String, Object> serverIp = new HashMap<String, Object>();
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = RoomMapper.class.getName()+".findServerIpAndXiaoJu";
                System.out.println("sql name ==>>" + sqlName);
                HashMap<String,Object> map = new HashMap<String, Object>();
                map.put("roomId", roomId);
                map.put("time", time);
                serverIp = session.selectOne(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("findServerIp数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return serverIp;
	}

	@Override
	public void updateRoomState(Integer roomId,Long time,Integer xiaoJuNum) {
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = RoomMapper.class.getName() + ".updateRoomState";
                Map<Object, Object> map =new HashMap<>();
                map.put("roomId",roomId);
                map.put("time", time);
                map.put("xiaoJuNum", xiaoJuNum);
                session.update(sqlName,map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            System.out.println("数据库操作出错！");
        } finally {
            session.close();
        }
	}

}
