package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.DissolveRoom;
import com.up72.server.mina.bean.InfoCount;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.MahjongUtils;

/**
 * Created by Administrator on 2017/7/13.
 * 游戏中
 */

public class GameFunctions extends TCPGameFunctions {
	
    /**
     * 用户点击准备，用在小结算那里，
     * @param session
     * @param readData
     */
    public static void interface_100200(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 100200");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        Integer roomId = obj.getInteger("roomSn");
        
        RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
        Player currentPlayer = null;
        List<Player> players = getPlayerList(room);
        for(Player p:players){
        	if (p.getUserId().equals(userId)) {
        		currentPlayer = p;
        		break;
			}
        }
        
        if (room.getStatus().equals(Cnst.ROOM_STATE_GAMIING)) {
			return;
		}
        if (currentPlayer==null||currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_PREPARED)) {
			return;
		}

        currentPlayer.initPlayer(currentPlayer.getRoomId(),currentPlayer.getPosition(),currentPlayer.getZhuang(),Cnst.PLAYER_STATE_PREPARED,
        		currentPlayer.getScore(),currentPlayer.getHuNum(),currentPlayer.getLoseNum());
        
       
        boolean allPrepared = true;
        
        for (Player p:players){
            if (!p.getPlayStatus().equals(Cnst.PLAYER_STATE_PREPARED)){
                allPrepared = false;
            }
        }
        
        room.setCurrentMjList(null);

        
        if (allPrepared&&players!=null&&players.size()==4){
            startGame(room,players);
            BackFileUtil.write(null, interfaceId, room,players,null);//写入文件内容
        }

