package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.InfoCount;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.MahjongUtils;

/**
 * Created by Administrator on 2017/7/10. 推送消息类
 */
public class MessageFunctions extends TCPGameFunctions {

	/**
	 * 发送玩家信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100100(IoSession session, ProtocolData readData)
			throws Exception {
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Map<String, Object> info = new HashMap<>();
		if (interfaceId.equals(100100)) {// 刚进入游戏主动请求
			String openId = obj.getString("openId");
			Player currentPlayer = null;
			String cid = null;
			if (openId == null) {
				Long userId = obj.getLong("userId");
				if (userId == null) {
					illegalRequest(interfaceId, session);
					return;
				} else {
					currentPlayer = getPlayerByUserId(String.valueOf(session
							.getAttribute(Cnst.USER_SESSION_USER_ID)));
				}
			} else {
				String ip = (String) session.getAttribute(Cnst.USER_SESSION_IP);
				cid = obj.getString("cId");
				currentPlayer = HallFunctions.getPlayerInfos(openId, ip, cid,
						session);
			}
			if (currentPlayer == null) {
				illegalRequest(interfaceId, session);
				return;
			}

			// 更新心跳为最新上线时间
			currentPlayer.setLastHeartTimeLong(new Date().getTime());
			if (cid != null) {
				currentPlayer.setCid(cid);
			}
			// setIoSessionByUserId(String.valueOf(p.getUserId()),
			// session);//更新sesison
			// ioSessionMap.put(p.getUserId(), session);//更新sesison
			currentPlayer.setSessionId(session.getId());// 更新sesisonId
			session.setAttribute(Cnst.USER_SESSION_USER_ID,
					currentPlayer.getUserId());
			if (openId != null) {
				setUserIdByOpenId(openId,
						String.valueOf(currentPlayer.getUserId()));
			}

			RoomResp room = null;
			List<Player> players = null;
			if (currentPlayer.getRoomId() != null) {// 玩家下有roomId，证明在房间中
				room = getRoomRespByRoomId(String.valueOf(currentPlayer
						.getRoomId()));
				if (room != null
						&& !room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
					info.put("roomInfo", getRoomInfo(room));
					players = getPlayerList(room);
					for (int m = 0; m < players.size(); m++) {
						Player p = players.get(m);
						if (p.getUserId().equals(currentPlayer.getUserId())) {
							players.set(m, currentPlayer);
							break;
						}
					}

					List<Map<String, Object>> anotherUsers = new ArrayList<>();
					for (Player pp : players) {
						if (!pp.getUserId().equals(currentPlayer.getUserId())) {
							anotherUsers.add(getAnotherUsers(pp));
						}
					}
					info.put("anotherUsers", anotherUsers);

				} else {
					currentPlayer.initPlayer(null, null, null,
							Cnst.PLAYER_STATE_DATING, 0, 0, 0);
				}

			}

			updateRedisData(room, currentPlayer);
			info.put("currentUser", getCurrentUserMap(currentPlayer));

			if (room != null) {
				// room.setWsw_sole_main_id(room.getWsw_sole_main_id()+1);

				info.put("wsw_sole_main_id", room.getWsw_sole_main_id());
				info.put("wsw_sole_action_id", room.getWsw_sole_action_id());
				Map<String, Object> roomInfo = (Map<String, Object>) info
						.get("roomInfo");
				List<Map<String, Object>> anotherUsers = (List<Map<String, Object>>) info
						.get("anotherUsers");

				info.remove("roomInfo");
				info.remove("anotherUsers");

				JSONObject result = getJSONObj(interfaceId, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				session.write(pd);

				info.remove("currentUser");
				info.put("roomInfo", roomInfo);
				result = getJSONObj(interfaceId, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);

				info.remove("roomInfo");
				info.put("anotherUsers", anotherUsers);
				result = getJSONObj(interfaceId, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);

				MessageFunctions.interface_100109(players,
						Cnst.PLAYER_LINE_STATE_INLINE,
						currentPlayer.getUserId(), session,
						currentPlayer.getPlayStatus());
			} else {
				JSONObject result = getJSONObj(interfaceId, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				session.write(pd);
			}

		} else if (interfaceId != 100100) {
			Player currentPlayer = getPlayerByUserId(String.valueOf(session
					.getAttribute(Cnst.USER_SESSION_USER_ID)));
			if (currentPlayer == null) {// 如果session中，没有用户，关闭连接
				session.close(true);
				return;
			}
			RoomResp room = getRoomRespByRoomId(String.valueOf(currentPlayer
					.getRoomId()));
			List<Player> players = getPlayerList(room);
			for (int m = 0; m < players.size(); m++) {
				Player p = players.get(m);
				if (p.getUserId().equals(currentPlayer.getUserId())) {
					players.set(m, currentPlayer);
					break;
				}
			}

			room.setWsw_sole_main_id(room.getWsw_sole_main_id() + 1);
			updateRedisData(room, null);
			for (Player p : players) {
				info = new HashMap<>();
				info.put("wsw_sole_main_id", room.getWsw_sole_main_id());
				info.put("wsw_sole_action_id", room.getWsw_sole_action_id());

				info.put("roomInfo", getRoomInfo(room));
				JSONObject result = getJSONObj(100100, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				IoSession se = MinaServerManager.tcpServer.getSessions().get(
						p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
				info.remove("roomInfo");

				info.put("currentUser", getCurrentUserMap(p));
				result = getJSONObj(100100, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
				info.remove("currentUser");

				List<Map<String, Object>> anotherUsers = new ArrayList<>();
				for (Player ops : players) {
					if (!ops.getUserId().equals(p.getUserId())) {
						anotherUsers.add(getAnotherUsers(ops));
					}
				}
				info.put("anotherUsers", anotherUsers);
				result = getJSONObj(100100, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
				info.remove("anotherUsers");
			}
		} else {
			session.close(true);
		}

	}

	// 封装房间信息
	public static Map<String, Object> getRoomInfo(RoomResp room) {
		Map<String, Object> roomInfo = new HashMap<>();
		roomInfo.put("userId", room.getCreateId());
		roomInfo.put("openName", room.getOpenName());
		roomInfo.put("roomSn", room.getRoomId());
		roomInfo.put("status", room.getStatus());
		roomInfo.put("maxScore", room.getMaxScoreInRoom());
		roomInfo.put("lastNum", room.getLastNum() - 1);
		roomInfo.put("totalNum", room.getCircleNum());
		roomInfo.put("cnrrMJNum", room.getCurrentMjList() == null ? 0 : room
				.getCurrentMjList().size());
		roomInfo.put("roomType", room.getRoomType());
		roomInfo.put("lastPai", room.getLastPai());
		roomInfo.put("lastUserId", room.getLastUserId());
		roomInfo.put("hunPai", room.getHunPai());
		roomInfo.put("dianType", room.getDianType());
		roomInfo.put("xiaoSa", room.getXiaoSa());
		roomInfo.put("liangFeng", room.getLiangFeng());
		roomInfo.put("daHun", room.getDaHun());
		roomInfo.put("siHunHu", room.getSiHunHu());
//		roomInfo.put("globalType", room.getGlobalType());
		roomInfo.put("ct", room.getCreateTime());
		roomInfo.put("dissolveRoom", room.getDissolveRoom());
		roomInfo.put("xjst", room.getXiaoJuStartTime());
		roomInfo.put("roomIp", room.getIp().concat(":").concat(Cnst.MINA_PORT));
		return roomInfo;
	}

	// 封装其他玩家信息
	private static Map<String, Object> getAnotherUsers(Player p) {
		Map<String, Object> anotherUsers = new HashMap<>();
		anotherUsers.put("userId", p.getUserId());
		anotherUsers.put("position", p.getPosition());
		anotherUsers.put("score", p.getScore());
		anotherUsers.put("status", p.getStatus());
		anotherUsers.put("playStatus", p.getPlayStatus());
		anotherUsers.put("openName", p.getUserName());
		anotherUsers.put("openImg", p.getUserImg());
		anotherUsers.put("gender", p.getGender());
		anotherUsers.put("ip", p.getIp());
		anotherUsers.put("joinIndex", p.getJoinIndex());
		anotherUsers.put("money", p.getMoney());
		anotherUsers.put("zhuang", p.getZhuang());
		anotherUsers.put("needFaPai", p.getNeedFaPai());

		if (p.getLastFaPai() != null) {
			anotherUsers.put("lastFaPai", new Integer[][] { { -1, -1 } });
		}

		anotherUsers.put("kouTing", p.getKouTing());
		anotherUsers.put("xiaoSa", p.getXiaoSa());

		Map<String, Object> paiInfos = new HashMap<>();

		if (p.getXiaoSa() != null && p.getXiaoSa().equals(Cnst.XIAO_SA_YES)) {
			paiInfos.put("currentMjList", p.getCurrentMjList());
		} else {
			paiInfos.put("currentMjList", p.getCurrentMjList() == null ? null
					: p.getCurrentMjList().size());

		}
		paiInfos.put("chuList", p.getChuList());
		paiInfos.put("chiList", p.getChiList());
		paiInfos.put("pengList", p.getPengList());
		paiInfos.put("gangListType2", p.getGangListType2());
		paiInfos.put("gangListType3", p.getGangListType3());
		paiInfos.put("gangListType4", p.getGangListType4());
		if (p.getGangListType5() == null) {
			paiInfos.put("gangListType5", null);
		} else {
			List<Map<String, String>> list = new ArrayList<>();
			for (InfoCount info : p.getGangListType5()) {
				Map<String, String> map = new HashMap<>();
				map.put("t", info.getT().toString());
				map.put("l", null);
				list.add(map);
			}
			paiInfos.put("gangListType5", list);
		}

		anotherUsers.put("paiInfos", paiInfos);
		return anotherUsers;
	}

	// 封装当前玩家信息
	public static Map<String, Object> getCurrentUserMap(Player p) {
		Map<String, Object> currentUser = new HashMap<>();
		currentUser.put("version", String.valueOf(Cnst.version));
		currentUser.put("userId", p.getUserId());
		currentUser.put("position", p.getPosition());
		currentUser.put("score", p.getScore());
		currentUser.put("status", p.getStatus());
		currentUser.put("playStatus", p.getPlayStatus());
		currentUser.put("openName", p.getUserName());
		currentUser.put("gender", p.getGender());
		currentUser.put("openImg", p.getUserImg());
		currentUser.put("ip", p.getIp());
		currentUser.put("userAgree", p.getUserAgree());
		currentUser.put("money", p.getMoney());
		currentUser.put("joinIndex", p.getJoinIndex());
		currentUser.put("notice", p.getNotice());
		currentUser.put("lastFaPai", p.getLastFaPai());
		currentUser.put("needFaPai", p.getNeedFaPai());
		currentUser.put("kouTing", p.getKouTing());
		currentUser.put("xiaoSa", p.getXiaoSa());
		currentUser.put("actions", p.getCurrentActions());
		currentUser.put("zhuang", p.getZhuang());

		// 牌的信息
		Map<String, Object> paiInfos = new HashMap<>();
		paiInfos.put("currentMjList", p.getCurrentMjList());
		paiInfos.put("chuList", p.getChuList());
		paiInfos.put("chiList", p.getChiList());
		paiInfos.put("pengList", p.getPengList());
		paiInfos.put("gangListType2", p.getGangListType2());
		paiInfos.put("gangListType3", p.getGangListType3());
		paiInfos.put("gangListType4", p.getGangListType4());
		paiInfos.put("gangListType5", p.getGangListType5());
		currentUser.put("paiInfos", paiInfos);
		return currentUser;
	}

	protected static Map<String, Object> getPaiInfo(Player p, boolean isSelf) {
		Map<String, Object> paiInfos = new HashMap<>();
		paiInfos.put("chuList", p.getChuList());
		paiInfos.put("chiList", p.getChiList());
		paiInfos.put("pengList", p.getPengList());

		paiInfos.put("gangListType2", p.getGangListType2());
		paiInfos.put("gangListType3", p.getGangListType3());
		paiInfos.put("gangListType4", p.getGangListType4());
		if (isSelf) {
			paiInfos.put("currentMjList", p.getCurrentMjList());
			paiInfos.put("gangListType5", p.getGangListType5());
		} else {
			if (p.getXiaoSa() != null && p.getXiaoSa().equals(Cnst.XIAO_SA_YES)) {
				paiInfos.put("currentMjList", p.getCurrentMjList());
			} else {
				paiInfos.put("currentMjList",
						p.getCurrentMjList() == null ? null : p
								.getCurrentMjList().size());
			}
			List<Map<String, String>> list = new ArrayList<>();
			for (InfoCount info : p.getGangListType5()) {
				Map<String, String> map = new HashMap<>();
				map.put("t", info.getT().toString());
				map.put("l", null);
				list.add(map);
			}
			paiInfos.put("gangListType5", list);
		}
		return paiInfos;

	}

	/**
	 * 发牌推送
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100101(IoSession session, ProtocolData readData)
			throws Exception {
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Player currentPlayer = getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)));

		RoomResp room = getRoomRespByRoomId(String.valueOf(currentPlayer
				.getRoomId()));

		/* 是否该给当前玩家发牌校验 */
		if (!currentPlayer.getNeedFaPai()) {
			System.err.println("系统发牌，当前状态不对！");
			return;
		}

