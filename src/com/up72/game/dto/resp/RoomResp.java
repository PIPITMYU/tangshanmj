package com.up72.game.dto.resp;

import com.up72.game.model.Room;
import com.up72.server.mina.bean.DissolveRoom;
import com.up72.server.mina.utils.MahjongUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/8.
 */
public class RoomResp extends Room {

    private List<Integer[][]> currentMjList;//房间内剩余麻将集合；
    private Long zhuangId;
    //本房间状态，0等待玩家入坐；1人满等待；2游戏中；3小结算
    private Integer status;
    private Integer lastNum;//房间剩余局数
    private Integer currentJuNum;//当前第几局
    //13张的牌初始检测属性
    private Integer actionNum;//执行过的人的次数
    private String positionIds;//执行过的人id
    
    private Integer circleWind;//圈风...
    private Integer circleNum;//圈数
    private Integer roomType;//房间模式，房主模式1；自由模式2
    private Integer maxScoreInRoom;//封顶分

    private DissolveRoom dissolveRoom;//申请解散信息

    private Integer[][] lastPai;//最后出的牌
    private Long lastUserId;//最后出牌的玩家
    private Integer createDisId;
    private Integer applyDisId;
    private Integer outNum;
    private List<Long> guoUserIds;//动作  点击过的人
    
    private List<Map<String, Object>> overInfo;
    
    private Boolean hasInsertRecord;
    

    private List<Integer[][]> hunPai;//格式为：[[[3,3]],[[3,4]]]，第一个元素为开的混牌，第二个为另一张混牌，没有排序
    
    
    private Integer wsw_sole_main_id;//大接口id
    private Integer wsw_sole_action_id;//吃碰杠出牌发牌id
    
    private Integer diXianPaiNum;//房间内不能出的牌个数
    private String openName;
    
    private Long[] playerIds;//玩家id集合
    
    private Long xiaoJuStartTime;//小局开始时间
    
    //存放每一圈玩家出的牌，从庄家开始存放，再次轮到庄家的时候，清空重新存放，计算跟庄数的
    //所以这个集合最多只能存放4张牌
    private List<Integer[][]> curCiecleChuPais;
    private Integer getnZhuangNum;
    
    //暂时没有用，因为过杠胡时暂定没有过的操作，所以暂时没用，如果有过的操作的时候，才能用上
    private Long[] lastPengGangUser;//用于过杠胡,存放最后一个碰杠的玩家，前提是过杠的时候有其他玩家胡牌，玩家出牌，则置空,第一个存放杠后抢胡的玩家id，是真正的抢杠胡；第二个存放抢风胡的玩家id，不属于抢杠胡
    
    private Boolean haiLao;//海捞的状态，true/false
    

    private Integer xiaoJuNum;//每次小局（刘局或者有人胡），这个字段++，回放用
    
//    private String ip;//当前房间所在服务器的ip
    //关于喜	    
//    private Integer guoGangXiType;//房间的过喜时喜的类型
    private Integer[][] actionPai;//最后出的牌
//    private Integer xiKouMark;//13张的时候设置为1,开始发牌后为0

	public Long getXiaoJuStartTime() {
		return xiaoJuStartTime;
	}


	public Integer[][] getActionPai() {
		return actionPai;
	}


	public void setActionPai(Integer[][] actionPai) {
		this.actionPai = actionPai;
	}




	public void setXiaoJuStartTime(Long xiaoJuStartTime) {
		this.xiaoJuStartTime = xiaoJuStartTime;
	}




	public Integer getActionNum() {
		return actionNum;
	}


	public void setActionNum(Integer actionNum) {
		this.actionNum = actionNum;
	}


	public String getPositionIds() {
		return positionIds;
	}


	public void setPositionIds(String positionIds) {
		this.positionIds = positionIds;
	}


	public Integer getDiXianPaiNum() {
		return diXianPaiNum;
	}


	public void setDiXianPaiNum(Integer diXianPaiNum) {
		this.diXianPaiNum = diXianPaiNum;
	}


	public void initRoom(){
    	this.lastPai = null;
    	this.lastUserId = null;
    	this.hunPai = null;
    	this.getnZhuangNum = 0;
    	this.lastPengGangUser = null;
    	this.curCiecleChuPais = null;
    	this.haiLao = false;
    }
    
    
//    public String getIp() {
//		return ip;
//	}
//
//
//	public void setIp(String ip) {
//		this.ip = ip;
//	}


	public Integer getXiaoJuNum() {
		return xiaoJuNum;
	}


	public void setXiaoJuNum(Integer xiaoJuNum) {
		this.xiaoJuNum = xiaoJuNum;
	}


	public Boolean getHaiLao() {
		return haiLao;
	}


	public void setHaiLao(Boolean haiLao) {
		this.haiLao = haiLao;
	}


	public Long[] getLastPengGangUser() {
		return lastPengGangUser;
	}


	public void setLastPengGangUser(Long lastPengGangUser[]) {
		this.lastPengGangUser = lastPengGangUser;
	}


	public List<Integer[][]> getCurCiecleChuPais() {
		return curCiecleChuPais;
	}


	public void setCurCiecleChuPais(List<Integer[][]> curCiecleChuPais) {
		this.curCiecleChuPais = curCiecleChuPais;
	}


	public Integer getGetnZhuangNum() {
		return getnZhuangNum;
	}


	public void setGetnZhuangNum(Integer getnZhuangNum) {
		this.getnZhuangNum = getnZhuangNum;
	}


