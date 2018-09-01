package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leo.rms.utils.StringUtils;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.dto.resp.ClubUser;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.service.IClubGamePlayRecordService;
import com.up72.game.service.IClubInfoService;
import com.up72.game.service.IClubUserService;
import com.up72.game.service.IClubUserUseService;
import com.up72.game.service.IUserService;
import com.up72.game.service.impl.ClubGamePlayRecordServiceImpl;
import com.up72.game.service.impl.ClubInfoServiceImpl;
import com.up72.game.service.impl.ClubUserServiceImpl;
import com.up72.game.service.impl.ClubUserUseServiceImpl;
import com.up72.game.service.impl.UserServiceImpl;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.utils.CommonUtil;

/**
 * 俱乐部
 */

public class ClubInfoFunctions extends TCPGameFunctions {
	
	public static IClubInfoService clubInfoService = new ClubInfoServiceImpl();
	public static IClubUserService clubUserService = new ClubUserServiceImpl();
	public static IClubUserUseService clubUserUseService = new ClubUserUseServiceImpl();
	public static IClubGamePlayRecordService clubGamePlayRecordService = new ClubGamePlayRecordServiceImpl();
	public static IUserService userService = new UserServiceImpl();
	
    /**
     * 扫描二维码查询俱乐部
     * "clubId":"俱乐部id",
     * userId：玩家id
     */
    public static void interface_500001(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 500001");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
        Object clubId = obj.get("clubId");
        
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(clubId.toString()));
		
        Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
        if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }
        Map<String,Object> info = new HashMap<>();
        //通过clubId从redis中获取俱乐部信息
        ClubInfo redisClub = getClubInfoByClubId(clubId.toString());
        if(null == redisClub){//如果为空 从数据库查询
        	redisClub = clubInfoService.selectByClubId(StringUtils.parseInt(clubId));//根据俱乐部id查询
        	//保存到redis
        	setClubInfoByClubId(clubId.toString(), redisClub);
        }
        if(null != redisClub){
	        info.put("clubId", redisClub.getClubId());
	        info.put("name", redisClub.getClubName());
	        info.put("user", clubInfoService.selectCreateName(Integer.valueOf(redisClub.getCreateId()+"")));
	        info.put("num", clubUserService.allUsers(redisClub.getClubId()));
	        info.put("ct", redisClub.getCreateTime());
	        info.put("total", redisClub.getPersonQuota());
        }
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        System.out.println("扫描二维码查询俱乐部成功**********************");
    }
    /**
     * 查询我的俱乐部
     */
    public static void interface_500002(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 500002");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
        
        isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));
		Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
        if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }
        
        List<Map<String,Object>> listInfo = new ArrayList<Map<String,Object>>();
        List<ClubUser> list = clubUserService.selectClubByUserId(StringUtils.parseLong(userId));//查询我加入的俱乐部信息
        if (list!=null && list.size()>0){
	        for(int a =0;a<list.size();a++){
	        	Map<String,Object> info = new HashMap<>();
	            //通过clubId从redis中获取俱乐部信息
	            ClubInfo redisClub = getClubInfoByClubId(list.get(a).getClubId().toString());
	            if(null == redisClub){//如果为空 从数据库查询
	            	redisClub = clubInfoService.selectByClubId(list.get(a).getClubId());//根据俱乐部id查询
	            	//保存到redis
	            	setClubInfoByClubId(list.get(a).getClubId().toString(), redisClub);
	            }
	            if(null != redisClub){
	            	info.put("clubId", redisClub.getClubId());
		            info.put("user", clubInfoService.selectCreateName(Integer.valueOf(redisClub.getCreateId()+"")));
		            info.put("name", redisClub.getClubName());
		            info.put("total", redisClub.getPersonQuota());
	            }
	            info.put("num", clubUserService.allUsers(redisClub.getClubId()));
	            listInfo.add(info);
	        }
        }
        JSONObject result = getJSONObj(interfaceId,1,listInfo);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        System.out.println("查询我的俱乐部成功**********************");
    }
    /**
     * 申请加入俱乐部
     */
    public static void interface_500000(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 500000");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
        Object clubId = obj.get("clubId");
        
        isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(clubId.toString()));
		Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }
        
        Map<String,Object> info = new HashMap<>();
        ClubUser user = clubUserService.selectUserByUserIdAndClubId(StringUtils.parseLong(userId), StringUtils.parseInt(clubId));
        if(null != user){//
        	info.put("reqState", 3);
        }else{
        	
        	Integer count = clubUserService.countByUserId(StringUtils.parseLong(userId));//查询我加入的俱乐部
            if(null != count && count >= 3){//如果加入的大于3个
            	info.put("reqState", 2);
            }else{
            	ClubUser clubUser = new ClubUser();
                clubUser.setUserId(StringUtils.parseLong(userId));
                clubUser.setClubId(StringUtils.parseInt(clubId));
                clubUser.setStatus(0);//默认申请中
                clubUser.setCreateTime(new Date().getTime());//申请时间
                clubUserService.insert(clubUser);//保存
                info.put("reqState", 1);
            }
        }
        
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        System.out.println("申请加入俱乐部成功**********************");
    }
    /**
     * 申请离开俱乐部
     */
    public static void interface_500007(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 500007");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
        Object clubId = obj.get("clubId");
        
        isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(clubId.toString()));
		Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }
        
        Map<String,Object> info = new HashMap<>();
        //根据用户id 和通过状态 查询
    	ClubUser clubUser = clubUserService.selectUserByUserIdAndClubId(StringUtils.parseLong(userId), StringUtils.parseInt(clubId));
    	if(null != clubUser){
    		if(clubUser.getStatus() == 1){
    			info.put("reqState", 1);
				info.put("exState",1);
    			clubUser.setStatus(2);//状态 状态 0申请加入 1已通过 2申请退出
            	clubUserService.updateById(clubUser);//修改保存记录
    		}else if(clubUser.getStatus() == 2){
    			info.put("reqState", 2);
				info.put("exState",1);
    		}
    	}
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        System.out.println("申请离开俱乐部成功**********************");
    }
    /**
     * 查询俱乐部详情
     */
    public static void interface_500003(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 500003");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
        Object clubId = obj.get("clubId");
        
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(clubId.toString()));
		Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }
        
        Map<String,Object> info = new HashMap<>();
        //根据用户id 和通过状态 查询
        ClubInfo clubInfo = getClubInfoByClubId(clubId.toString());
        if(null == clubInfo){//如果为空 从数据库查询
        	clubInfo = clubInfoService.selectByClubId(StringUtils.parseInt(clubId));//根据俱乐部id查询
        	//保存到redis
        	setClubInfoByClubId(clubId.toString(), clubInfo);
        }
        if(null != clubInfo){
        	 info.put("clubId", clubInfo.getClubId());
             info.put("user", clubInfoService.selectCreateName(Integer.valueOf(clubInfo.getCreateId()+"")));
             info.put("mlast", clubInfo.getRoomCardNum());
             info.put("need", clubInfo.getRoomCardQuota());
        }
        
        //根据俱乐部id和时间 查询消费房卡数
        Integer used = clubUserUseService.sumMoneyByClubIdAndDate(DateUtil.formatDate(new Date(), "yyyy-MM-dd"), StringUtils.parseInt(clubId));
        info.put("used", used==null?0:used);
        //根据俱乐部id和时间查询开局数
        Integer juNum = clubUserUseService.countJuNumByClubIdAndDate(DateUtil.formatDate(new Date(), "yyyy-MM-dd"), StringUtils.parseInt(clubId));
        info.put("juNum", juNum==null?0:juNum);
        //根据俱乐部id和时间查询 活跃牌友数
        Integer actNum = clubGamePlayRecordService.countActNumByClubIdAndDate(DateUtil.formatDate(new Date(), "yyyy-MM-dd"), StringUtils.parseInt(clubId));
        info.put("actNum", actNum==null?0:actNum);
        //根据俱乐部id和userid查询当前状态
        Integer exState = clubUserService.selectUserState(StringUtils.parseInt(clubId), StringUtils.parseLong(userId));
       
        //俱乐部页面刷新 此时管理员已同意退出  
        if(exState==null){
        	info.put("reqState", 0);
        }else{
        	info.put("exState", exState==2?1:0);
        }      
        /**************************未开局的房间数**********************************/
        JSONArray jsonArray = getClubRoomListByClubId(clubId.toString());
        JSONArray jsonArrayInfo = new JSONArray();
        if (jsonArray!=null && jsonArray.size()>0){
	        for(int a = 0;a<jsonArray.size();a++){
	        	
	        	RoomResp room1 = JSONObject.parseObject(jsonArray.get(a).toString(), RoomResp.class);
	        	RoomResp room = TCPGameFunctions.getRoomRespByRoomId(room1.getRoomId()+"");
//	         	if(room.getRoomId().equals(4705160) || room.getRoomId().equals(2991722) || room.getRoomId().equals(4397103)){
//	    			jsonArray.remove(a);
//	    			setClubRoomListByClubId(room.getClubId().toString(), jsonArray);//更新redis数据
//	    		}
	        	JSONObject jsobj = new JSONObject();
	        	JSONObject roomobj = new JSONObject();
	        	jsobj.put("rId", room.getRoomId());
	        	Player play = userService.isExistUserId(room.getCreateId());//查询用户信息
	        	jsobj.put("cname", play.getUserName());
	        	jsobj.put("cimg", play.getUserImg());
//	        	jsobj.put("gtype", room.getGlobalType());
	        	int num = 0;
	        	for(int i =0;i<room.getPlayerIds().length;i++){
	        	       if(room.getPlayerIds()[i] != null){
	        	    	   num++; 
	        	       }
	        	}
	        	jsobj.put("num", num);//当前人数
//	        	if(room.getGlobalType()==1){
		        	roomobj.put("xs", room.getXiaoSa());//0/1(潇洒，默认勾选)
		        	roomobj.put("lf", room.getLiangFeng());//0/1（亮风）
		        	roomobj.put("dh", room.getDaHun());//0/1（打混）
		        	roomobj.put("siHun", room.getSiHunHu());//0/1（四混胡牌）
		        	roomobj.put("dt", room.getDianType());//点炮赔法，1点炮大包；2点炮陪三家；3点炮三家付
		        	roomobj.put("ms", room.getMaxScoreInRoom());//封顶分，固定值0（不封顶） 64 128 256；
//	        	}
//	        	if(room.getGlobalType()==2){
//	        		roomobj.put("xt", room.getXiType());//撸麻巧房间
//	        	}
	        	jsobj.put("rule", roomobj);
	        	
	        	jsonArrayInfo.add(jsobj);
	        }
        }
        info.put("rooms", jsonArrayInfo);
        /**************************未开局的房间数结束**********************************/
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        System.out.println("查询俱乐部详情成功**********************");
    }
    /**
     * 查询我的战绩
     */
    public static void interface_500006(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 500006");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
        Object clubId = obj.get("clubId");
        Object page = obj.get("page");
        Object st = obj.get("st");
        Object et = obj.get("et");
        
        isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(clubId.toString()));
		
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(page.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(page.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(st.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(et.toString()));
		
		if(st.toString().length() != 13){
			illegalRequest(interfaceId,session);
            return;
		}
		if(et.toString().length() != 13){
			illegalRequest(interfaceId,session);
            return;
		}
		
		Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }
        
        //根据俱乐部id，人员id和时间查询 总局数
        Integer juNum = clubGamePlayRecordService.countJuNumByClubIdAndDateAndUserId(DateUtil.formatDate(new Date(), "yyyy-MM-dd"), 
        			StringUtils.parseInt(clubId), StringUtils.parseLong(userId));
        
        //根据俱乐部id，人员id和时间查询 其参与的 战绩 分页
        List<PlayerRecord> list = clubGamePlayRecordService.findPlayerRecordByUserId(StringUtils.parseLong(userId),
        			(StringUtils.parseInt(page)-1)*Cnst.PAGE_SIZE,Cnst.PAGE_SIZE, StringUtils.parseInt(clubId),
        			 StringUtils.parseLong(st), StringUtils.parseLong(et));
        //根据俱乐部id，人员id和时间查询 总分数
        Integer score = clubGamePlayRecordService.findScoreByUserIdAndClubId(StringUtils.parseLong(userId), 
        			StringUtils.parseInt(clubId), DateUtil.formatDate(new Date(), "yyyy-MM-dd"));
        
        Map<String,Object> info = new HashMap<>();
        info.put("pages", "");
        info.put("juNum", juNum==null?0:juNum);
        info.put("score", score==null?0:score);
        JSONArray jsonArrayInfo = new JSONArray();
        if (list!=null&&list.size()>0){
	        for(PlayerRecord play : list){
	        	JSONObject recordobj = new JSONObject();
	        	JSONObject jsobj = new JSONObject();
	        	recordobj.put("rId", play.getRoomId());
	        	recordobj.put("ct", play.getStartTime());
	        	
	        	jsobj.put(play.getEastUserName(), String.valueOf(play.getEastUserMoneyRecord()));
	        	jsobj.put(play.getSouthUserName(), String.valueOf(play.getSouthUserMoneyRecord()));
	        	jsobj.put(play.getWestUserName(),String.valueOf(play.getWestUserMoneyRecord()));
	        	jsobj.put(play.getNorthUserName(),String.valueOf(play.getNorthUserMoneyRecord()));
	        	
	        	recordobj.put("users", jsobj);
	        	jsonArrayInfo.add(recordobj);
	        }
        }
        info.put("record", jsonArrayInfo);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        System.out.println("查询我的战绩成功**********************");
    }
    /**
     * 经典创建房间
     */
	public static void interface_500004(IoSession session, ProtocolData readData) {
		logger.I("创建房间,interfaceId -> 500004");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Object userId = obj.get("userId");
        Object clubId = obj.get("clubId");
        Object circleNum = obj.get("circleNum");
        Object roomType = obj.get("roomType");
//        Object globalType = obj.get("globalType");
//        Integer globalType = obj.getInteger("globalType");
        
        JSONObject playRule = JSONObject.parseObject(obj.getString("playRule"));
        
        isParameterError(interfaceId, session, StringUtils.isNotEmpty(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(userId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(clubId.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(clubId.toString()));
		
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(circleNum.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(circleNum.toString()));
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(roomType.toString()));
		isParameterError(interfaceId, session, StringUtils.isNumeric(roomType.toString()));
		isParameterError(interfaceId, session, StringUtils.isNotEmpty(playRule.toString()));
		Long sessionUserId = (Long)session.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (!sessionUserId.equals(StringUtils.parseLong(userId))){//不相同
            illegalRequest(interfaceId,session);
            return;
        }

        //从redis中 拿到存放俱乐部未开局房间信息的
        JSONArray jsonArray = getClubRoomListByClubId(clubId.toString());
        if(null !=  jsonArray && jsonArray.size() > 0){//未开满的房间数不能超过5个
        	if(jsonArray.size() >= 5){
        		JSONObject result = getJSONObj(interfaceId,0,null);
                ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
                session.write(pd);
                return;
        	}
        }else{
        	jsonArray = new JSONArray();
        }
        
        

        Player p = getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)));
         
        //通过clubId从redis中获取俱乐部信息
        ClubInfo redisClub = getClubInfoByClubId(clubId.toString());
        //超过每日限额
        int max = redisClub.getRoomCardQuota();
        Integer todayUse = clubUserUseService.todayUse(StringUtils.parseInt(clubId), StringUtils.parseInt(userId));
        int willUse = Cnst.clubMoneyMap.get(StringUtils.parseInt(circleNum));//获取本局房卡数
        if((todayUse+willUse/4)>max){
        	 JSONObject error = getJSONObj(interfaceId,0,null);
             error.put("message","已达到每日消耗限额，明日再来吧");
             ProtocolData pd = new ProtocolData(interfaceId, error.toJSONString());
             session.write(pd);
             return;
        }
        if(redisClub.getRoomCardNum()<willUse){//俱乐部房卡不足
            playerMoneyNotEnough(interfaceId,session,StringUtils.parseInt(roomType));
            return ;
        }
        if (p.getRoomId()!=null) {//已存在其他房间
			playerExistOtherRoom(interfaceId,session);
			return ;
		}
        String createTime = String.valueOf(new Date().getTime());
        RoomResp room = new RoomResp();
