/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;



/**
 * 
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public class Room implements java.io.Serializable{

    private Long id;
    private Integer roomId;
    private Long createId;
    private String createTime;
    private Integer isPlaying;
    private Integer maxScore;
    private Long userId1;
    private Long userId2;
    private Long userId3;
    private Long userId4;
    private Integer roomType;
    private Integer circleNum;
    
//    private Integer globalType;
//    private Integer xiType;
    
    private Integer xiaoSa;
    private Integer liangFeng;
    private Integer daHun;
    private Integer siHunHu;
    private Integer dianType;//1大包；2包三家；3三家付
    private Integer clubId;// 俱乐部id
    
    
    private String ip;//当前房间所在服务器的ip

    

//	public Integer getGlobalType() {
//		return globalType;
//	}
//
//	public void setGlobalType(Integer globalType) {
//		this.globalType = globalType;
//	}
//
//	public Integer getXiType() {
//		return xiType;
//	}
//
//	public void setXiType(Integer xiType) {
//		this.xiType = xiType;
//	}

	public Integer getXiaoSa() {
		return xiaoSa;
	}

	public void setXiaoSa(Integer xiaoSa) {
		this.xiaoSa = xiaoSa;
	}

	public Integer getLiangFeng() {
		return liangFeng;
	}

	public void setLiangFeng(Integer liangFeng) {
		this.liangFeng = liangFeng;
	}

	public Integer getDaHun() {
		return daHun;
	}

	public void setDaHun(Integer daHun) {
		this.daHun = daHun;
	}

	public Integer getSiHunHu() {
		return siHunHu;
	}

	public void setSiHunHu(Integer siHunHu) {
		this.siHunHu = siHunHu;
	}

	public Integer getDianType() {
		return dianType;
	}

	public void setDianType(Integer dianType) {
		this.dianType = dianType;
	}

	public Long getUserId4() {
		return userId4;
	}

	public void setUserId4(Long userId4) {
		this.userId4 = userId4;
	}

	public Long getCreateId() {
		return createId;
	}

	public void setCreateId(Long createId) {
		this.createId = createId;
	}

	public Integer getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(Integer isPlaying) {
        this.isPlaying = isPlaying;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Long getUserId1() {
        return userId1;
    }

    public void setUserId1(Long userId1) {
        this.userId1 = userId1;
    }

    public Long getUserId2() {
        return userId2;
    }

    public void setUserId2(Long userId2) {
        this.userId2 = userId2;
    }

    public Long getUserId3() {
        return userId3;
    }

    public void setUserId3(Long userId3) {
        this.userId3 = userId3;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public Integer getRoomType() {
        return roomType;
    }

    public void setRoomType(Integer roomType) {
        this.roomType = roomType;
    }

    public Integer getCircleNum() {
        return circleNum;
    }

    public void setCircleNum(Integer circleNum) {
        this.circleNum = circleNum;
    }

    public Integer getClubId() {
		return clubId;
	}

	public void setClubId(Integer clubId) {
		this.clubId = clubId;
	}

	public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }

    public boolean equals(Object obj) {
        if(obj instanceof Room == false) return false;
        if(this == obj) return true;
        Room other = (Room)obj;
        return new EqualsBuilder()
            .append(getId(),other.getId())
            .isEquals();
    }

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}