		room.setWsw_sole_action_id(room.getWsw_sole_action_id() + 1);
		room.setLastPengGangUser(null);

		if (room.getCurrentMjList().size() == room.getDiXianPaiNum()) {
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_1);

			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			if (room.getStatus().equals(Cnst.ROOM_STATE_XJS)) {
				return;
			}
			liuJu(currentPlayer.getRoomId(), session);
			return;
		}

		/* 是否该给当前玩家发牌校验 */
		if (!currentPlayer.getNeedFaPai()) {
			return;
		}

		boolean haiLao = false;
		if (room.getCurrentMjList().size() <= room.getDiXianPaiNum() + 4) {
			haiLao = true;
		}
		room.setHaiLao(haiLao);

		currentPlayer.setNeedFaPai(false);
		currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);// 出牌状态

		Integer[][] pai = MahjongUtils.faPai(room.getCurrentMjList(), 1).get(0);

		currentPlayer
				.setLastFaPai(new Integer[][] { { pai[0][0], pai[0][1] } });

		currentPlayer.setZhuaPaiNum(currentPlayer.getZhuaPaiNum() + 1);

		boolean hasAction = false;
		hasAction = GameFunctions.checkActions(currentPlayer,
				currentPlayer.getLastFaPai(), false, currentPlayer, room, null);

		// 海捞之后，不能有其他动作
		if (haiLao && hasAction) {
			if (currentPlayer.getCurrentActions().containsKey(
					String.valueOf(Cnst.ACTION_HU))) {

				Iterator<String> iterator = currentPlayer.getCurrentActions()
						.keySet().iterator();
				while (iterator.hasNext()) {
					String str = iterator.next();
					if (!str.equals(String.valueOf(Cnst.ACTION_HU))) {
						iterator.remove();
					}
				}
			} else {
				currentPlayer.setCurrentActions(null);
				hasAction = false;
			}
		}
		if (hasAction) {
			if (!currentPlayer.getCurrentActions().containsKey(
					String.valueOf(Cnst.ACTION_TING))) {
				currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
			}
		}
		updateRedisData(room, currentPlayer);

		List<Player> players = getPlayerList(room);
		for (int m = 0; m < players.size(); m++) {
			Player p = players.get(m);
			if (p.getUserId().equals(currentPlayer.getUserId())) {
				players.set(m, currentPlayer);
				break;
			}
		}
		Player nextUser = null;
		if (currentPlayer.getPosition().equals(Cnst.WIND_NORTH)) {
			nextUser = players.get(0);
		} else {
			nextUser = players.get(currentPlayer.getPosition());
		}
		if (!hasAction && haiLao) {
			nextUser.setNeedFaPai(true);
		}
		if (haiLao) {
			currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
		}

		updateRedisData(null, currentPlayer);
		if (nextUser != null) {
			updateRedisData(null, nextUser);
		}

		Map<String, Object> users = new HashMap<String, Object>();
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			Map<String, Object> info = new HashMap<>();
			if (p.getUserId().equals(currentPlayer.getUserId())) {// 给自己的信息
				info.put("userId", String.valueOf(p.getUserId()));
				info.put("pai", pai);
				if (hasAction) {
					Map<String, Object> actionInfo = new HashMap<String, Object>();
					actionInfo.put("actions", p.getCurrentActions());
					actionInfo.put("userId", p.getUserId());
					info.put("actionInfo", actionInfo);
				}
			} else {
				if (hasAction) {
					info.put("actionInfo", 1);
				}
				info.put("userId", String.valueOf(currentPlayer.getUserId()));
			}

			info.put("needFaPai", p.getNeedFaPai());
			info.put("playStatus", p.getPlayStatus());
			info.put("mjNum", room.getCurrentMjList().size());
			info.put("wsw_sole_main_id", room.getWsw_sole_main_id());
			info.put("wsw_sole_action_id", room.getWsw_sole_action_id());

			JSONObject result = getJSONObj(100101, 1, info);
			ProtocolData pd = new ProtocolData(100101, result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());

			// 写文件用
			if (p.getUserId().equals(currentPlayer.getUserId())) {
				users.put("faPaiUser", info);
			}

			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

		BackFileUtil.write(null, 100101, room, players, users);// 写入文件内容
	}

	/**
	 * 流局结算（小）
	 */
	public static void liuJu(Integer roomId, IoSession session) {
		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		room.setStatus(Cnst.ROOM_STATE_XJS);
		List<Player> players = getPlayerList(room);
		Map<String, Object> info = new HashMap<>();
		List<Map<String, Object>> userInfos = new ArrayList<>();
		Player zhuang = null;
		// 取出当前庄家，并设置荒庄次数
		for (Player p : players) {
			if (p.getZhuang()) {
				zhuang = p;
				if (p.getHuangZhuangNum() == null) {
					p.setHuangZhuangNum(1);
				} else {
					p.setHuangZhuangNum(p.getHuangZhuangNum() + 1);
				}
				break;
			}
		}
		int huangZhuangScore = zhuang.getHuangZhuangNum()
				* Cnst.SCORE_HUANG_ZHUANG_BASE;// 荒庄基础分

		for (Player p : players) {
			p.setPlayStatus(Cnst.PLAYER_STATE_XJS);
			Map<String, Object> map = new HashMap<>();
			map.put("userId", p.getUserId());
			map.put("currentMjList", p.getCurrentMjList());
			map.put("chuList", p.getChuList());
			map.put("chiList", p.getChiList());
			map.put("pengList", p.getPengList());
			map.put("gangListType2", p.getGangListType2());
			map.put("gangListType3", p.getGangListType3());
			map.put("gangListType4", p.getGangListType4());
			map.put("gangListType5", p.getGangListType5());
			map.put("isWin", false);
			map.put("isDian", false);
			Integer gangScore = 0;
			map.put("gangScore", gangScore);
			int gzNum = room.getGetnZhuangNum() == null ? 0 : room
					.getGetnZhuangNum();
			map.put("genZhuangNum", gzNum);
			map.put("huangZhuangNum", zhuang.getHuangZhuangNum());
			map.put("genZhuangScore", 0);
			map.put("xiaoSa", p.getXiaoSa());
			int hs = huangZhuangScore;
			if (p.getZhuang()) {
				hs = huangZhuangScore * 3 * -1;
			}
			map.put("winScore", hs);
//			map.put("globalType", room.getGlobalType());

			p.setScore(p.getScore() + hs);
			map.put("score", p.getScore());
			userInfos.add(map);
		}

		Integer lastCircle = room.getLastNum();
		lastCircle--;
		
		if (lastCircle - 1 < 0) {
			room.setStatus(Cnst.ROOM_STATE_YJS);
		} else {
			room.setStatus(Cnst.ROOM_STATE_XJS);
		}

		setOverInfo(room, players);
		
		if (room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
			room.setHasInsertRecord(true);
			// 说明是俱乐部创建房间
			if (null != room.getClubId()
					&& String.valueOf(room.getRoomId()).length() > 6) {
				// 向数据库添加 玩家积分记录
				updateClubDatabasePlayRecord(room);
			} else {
				// 向数据库添加 玩家积分记录
				updateDatabasePlayRecord(room);
			}
		}
		
		

		room.setLastNum(lastCircle);
		info.put("userInfos", userInfos);
		info.put("lastNum", lastCircle);

		ProtocolData pd = null;
		for (Player p : players) {
			// 清空玩家的牌数据
			p.initPlayer(p.getRoomId(), p.getPosition(), p.getZhuang(),
					p.getPlayStatus(), p.getScore(), p.getHuNum(),
					p.getLoseNum());

			Integer interfaceId = 100102;
			JSONObject result = getJSONObj(interfaceId, 1, info);
			pd = new ProtocolData(interfaceId, result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
			updateRedisData(null, p);
		}
		room.initRoom();
		updateRedisData(room, null);

		BackFileUtil.write(pd, 100102, room, players, info);// 写入文件内容

	}

	/**
	 * 包括诈胡 胡牌结算（小）
	 */
	public static void hu(Player huUser, Player toUser, Integer[][] pai,
			IoSession session, Map<Integer, Integer> huTYpeAndFans) {
		RoomResp room = getRoomRespByRoomId(String.valueOf(huUser.getRoomId()));

		if (huTYpeAndFans == null) {
			// huTYpeAndFans = MahjongUtils.checkHuInfo(huUser, toUser, pai,
			// room);

			List<Map<Integer, Integer>> allInfo = MahjongUtils.checkHuInfo(
					huUser, toUser, pai, room);
			Map<Integer, Integer> huInfo = null;

			int tempFan = 0;
			for (Map<Integer, Integer> map : allInfo) {
				int ttemp = 0;
				for (Integer key : map.keySet()) {
					ttemp += map.get(key);
				}
				if (ttemp > tempFan) {
					tempFan = ttemp;
					huTYpeAndFans = map;
				}
			}

		}
		
		boolean qiangGangHu = false;
		int winType = 0;
		if (huUser.getUserId().equals(toUser.getUserId())) {// 说明是自摸，不用加如手牌
			huUser.getCurrentMjList().add(
					new Integer[][] { { huUser.getLastFaPai()[0][0],
							huUser.getLastFaPai()[0][1] } });
			pai = new Integer[][] { { huUser.getLastFaPai()[0][0],
					huUser.getLastFaPai()[0][1] } };
			winType = Cnst.HU_TYPE_ZI_MO;
		} else {
			huUser.getCurrentMjList().add(pai);
			winType = Cnst.HU_TYPE_DIAN_PAO;
			if (room.getLastPengGangUser() != null
					&& room.getLastPengGangUser()[0] != null
					&& toUser.getUserId().equals(room.getLastPengGangUser()[0])) {// 是过杠胡的，要把点炮人的这个杠移除掉，加到碰里面
				InfoCount info = toUser.getGangListType3().get(
						toUser.getGangListType3().size() - 1);// 拿出洗后一个碰杠
				toUser.getGangListType3().remove(info);// 杠得集合移除当前元素
				info.getL().remove(0);// 当前是四个元素，需要移除一个元素加入碰的list中
				toUser.getPengList().add(info);// 加入碰的集合
				updateRedisData(null, toUser);// 更新玩家
				qiangGangHu = true;
				huTYpeAndFans.remove(Cnst.HU_GANG_HOU_DIAN_PAO);// 抢杠胡需要把杠后点炮的番数去掉
			} else if (room.getLastPengGangUser() != null
					&& room.getLastPengGangUser()[1] != null
					&& toUser.getUserId().equals(room.getLastPengGangUser()[1])) {
				InfoCount info = toUser.getGangListType2().get(
						toUser.getGangListType2().size() - 1);// 拿出洗后一个碰杠
				toUser.getGangListType2().remove(info);// 杠得集合移除当前元素
				updateRedisData(null, toUser);// 更新玩家
			}
		}
		updateRedisData(null, huUser);
		
		/**杠后点炮现在要放在点炮人身上，只对点炮人单独结算*/
		boolean gangHouDianPao = false;
		if (huTYpeAndFans.containsKey(Cnst.HU_GANG_HOU_DIAN_PAO)) {
			gangHouDianPao = true;
			huTYpeAndFans.remove(Cnst.HU_GANG_HOU_DIAN_PAO);
		}

		int fans = 0;// 不包含扣听以及潇洒的番
		StringBuffer sbWinInfo = new StringBuffer();
		for (Integer win : huTYpeAndFans.keySet()) {
			if (win.equals(Cnst.HU_DIAN_PAO)) {
				continue;
			}
			sbWinInfo.append(win).append("_");
			fans += huTYpeAndFans.get(win);
		}

		// 抢杠胡的番数
		if (qiangGangHu) {
			sbWinInfo.append(Cnst.HU_QIANG_GANG_HU).append("_");
			fans += Cnst.getFan(Cnst.HU_QIANG_GANG_HU);
		}

		// 玩家诈胡
		if (huUser.getZhaHu()) {
			if (MahjongUtils.isMenQing(huUser)) {
				fans++;
				sbWinInfo.append(Cnst.HU_MEN_QING).append("_");
			}
			if (MahjongUtils.checkMeiHun(huUser, toUser, pai, room) != 0) {
				fans++;
				sbWinInfo.append(Cnst.HU_MEI_HUN).append("_");
			}

			// 新龟腚：诈胡要加上自摸的番数
			fans++;
			sbWinInfo.append(Cnst.HU_ZI_MO).append("_");
		}

		if (huUser.getZhuang() && !huTYpeAndFans.containsKey(Cnst.HU_ZHAUNG)) {
			fans++;
			sbWinInfo.append(Cnst.HU_ZHAUNG).append("_");
		}

		String winInfo = sbWinInfo.toString();
		if (winInfo != null && winInfo.length() > 0) {
			winInfo = winInfo.substring(0, winInfo.length() - 1);
		}

		

		List<Player> players = getPlayerList(room);
		// 所有人的杠分+胡分，key为userId；value[0]为 胡分；value[1]为杠分
		Map<Long, Integer[]> allScores = null;

		if (winInfo.contains(String.valueOf(Cnst.HU_SI_HUN_HU))) {
			winInfo = String.valueOf(Cnst.HU_SI_HUN_HU);
			allScores = new HashMap<Long, Integer[]>();
			for (Player p : players) {
				Integer[] s = new Integer[2];
				if (p.getUserId().equals(huUser.getUserId())) {// 赢的人
					s[0] = Cnst.getFan(Integer.valueOf(winInfo)) * 3;// 四混胡牌加上手持四混的每人10分
				} else {
					s[0] = Cnst.getFan(Integer.valueOf(winInfo)) * -1;
				}
				s[1] = getGangScore(p, players);
				allScores.put(p.getUserId(), s);
			}

			int winS = getGangScore(huUser, players);
			allScores.get(huUser.getUserId())[1] = winS;// 庄家赢的分
		} else {
			if (huUser.getKouTing()) {
				fans += 2;// 扣听+2
			}
			if (huUser.getXiaoSa() != null
					&& huUser.getXiaoSa().equals(Cnst.XIAO_SA_YES)) {
				fans += 1;
			}
			allScores = getAllWinScore(players, huUser, winType, toUser, fans,
					room,gangHouDianPao);
		}

		boolean hasSiHun = MahjongUtils.checkSiHun(huUser.getCurrentMjList(),
				room);
		if (hasSiHun) {
			winInfo = winInfo.concat("_").concat(
					String.valueOf(Cnst.HU_SI_HUN_SCORE));
			for (Long uid : allScores.keySet()) {
				if (uid.equals(huUser.getUserId())) {
					allScores.get(uid)[0] += Cnst.SI_HUN_SCORE * 3;
				} else {
					allScores.get(uid)[0] = allScores.get(uid)[0]
							- Cnst.SI_HUN_SCORE;
				}
			}
		}

		Map<String, Object> info = new HashMap<>();
		List<Map<String, Object>> userInfos = new ArrayList<>();
		Player zhuangUser = null;
		for (Player p : players) {
			if (p.getZhuang()) {
				zhuangUser = p;
			}
			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("userId", p.getUserId());
			userInfo.put("currentMjList", p.getCurrentMjList());
			userInfo.put("chuList", p.getChuList());
			userInfo.put("chiList", p.getChiList());
			userInfo.put("pengList", p.getPengList());
			userInfo.put("gangListType2", p.getGangListType2());
			userInfo.put("gangListType3", p.getGangListType3());
			userInfo.put("gangListType4", p.getGangListType4());
			userInfo.put("gangListType5", p.getGangListType5());
			Integer gangScore = allScores.get(p.getUserId())[1];
			userInfo.put("isWin", p.getIsHu());
//			userInfo.put("isDian", p.getUserId().equals(toUser.getUserId()) && !p.getUserId().equals(huUser.getUserId()));//我也不知道以前咋特么这么写
			userInfo.put("isDian", p.getIsDian());
			if (p.getIsDian()&&gangHouDianPao) {
				userInfo.put("gd", Cnst.getFan(Cnst.HU_GANG_HOU_DIAN_PAO));
			}
			Integer currwinScore = allScores.get(p.getUserId())[0];
			userInfo.put("winScore", currwinScore);
			userInfo.put("gangScore", gangScore);
			p.setScore(p.getScore() + currwinScore + gangScore);
			userInfo.put("score", p.getScore());
			userInfo.put("xiaoSa", p.getXiaoSa());
			userInfo.put("kouTing", p.getKouTing());
//			userInfo.put("globalType", room.getGlobalType());
			if (p.getIsHu()) {
				userInfo.put("winInfo", winInfo);
				userInfo.put("fanInfo", fans);
				userInfo.put("zhaHu", p.getZhaHu());
			} else {
				int chiPengNum = 0;
				if (p.getChiList() != null) {
					chiPengNum += p.getChiList().size();
				}
				if (p.getPengList() != null) {
					chiPengNum += p.getPengList().size();
				}
				if (p.getGangListType3() != null) {
					chiPengNum += p.getGangListType3().size();
				}
				if (p.getGangListType4() != null) {
					chiPengNum += p.getGangListType4().size();
				}
				if (chiPengNum > 0) {
					userInfo.put("hasChiPeng", chiPengNum);
				}
			}
			userInfos.add(userInfo);

		}
		info.put("userInfos", userInfos);
		Integer lastCircle = room.getLastNum();
		lastCircle--;
		if (!huUser.getZhuang()) {
			Integer position = zhuangUser.getPosition();
			if (position == 4) {
				room.setCurrentJuNum(room.getCurrentJuNum() == null ? 1 : room
						.getCurrentJuNum() + 1);
				room.setZhuangId(players.get(0).getUserId());
			} else {
				room.setZhuangId(players.get(position).getUserId());
			}
			for (Player p : players) {
				if (p.getUserId().equals(room.getZhuangId())) {
					p.setZhuang(true);
				} else {
					p.setZhuang(false);
				}
			}
		}
		room.setLastNum(lastCircle);
		info.put("lastNum", lastCircle - 1);

		if (lastCircle - 1 < 0) {
			room.setStatus(Cnst.ROOM_STATE_YJS);
		} else {
			room.setStatus(Cnst.ROOM_STATE_XJS);
		}

		setOverInfo(room, players);

		if (room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
			room.setHasInsertRecord(true);
			if (null != room.getClubId()
					&& String.valueOf(room.getRoomId()).length() > 6) {
				// 向数据库添加 玩家积分记录
				updateClubDatabasePlayRecord(room);
			} else {
				// 向数据库添加 玩家积分记录
				updateDatabasePlayRecord(room);
			}
		}

		ProtocolData pd = null;
		for (Player p : players) {
			p.setHuangZhuangNum(0);// 清空所有玩家的荒庄次数
			// 清空玩家的牌数据
			p.initPlayer(p.getRoomId(), p.getPosition(), p.getZhuang(),
					p.getPlayStatus(), p.getScore(), p.getHuNum(),
					p.getLoseNum());

			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				JSONObject result = getJSONObj(100102, 1, info);
				pd = new ProtocolData(100102, result.toJSONString());
				se.write(pd);
			}
			updateRedisData(null, p);
		}
		room.initRoom();
		updateRedisData(room, null);
		BackFileUtil.write(pd, 100102, room, players, info);// 写入文件内容
	}

	/**
	 * 返回值为：userId-[huScore,gangScore] 计算项有：
	 * 诈胡、包三家、封顶分、非四混胡牌的手持四混的分、输家的加翻项（1、开门玩家，每开一个，多出一番；2、输的玩家潇洒，加翻；3、庄输了，加翻）
	 * 不包含四混胡牌的分数
	 * 
	 * @param players
	 * @param winUser
	 * @param winType
	 *            自摸还是点炮
	 * @param dianUser
	 * @param baseFan
	 * @param room
	 * @return
	 */
	public static Map<Long, Integer[]> getAllWinScore(List<Player> players,
			Player winUser, Integer winType, Player dianUser, Integer baseFan,
			RoomResp room,boolean gangHouDian) {
		Map<Long, Integer[]> wins = new HashMap<Long, Integer[]>();
		// boolean hasSiHun =
		// MahjongUtils.checkSiHun(winUser.getCurrentMjList(), room);
		if (winUser.getZhaHu()) {
			// baseFan = winUser.getZhHuFans();
			// if (MahjongUtils.isMenQing(winUser)) {
			// baseFan++;
			// }
			// if (MahjongUtils.checkMeiHun(winUser, null,
			// winUser.getCurrentMjList(), room.getHunPai(), room)!=0) {
			// baseFan++;
			// }
			int winUserScores = 0;
			// 不管自摸还是点炮，先算出所有玩家的钱，即默认为三家付
			for (Player p : players) {
				int addFan = 0;// 输的玩家的加番项，包括：1、开门玩家，每开一个，多出一番；2、输的玩家潇洒，加翻；3、庄输了，加翻
				if (!p.getUserId().equals(winUser.getUserId())) {
					Integer[] s = new Integer[2];
					if (p.getChiList() != null && p.getChiList().size() > 0) {//
						addFan += p.getChiList().size();
					}
					if (p.getPengList() != null && p.getPengList().size() > 0) {//
						addFan += p.getPengList().size();
					}
					if (p.getGangListType3() != null
							&& p.getGangListType3().size() > 0) {//
						addFan += p.getGangListType3().size();
					}
					if (p.getGangListType4() != null
							&& p.getGangListType4().size() > 0) {//
						addFan += p.getGangListType4().size();
					}
					if (p.getXiaoSa() != null
							&& p.getXiaoSa().equals(Cnst.XIAO_SA_YES)) {// 玩家潇洒
						addFan++;
					}

					if (p.getKouTing()) {// 玩家扣听
						addFan += 2;
					}
					if (p.getZhuang()) {// 诈胡时，番数里没有算庄的
						addFan++;
					}

					int lose = (int) Math.pow(2, baseFan + addFan);
					// if (hasSiHun) {
					// lose+=Cnst.SI_HUN_SCORE;
					// }

					if (!room.getMaxScoreInRoom().equals(0)) {// 跟封顶分对比
						if (lose > room.getMaxScoreInRoom()) {
							lose = room.getMaxScoreInRoom();
						}
					}
					s[0] = lose;
					s[1] = getGangScore(p, players);
					wins.put(p.getUserId(), s);
					winUserScores -= lose;
				}
			}
			Integer[] s = new Integer[2];
			s[0] = winUserScores;
			wins.put(winUser.getUserId(), s);

			int winS = getGangScore(winUser, players);
			wins.get(winUser.getUserId())[1] = winS;

		} else {
			int winUserScores = 0;
			// 不管自摸还是点炮，先算出所有玩家的钱，即默认为三家付
			for (Player p : players) {
				int addFan = 0;// 输的玩家的加番项，包括：1、开门玩家，每开一个，多出一番；2、输的玩家潇洒，加翻；3、庄输了，加翻
				if (!p.getUserId().equals(winUser.getUserId())) {
					Integer[] s = new Integer[2];
					if (p.getChiList() != null && p.getChiList().size() > 0) {//
						addFan += p.getChiList().size();
					}
					if (p.getPengList() != null && p.getPengList().size() > 0) {//
						addFan += p.getPengList().size();
					}
					if (p.getGangListType3() != null
							&& p.getGangListType3().size() > 0) {//
						addFan += p.getGangListType3().size();
					}
					if (p.getGangListType4() != null
							&& p.getGangListType4().size() > 0) {//
						addFan += p.getGangListType4().size();
					}
					if (p.getXiaoSa() != null
							&& p.getXiaoSa().equals(Cnst.XIAO_SA_YES)) {// 玩家潇洒
						addFan++;
					}
					if (p.getZhuang()) {
						addFan++;
					}
					if (p.getIsDian()) {
						addFan++;
						/**杠后点炮的番只针对点炮人单独算*/
						if (gangHouDian) {
							addFan+=Cnst.getFan(Cnst.HU_GANG_HOU_DIAN_PAO);
						}
					}
					
					
					

					int lose = (int) Math.pow(2, baseFan + addFan);
					// if (hasSiHun) {
					// lose+=Cnst.SI_HUN_SCORE;
					// }

					if (!room.getMaxScoreInRoom().equals(0)) {// 跟封顶分对比
						if (lose > room.getMaxScoreInRoom()) {
							lose = room.getMaxScoreInRoom();
						}
					}
					s[0] = lose * -1;
					s[1] = getGangScore(p, players);
					wins.put(p.getUserId(), s);
					winUserScores += lose;
				}
			}
			Integer[] s = new Integer[2];
			s[0] = winUserScores;
			wins.put(winUser.getUserId(), s);

			// 计算杠分
			int winS = getGangScore(winUser, players);
			wins.get(winUser.getUserId())[1] = winS;

			// 点炮的包法，由点炮人出
			if (room.getDianType().equals(Cnst.ROOM_DIAN_TYPE_DA_BAO)
					&& winType.equals(Cnst.HU_TYPE_DIAN_PAO)) {// 大包
				// 包杠 包胡
				int allGangScore = 0;
				int allHuScore = 0;
				for (Long uid : wins.keySet()) {
					if (!uid.equals(dianUser.getUserId())) {
						Integer[] temp = wins.get(uid);
						if (temp[1] < 0) {
							allGangScore += temp[1];
							temp[1] = 0;
						}
						if (!uid.equals(winUser.getUserId())) {
							allHuScore += temp[0];
							temp[0] = 0;
						}
					}
				}
				wins.get(dianUser.getUserId())[1] += allGangScore;
				wins.get(dianUser.getUserId())[0] += allHuScore;
			} else if (room.getDianType().equals(
					Cnst.ROOM_DIAN_TYPE_BAO_SAN_JIA)
					&& winType.equals(Cnst.HU_TYPE_DIAN_PAO)) {// 只包胡
				// 包杠 包胡
				int allHuScore = 0;
				for (Long uid : wins.keySet()) {
					if (!uid.equals(dianUser.getUserId())
							&& !uid.equals(winUser.getUserId())) {
						Integer[] temp = wins.get(uid);
						allHuScore += temp[0];
						temp[0] = 0;
					}
				}
				wins.get(dianUser.getUserId())[0] += allHuScore;
			}
		}

		return wins;
	}

	public static void setOverInfo(RoomResp room, List<Player> players) {
		List<Map<String, Object>> overInfoOld = room.getOverInfo();
		List<Map<String, Object>> xiaoJieSuanInfo = new ArrayList<Map<String, Object>>();

		List<Map<String, Object>> overInfo = new ArrayList<Map<String, Object>>();
		if (players != null && players.size() > 0) {
			for (int i = 0; i < players.size(); i++) {
				Player p = players.get(i);
				Map<String, Object> info = new HashMap<String, Object>();
				info.put("userId", p.getUserId());
				info.put("score", p.getScore());
				info.put("xjn", room.getXiaoJuNum());
				info.put("huNum", p.getHuNum() == null ? 0 : p.getHuNum());
				info.put("loseNum", p.getLoseNum() == null ? 0 : p.getLoseNum());
				info.put("dianNum", p.getDianNum() == null ? 0 : p.getDianNum());
				info.put("zhuangNum",
						p.getZhuangNum() == null ? 0 : p.getZhuangNum());
				info.put("zimoNum", p.getZimoNum() == null ? 0 : p.getZimoNum());
				info.put("position", p.getPosition());
				overInfo.add(info);

				Map<String, Object> xjsi = new LinkedHashMap<String, Object>();
				xjsi.put("openName", p.getUserName());
				xjsi.put("openImg", p.getUserImg());
				if (overInfoOld == null) {
					xjsi.put("score", p.getScore());
				} else {
					xjsi.put(
							"score",
							p.getScore()
									- (Integer) overInfoOld.get(i).get("score"));
				}
				xiaoJieSuanInfo.add(xjsi);
			}
		}
		room.setOverInfo(null);
		room.setOverInfo(overInfo);

		BackFileUtil.writeXiaoJieSuanInfo(room, xiaoJieSuanInfo);// 写入文件
	}

	public static void main(String[] args) {
		List<Player> players = new ArrayList<Player>();
		Player p1 = new Player();
		p1.setUserId(1L);
		p1.setIsDian(false);
		players.add(p1);

		Player p2 = new Player();
		p2.setUserId(2L);
		p2.setIsDian(true);
		players.add(p2);

		Player p3 = new Player();
		p3.setUserId(3L);
		p3.setIsDian(false);
		players.add(p3);
		List<InfoCount> gangT4 = new ArrayList<InfoCount>();
		InfoCount in1 = new InfoCount();
		gangT4.add(in1);
		List<Integer[][]> pais = new ArrayList<>();
		in1.setL(pais);
		p3.setGangListType3(gangT4);

		Player p4 = new Player();
		p4.setUserId(4L);
		p4.setIsDian(false);
		players.add(p4);
		gangT4 = new ArrayList<InfoCount>();
		in1 = new InfoCount();
		pais = new ArrayList<>();
		pais.add(new Integer[][] { { 1, 1 } });
		pais.add(new Integer[][] { { 1, 1 } });
		pais.add(new Integer[][] { { 1, 1 } });
		pais.add(new Integer[][] { { 1, 1 } });
		in1.setL(pais);
		gangT4.add(in1);

		in1 = new InfoCount();
		pais = new ArrayList<>();
		pais.add(new Integer[][] { { 1, 1 } });
		in1.setL(pais);
		gangT4.add(in1);

		in1 = new InfoCount();
		pais = new ArrayList<>();
		pais.add(new Integer[][] { { 1, 1 } });
		in1.setL(pais);
		gangT4.add(in1);
		p4.setGangListType2(gangT4);

		for (Player p : players) {
			p.setZhaHu(false);
		}

		Map<Long, Integer[]> wins = new LinkedHashMap<Long, Integer[]>();
		for (Player p : players) {
			Integer i = getGangScore(p, players);
			wins.put(p.getUserId(), new Integer[] { 0, i });
			System.out.println(i);
		}

		Integer allGangScore = 0;
		Integer allHuScore = 0;
		for (Long uid : wins.keySet()) {
			if (!uid.equals(p2.getUserId())) {
				Integer[] temp = wins.get(uid);
				if (temp[1] < 0) {
					allGangScore += temp[1];
					temp[1] = 0;
				}
				if (!uid.equals(p1.getUserId())) {
					allHuScore += temp[0];
					temp[0] = 0;
				}
			}
		}
		wins.get(p2.getUserId())[1] += allGangScore;
		wins.get(p2.getUserId())[0] += allHuScore;
		for (Long uid : wins.keySet()) {
			System.out.println(wins.get(uid)[1]);
		}
		System.out.println();

	}

	private static Integer getGangScore(Player p, List<Player> players) {
		int finalScore = 0;
		for (Player ps : players) {
			if (ps.getUserId().equals(p.getUserId()) && !ps.getIsDian()) {// 点炮人的杠分不计
				finalScore = getPersonalGangScore(ps, finalScore, 1, p);
			} else {
				finalScore = getPersonalGangScore(ps, finalScore, -1, p);
			}
		}
		return finalScore;
	}

	/**
	 * type=1,+ type=-1,-
	 */
	private static Integer getPersonalGangScore(Player p, int score,
			Integer type, Player current) {
        Integer temp = 0;
        if (p.getIsDian()) {
			return score;
		}
        if (p.getGangListType2()!=null&&p.getGangListType2().size()>0&&p.getGangListType2().get(0).getL().size()==4){//起手东南西北杠
//            for(InfoCount info:p.getGangListType2()){
//            	temp+=info.getL().size()*Cnst.GANG_TYPE_SPECIAL;
//            }
        	//新龟腚：补的风不算杠，凑齐4张后，只算4张的分；只有四分，补多少都不算分
        	temp+=p.getGangListType2().get(0).getL().size()*Cnst.GANG_TYPE_SPECIAL;
        }
        if (p.getGangListType3()!=null&&p.getGangListType3().size()>0){
        	temp+=p.getGangListType3().size()*Cnst.GANG_TYPE_MING;
        }
        if (p.getGangListType4()!=null&&p.getGangListType4().size()>0){
        	temp+=p.getGangListType4().size()*Cnst.GANG_TYPE_MING;
        }
        if (p.getGangListType5()!=null&&p.getGangListType5().size()>0){
        	temp+=p.getGangListType5().size()*Cnst.GANG_TYPE_AN;
        }
        if (type>0){//计算赢的钱，赢三份儿
        	temp = temp*3;
        	if (p.getZhaHu()) {
				temp = temp*-1;
			}
        }else{//计算输的钱，输一份儿
        	temp = temp*-1;
        	if (p.getZhaHu()) {
				temp = temp*-1;
			}
        }
        score+=temp;
        return score;
    }

	/**
	 * type=1,+ type=-1,-
	 */
	private static Integer getPersonalGangScore1(Player winUser, RoomResp room) {
		Integer temp = 0;
		if (winUser.getGangListType3() != null
				&& winUser.getGangListType3().size() > 0) {
			for (InfoCount info : winUser.getGangListType3()) {
				if (info.getL().get(0)[0][0]
						.equals(room.getHunPai().get(1)[0][0])
						&& info.getL().get(0)[0][1].equals(room.getHunPai()
								.get(1)[0][1])) {
					temp += 2;
				} else {
					temp++;
				}
			}
		}
		if (winUser.getGangListType4() != null
				&& winUser.getGangListType4().size() > 0) {
			for (InfoCount info : winUser.getGangListType4()) {
				if (info.getL().get(0)[0][0]
						.equals(room.getHunPai().get(1)[0][0])
						&& info.getL().get(0)[0][1].equals(room.getHunPai()
								.get(1)[0][1])) {
					temp += 2;
				} else {
					temp++;
				}
			}
		}
		if (winUser.getGangListType5() != null
				&& winUser.getGangListType5().size() > 0) {
			temp += winUser.getGangListType5().size() * 2;
		}

		// 加入亮风的分

		temp = temp * 3;
		if (winUser.getZhaHu()) {
			temp = temp * -1;
		}
		return temp;
	}

	/**
	 * 大结算
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100103(IoSession session, ProtocolData readData) {
		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
		Integer interfaceId = obj.getInteger("interfaceId");
		Integer roomId = obj.getInteger("roomSn");
		RoomResp room = getRoomRespByRoomId(String.valueOf(roomId));
		List<Map<String, Object>> info = new ArrayList<>();

		List<Map<String, Object>> overInfoList = room.getOverInfo();
		if (overInfoList != null && overInfoList.size() > 0) {
			for (Map<String, Object> infoMap : overInfoList) {
				info.add(infoMap);
			}
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);

			Player p = getPlayerByUserId(String.valueOf(session
					.getAttribute(Cnst.USER_SESSION_USER_ID)));
			p.initPlayer(null, null, null, Cnst.PLAYER_STATE_DATING, 0, 0, 0);

			room.setOutNum(room.getOutNum() == null ? 1 : room.getOutNum() + 1);

			if (room.getHasInsertRecord() == null || !room.getHasInsertRecord()) {
				room.setHasInsertRecord(true);
				if (null != room.getClubId()
						&& String.valueOf(room.getRoomId()).length() > 6) {
					// 向数据库添加 玩家积分记录
					updateClubDatabasePlayRecord(room);
				} else {
					// 向数据库添加 玩家积分记录
					updateDatabasePlayRecord(room);
				}
			}
			updateRedisData(room, p);
//			if(room.getGlobalType()!=1){//鲁麻巧解散房间
//				Long[] playerIds = room.getPlayerIds();
//				for (Long long1 : playerIds) {
//					deleteByKey(getStringByKey(Cnst.REDIS__USER_NOW_SCORE.concat(long1.toString())));
//					deleteByKey(getStringByKey(Cnst.REDIS__USER_SCORE.concat(long1.toString())));
//				}
//			}
			if (room.getOutNum() == 4) {
				deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String
						.valueOf(roomId)));
				room = null;
			}
		}
	}

	/**
	 * 非俱乐部 向数据库添加玩家分数信息
	 */
	public static void updateDatabasePlayRecord(RoomResp room) {
		if (room == null)
			return;
		Integer roomId = room.getRoomId();
		// 刷新数据库
		roomService.updateRoomState(roomId, Long.parseLong(room.getCreateTime()), room.getXiaoJuNum());
		
		PlayerRecord playerRecord = new PlayerRecord();
		playerRecord.setRoomId(roomId);
		playerRecord.setStartTime(String.valueOf(room.getCreateTime()));
		playerRecord.setEndTime(String.valueOf(new Date().getTime()));
		//删除记录分数的key
		Long[] playerIds = room.getPlayerIds();
		for (Long long1 : playerIds) {
			deleteJedisScore(String.valueOf(long1));
		}
		List<Map<String, Object>> overInfoList = room.getOverInfo();
		if (overInfoList != null && overInfoList.size() > 0) {
			for (Map<String, Object> infoMap : overInfoList) {
				if ((int) (infoMap.get("position")) == Cnst.WIND_EAST) {
					
					playerRecord.setEastUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setEastUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setEastUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_SOUTH) {
				
					playerRecord.setSouthUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setSouthUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setSouthUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_WEST) {
					
					playerRecord.setWestUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setWestUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setWestUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_NORTH) {
					
					playerRecord.setNorthUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setNorthUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setNorthUserMoneyRemain((int) infoMap
							.get("score"));
				} else {
					return;
				}
			}
		} else {
			return;
		}
		userService.insertPlayRecord(playerRecord);
	}

	/**
	 * 俱乐部 向数据库添加玩家分数信息
	 */
	public static void updateClubDatabasePlayRecord(RoomResp room) {
		if (room == null)
			return;
		Integer roomId = room.getRoomId();
		// 刷新数据库
		clubGameRoomService.updateRoomState(roomId, Long.parseLong(room.getCreateTime()), room.getXiaoJuNum());
		
		PlayerRecord playerRecord = new PlayerRecord();
		playerRecord.setRoomId(roomId);
		playerRecord.setClubId(room.getClubId());// 俱乐部id
		playerRecord.setStartTime(String.valueOf(room.getCreateTime()));
		playerRecord.setEndTime(String.valueOf(new Date().getTime()));
		//删除记录分数的key
		Long[] playerIds = room.getPlayerIds();
		for (Long long1 : playerIds) {
			deleteJedisScore(String.valueOf(long1));
		}
		List<Map<String, Object>> overInfoList = room.getOverInfo();
		if (overInfoList != null && overInfoList.size() > 0) {
			for (Map<String, Object> infoMap : overInfoList) {
				if ((int) (infoMap.get("position")) == Cnst.WIND_EAST) {
					playerRecord.setEastUserId(String.valueOf(infoMap
							.get("userId")));
					
					playerRecord.setEastUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setEastUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_SOUTH) {
					
					playerRecord.setSouthUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setSouthUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setSouthUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_WEST) {
					
					playerRecord.setWestUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setWestUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setWestUserMoneyRemain((int) infoMap
							.get("score"));
				} else if ((int) (infoMap.get("position")) == Cnst.WIND_NORTH) {
					
					playerRecord.setNorthUserId(String.valueOf(infoMap
							.get("userId")));
					playerRecord.setNorthUserMoneyRecord((int) infoMap
							.get("score"));
					playerRecord.setNorthUserMoneyRemain((int) infoMap
							.get("score"));
				} else {
					return;
				}
			}
		} else {
			return;
		}
		clubGamePlayRecordService.insertPlayRecord(playerRecord);
	}

	/**
	 * 玩家动作执行后的提示
	 * 
	 * @param roomId
	 * @param p
	 */
	public static void interface_100111bak(Integer roomId, Player p,
			IoSession session) {
		List<Player> players = getPlayerList(roomId);
		Map<String, Object> info = new HashMap<>();
		info.put("userId", p.getUserId());
		info.put("playStatus", p.getPlayStatus());
		info.put("paiInfos", getPaiInfo(p, true));
		Integer interfaceId = 100111;
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		if (players != null && players.size() > 0) {
			for (Player pp : players) {
				IoSession se = MinaServerManager.tcpServer.getSessions().get(
						pp.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
			}
		}
	}

	/**
	 * 给其他玩家推送动作提示
	 * 
	 * @param players
	 * @param userId
	 * @param action
	 */
	public static void interface_100104(List<Player> players, Long userId,
			Integer action, Long toUserId, Integer gangType, IoSession session,
			RoomResp room) {

		Integer interfaceId = 100104;
		Player actionUser = null;
		Player hasActionUser = null;
		List<Map<String, Object>> playStatusInfo = new ArrayList<Map<String, Object>>();
		for (Player p : players) {
			Map<String, Object> pi = new HashMap<String, Object>();
			pi.put("userId", p.getUserId());
			pi.put("playStatus", p.getPlayStatus());
			pi.put("kouTing", p.getKouTing());
			playStatusInfo.add(pi);
			if (p.getUserId().equals(userId)) {
				actionUser = p;
			}
			if (p.getCurrentActions() != null
					&& p.getCurrentActions().size() > 0) {
				hasActionUser = p;
			}
		}
		// List<Map<String,Object>> scoreInfo = null;
		// if (action.equals(Cnst.ACTION_LIANG_FENG)) {
		// scoreInfo = new ArrayList<Map<String,Object>>();
		// for(Player ps:players){
		// Map<String,Object> sco = new HashMap<String, Object>();
		// sco.put("userId", ps.getUserId());
		// sco.put("score", ps.getScore());
		// scoreInfo.add(sco);
		// }
		// }

		Map<String, Object> users = new HashMap<String, Object>();
		Integer[][] fuyi = new Integer[][] { { -1, -1 } };
		for (Player p : players) {
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", userId);
				map.put("action", action);
				map.put("xiaoSa", p.getXiaoSa());
				map.put("kouTing", p.getKouTing());

				map.put("wsw_sole_main_id", room.getWsw_sole_main_id());
				map.put("wsw_sole_action_id", room.getWsw_sole_action_id());

				if (action == Cnst.ACTION_HU) {
					if (actionUser.getZhaHu()) {
						map.put("huType", Cnst.HU_TYPE_ZHA_HU);
					} else {
						map.put("huType",
								userId.equals(toUserId) ? Cnst.HU_TYPE_ZI_MO
										: Cnst.HU_TYPE_DIAN_PAO);
					}
				}

				if (p.getUserId().equals(userId)) {
					map.put("lastFaPai", p.getLastFaPai());
					map.put("paiInfos", getPaiInfo(actionUser, true));
				} else {
					map.put("lastFaPai", fuyi);
					map.put("paiInfos", getPaiInfo(actionUser, false));
				}

				if (hasActionUser != null) {// 还有玩家继续有动作
					if (hasActionUser.getUserId().equals(p.getUserId())) {
						Map<String, Object> actionInfo = new HashMap<String, Object>();
						actionInfo.put("actions", p.getCurrentActions());
						actionInfo.put("userId", p.getUserId());
						map.put("actionInfo", actionInfo);
					} else {
						map.put("actionInfo", 1);
					}
				}

				map.put("toUserId", toUserId);
				map.put("needFaPai", p.getNeedFaPai());
				map.put("playStatusInfo", playStatusInfo);
				JSONObject result = getJSONObj(interfaceId, 1, map);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());

				// 写文件用
				if (map.containsKey("actionInfo")
						&& !(map.get("actionInfo") instanceof Integer)) {
					users.put("hasActionUser", map);
				} else if (p.getUserId().equals(userId)) {
					users.put("actionUser", map);
				} else if (p.getNeedFaPai()) {
					users.put("needFaUser", map);
				}
				se.write(pd);
			}
		}

		BackFileUtil.write(null, interfaceId, room, players, users);// 写入文件内容
	}

	/**
	 * 给其他玩家推送出牌提示 如果actionPlayer==null的话，所有玩家都没有动作
	 * 
	 * @param others
	 * @param userId
	 * @param paiInfo
	 */
	public static void interface_100105(Long userId, Integer[][] paiInfo,
			Integer roomId, Player actionPlayer, boolean genZhuang,
			RoomResp room, List<Player> players) {
		Integer interfaceId = 100105;

		List<Map<String, Object>> scoreInfo = null;
		if (genZhuang) {
			scoreInfo = new ArrayList<Map<String, Object>>();
			for (Player ps : players) {
				Map<String, Object> sco = new HashMap<String, Object>();
				sco.put("userId", ps.getUserId());
				sco.put("score", ps.getScore());
				scoreInfo.add(sco);
			}
		}
		Player currentPlayer = null;
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
		}

		Map<String, Object> users = new HashMap<String, Object>();
		/* 给其他玩家推送出牌提示 */
		for (Player p : players) {
			Map<String, Object> map = new JSONObject();
			map.put("userId", userId);
			map.put("paiInfo", paiInfo);
			map.put("wsw_sole_main_id", room.getWsw_sole_main_id());
			map.put("wsw_sole_action_id", room.getWsw_sole_action_id());
			map.put("needFaPai", p.getNeedFaPai());
			map.put("kouTing", currentPlayer.getKouTing());
			map.put("playStatus", p.getPlayStatus());
			map.put("xiaoSa", p.getXiaoSa());
			map.put("genZhuang", genZhuang);
			if (genZhuang) {
				map.put("scoreInfo", scoreInfo);
			}

			if (actionPlayer != null) {
				if (actionPlayer.getUserId().equals(p.getUserId())) {
					// System.err.println("动作人id"+p.getUserId());
					Map<String, Object> actionInfo = new HashMap<String, Object>();
					actionInfo.put("actions", p.getCurrentActions());
					actionInfo.put("userId", p.getUserId());
					map.put("actionInfo", actionInfo);
				} else {
					map.put("actionInfo", 1);
				}
			}

			JSONObject result = getJSONObj(interfaceId, 1, map);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());

			// 写文件用
			if (map.containsKey("actionInfo")
					&& !(map.get("actionInfo") instanceof Integer)) {
				users.put("hasActionUser", map);
			} else if (p.getUserId().equals(userId)) {
				users.put("chuUser", map);
			} else if (p.getNeedFaPai()) {
				users.put("needFaUser", map);
			}

			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

		BackFileUtil.write(null, interfaceId, room, players, users);// 写入文件内容
	}

	/**
	 * 多地登陆提示
	 * 
	 * @param session
	 */
	public static void interface_100106(IoSession session) {
		Integer interfaceId = 100106;
		JSONObject result = getJSONObj(interfaceId, 1, "out");
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		session.close(true);
	}

	/**
	 * 玩家被踢/房间被解散提示
	 * 
	 * @param session
	 */
	public static void interface_100107(Long userId, String type,List<Player> players) {
		Integer interfaceId = 100107;
		Map<String, Object> info = new HashMap<String, Object>();

		if (players == null || players.size() == 0) {
			return;
		}
		info.put("userId", userId);
		info.put("type", type);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		for (Player p : players) {
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
	}

	/**
	 * 方法id不符合
	 * 
	 * @param session
	 */
	public static void interface_100108(IoSession session) {
		Integer interfaceId = 100108;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", Cnst.REQ_STATE_9);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 用户离线/上线提示
	 * 
	 * @param state
	 */
	public static void interface_100109(List<Player> players, String status,
			Long userId, IoSession session, String playState) {
		Integer interfaceId = 100109;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("userId", userId);
		info.put("status", status);
		info.put("playStatus", playState);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		if (players != null && players.size() > 0) {
			for (Player p : players) {
				if (p != null && !p.getUserId().equals(userId)) {
					IoSession se = MinaServerManager.tcpServer.getSessions()
							.get(p.getSessionId());
					if (se != null && se.isConnected()) {
						se.write(pd);
					}
				}

			}
		}
	}

	/**
	 * 玩家潇洒提示
	 * 
	 * @param state
	 */
	public static void interface_100110(Player currentUser,
			Player actionPlayer, RoomResp room, List<Player> players) {
		Integer interfaceId = 100110;

		/* 给其他玩家推送出牌提示 */
		Map<String, Object> users = new HashMap<String, Object>();

		for (Player p : players) {
			Map<String, Object> map = new JSONObject();
			map.put("userId", currentUser != null ? currentUser.getUserId()
					: null);
			map.put("wsw_sole_main_id", room.getWsw_sole_main_id());
			map.put("wsw_sole_action_id", room.getWsw_sole_action_id());
			map.put("needFaPai", p.getNeedFaPai());
			map.put("playStatus", p.getPlayStatus());
			if (currentUser != null) {
				map.put("paiInfos", getPaiInfo(currentUser, false));
			}

			if (actionPlayer != null) {
				if (actionPlayer.getUserId().equals(p.getUserId())) {
					Map<String, Object> actionInfo = new HashMap<String, Object>();
					actionInfo.put("actions", p.getCurrentActions());
					actionInfo.put("userId", p.getUserId());
					map.put("actionInfo", actionInfo);
				} else {
					map.put("actionInfo", 1);
				}
			}

			JSONObject result = getJSONObj(interfaceId, 1, map);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());

			// 写文件用
			if (map.containsKey("actionInfo")
					&& !(map.get("actionInfo") instanceof Integer)) {
				users.put("hasActionUser", map);
			} else if (currentUser != null
					&& p.getUserId().equals(currentUser.getUserId())) {
				users.put("xiaoSaUser", map);
			} else if (p.getNeedFaPai()) {
				users.put("needFaUser", map);
			}

			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

		BackFileUtil.write(null, interfaceId, room, players, users);// 写入文件内容
	}

    /**
     * 后端主动解散房间推送
     * @param reqState
     * @param players
     */
    public static void interface_100111(int reqState,List<Player> players,Integer roomId){
    	Integer interfaceId = 100111;
        Map<String,Object> info = new HashMap<String, Object>();
        info.put("reqState",reqState);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        if (players!=null&&players.size()>0) {
			for(Player p:players){
				if (p.getRoomId()!=null&&p.getRoomId().equals(roomId)) {
					IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
					if (se!=null&&se.isConnected()) {
						se.write(pd);
					}
				}
			}
		}
    	
    }

}