//        if(globalType==1){
	        Integer xiaoSa = playRule.getInteger("xs");
	        Integer liangFeng = playRule.getInteger("lf");
	        Integer daHun = playRule.getInteger("dh");
	        Integer siHunHu = playRule.getInteger("siHun");
	        Integer dianType = playRule.getInteger("dt");
	        Integer maxScore = playRule.getInteger("ms");
	        room.setMaxScoreInRoom(maxScore);//封顶分
	        room.setDianType(dianType);
	        room.setXiaoSa(xiaoSa);
	        room.setLiangFeng(liangFeng);
	        room.setDaHun(daHun);
	        room.setSiHunHu(siHunHu);
//        }
//        if(globalType==2){
//        	Integer xt = playRule.getInteger("xt");
//        	room.setXiType(xt);
//        }
        room.setClubId(StringUtils.parseInt(clubId));//俱乐部id
        room.setCreateId(StringUtils.parseLong(userId));//创建人
        room.setStatus(Cnst.ROOM_STATE_CREATED);//房间状态为等待玩家入坐
        room.setCircleNum(StringUtils.parseInt(circleNum));//房间的总圈数
        room.setLastNum(StringUtils.parseInt(circleNum));//剩余圈数
        room.setCircleWind(Cnst.WIND_EAST);//圈风为东风
        room.setRoomType(StringUtils.parseInt(roomType));//房间类型：房主模式或者自由模式
       
        room.setCreateTime(createTime);//创建时间，long型数据
        room.setOpenName(p.getUserName());
        
    	//初始化大接口的id
    	room.setWsw_sole_action_id(1);
    	room.setWsw_sole_main_id(1);
        
        //toEdit  需要去数据库匹配，查看房间号是否存在，如果存在，则重新生成
        while (true){
            room.setRoomId(CommonUtil.getGivenRamdonNum(7));//设置随机房间密码
            if (getRoomRespByRoomId(String.valueOf(room.getRoomId()))==null) {
				break;
			}
        }
        
        Long[] userIds = new Long[4];
        
        Map<String,Object> info = new HashMap<>();
        //处理开房模式
        if (room.getRoomType()==null) {
        	illegalRequest(interfaceId, session);
		}else if(room.getRoomType().equals(Cnst.ROOM_TYPE_1)){//房主模式
	        //设置用户信息
	        p.setPosition(getWind(null));//设置庄家位置为东
	        if (p.getPosition().equals(Cnst.WIND_EAST)){
	            p.setZhuang(true);
	            room.setZhuangId(StringUtils.parseLong(userId));
	        }else{
	            p.setZhuang(false);
	        }
	        p.setPlayStatus(Cnst.PLAYER_STATE_IN);//进入房间状态
	        p.setRoomId(room.getRoomId());
	        p.setJoinIndex(1);
	        p.initPlayer(p.getRoomId(),p.getPosition(),p.getZhuang(),Cnst.PLAYER_STATE_IN,p.getScore(),p.getHuNum(),p.getLoseNum());
	        userIds[p.getPosition()-1] = p.getUserId();
	        info.put("reqState",Cnst.REQ_STATE_1);
	        info.put("playerNum",1);
	        info.put("roomSn", room.getRoomId());
	        //减去房卡数?应该是开局扣钱吧
//	        redisClub.setRoomCardNum(redisClub.getRoomCardNum()-Cnst.clubMoneyMap.get(room.getCircleNum()));
//	        p.setMoney(p.getMoney()-Cnst.moneyMap.get(room.getCircleNum()));
		}else{
        	illegalRequest(interfaceId, session);
        	return ;
		}
        room.setPlayerIds(userIds);
        room.setIp(Cnst.SERVER_IP);
//        room.setIp(p.getIp());
       
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        
        //更新redis数据
        updateRedisData(room, p);
        //更新俱乐部redis数据
        setClubInfoByClubId(clubId.toString(), redisClub);
        //添加到 存放俱乐部未开局房间信息的redis中
        jsonArray.add(room);
        setClubRoomListByClubId(clubId.toString(), jsonArray);
        //解散房间命令
        startDisRoomTask(room.getRoomId(),Cnst.DIS_ROOM_TYPE_1);
        
        //推送玩家信息
//        MessageFunctions.interface_100100(session,null);
	}
	/**
     * 产生随机的风
     */
    protected static Integer getWind(Long[] userIds){
        List<Integer> ps = new ArrayList<>();
        ps.add(Cnst.WIND_EAST);
        ps.add(Cnst.WIND_SOUTH);
        ps.add(Cnst.WIND_WEST);
        ps.add(Cnst.WIND_NORTH);
        if (userIds!=null){
            for(int i=userIds.length-1;i>=0;i--){
                if (userIds[i]!=null){
                    ps.remove(i);
                }
            }
        }
        return ps.get(CommonUtil.getRamdonInNum(ps.size()));
    }

   
}