        List<Map<String, Object>> info = new ArrayList<Map<String,Object>>();
        //old
        for(Player p:players){
            Map<String,Object> i = new HashMap<String, Object>();
            i.put("userId", p.getUserId());
            i.put("playStatus",p.getPlayStatus()); 
            info.add(i);
        }
        //old
        //new
//        for(Player p:players){
//        	Map<String,Object> i = new HashMap<String, Object>();
//        	i.put("userId", p.getUserId());
//        	i.put("playStatus",p.getPlayStatus()); 
//        	//如果房间时在创建状态，有人请求准备，而且不是房主请求准备
//        	//代表着有人加入房间，需要把加入人的信息返回
//        	if (room.getStatus().equals(Cnst.ROOM_STATE_CREATED)) {
//				if (p.getUserId().equals(currentPlayer.getUserId())&&!room.getCreateId().equals(currentPlayer.getUserId())) {
//			        getOneUserBaseInfo(i, p);
//				}else{
//					i.put("rinfo", getRoomBaseInfoForStart(room));
//				}
//			}
//        	info.add(i);
//        }
        //new
        
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        
        for(Player p:players){
        	IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
        	if (se!=null&&se.isConnected()) {
				se.write(pd);
			}
        }
        updateRedisData(room, null);
		setPlayersList(players);
    }
    
    /**
     * 根据第一张混排，计算第二个
     * @return
     */
    public static Integer[][] getHunPai(Integer[][] firstHun){
    	Integer[][] anotherHun = null;
    	if ((firstHun[0][1].equals(9))||(firstHun[0][0].equals(4)&&firstHun[0][1].equals(4))||(firstHun[0][0].equals(5)&&firstHun[0][1].equals(3))) {//9万、9条、9桶
    		anotherHun = new Integer[][]{{firstHun[0][0],1}};
		}else{//正常情况，递增即可
			anotherHun = new Integer[][]{{firstHun[0][0],firstHun[0][1]+1}};
		}
    	return anotherHun;
    }

    /**
     * 开局发牌
     * @param roomId
     */
    public static void startGame(RoomResp room,List<Player> players){
        room.setDiXianPaiNum(Cnst.DI_XIAN_PAI);//初始化房间的底线牌
        //关闭解散房间计时任务
        notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_1);
        
        if (room.getXiaoJuNum()==null) {
			room.setXiaoJuNum(1);
		}else{
			room.setXiaoJuNum(room.getXiaoJuNum()+1);
		}
        room.setXiaoJuStartTime(new Date().getTime());
        
        room.setStatus(Cnst.ROOM_STATE_GAMIING);
        room.setCurrentMjList(MahjongUtils.xiPai(MahjongUtils.initMahjongs()));
        //打混
        List<Integer[][]> hunPai = MahjongUtils.faPai(room.getCurrentMjList(),1);
        hunPai.add(getHunPai(hunPai.get(0)));
        room.setHunPai(hunPai);
        
        
        for(Player p:players){
        	if (p.getZhuang()){
            	p.setNeedFaPai(true);
            }else{
            	p.setNeedFaPai(false);
            }
        }
        
        
        for(Player p:players){
        	p.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
			p.setCurrentMjList(MahjongUtils.faPai(room.getCurrentMjList(),13));
            if (p.getZhuang()) {
				p.setZhuangNum(p.getZhuangNum()==null?1:p.getZhuangNum()+1);
			}
        }
        
    }

    /**
     * 出牌
     * @param session
     * @param readData
     */
    public static void interface_100201(IoSession session, ProtocolData readData) throws Exception{
        logger.I("出牌,interfaceId -> 100201");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");

        Integer roomId = obj.getInteger("roomSn");
        Boolean  kouTing = obj.getBoolean("kouTing");
        RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
        Player currentPlayer = getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)));
        

        if (!currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_CHU)) {
        	System.err.println("玩家出牌，当前状态不对！");
			return;
		}
        
        //设置递增id
        Integer wsw_sole_action_id = obj.getInteger("wsw_sole_action_id");
        if (!room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			MessageFunctions.interface_100108(session);
			return ;
		}else{
			room.setWsw_sole_action_id(wsw_sole_action_id+1);
		}
        
		if (!currentPlayer.getKouTing()&&kouTing!=null) {
			currentPlayer.setKouTing(kouTing);
		}
		currentPlayer.setCurrentActions(null);
        
        Long userId = obj.getLong("userId");

        

        List<Player> players = getPlayerList(room);
        for (int m = 0; m < players.size(); m++) {
			Player p = players.get(m);
			if (p.getUserId().equals(currentPlayer.getUserId())) {
				players.set(m, currentPlayer);
				break;
			}
		}

        Integer[][] paiInfo = getIntegerList(obj.getString("paiInfo"));

        List<Player> others = new ArrayList<>();

        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
        room.setLastUserId(userId);
        room.setLastPai(new Integer[][]{{paiInfo[0][0],paiInfo[0][1]}});
        //需要检测出的哪张牌是不是发的那个，如果不是，需要把发的哪张牌加入手牌集合
        if (currentPlayer.getLastFaPai()!=null&&
                paiInfo[0][0].equals(currentPlayer.getLastFaPai()[0][0])&&paiInfo[0][1].equals(currentPlayer.getLastFaPai()[0][1])){
            currentPlayer.getChuList().add(new Integer[][]{{currentPlayer.getLastFaPai()[0][0],currentPlayer.getLastFaPai()[0][1]}});

        }else{
            for(int i=0;i<currentPlayer.getCurrentMjList().size();i++){
                if (currentPlayer.getCurrentMjList().get(i)[0][0].equals(paiInfo[0][0])&&currentPlayer.getCurrentMjList().get(i)[0][1].equals(paiInfo[0][1])){
                    currentPlayer.getChuList().add(currentPlayer.getCurrentMjList().get(i));
                    currentPlayer.getCurrentMjList().remove(i);
                    break;
                }
            }
            if (currentPlayer.getLastFaPai()!=null){
                currentPlayer.getCurrentMjList().add(new Integer[][]{{currentPlayer.getLastFaPai()[0][0],currentPlayer.getLastFaPai()[0][1]}});
                currentPlayer.setCurrentMjList(MahjongUtils.paiXu(currentPlayer.getCurrentMjList()));
            }
        }
        currentPlayer.setLastFaPai(null);
        currentPlayer.setNeedFaPai(false);
        currentPlayer.setChuPaiNum(currentPlayer.getChuPaiNum()==null?1:currentPlayer.getChuPaiNum()+1);
        
        //有玩家出牌，则置空最后碰杠的人
        room.setLastPengGangUser(null);
        Player nextUser = null;
        for(int i=0;i<players.size();i++){
            if (!players.get(i).getUserId().equals(userId)){//非当前用户之外的其他三家
                others.add(players.get(i));
                players.get(i).setHasGang(false);//一个人出牌之后，把其他三家的hasGang字段置false
            }else{
                if (i == players.size()-1){
                    nextUser = players.get(0);
                }else{
                    nextUser = players.get(i+1);
                }
            }
        }

        //计算跟庄
    	boolean hasGen = false;
        if (currentPlayer.getZhuang()) {
			room.setCurCiecleChuPais(new ArrayList<Integer[][]>());
			room.getCurCiecleChuPais().add(new Integer[][]{{paiInfo[0][0],paiInfo[0][1]}});
		}else{
			room.getCurCiecleChuPais().add(new Integer[][]{{paiInfo[0][0],paiInfo[0][1]}});
			if (nextUser.getZhuang()&&room.getCurCiecleChuPais()!=null) {
		        	List<Integer[][]> curCirChus = room.getCurCiecleChuPais();
		            if (curCirChus!=null&&curCirChus.size()==4) {//需要计算上一轮玩家出的牌，计算跟庄
		            	int num = 0;
		    			for(int i=1;i<curCirChus.size();i++){//庄家出的牌在第一个，所以要从第二开始，一次跟庄家的牌对比
		    				if (curCirChus.get(0)[0][0].equals(curCirChus.get(i)[0][0])&&curCirChus.get(0)[0][1].equals(curCirChus.get(i)[0][1])) {
		    					num++;
							}
		    			}
		    			if (num==3) {
		    				hasGen = true;
						}
		    			if (hasGen) {
							room.setGetnZhuangNum(room.getGetnZhuangNum()==null?1:room.getGetnZhuangNum()+1);
							//需要立刻计算跟庄的分
							for(Player p:players){
								if (p.getZhuang()) {
									p.setScore(p.getScore()-Cnst.GEN_ZHUANG_SCORE*3);
								}else{
									p.setScore(p.getScore()+Cnst.GEN_ZHUANG_SCORE);
								}
							}
						}
		    		}
		            //计算完跟庄之后，清空集合，重新加入当前庄出的这张牌
					curCirChus.clear();
					curCirChus.add(new Integer[][]{{paiInfo[0][0],paiInfo[0][1]}});
				}
		        //跟庄计算完
		}
       
        
        //置空过手信息
        currentPlayer.setGuoShouFan(null);
        
        if (!room.getHaiLao()) {
        	 //如果出牌玩家能潇洒，就不检测其他玩家动作了
            if (room.getXiaoSa().equals(Cnst.NO)||(currentPlayer.getXiaoSa()!=null&&currentPlayer.getXiaoSa().equals(Cnst.XIAO_SA_YES))||!checkCurrentXiaoSa(currentPlayer,room)) {
    			checkOthersAction(others, paiInfo, currentPlayer, nextUser, room, players);
    		}
		}
        
        String sss = players.get(0).getNeedFaPai() + "_"
				+ players.get(1).getNeedFaPai() + "_"
				+ players.get(2).getNeedFaPai() + "_"
				+ players.get(3).getNeedFaPai();
		while (true) {
			setPlayersList(players);
			updateRedisData(room, null);
			RoomResp room1 = getRoomRespByRoomId(String.valueOf(roomId));
			List<Player> players1 = getPlayerList(room1);

			String mmm = new StringBuffer()
					.append(players1.get(0).getNeedFaPai())
					.append("_")
					.append(players1.get(1).getNeedFaPai())
					.append("_")
					.append(players1.get(2).getNeedFaPai())
					.append("_")
					.append(players1.get(3).getNeedFaPai()).toString();
			if (!mmm.equals(sss)) {
				continue;
			} else {
				break;
			}
		}
        
        MessageFunctions.interface_100105(userId,paiInfo,roomId,getActionPlayer(players),hasGen,room,players);

    }
    
    private static Boolean checkCurrentXiaoSa(Player currentPlayer,RoomResp room){
    	currentPlayer.setXiaoSa(null);
    	boolean hasXiaoSa = false;
//    	if (MahjongUtils.checkXiaoSa(currentPlayer.getCurrentMjList(),room)&&MahjongUtils.isMenQing(currentPlayer)) {//&&后面的条件是新龟腚，不门清，不能潇洒
    	if (MahjongUtils.checkXiaoSa(currentPlayer.getCurrentMjList(),room)) {//上面的新龟腚作废，这才是新龟腚：不门清也可以潇洒！！！
    		//新龟腚：只要玩家潇洒，可以不受3番起的条件约束
//    		currentPlayer.getCurrentMjList().add(new Integer[][]{{room.getHunPai().get(1)[0][0],room.getHunPai().get(1)[0][1]}});
//    		currentPlayer.setLastFaPai(new Integer[][]{{room.getHunPai().get(1)[0][0],room.getHunPai().get(1)[0][1]}});
//    		currentPlayer.setCurrentMjList(MahjongUtils.paiXu(currentPlayer.getCurrentMjList()));
//    		boolean hasGang = currentPlayer.getHasGang();
//    		currentPlayer.setHasGang(false);
//
//    		Map<Integer, Integer> huInfo = MahjongUtils.checkHuInfo(currentPlayer, currentPlayer, null, room);
//    		
//    		for(int i=0;i<currentPlayer.getCurrentMjList().size();i++){
//    			if (currentPlayer.getCurrentMjList().get(i)[0][0].equals(room.getHunPai().get(1)[0][0])&&currentPlayer.getCurrentMjList().get(i)[0][1].equals(room.getHunPai().get(1)[0][1])) {
//    				currentPlayer.getCurrentMjList().remove(i);
//    				break;
//				}
//    		}
//    		currentPlayer.setLastFaPai(null);
//    		currentPlayer.setHasGang(hasGang);
//    		
//    		int fans = 0;
//			if (huInfo!=null&&huInfo.size()>0) {
//				for(Integer fan:huInfo.values()){
//					fans += fan;
//				}
//			}
			
//        	if (MahjongUtils.isMenQing(currentPlayer)||fans>2) {
        		hasXiaoSa = true;
        		currentPlayer.setXiaoSa(Cnst.XIAO_SA_NO_CHOOSE);
//			}
		}
    	return hasXiaoSa;
    }
    
    
    /**
     * 如果返回true，代表有action，并且已经在player的属性中了
     */
    private static Boolean checkOthersAction(List<Player> others,Integer[][] paiInfo,Player currentPlayer,Player nextUser,RoomResp room,List<Player> players){
    	//给其他玩家检测动作
        Boolean hasAction = false;
        for(Player ps:others){
            if(checkActions(ps,new Integer[][]{{paiInfo[0][0],paiInfo[0][1]}},ps.getUserId().equals(nextUser.getUserId()),currentPlayer,room,players)){
                hasAction = true;
            }

        }
        //检测玩家动作优先级,删除优先级低的玩家动作
        removeActions(others,currentPlayer);
        
        if (hasAction){//有玩家有动作
        	
          //添加自己为过的人（出牌人对自己的牌肯定不能有动作）
            if (room.getGuoUserIds()==null) {
    			room.setGuoUserIds(new ArrayList<Long>());
    		}
            room.getGuoUserIds().add(currentPlayer.getUserId());
        }else{//没有动作了,推发牌
        	//出牌提示
        	nextUser.setNeedFaPai(true);
        	nextUser.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
            room.setGuoUserIds(null);
        }
        return hasAction;
    }
    
    
    public static Player getActionPlayer(List<Player> players){
    	//在按照优先级移除之后，只有一个玩家有动作
        Player actionPlayer = null;
        for(Player p:players){
        	if (p.getCurrentActions()!=null&&p.getCurrentActions().size()>0) {
        		actionPlayer = p;
        		break;
			}
        }
        return actionPlayer;
    }

    /**
     * 对比两个玩家的动作，把优先级低的玩家动作清空
     * @param others
     */
    protected static void removeActions(List<Player> others,Player chuUser){
        a:for(int i=0;i<others.size();i++){
            Player p1 = others.get(i);
            if (p1.getCurrentActions()!=null&&p1.getCurrentActions().size()>0){
                for(int j=i+1;j<others.size();j++){
                    Player p2 = others.get(j);
                    if (p2.getCurrentActions()!=null&&p2.getCurrentActions().size()>0){
                        Integer p1Act = 0;
                        Integer[] p1as = new Integer[p1.getCurrentActions().keySet().size()];
                        Integer p2Act = 0;
                        Integer[] p2as = new Integer[p2.getCurrentActions().keySet().size()];
                        int num = 0;
                        for(String act1 : p1.getCurrentActions().keySet()){
                            p1as[num++] = Integer.valueOf(act1);
                        }
                        num = 0;
                        for(String act2 : p2.getCurrentActions().keySet()){
                            p2as[num++] = Integer.valueOf(act2);
                        }
                        Arrays.sort(p1as);
                        Arrays.sort(p2as);
                        p1Act = p1as[p1as.length-1];
                        p2Act = p2as[p2as.length-1];
                        if (p1Act.equals(p2Act)){//两家都胡牌，分局圈风确定向下推
                            Integer circleWind = chuUser.getPosition();
                            //玩家的风向跟出牌人的位置对比，都大于牌人的位置，则取大的；都小于牌人的位置则取小的；一大一小则取大
                            Integer wind1 = p1.getPosition();
                            Integer wind2 = p2.getPosition();
                            Integer[] winds = new Integer[3];
                            winds[0] = wind1;
                            winds[1] = wind2;
                            winds[2] = circleWind;
                            Arrays.sort(winds);
                            if(winds[0].equals(circleWind)){
                                if (winds[1].equals(wind1)){
                                    p2.setCurrentActions(null);
                                }else{
                                    p1.setCurrentActions(null);
                                    continue a;
                                }
                            }else if(winds[1].equals(circleWind)){
                                if (winds[2].equals(wind1)){
                                    p2.setCurrentActions(null);
                                }else{
                                    p1.setCurrentActions(null);
                                    continue a;
                                }
                            }else if(winds[2].equals(circleWind)){
                                if (winds[0].equals(wind1)){
                                    p2.setCurrentActions(null);
                                }else{
                                    p1.setCurrentActions(null);
                                    continue a;
                                }
                            }
                        }else if (p1Act>p2Act){//玩家1优先级高
                            p2.setCurrentActions(null);
                        }else if (p1Act<p2Act){//玩家2优先级高
                            p1.setCurrentActions(null);
                            continue a;
                        }
                    }
                }
            }
        }
    }

    /**
     * 玩家动作
     * @param session
     * @param readData
     */
    public static void interface_100202(IoSession session, ProtocolData readData) throws Exception{
        logger.I("玩家动作,interfaceId -> 100202");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        Integer action = obj.getInteger("action");
        Long toUserId = obj.getLong("toUserId");
        Integer[][] actionPai = getIntegerList(obj.getString("actionPai"));
        if (actionPai[0][0]==null||actionPai[0][1]==null) {
        	actionPai = null;
		}
        Integer[][] pais = getIntegerList(obj.getString("pais"));
        Player currentPlayer = getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)));
        
        //获取到胡信息
        Map<String, String> huInfo = null;
        if (obj.containsKey("fanInfo")) {
        	huInfo = (Map<String, String>) obj.get("fanInfo");
		}
        if (currentPlayer.getCurrentActions()==null||currentPlayer.getCurrentActions().size()==0||!currentPlayer.getCurrentActions().containsKey(String.valueOf(action))) {
        	System.err.println("玩家动作，当前状态不对！");
			return;
		}

        RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
        //设置递增id
        Integer wsw_sole_action_id = obj.getInteger("wsw_sole_action_id");
        if (!room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			MessageFunctions.interface_100108(session);
			return ;
		}else{
			room.setWsw_sole_action_id(wsw_sole_action_id+1);
		}
        
        Boolean isNextUser = false;
        Player nextUser = null;//下一个发牌的人
        Player chuPlayer = null;//最后出牌人

    	currentPlayer.setNeedFaPai(false);
    	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
        
        Map<String, Object> userActionMap = new HashMap<String, Object>();
        
        updateRedisData(null,currentPlayer);
        List<Player> players = getPlayerList(room);
        for (int m = 0; m < players.size(); m++) {
			Player p = players.get(m);
			if (p.getUserId().equals(currentPlayer.getUserId())) {
				players.set(m, currentPlayer);
				break;
			}
		}
        
        //清空玩家动作
        for (int i=0;i<players.size();i++){
        	if (players.get(i).getUserId().equals(room.getLastUserId())) {
        		chuPlayer = players.get(i);
        		if (i==3) {
					nextUser = players.get(0);
				}else{
					nextUser = players.get(i+1);
				}
        		if (nextUser.getUserId().equals(currentPlayer.getUserId())) {
					isNextUser = true;
				}
			}
        	if (players.get(i).getUserId().equals(userId)) {
            	userActionMap = players.get(i).getCurrentActions();
			}
            players.get(i).setCurrentActions(null);
        }
        
        if (nextUser==null) {//说明是首轮发牌，还没有人出牌，计算当前玩家的下家
			if (currentPlayer.getPosition().equals(Cnst.WIND_NORTH)) {//
				nextUser = players.get(0);
			}else{
				nextUser = players.get(currentPlayer.getPosition());
			}
		}
        
        
        InfoCount info = new InfoCount();
        info.setActionType(action);
        info.setUserId(currentPlayer.getUserId());
        info.setToUserId(toUserId);
        info.setT(new Date().getTime());

        List<Integer[][]> list = new ArrayList<>();
        switch (action){
            case Cnst.ACTION_CHI:
                list.add(new Integer[][]{{pais[0][0],pais[0][1]}});
                list.add(new Integer[][]{{pais[1][0],pais[1][1]}});
                MahjongUtils.chi(currentPlayer.getCurrentMjList(),list);
                list.add(new Integer[][]{{actionPai[0][0],actionPai[0][1]}});
                info.setL(list);
                currentPlayer.getChiList().add(info);
                currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
                chuPlayer.getChuList().remove(chuPlayer.getChuList().size()-1);//把出牌人的最后一张从出牌list中移除
                room.setLastPai(null);
                room.setLastUserId(null);
                
                for(Player p:players){
                	updateRedisData(null,p);
                }
                updateRedisData(room,null);
                MessageFunctions.interface_100104(players,userId,action,chuPlayer.getUserId(),null,session,room);
                break;
            case Cnst.ACTION_PENG:
                MahjongUtils.peng(currentPlayer.getCurrentMjList(),actionPai);
                list.add(new Integer[][]{{actionPai[0][0],actionPai[0][1]}});
                list.add(new Integer[][]{{actionPai[0][0],actionPai[0][1]}});
                list.add(new Integer[][]{{actionPai[0][0],actionPai[0][1]}});
                info.setL(list);
                currentPlayer.getPengList().add(info);
                currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
                chuPlayer.getChuList().remove(chuPlayer.getChuList().size()-1);//把出牌人的最后一张从出牌list中移除
                room.setLastPai(null);
                room.setLastUserId(null);
                
                for(Player p:players){
                	updateRedisData(null,p);
                }
                updateRedisData(room,null);
                MessageFunctions.interface_100104(players,userId,action,chuPlayer.getUserId(),null,session,room);
                break;
            case Cnst.ACTION_LIANG_FENG:
            	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
            	for(int i=0;i<pais.length;i++){
                    list.add(new Integer[][]{{pais[i][0],pais[i][1]}});
                }
            	Integer num = null;
            	if (list.size()==1) {
            		num = currentPlayer.getGangListType2().get(0).getL().size();
            	}
            	
            	MahjongUtils.liangFeng(currentPlayer, list);
            	info.setL(list);
            	
            	if (list.size()==1) {
					int temp = currentPlayer.getGangListType2().get(0).getL().size();
					if (num==temp) {
		                currentPlayer.getGangListType2().add(info);
					}
				}else{
	                currentPlayer.getGangListType2().add(info);
				}
            	
                if (currentPlayer.getLastFaPai()!=null) {
            		currentPlayer.getCurrentMjList().add(new Integer[][]{{currentPlayer.getLastFaPai()[0][0],currentPlayer.getLastFaPai()[0][1]}});
                	currentPlayer.setCurrentMjList(MahjongUtils.paiXu(currentPlayer.getCurrentMjList()));
                	currentPlayer.setLastFaPai(null);
				}
                
                if (list.size()==3) {//不需要补牌
	            	currentPlayer.setNeedFaPai(false);
					//需要继续监测动作
					boolean hasAction = GameFunctions.checkActions(currentPlayer,currentPlayer.getLastFaPai(),false,currentPlayer,room,null);
					if (hasAction) {
		            	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
					}else{
		            	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
					}
				}else if(list.size()==1){
					//newAdd 补风补计算抢杠胡
	                if (checkQiangGangHu(players, currentPlayer, nextUser, list.get(0), room,action)) {
		            	currentPlayer.setNeedFaPai(false);
		            	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
					}else{
		            	currentPlayer.setNeedFaPai(true);
		            	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
					}
				}else{
	            	currentPlayer.setNeedFaPai(true);
	            	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
				}
                
                if (!nextUser.getUserId().equals(currentPlayer.getUserId())) {
                    nextUser.setNeedFaPai(false);
				}
            	for(Player p:players){
                	updateRedisData(null,p);
                }
                updateRedisData(room,null);
                MessageFunctions.interface_100104(players,userId,action,currentPlayer.getUserId(),null,session,room);
                break;
            case Cnst.ACTION_GANG:
                currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
                for(int i=0;i<pais.length;i++){
                    list.add(new Integer[][]{{pais[i][0],pais[i][1]}});
                }
                Integer gangType = MahjongUtils.gang(currentPlayer,list);
                currentPlayer.setHasGang(true);
                
                switch (gangType){
                    case 3://碰杠，在过杠得时候，得加其他人胡的检测
                    	
                    	if (checkQiangGangHu(players, currentPlayer, nextUser, list.get(0), room,action)) {
        	            	currentPlayer.setNeedFaPai(false);
        	            	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
        				}else{
                    		currentPlayer.setNeedFaPai(true);
                        	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
        				}
                    	
                        for(Player p:players){
                        	updateRedisData(null,p);
                        }
                        updateRedisData(room,null);
                        MessageFunctions.interface_100104(players,userId,action,currentPlayer.getUserId(),gangType,session,room);
                        break;
                    case 4:
                        list.add(new Integer[][]{{list.get(0)[0][0],list.get(0)[0][1]}});
                        info.setL(list);
                        currentPlayer.getGangListType4().add(info);
                        chuPlayer.getChuList().remove(chuPlayer.getChuList().size()-1);//把出牌人的最后一张从出牌list中移除
                        room.setLastPai(null);
                        room.setLastUserId(null);

                    	currentPlayer.setNeedFaPai(true);
                    	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);

                        for(Player p:players){
                        	updateRedisData(null,p);
                        }
                        updateRedisData(room,null);
                        MessageFunctions.interface_100104(players,userId,action,chuPlayer.getUserId(),gangType,session,room);
                        break;
                    case 5:
                        info.setL(list);
                        currentPlayer.getGangListType5().add(info);
                        if (currentPlayer.getLastFaPai()!=null) {
                    		currentPlayer.getCurrentMjList().add(new Integer[][]{{currentPlayer.getLastFaPai()[0][0],currentPlayer.getLastFaPai()[0][1]}});
                        	currentPlayer.setCurrentMjList(MahjongUtils.paiXu(currentPlayer.getCurrentMjList()));
                        	currentPlayer.setLastFaPai(null);
						}

                    	currentPlayer.setNeedFaPai(true);
                    	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);

                        for(Player p:players){
                        	updateRedisData(null,p);
                        }
                        updateRedisData(room,null);
                        MessageFunctions.interface_100104(players,userId,action,currentPlayer.getUserId(),gangType,session,room);
                        break;
                }
                break;
            case Cnst.ACTION_HU:
            	/**诈胡处理*/
            	boolean zhaHu = false;
            	Map<Integer, Integer> fanInfo = null;
            	if (room.getXiaoSa().equals(Cnst.YES)) {//房间有潇洒规则
					if (currentPlayer.getXiaoSa()!=null&&currentPlayer.getXiaoSa().equals(Cnst.XIAO_SA_YES)) {
						fanInfo = new HashMap<Integer, Integer>();
						for(String key:huInfo.keySet()){
							fanInfo.put(Integer.valueOf(key), Integer.valueOf(huInfo.get(key)));
						}
						String chooseHuType = obj.getString("chooseHuType");
						String[] types = chooseHuType.split("_");
						for(int i=0;i<types.length;i++){
							if (!fanInfo.containsKey(Integer.valueOf(types[i]))) {
								zhaHu = true;
								currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
								int zhaHuFans = 0;
								for(int j=0;j<types.length;j++){
									zhaHuFans+=Cnst.getFan(Integer.valueOf(types[i]));
								}
								currentPlayer.setZhHuFans(zhaHuFans);
								break;
							}
						}
						//取出玩家胡牌的最大番的类型
						Map<Integer,Integer> maxFan = null;
						boolean choosePingHu = false;
						if (zhaHu) {//如果玩家诈胡，按照现在传的数据，如果玩家选择平胡，但是胡的信息是边卡钓，这样也会造成诈胡，所以要排除
							if (types.length==1&&types[0].equals(String.valueOf(Cnst.HU_PING_HU))) {
								choosePingHu = true;
								zhaHu = false;
							}
							//在旧的检测诈胡的时候，没有对所有牌形检测，所以可能玩家选择的可能不在计算的里面，但是也是一种情况，
							//所以，在之前最优的里面没有的话，要在看有没有其他情况，如果再所有的类型中，都没有玩家选择的类型的话， 真正的构成诈胡

							List<Map<Integer,Integer>> allInfo = MahjongUtils.checkHuInfo(currentPlayer, currentPlayer, null, room);
							maxFan = allInfo.get(0);
							a:for(Map<Integer,Integer> map:allInfo){
								for(int i=0;i<types.length;i++){
									if (!map.containsKey(Integer.valueOf(types[i]))) {
										continue a;
									}
								}
								zhaHu = false;
								break;
							}
							
							
						}
						
						Map<Integer, Integer> temp = fanInfo;
						//无论是否诈胡，都按照玩家选的去结算
						fanInfo = new HashMap<Integer, Integer>();
						for(String choose:types){
							if (choose.equals(String.valueOf(Cnst.HU_PING_HU))) {
								continue;
							}
							fanInfo.put(Integer.valueOf(choose), Cnst.getFan(Integer.valueOf(choose)));
						}

						currentPlayer.setZhaHu(zhaHu);
						
						if (!zhaHu) {//如果没有炸胡，就把玩家选择的所有的基本胡类型番，加上其他的起番项
							
							//如果没有诈胡，如果是四混胡牌，需要把玩家选择的平胡给换成四混胡
							if (temp.containsKey(Cnst.HU_SI_HUN_HU)) {
								fanInfo.remove(Cnst.HU_PING_HU);
								fanInfo.put(Cnst.HU_SI_HUN_HU, temp.get(Cnst.HU_SI_HUN_HU));
							}
							for(Integer fan:temp.keySet()){
								if (!fanInfo.containsKey(fan)&&!(
										fan.equals(Cnst.HU_BIAN_KA_DIAO)||
										fan.equals(Cnst.HU_ZHUO_WU_KUI)||
										fan.equals(Cnst.HU_KA_DIAO_ZHUO_WU_KUI)||
										fan.equals(Cnst.HU_YI_TIAO_LONG)||
										fan.equals(Cnst.HU_BEN_HUN_YI_TIAO_LONG)||
										fan.equals(Cnst.HU_HUN_YI_SE)||
										fan.equals(Cnst.HU_QING_YI_SE)||
										fan.equals(Cnst.HU_DA_PIAO)||
										fan.equals(Cnst.HU_QI_DUI)||
										fan.equals(Cnst.HU_HAO_HUA_QI_DUI)||
										fan.equals(Cnst.HU_HAO_HUA)||
										fan.equals(Cnst.HU_SAN_HAO_HUA)||
										fan.equals(Cnst.HU_BIAN)||
										fan.equals(Cnst.HU_KA)||
										fan.equals(Cnst.HU_DIAO)||
										fan.equals(Cnst.HU_KA_ZHUO_WU)||
										fan.equals(Cnst.HU_DIAO_ZHUO_WU))) {
									fanInfo.put(fan, temp.get(fan));
								}
							}
						}
						
						//新龟腚：如果玩家本来胡的就是平胡，而且，选择了平胡，然后，要加上其他的除了基本胡牌类型之外的起番项，比如清一色等等
						if (choosePingHu&&
								!maxFan.containsKey(Cnst.HU_BIAN)&&
								!maxFan.containsKey(Cnst.HU_KA)&&
								!maxFan.containsKey(Cnst.HU_DIAO)&&
								!maxFan.containsKey(Cnst.HU_DA_PIAO)&&
								!maxFan.containsKey(Cnst.HU_KA_ZHUO_WU)&&
								!maxFan.containsKey(Cnst.HU_DIAO_ZHUO_WU)&&
								!maxFan.containsKey(Cnst.HU_QI_DUI)&&
								!maxFan.containsKey(Cnst.HU_HAO_HUA_QI_DUI)&&
								!maxFan.containsKey(Cnst.HU_HAO_HUA)&&
								!maxFan.containsKey(Cnst.HU_SAN_HAO_HUA)
								) {
							if (maxFan!=null&&maxFan.size()>0) {
								for(Integer huT:maxFan.keySet()){
									if (!fanInfo.containsKey(huT)) {
										fanInfo.put(huT, maxFan.get(huT));
									}
								}
							}
						}
						
					}
				}

            	for(Player p:players){
                	updateRedisData(null,p);
                }
                updateRedisData(room,null);
                MessageFunctions.interface_100104(players,userId,action,toUserId,null,session,room);
                //诈胡之后，结束牌局