	public Long[] getPlayerIds() {
		return playerIds;
	}


	public void setPlayerIds(Long[] playerIds) {
		this.playerIds = playerIds;
	}


	public String getOpenName() {
		return openName;
	}


	public void setOpenName(String openName) {
		this.openName = openName;
	}


	public Integer getWsw_sole_main_id() {
		return wsw_sole_main_id;
	}

	public void setWsw_sole_main_id(Integer wsw_sole_main_id) {
		this.wsw_sole_main_id = wsw_sole_main_id;
	}

	public Integer getWsw_sole_action_id() {
		return wsw_sole_action_id;
	}

	public void setWsw_sole_action_id(Integer wsw_sole_action_id) {
		this.wsw_sole_action_id = wsw_sole_action_id;
	}

	public Boolean getHasInsertRecord() {
		return hasInsertRecord;
	}

	public void setHasInsertRecord(Boolean hasInsertRecord) {
		this.hasInsertRecord = hasInsertRecord;
	}

	public List<Map<String, Object>> getOverInfo() {
		return overInfo;
	}

	public void setOverInfo(List<Map<String, Object>> overInfo) {
		this.overInfo = overInfo;
	}

	public List<Long> getGuoUserIds() {
		return guoUserIds;
	}

	public void setGuoUserIds(List<Long> guoUserIds) {
		this.guoUserIds = guoUserIds;
	}

	public List<Integer[][]> getCurrentMjList() {
        return currentMjList;
    }

    public void setCurrentMjList(List<Integer[][]> currentMjList) {
        this.currentMjList = currentMjList;
    }

    public Long getZhuangId() {
        return zhuangId;
    }

    public void setZhuangId(Long zhuangId) {
        this.zhuangId = zhuangId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLastNum() {
        return lastNum;
    }

    public void setLastNum(Integer lastNum) {
        this.lastNum = lastNum;
    }

    public Integer getCurrentJuNum() {
        return currentJuNum;
    }

    public void setCurrentJuNum(Integer currentJuNum) {
        this.currentJuNum = currentJuNum;
    }

    public Integer getCircleWind() {
        return circleWind;
    }

    public void setCircleWind(Integer circleWind) {
        this.circleWind = circleWind;
    }

    public Integer getCircleNum() {
        return circleNum;
    }

    public void setCircleNum(Integer circleNum) {
        this.circleNum = circleNum;
    }

    public Integer getRoomType() {
        return roomType;
    }

    public void setRoomType(Integer roomType) {
        this.roomType = roomType;
    }

    public DissolveRoom getDissolveRoom() {
        return dissolveRoom;
    }

    public void setDissolveRoom(DissolveRoom dissolveRoom) {
        this.dissolveRoom = dissolveRoom;
    }


    public Integer getMaxScoreInRoom() {
		return maxScoreInRoom;
	}

	public void setMaxScoreInRoom(Integer maxScoreInRoom) {
		this.maxScoreInRoom = maxScoreInRoom;
	}

	public Integer[][] getLastPai() {
        return lastPai;
    }

    public void setLastPai(Integer[][] lastPai) {
        this.lastPai = lastPai;
    }

    public Long getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(Long lastUserId) {
        this.lastUserId = lastUserId;
    }

    public Integer getCreateDisId() {
        return createDisId;
    }

    public void setCreateDisId(Integer createDisId) {
        this.createDisId = createDisId;
    }

    public Integer getApplyDisId() {
        return applyDisId;
    }

    public void setApplyDisId(Integer applyDisId) {
        this.applyDisId = applyDisId;
    }

	public Integer getOutNum() {
		return outNum;
	}

	public void setOutNum(Integer outNum) {
		this.outNum = outNum;
	}


	public List<Integer[][]> getHunPai() {
		return hunPai;
	}


	public void setHunPai(List<Integer[][]> hunPai) {
		this.hunPai = hunPai;
	}


	@Override
	public String toString() {
		return "RoomResp [currentMjList=" + currentMjList + ", zhuangId="
				+ zhuangId + ", status=" + status + ", lastNum=" + lastNum
				+ ", currentJuNum=" + currentJuNum + ", circleWind="
				+ circleWind + ", circleNum=" + circleNum + ", roomType="
				+ roomType + ", maxScoreInRoom=" + maxScoreInRoom
				+ ", dissolveRoom=" + dissolveRoom + ", lastPai="
				+ Arrays.toString(lastPai) + ", lastUserId=" + lastUserId
				+ ", createDisId=" + createDisId + ", applyDisId=" + applyDisId
				+ ", outNum=" + outNum + ", guoUserIds=" + guoUserIds
				+ ", overInfo=" + overInfo + ", hasInsertRecord="
				+ hasInsertRecord + ", hunPai=" + hunPai
				+ ", wsw_sole_main_id=" + wsw_sole_main_id
				+ ", wsw_sole_action_id=" + wsw_sole_action_id
				+ ", diXianPaiNum=" + diXianPaiNum + ", openName=" + openName
				+ ", playerIds=" + Arrays.toString(playerIds)
				+ ", curCiecleChuPais=" + curCiecleChuPais + ", getnZhuangNum="
				+ getnZhuangNum + ", lastPengGangUser="
				+ Arrays.toString(lastPengGangUser) + ", haiLao=" + haiLao
				+ ", xiaoJuNum=" + xiaoJuNum +"]";
	}
	
    
}