//                if (zhaHu) {
//					return;
//				}
                for(Player p:players){
                    if (p.getUserId().equals(currentPlayer.getUserId())){
                        p.setIsHu(true);
                        p.setIsDian(false);
                        if (p.getHuNum()==null){
                            p.setHuNum(1);
                        }else{
                            p.setHuNum(p.getHuNum()+1);
                        }
                    }else{
                    	if (p.getLoseNum()==null){
                            p.setLoseNum(1);
                        }else{
                            p.setLoseNum(p.getLoseNum()+1);
                        }
                        p.setIsHu(false);
                        if (!toUserId.equals(currentPlayer.getUserId())&&p.getUserId().equals(toUserId)){

                        	p.setDianNum(p.getDianNum()==null?1:p.getDianNum()+1);
                            p.setIsDian(true);
                        }else{
                            p.setIsDian(false);
                        }
                    }
                    p.setPlayStatus(Cnst.PLAYER_STATE_XJS);
                }
                Player dianUser = null;
                if (toUserId.equals(userId)){
                	currentPlayer.setZimoNum(currentPlayer.getZimoNum()==null?1:currentPlayer.getZimoNum()+1);
                	dianUser = currentPlayer;
                }else{
                	if (room.getLastPengGangUser()!=null) {//抢杠胡
						for(Player p:players){
							if (p.getUserId().equals(toUserId)&&toUserId.equals(room.getLastPengGangUser()[0])) {//抢杠胡
								dianUser = p;
								Integer[][] g3 = p.getGangListType3().get(p.getGangListType3().size()-1).getL().get(0);
								actionPai = new Integer[][]{{g3[0][0],g3[0][1]}};
								break;
							}else if(p.getUserId().equals(toUserId)&&toUserId.equals(room.getLastPengGangUser()[1])){//抢风胡
								dianUser = p;
								Integer[][] g3 = p.getGangListType2().get(p.getGangListType2().size()-1).getL().get(0);
								actionPai = new Integer[][]{{g3[0][0],g3[0][1]}};
								break;
							}
						}
					}else{
	                	chuPlayer.getChuList().remove(chuPlayer.getChuList().size()-1);//把出牌人的最后一张从出牌list中移除
	                	dianUser = chuPlayer;
					}
                }
                
                for(Player p:players){
                	updateRedisData(null,p);
                }
                updateRedisData(room,null);
                MessageFunctions.hu(currentPlayer,dianUser,actionPai,session,fanInfo);
                break;
            case Cnst.ACTION_GUO:
            	if (room.getLastPengGangUser()!=null) {
        			Long lastGangUid = room.getLastPengGangUser()[0]==null?room.getLastPengGangUser()[1]:room.getLastPengGangUser()[0];
        			Player lastGangUser = null;
        			for(Player p:players){
        				if (p.getUserId().equals(lastGangUid)) {
        					lastGangUser = p;
							if (p.getPosition().equals(Cnst.WIND_NORTH)) {
								nextUser = players.get(0);
								isNextUser = false;
							}else{
								nextUser = players.get(p.getPosition());
								if (nextUser.getUserId().equals(currentPlayer.getUserId())) {
									isNextUser = true;
								}
							}
        					break;
						}
        			}
        			
        			//设置过手信息
        			Map<String,Map<String,String>> rnmmp = (Map<String, Map<String,String>>) userActionMap.get(String.valueOf(Cnst.ACTION_HU));
					
					Map<String, String> temp = rnmmp.get("fanInfo");
					
					Map<Integer,Integer> mmp = new HashMap<>();
					for(String str:temp.keySet()){
						mmp.put(Integer.valueOf(str), Cnst.getFan(Integer.valueOf(temp.get(str))));
					}
					currentPlayer.setGuoShouFan(mmp);
					
					//向过的人里面添加自己
                	if (room.getGuoUserIds()==null) {
                    	room.setGuoUserIds(new ArrayList<>());
    				}
                	room.getGuoUserIds().add(currentPlayer.getUserId());
                	if (!room.getGuoUserIds().contains(lastGangUser.getUserId())) {
						room.getGuoUserIds().add(lastGangUser.getUserId());
					}
                	
                	//检测其他人胡牌操作
                	chuPlayer = lastGangUser;
                	boolean hasAction = false;
                    for(Player p:players){
                        if (!room.getGuoUserIds().contains(p.getUserId())){//检测没有过的人是否有动作
                            if (checkActions(p,actionPai,p.getUserId().equals(nextUser.getUserId()),chuPlayer,room,players)){
                                hasAction = true;
                            }
                        }
                    }
//                    if (hasAction){//有动作，推送大接口
//                    	 List<Player> othersList = new ArrayList<Player>();
//                         for(Player pppp:players){
//                         	if (!chuPlayer.getUserId().equals(pppp.getUserId())) {
//                         		othersList.add(pppp);
//         					}
//                         }
//                         
//                        removeActions(othersList, chuPlayer);
//                    }else{//没有动作，发牌
//    					//置空过的人
//    					room.setGuoUserIds(null);
//                    	currentPlayer.setNeedFaPai(true);
//                    	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
//                    }
					
                    //抢杠胡的过处理
                    if (qiangGangGuo(players, room)) {//存在过杠胡
                    	currentPlayer.setNeedFaPai(false);
                    	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
					}else{//没有过杠胡
						if (room.getLastPengGangUser()!=null) {
							for(Player p:players){
								if (p.getUserId().equals(room.getLastPengGangUser()[0])||p.getUserId().equals(room.getLastPengGangUser()[1])) {
									p.setNeedFaPai(true);
							    	p.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
								}
							}
						}
					}
                    
                    for(Player p:players){
                    	updateRedisData(null,p);
                    }
                    updateRedisData(room,null);
                    MessageFunctions.interface_100104(players,userId,action,toUserId,null,session,room);
        			
        		}else{
        			if (currentPlayer.getLastFaPai()==null&&room.getLastPai()!=null) {//弃别人的牌
                		//设置过手信息
                		if (userActionMap.containsKey(String.valueOf(Cnst.ACTION_HU))) {//过的是胡的牌
    						Map<String,Map<String,String>> rnmmp = (Map<String, Map<String,String>>) userActionMap.get(String.valueOf(Cnst.ACTION_HU));
    						
    						Map<String, String> temp = rnmmp.get("fanInfo");
    						
    						Map<Integer,Integer> mmp = new HashMap<>();
    						for(String str:temp.keySet()){
    							mmp.put(Integer.valueOf(str), Cnst.getFan(Integer.valueOf(temp.get(str))));
    						}
    						
    						currentPlayer.setGuoShouFan(mmp);
    					}
                    	//向过的人里面添加自己
                    	if (room.getGuoUserIds()==null) {
                        	room.setGuoUserIds(new ArrayList<>());
        				}
                    	room.getGuoUserIds().add(currentPlayer.getUserId());
    					if (isNextUser) {//自己是下家
    						if (userActionMap.containsKey(String.valueOf(Cnst.ACTION_HU))) {//胡牌  弃，因为胡牌的优先级较高，如果胡牌过了之后，要检测其他玩家动作
    							boolean hasAction = false;
    		                    for(Player p:players){
    		                        if (!room.getGuoUserIds().contains(p.getUserId())){//检测没有过的人是否有动作
    		                            if (checkActions(p,actionPai,p.getUserId().equals(nextUser.getUserId()),chuPlayer,room,players)){
    		                                hasAction = true;
    		                            }
    		                        }
    		                    }
    		                    if (hasAction){//有动作，推送大接口
    		                    	 List<Player> othersList = new ArrayList<Player>();
    		                         for(Player pppp:players){
    		                         	if (!chuPlayer.getUserId().equals(pppp.getUserId())) {
    		                         		othersList.add(pppp);
    		         					}
    		                         }
    		                         
    		                        removeActions(othersList, chuPlayer);
    		                    }else{//没有动作，发牌
    		    					//置空过的人
    		    					room.setGuoUserIds(null);
    		                    	currentPlayer.setNeedFaPai(true);
    		                    	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
    		                    }
    		                    
    		                    for(Player p:players){
    		                    	updateRedisData(null,p);
    		                    }
    		                    updateRedisData(room,null);
    	                        MessageFunctions.interface_100104(players,userId,action,toUserId,null,session,room);
    						}else{//非胡牌 弃
    							//置空过的人
    							room.setGuoUserIds(null);
    		                    currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
    	                    	currentPlayer.setNeedFaPai(true);
    	                    	
    	                    	if (userActionMap.containsKey(String.valueOf(Cnst.ACTION_LIANG_FENG))&&
    	                    			currentPlayer.getGangListType2()!=null&&currentPlayer.getGangListType2().size()==1&&
    	                    			currentPlayer.getGangListType2().get(0).getL().size()==3
    	                    			) {
    			                    currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
    		                    	currentPlayer.setNeedFaPai(false);
    							}

    		                    for(Player p:players){
    		                    	updateRedisData(null,p);
    		                    }
    		                    updateRedisData(room,null);
    	                        MessageFunctions.interface_100104(players,userId,action,toUserId,null,session,room);
    		                    
    						}
    					}else{//不是下家    检测其他
    						boolean hasAction = false;
    	                    for(Player p:players){
    	                        if (!room.getGuoUserIds().contains(p.getUserId())){//检测没有过的人是否有动作
    	                            if (checkActions(p,actionPai,p.getUserId().equals(nextUser.getUserId())	,chuPlayer,room,players)){
    	                                hasAction = true;
    	                            }
    	                        }
    	                    }
    	                    if (hasAction){//有动作，推送大接口
    	                    	List<Player> othersList = new ArrayList<Player>();
    	                         for(Player pppp:players){
    	                         	if (!chuPlayer.getUserId().equals(pppp.getUserId())) {
    	                         		othersList.add(pppp);
    	         					}
    	                         }
    	                         
    	                         removeActions(othersList, chuPlayer);
    	                    }else{//没有动作，发牌
    	    					//置空过的人
    	    					room.setGuoUserIds(null);
    	    					nextUser.setNeedFaPai(true);
    	    	            	nextUser.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
    	                    }
    	                    
    	                    
    	                    //抢杠胡的过处理
//                            if (qiangGangGuo(players, room)) {//存在过杠胡
//                            	nextUser.setNeedFaPai(false);
//                            	nextUser.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
//    						}else{//没有过杠胡
//    							if (room.getLastPengGangUser()!=null) {
//    								for(Player p:players){
//    									if (p.getUserId().equals(room.getLastPengGangUser()[0])||p.getUserId().equals(room.getLastPengGangUser()[1])) {
//    										p.setNeedFaPai(true);
//    									}
//    								}
//    							}
//    						}
    	                    

    	                    for(Player p:players){
    	                    	updateRedisData(null,p);
    	                    }
    	                    updateRedisData(room,null);
                            MessageFunctions.interface_100104(players,userId,action,toUserId,null,session,room);
    					}
    				}else{//弃自己的牌
    					//置空过的人
    					room.setGuoUserIds(null);
    					nextUser.setNeedFaPai(false);
                        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);

                        for(Player p:players){
                        	updateRedisData(null,p);
                        }
                        updateRedisData(room,null);
                        MessageFunctions.interface_100104(players,userId,action,toUserId,null,session,room);
    				}
        		}
            	
                break;
        }
    }
    
    /**
     * 此方法的前提是必须过的动作是胡
     * @param players
     * @param room
     */
    private static boolean qiangGangGuo(List<Player> players,RoomResp room){
    	boolean hasHu = false;
    	if (room.getLastPengGangUser()!=null&&room.getLastPengGangUser()[0]!=null) {
    		Player hasActionPlayer = getActionPlayer(players);
    		if (hasActionPlayer!=null) {
    			if (hasActionPlayer.getCurrentActions().containsKey(String.valueOf(Cnst.ACTION_HU))) {
                	Iterator<String> iterator = hasActionPlayer.getCurrentActions().keySet().iterator();
    		        while(iterator.hasNext()) {  
    		            String str = iterator.next();  
    		            if(!str.equals(String.valueOf(Cnst.ACTION_HU))&&!str.equals(String.valueOf(Cnst.ACTION_GUO))) {  
    		                iterator.remove();  
    		            }  
    		        }
    		        hasHu = true;
    			}else{
    				hasActionPlayer.setCurrentActions(null);
    			}
			}
		}
    	return hasHu;
    }
    
    
    private static boolean checkQiangGangHu(List<Player> players,Player currentPlayer,Player nextUser,Integer[][] pai,RoomResp room,Integer action){
    	boolean canHu = false;
    	
    	//过杠胡逻辑
    	List<Player> others = new ArrayList<Player>();
    	for(Player p:players){
    		if (!p.getUserId().equals(currentPlayer.getUserId())) {
    			others.add(p);
			}
    	}
    	if (checkOthersAction(others, new Integer[][]{{pai[0][0],pai[0][1]}}, currentPlayer, nextUser, room, players)) {//玩家有动作
    		//查看玩家的动作是不是胡，如果是胡，则构成过杠胡；
			Player p = getActionPlayer(players);
			if (p.getCurrentActions().containsKey(String.valueOf(Cnst.ACTION_HU))) {//如果其他玩家有胡的动作
				//抢杠胡：能过

		        Iterator<String> iterator = p.getCurrentActions().keySet().iterator();
		        while(iterator.hasNext()) {  
		            String str = iterator.next();  
		            if(!str.equals(String.valueOf(Cnst.ACTION_HU))&&!str.equals(String.valueOf(Cnst.ACTION_GUO))) {  
		                iterator.remove();  
		            }  
		        }  
		        
		        canHu = true;
		        Long[] lastPengGangUser = new Long[2];
		        if (action.equals(Cnst.ACTION_LIANG_FENG)) {
			        lastPengGangUser[1] = currentPlayer.getUserId();
				}else if(action.equals(Cnst.ACTION_GANG)){
			        lastPengGangUser[0] = currentPlayer.getUserId();
				}
				room.setLastPengGangUser(lastPengGangUser);//设置过杠胡的人
			}else{
				p.setCurrentActions(null);
			}
		}
    	return canHu;
    }
    
    

    public static Integer[][] getIntegerList(String str) {
        if (str==null){
            return null;
        }
        JSONArray arr = JSONArray.parseArray(str);
        Integer[][] list = new Integer[arr.size()][2];
        for(int i = 0; i < arr.size(); i ++){
            JSONArray arr2 = arr.getJSONArray(i);
            list[i][0] = (Integer) arr2.get(0);
            list[i][1] = (Integer) arr2.get(1);
        }
        return list;
    }
    
    //在亮风之后，不能检测胡的动作，检测动作的时候
    private static boolean isHuPaiNum(List<Integer[][]> pais){
    	boolean result = true;
    	if (pais.size()==2||pais.size()==5||pais.size()==8||pais.size()==11||pais.size()==14) {
			result = false;
		}
    	return result;
    }
    
    public static boolean checkActions(Player p,Integer[][] pai,boolean isNextUser,Player chuUser,RoomResp room,List<Player> players){
        Map<String,Object> currentActions = p.getCurrentActions();
        if (currentActions==null){
            currentActions = new LinkedHashMap<>();  
        }
        p.setCurrentActions(currentActions);
        if (p.getUserId().equals(chuUser.getUserId())){//发牌检测，只需要检测杠或者胡

        	//胡检测		这是最新的龟腚：自摸胡不受任何限制，都可以胡
            if (isHuPaiNum(p.getCurrentMjList())&&MahjongUtils.checkHuNew(null,p,p, pai, room)){
            	
            	List<Map<Integer,Integer>> allInfo = MahjongUtils.checkHuInfo(p, p, null, room);
            	Map<Integer,Integer> huInfo = null;
            	
            	int tempFan = 0;
            	for(Map<Integer,Integer> map : allInfo){
            		int ttemp = 0;
            		for(Integer key:map.keySet()){
            			ttemp+=map.get(key);
            		}
            		if (ttemp>tempFan) {
            			tempFan = ttemp;
            			huInfo = map;
					}
            	}
            	//新龟腚：自摸时，如果开门1次，番数必须大于等于2；开门2次，番数大于等于3；以此类推
            	int chiPengNum = 0;
            	if (p.getChiList()!=null) {
            		chiPengNum+=p.getChiList().size();
				}
            	if (p.getPengList()!=null) {
            		chiPengNum+=p.getPengList().size();
				}
            	if (p.getGangListType3()!=null) {
            		chiPengNum+=p.getGangListType3().size();
				}
            	if (p.getGangListType4()!=null) {
            		chiPengNum+=p.getGangListType4().size();
				}
            	
            	if (tempFan>=(chiPengNum+1)) {//自摸番新龟腚
            		Map<String, Object> fanInfos = new HashMap<String, Object>();
    				fanInfos.put("toUserId", p.getUserId());
    				Map<String,String> hi = new HashMap<String, String>();
    				for(Integer fan:huInfo.keySet()){
    					hi.put(String.valueOf(fan),String.valueOf( huInfo.get(fan)));
    				}
    				fanInfos.put("fanInfo", hi);
    				
                    currentActions.put(String.valueOf(Cnst.ACTION_HU),fanInfos);
				}
        	}
            
        	
            //扣听检测
        	if(p.getZhuaPaiNum()==1&&p.getChuPaiNum()==0&&p.getCurrentMjList().size()==13){
        		List<Integer[][]> tingList = MahjongUtils.checkTing(p.getCurrentMjList(),pai,room);
        		if (tingList!=null&&tingList.size()>0) {
					currentActions.put(String.valueOf(Cnst.ACTION_TING), tingList);
				}
        	}
        	//亮风检测
        	if (room.getLiangFeng().equals(Cnst.YES)) {
                List<Integer[][]> fengs = MahjongUtils.checkLiangFeng(p, pai);
                if (fengs!=null&&fengs.size()>0) {
                	if (p.getXiaoSa()!=null&&p.getXiaoSa().equals(Cnst.XIAO_SA_YES)||p.getKouTing()) {
                		if (p.getGangListType2()!=null&&p.getGangListType2().size()>0) {
                			for(int i=0;i<fengs.size();i++){
                				if(!fengs.get(i)[0][0].equals(pai[0][0])||!fengs.get(i)[0][1].equals(pai[0][1])){
                					fengs.remove(i--);
    							}
                			}
                			if (fengs.size()>0) {
                				if (fengs.size()>1) {
                					Integer[][] first = fengs.get(0);
                					fengs = new ArrayList<Integer[][]>();
                					fengs.add(first);
								}
            					currentActions.put(String.valueOf(Cnst.ACTION_LIANG_FENG), fengs);
							}
            			}
					}else{
    					currentActions.put(String.valueOf(Cnst.ACTION_LIANG_FENG), fengs);
					}
                	
				}
			}
        	//杠检测
            List<Integer[][]> gangs = MahjongUtils.checkGang(p,chuUser,pai);
            if (gangs!=null&&gangs.size()>0){
            	if ((p.getXiaoSa()!=null&&p.getXiaoSa().equals(Cnst.XIAO_SA_YES))||p.getKouTing()) {
            		for(int i=0;i<gangs.size();i++){
            			if (gangs.get(i).length>1) {
            				gangs.remove(i--);
						}else{
							if(!gangs.get(i)[0][0].equals(pai[0][0])||!gangs.get(i)[0][1].equals(pai[0][1])){
	            				gangs.remove(i--);
							}
						}
            		}
                    if (gangs.size()>0) {
                    	if (gangs.size()>1) {
							Integer[][] first = gangs.get(0);
							gangs = new ArrayList<Integer[][]>();
							gangs.add(first);
						}
                        currentActions.put(String.valueOf(Cnst.ACTION_GANG),gangs);
					}
    			}else{
                    currentActions.put(String.valueOf(Cnst.ACTION_GANG),gangs);
    			}
            }
        	
        }else{//出牌过程中检测
        	/*
        	 * 1、发牌的一圈之内，过了之前玩家的牌，就不能胡其他玩家的牌，除非……
        	 * 2、如果有人潇洒，未潇洒玩家不能相互点炮，潇洒玩家可以向未潇洒玩家点炮
        	 * */

        	/**检测上述第2条*/
        	boolean canCheckHu = true;
        	Boolean canCheckOthers = true;
        	if (room.getXiaoSa().equals(Cnst.YES)) {//房间带潇洒玩儿法
        		if (p.getXiaoSa()!=null&&p.getXiaoSa().equals(Cnst.XIAO_SA_YES)) {//检测人潇洒了
        			canCheckOthers = false;
            		canCheckHu = false;
    			}else{//检测人未潇洒
    				if (!(chuUser.getXiaoSa()!=null&&chuUser.getXiaoSa().equals(Cnst.XIAO_SA_YES))) {//出牌人未潇洒，要检测是否有其他玩家潇洒
    					for(Player player:players){
        	        		if (player.getXiaoSa()!=null&&player.getXiaoSa().equals(Cnst.XIAO_SA_YES)) {//其他有玩家潇洒，不能对当前玩家p检测胡的动作
        	        			canCheckHu = false;
        					}
        	        	}
					}
    			}
			}
        	//如果玩家扣听未潇洒，别人出牌只能检测胡
        	if (p.getKouTing()&&(p.getXiaoSa()==null||p.getXiaoSa().equals(Cnst.XIAO_SA_NO_CHOOSE)||p.getXiaoSa().equals(Cnst.XIAO_SA_NO))) {
        		canCheckHu = true;
        		canCheckOthers = false;
			}
        	
        	//新龟腚：如果房间不可打混，并且玩家手上有混牌，不允许检测他的开门行为
        	if (MahjongUtils.checkHasHun(p.getCurrentMjList(), null, room)&&room.getDaHun().equals(Cnst.NO)) {
        		canCheckOthers = false;
			}
        	/**第2条检测结束*/
        	
        	if (canCheckHu) {
        		if (MahjongUtils.checkHuNew(null,p,chuUser, new Integer[][]{{pai[0][0],pai[0][1]}}, room)){
        			
        			List<Map<Integer,Integer>> allInfo = MahjongUtils.checkHuInfo(p, chuUser, new Integer[][]{{pai[0][0],pai[0][1]}}, room);
                	Map<Integer,Integer> huInfo = null;
                	
                	int tempFan = 0;
                	for(Map<Integer,Integer> map : allInfo){
                		int ttemp = 0;
                		for(Integer key:map.keySet()){
                			ttemp+=map.get(key);
                		}
                		if (ttemp>tempFan) {
                			tempFan = ttemp;
                			huInfo = map;
    					}
                	}
        			
        			int fans = 0;
					if (huInfo!=null&&huInfo.size()>0) {
						for(Integer fan:huInfo.values()){
							fans += fan;
						}
					}
					

					Map<String, Object> fanInfos = new HashMap<String, Object>();
					fanInfos.put("toUserId", chuUser.getUserId());
					Map<String,String> hi = new HashMap<String, String>();
					for(Integer fan:huInfo.keySet()){
						hi.put(String.valueOf(fan),String.valueOf( huInfo.get(fan)));
					}
					fanInfos.put("fanInfo", hi);
					
                	if (MahjongUtils.isMenQing(p)) {
                    	/**第1条检测*/
                		if (p.getGuoShouFan()!=null) {
                			Map<Integer,Integer> guo = p.getGuoShouFan();
                			int temp = 0;
                			for(Integer i:guo.keySet()){
                				temp += guo.get(i);
                			}
							if (fans>temp) {
		                        currentActions.put(String.valueOf(Cnst.ACTION_HU),fanInfos);
							}
						}else {
	                        currentActions.put(String.valueOf(Cnst.ACTION_HU),fanInfos);
						}
    				}else{//当前玩家非门清，胡牌番数要大于2番

    	            	//新龟腚：被点炮时，如果开门1次，番数必须大于等于3；开门2次，番数大于等于4；以此类推
    					int chiPengNum = 0;
    	            	if (p.getChiList()!=null) {
    	            		chiPengNum+=p.getChiList().size();
    					}
    	            	if (p.getPengList()!=null) {
    	            		chiPengNum+=p.getPengList().size();
    					}
    	            	if (p.getGangListType3()!=null) {
    	            		chiPengNum+=p.getGangListType3().size();
    					}
    	            	if (p.getGangListType4()!=null) {
    	            		chiPengNum+=p.getGangListType4().size();
    					}
    					
    					
    					if (fans>=(chiPengNum+2)) {//胡番新龟腚
    						/**第1条检测*/
                    		if (p.getGuoShouFan()!=null) {
                    			Map<Integer,Integer> guo = p.getGuoShouFan();
                    			int temp = 0;

                    			for(Integer i:guo.keySet()){
                    				temp += guo.get(i);
                    			}
    							if (fans>temp) {
    		                        currentActions.put(String.valueOf(Cnst.ACTION_HU),fanInfos);
    							}
    						}else {
    	                        currentActions.put(String.valueOf(Cnst.ACTION_HU),fanInfos);
    						}
						}
    				}
                }
			}
        	//如果玩家潇洒了，啥都不能检测
        	if (canCheckOthers) {
        		List<Integer[][]> gangs = MahjongUtils.checkGang(p,chuUser,new Integer[][]{{pai[0][0],pai[0][1]}});
                if (gangs!=null&&gangs.size()>0){
                    currentActions.put(String.valueOf(Cnst.ACTION_GANG),gangs);
                }
                List<Integer[][]> pengs = MahjongUtils.checkPeng(p,new Integer[][]{{pai[0][0],pai[0][1]}});
                if (pengs!=null&&pengs.size()>0){
                    currentActions.put(String.valueOf(Cnst.ACTION_PENG),pengs);
                }
                if (isNextUser){
                    List<Integer[][]> chis = MahjongUtils.checkChi(p,new Integer[][]{{pai[0][0],pai[0][1]}});
                    if (chis!=null&&chis.size()>0){
                        currentActions.put(String.valueOf(Cnst.ACTION_CHI),chis);
                    }
                }
			}
        }
        if (currentActions.size()==0){
            p.setCurrentActions(null);
            return false;
        }else{
        	//2、玩家四混胡牌，不能有过
        	boolean canGuo = true;
        	//1、玩家潇洒后胡牌没有过动作，newAdd新龟腚：潇洒的时候，可以过
//        	if (p.getXiaoSa()!=null&&p.getXiaoSa().equals(Cnst.XIAO_SA_YES)&&p.getCurrentActions().containsKey(String.valueOf(Cnst.ACTION_HU))) {
//        		canGuo = false;
//			}
        	//3、海捞的时候，直接推到，也就是没有过
        	if (room.getHaiLao()) {
        		canGuo = false;
			}
        	
        	if (p.getCurrentActions().containsKey(String.valueOf(Cnst.ACTION_HU))) {
            	Map<String, Object> tt = (Map<String, Object>) p.getCurrentActions().get(String.valueOf(Cnst.ACTION_HU));
            	Map<String,String> fanInfos = (Map<String, String>) tt.get("fanInfo");
            	for(String k:fanInfos.keySet()){
            		if (k.equals(String.valueOf(Cnst.HU_SI_HUN_HU))) {
            			
            			Iterator<String> iterator = p.getCurrentActions().keySet().iterator();  
            		     while(iterator.hasNext()) {  
            		         String str = iterator.next();  
            		         if (!str.equals(String.valueOf(Cnst.ACTION_HU))) {  
            		             iterator.remove(); 
     						} 
            		     } 
                		canGuo = false;
					}
            	}
			}
        	
        	//在玩家听/潇洒后，如果来混，只能胡，不能过
        	if (p.getUserId().equals(chuUser.getUserId())) {//检测自己
				if ((p.getXiaoSa()!=null&&p.getXiaoSa().equals(Cnst.XIAO_SA_YES))||p.getKouTing()) {
					if (pai[0][0].equals(room.getHunPai().get(1)[0][0])&&pai[0][1].equals(room.getHunPai().get(1)[0][1])) {
                		canGuo = false;
					}
				}
			}
        	
        	if (canGuo) {
        		currentActions.put(String.valueOf(Cnst.ACTION_GUO),new ArrayList<>());
			}
            return true;
        }




    }

    /**
     * 玩家申请解散房间
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100203(IoSession session, ProtocolData readData) throws Exception{
        logger.I("玩家请求解散房间,interfaceId -> 100203");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
        if (room.getDissolveRoom()!=null){
            return ;
        }
        DissolveRoom dis = new DissolveRoom();
        dis.setDissolveTime(new Date().getTime());
        dis.setUserId(userId);
        List<Map<String,Object>> othersAgree = new ArrayList<>();
        List<Player> players = getPlayerList(room);
        for(Player p:players){
            if (!p.getUserId().equals(userId)){
                Map<String,Object> map = new HashMap<>();
                map.put("userId",p.getUserId());
                map.put("agree",0);//1同意；2解散；0等待
                othersAgree.add(map);
            }
        }
        dis.setOthersAgree(othersAgree);
        room.setDissolveRoom(dis);

        Map<String,Object> info = new HashMap<>();
        info.put("dissolveTime",dis.getDissolveTime());
        info.put("userId",dis.getUserId());
        info.put("othersAgree",dis.getOthersAgree());
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        for(Player p:players){
            IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
            if(se!=null&&se.isConnected()){
                se.write(pd);
            }
        }

        for(Player p:players){
        	updateRedisData(null,p);
        }
        
        updateRedisData(room,null);
        //解散房间超时任务开启
        startDisRoomTask(room.getRoomId(),Cnst.DIS_ROOM_TYPE_2);
    }

    /**
     * 同意或者拒绝解散房间
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100204(IoSession session, ProtocolData readData) throws Exception{
        logger.I("同意或者拒绝解散房间,interfaceId -> 100203");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        Integer userAgree = obj.getInteger("userAgree");
        RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
        if (room==null){//房间已经自动解散
            Map<String,Object> info = new HashMap<>();
            info.put("reqState",Cnst.REQ_STATE_4);
            JSONObject result = getJSONObj(interfaceId,1,info);
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
            session.write(pd);
            return;
        }
        if (room.getDissolveRoom()==null){
            Map<String,Object> info = new HashMap<>();
            info.put("reqState",Cnst.REQ_STATE_7);
            JSONObject result = getJSONObj(interfaceId,1,info);
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
            session.write(pd);
            return;
        }
        List<Map<String,Object>> othersAgree = room.getDissolveRoom().getOthersAgree();
        for(Map m:othersAgree){
            if (String.valueOf(m.get("userId")).equals(String.valueOf(userId))){
                m.put("agree",userAgree);
                break;
            }
        }
        Map<String,Object> info = new HashMap<>();
        info.put("dissolveTime",room.getDissolveRoom().getDissolveTime());
        info.put("userId",room.getDissolveRoom().getUserId());
        info.put("othersAgree",room.getDissolveRoom().getOthersAgree());
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

        
        if(userAgree==2){
            room.setDissolveRoom(null);
        }
        int agreeNum = 0;
        int rejectNunm = 0;
        

        for(Map m:othersAgree){
            if (m.get("agree").equals(1)){
                agreeNum++;
            }else if(m.get("agree").equals(2)){
                rejectNunm++;
            }
        }

        updateRedisData(room,null);

        List<Player> players = getPlayerList(room);
        
        if (agreeNum==3||rejectNunm>=1){
        	if (agreeNum==3) {
        		MessageFunctions.setOverInfo(room,players);
				room.setStatus(Cnst.ROOM_STATE_YJS);
				room.setHasInsertRecord(true);
				if(String.valueOf(roomId).length()>6){
					//俱乐部房间
					MessageFunctions.updateClubDatabasePlayRecord(room);
				}else{
					MessageFunctions.updateDatabasePlayRecord(room);
				}	
				for(Player p:players){
			        p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
		        }
		        BackFileUtil.write(null, 100103, room,null,null);//写入文件内容
			}
            //关闭超时任务
            notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_2);
        	for(Player p:players){
            	updateRedisData(null,p);
            }
            updateRedisData(room,null);
            
        }
        
        for(Player p:players){
            IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
            if(se!=null&&se.isConnected()){
                se.write(pd);
            }
        }

    }

//    public static void main(String[] args) {
//		Integer i=22222;
//		Integer l=22222;
//		
//		System.out.println(i.equals(l));
//	}
    /**
     * 退出房间
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100205(IoSession session, ProtocolData readData) throws Exception{
        logger.I("退出房间,interfaceId -> 100205");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
        if (room==null){
            roomDoesNotExist(interfaceId,session);
            return;
        }
        if (room.getStatus().equals(Cnst.ROOM_STATE_CREATED)){
        	List<Player> players = getPlayerList(room);
            Map<String,Object> info = new HashMap<>();
            info.put("userId",userId);
            if (room.getCreateId().equals(userId)){//房主退出，
                if (room.getRoomType().equals(Cnst.ROOM_TYPE_1)){//房主模式
                	int circle = room.getCircleNum();
                	
                	//俱乐部更新redis
                    if(null != room.getClubId() && String.valueOf(room.getRoomId()).length() > 6){
                    	
//                    	ClubInfo redisClub = getClubInfoByClubId(room.getClubId().toString());
//                    	redisClub.setRoomCardNum(redisClub.getRoomCardNum()+Cnst.moneyMap.get(circle));
//                    	setClubInfoByClubId(room.getClubId().toString(), redisClub);
                    	
                    	JSONArray jsonArray = getClubRoomListByClubId(room.getClubId().toString());
                    	for(int a=0;a<jsonArray.size();a++){
                    		RoomResp roomr = JSONObject.parseObject(jsonArray.get(a).toString(), RoomResp.class);
                    		if(roomr.getRoomId().equals(room.getRoomId()) || roomr.getRoomId() == room.getRoomId()){
                    			jsonArray.remove(a);
                    			setClubRoomListByClubId(room.getClubId().toString(), jsonArray);
                    		}
                    	}
                    }
                    
                	room = null;
                    info.put("type",Cnst.EXIST_TYPE_DISSOLVE);
                    
                    for(Player p:players){
                    	if (p.getUserId().equals(userId)) {
        					p.setMoney(p.getMoney()+Cnst.moneyMap.get(circle));
        					break;
        				}
                    }

                    deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)));
                    
                    for(Player p:players){
                        p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
                    }
                }else{//自由模式，走正常退出
                    info.put("type",Cnst.EXIST_TYPE_EXIST);
                    existRoom(room, players, userId);
                	updateRedisData(room,null);
                }
            }else{//正常退出
                info.put("type",Cnst.EXIST_TYPE_EXIST);
                existRoom(room, players, userId);
            	updateRedisData(room,null);
            }
            JSONObject result = getJSONObj(interfaceId,1,info);
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
            
            for(Player p:players){
            	updateRedisData(null,p);
            }
            
            for(Player p : players){
            	IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
                if (se!=null&&se.isConnected()){
                    se.write(pd);
                }
            }
            
        }else{
            roomIsGaming(interfaceId,session);
        }
    }

    private static void existRoom(RoomResp room,List<Player> players,Long userId){
    	for(Player p:players){
        	if (p.getUserId().equals(userId)) {
        		p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
        		break;
			}
        }
        Long[] pids = room.getPlayerIds();
        if (pids!=null) {
			for(int i=0;i<pids.length;i++){
				if (userId.equals(pids[i])) {
					pids[i] = null;
					break;
				}
			}
		}
    }


    /**
     * 语音表情
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100206(IoSession session, ProtocolData readData) throws Exception{
        logger.I("语音表情,interfaceId -> 100206");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        String type = obj.getString("type");
        String idx = obj.getString("idx");
        Map<String,Object> info = new HashMap<>();
        info.put("roomId",roomId);
        info.put("userId",userId);
        info.put("type",type);
        info.put("idx",idx);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        List<Player> players = getPlayerList(roomId);
        for(Player p:players){
            if (!p.getUserId().equals(userId)){
                IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
                if (se!=null&&se.isConnected()){
                    se.write(pd);
                }
            }
        }
    }
    
    
    
    /**
     * 补牌指令
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100207(IoSession session, ProtocolData readData) throws Exception{
        logger.I("补牌指令,interfaceId -> 100207");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        Integer wsw_sole_action_id = obj.getInteger("wsw_sole_action_id");
        
        Player currentPlayer = getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)));

        RoomResp room = getRoomRespByRoomId(String.valueOf(currentPlayer.getRoomId()));
        
        if (!room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			MessageFunctions.interface_100108(session);
			return ;
		}
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_1);
        
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);

        updateRedisData(room,currentPlayer);
        MessageFunctions.interface_100101(session, readData);
    }
    
    /**
     * 潇洒指令
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100208(IoSession session, ProtocolData readData) throws Exception{
        logger.I("潇洒指令,interfaceId -> 100208");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        Integer xiaoSa = obj.getInteger("xiaoSa");
        Integer wsw_sole_action_id = obj.getInteger("wsw_sole_action_id");
        if (xiaoSa==null) {
			illegalRequest(interfaceId, session);
			return;
		}
        
        Player currentPlayer = getPlayerByUserId(String.valueOf(userId));
        if (currentPlayer.getXiaoSa()==null||!currentPlayer.getXiaoSa().equals(Cnst.XIAO_SA_NO_CHOOSE)) {
			return;
		}
        
        
        RoomResp room = getRoomRespByRoomId(String.valueOf(currentPlayer.getRoomId()));
        //设置递增id
        if (!room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			MessageFunctions.interface_100108(session);
			return ;
		}else{
			room.setWsw_sole_action_id(wsw_sole_action_id+1);
		}
        if (xiaoSa.equals(Cnst.XIAO_SA_NO)||xiaoSa.equals(Cnst.XIAO_SA_YES)) {
            currentPlayer.setXiaoSa(xiaoSa);
		}else {
			illegalRequest(interfaceId, session);
			return;
		}
        updateRedisData(room, currentPlayer);
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_1);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        
        
        List<Player> players = getPlayerList(room);
        List<Player> others = new ArrayList<Player>();
        Player nextUser = null;
        for(Player p:players){
        	if (!p.getUserId().equals(userId)) {
        		others.add(p);
			}
        }
        if (currentPlayer.getPosition()==Cnst.WIND_NORTH) {
    		nextUser = players.get(Cnst.WIND_EAST-1);
		}else{
    		nextUser = players.get(currentPlayer.getPosition());
		}
        
        if (!checkOthersAction(others, currentPlayer.getChuList().get(currentPlayer.getChuList().size()-1), currentPlayer, nextUser, room, players)) {
        	nextUser.setNeedFaPai(true);
        	nextUser.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
		}
        
        

		setPlayersList(players);
        updateRedisData(room, null);
        
        if (xiaoSa.equals(Cnst.XIAO_SA_YES)) {
			MessageFunctions.interface_100110(currentPlayer,getActionPlayer(players),room,players);
		}else{
			MessageFunctions.interface_100110(null,getActionPlayer(players),room,players);
		}
    }
    
    
    
    
    
    
    public static String getShowPaiString(List<Integer[][]> pais){
    	StringBuffer sb = new StringBuffer();
    	if (pais!=null&&pais.size()>0) {
			for(Integer[][] pai:pais){
				sb.append(pai[0][0]+"_"+pai[0][1]+"\t\t");
			}
		}
    	return sb.toString();
    }
    
    
    /**
     * 开局请求牌信息
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100209(IoSession session, ProtocolData readData) throws Exception{
        logger.I("开局请求牌信息,interfaceId -> 100209");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");

        //基本校验
        if (roomId==null) {
			illegalRequest(interfaceId, session);
			return;
		}
        Long sessionUid = (Long) session.getAttribute(Cnst.USER_SESSION_USER_ID);
        if (sessionUid==null||userId==null||!sessionUid.equals(userId)) {
			illegalRequest(interfaceId, session);
			return;
		}
        RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
        Long[] uIds = room.getPlayerIds();
        boolean hasUser = false;
        for(Long uid:uIds){
        	if(uid!=null&&uid.equals(userId)){
        		hasUser = true;
        		break;
        	}
        }
        if (!hasUser) {
			illegalRequest(interfaceId, session);
			return;
		}
        //基本校验
        
        Map<String,Object> info = new HashMap<String, Object>();
        List<Player> players = getPlayerList(room);
        if (players!=null&&players.size()>0) {
        	List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        	for(Player p:players){
        		Map<String,Object> m = new HashMap<String, Object>();
        		if (p.getUserId().equals(userId)) {
            		m.put("cmj", p.getCurrentMjList());
				}else{
            		m.put("cmj", p.getCurrentMjList().size());
				}
        		m.put("ps", p.getPlayStatus());
        		m.put("nf", p.getNeedFaPai());
        		list.add(m);
            }
        	info.put("uinfo", list);
		}
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        
        if (session!=null&&session.isConnected()) {
			session.write(pd);
		}
    }
    

    
    private static Map<String,Object> getRoomBaseInfoForStart(RoomResp room){
    	if (room==null) {
			return null;
		}
    	Map<String,Object> rinfo = new HashMap<String, Object>();
        rinfo.put("status",room.getStatus());
        rinfo.put("lastNum",room.getLastNum()-1);
        rinfo.put("cnrrMJNum",room.getCurrentMjList()==null?0:room.getCurrentMjList().size());
    	return rinfo;
    }
    
    /**
     * 加入房间准备之后，玩家请求其他人信息和房间信息
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100210(IoSession session, ProtocolData readData) throws Exception{
        logger.I("玩家请求其他人信息和房间信息,interfaceId -> 100210");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        //基本校验
        if (roomId==null) {
			illegalRequest(interfaceId, session);
			return;
		}
        Long sessionUid = (Long) session.getAttribute(Cnst.USER_SESSION_USER_ID);
        if (sessionUid==null||userId==null||!sessionUid.equals(userId)) {
			illegalRequest(interfaceId, session);
			return;
		}
        RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
        Long[] uIds = room.getPlayerIds();
        boolean hasUser = false;
        for(Long uid:uIds){
        	if(uid!=null&&uid.equals(userId)){
        		hasUser = true;
        		break;
        	}
        }
        if (!hasUser) {
			illegalRequest(interfaceId, session);
			return;
		}
        //基本校验
        
        List<Player> players = getPlayerList(room);
        Map<String,Object> info = new HashMap<String, Object>();
        if (players!=null&&players.size()>0) {
        	List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			for(Player p:players){
				if (!p.getUserId().equals(userId)) {
					Map<String,Object> i = new HashMap<String, Object>();
					getOneUserBaseInfo(i, p);
					list.add(i);
				}
			}
			info.put("uinfo",list);
			info.put("rinfo", getRoomBaseInfo(room));
			
		}
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        
        if (session!=null&&session.isConnected()) {
			session.write(pd);
		}
    }
    
    private static Map<String,Object> getRoomBaseInfo(RoomResp room){
    	if (room==null) {
			return null;
		}
    	Map<String,Object> rinfo = new HashMap<String, Object>();
        rinfo.put("userId",room.getCreateId());
        rinfo.put("status",room.getStatus());
        rinfo.put("maxScore",room.getMaxScoreInRoom());
        rinfo.put("lastNum",room.getLastNum()-1);
        rinfo.put("totalNum",room.getCircleNum());
        rinfo.put("cnrrMJNum",room.getCurrentMjList()==null?0:room.getCurrentMjList().size());
        rinfo.put("roomType",room.getRoomType());
        rinfo.put("hunPai",room.getHunPai());
        rinfo.put("dianType",room.getDianType());
        rinfo.put("xiaoSa",room.getXiaoSa());
        rinfo.put("liangFeng",room.getLiangFeng());
//        rinfo.put("xiType",room.getXiType());//获取喜类型
        rinfo.put("daHun",room.getDaHun());
        rinfo.put("siHunHu",room.getSiHunHu());
//        rinfo.put("globalType",room.getGlobalType());
        rinfo.put("ct",room.getCreateTime());
    	return rinfo;
    }
    
    /**
     * 获取单个用户的基本信息
     * @param i
     * @param p
     */
    private static void getOneUserBaseInfo(Map<String,Object> i,Player p){
    	i.put("userId", p.getUserId());
    	i.put("playStatus",p.getPlayStatus()); 
    	i.put("position",p.getPosition());
        i.put("score",p.getScore());
        i.put("status",p.getStatus());
        i.put("openName",p.getUserName());
        i.put("openImg",p.getUserImg());
        i.put("gender",p.getGender());
        i.put("ip",p.getIp());
        i.put("joinIndex",p.getJoinIndex());
        i.put("money",p.getMoney());
        i.put("zhuang",p.getZhuang());
    }

}
