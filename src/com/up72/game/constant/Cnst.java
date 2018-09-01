package com.up72.game.constant;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.up72.game.utils.ProjectInfoPropertyUtil;

/**
 * 常量
 */
public class Cnst {
	
	// 获取项目版本信息
    public static final String version = ProjectInfoPropertyUtil.getProperty("project_version", "1.5");
    public static Boolean isTest = true;//是否是测试环境
    public static final String cid = ProjectInfoPropertyUtil.getProperty("cid", "4");


    public static final String p_name = ProjectInfoPropertyUtil.getProperty("p_name", "wsw_X1");
    public static final String o_name = ProjectInfoPropertyUtil.getProperty("o_name", "u_consume");
    public static final String gm_url = ProjectInfoPropertyUtil.getProperty("gm_url", "");

    //回放配置
    public static final String BACK_FILE_PATH = ProjectInfoPropertyUtil.getProperty("backFilePath", "1.5");
    public static final String FILE_ROOT_PATH = ProjectInfoPropertyUtil.getProperty("fileRootPath", "1.5");
    public static String SERVER_IP = getLocalAddress();
    public static String HTTP_URL = "http://".concat(Cnst.SERVER_IP).concat(":").concat(ProjectInfoPropertyUtil.getProperty("httpUrlPort", "8086")).concat("/");
    public static String getLocalAddress(){
		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ip;
	}
    
    
    
    //redis配置
    public static final String REDIS_HOST = ProjectInfoPropertyUtil.getProperty("redis.host", "");
    public static final String REDIS_PORT = ProjectInfoPropertyUtil.getProperty("redis.port", "");
    public static final String REDIS_PASSWORD = ProjectInfoPropertyUtil.getProperty("redis.password", "");

    //mina的端口
    public static final String MINA_PORT = ProjectInfoPropertyUtil.getProperty("mina.port", "");
    
    public static final String rootPath = ProjectInfoPropertyUtil.getProperty("rootPath", "");


    public static final long HEART_TIME = 17000;
    public static final int MONEY_INIT = 4;//初始赠送给用户的房卡数
    //开房选项中的是否
    public static final int YES = 1;
    public static final int NO = 0;
    //玩家是否选择潇洒的定义
    public static final int XIAO_SA_NO = 2;
    public static final int XIAO_SA_YES = 1;
    public static final int XIAO_SA_NO_CHOOSE = 0;
    
    public static final long AUTO_CHU_TIME = 60000;//默认超过上次心跳60，就会自动出牌，


    public static final long ROOM_OVER_TIME = 5*60*60*1000;//房间定时24小时解散
    public static final long ROOM_CREATE_DIS_TIME = 40*60*1000;//创建房间之后，40分钟解散
    public static final long ROOM_DIS_TIME = 5*60*1000;//玩家发起解散房间之后，5分钟自动解散
    public static final String CLEAN_3 = "0 0 3 * * ?";
    public static final String CLEAN_EVERY_HOUR = "0 0 0/1 * * ?";
    public static final String COUNT_EVERY_TEN_MINUTE = "0 0/1 * * * ?";
    public static final long BACKFILE_STORE_TIME = 3*24*60*60*1000;//回放文件保存时间
    
    
    //测试时间
//    public static final long ROOM_OVER_TIME = 60*1000;//
//    public static final long ROOM_CREATE_DIS_TIME = 20*1000;
//    public static final long ROOM_DIS_TIME = 10*1000;
//	public static final String CLEAN_3 = "0/5 * * * * ?";
//	public static final String CLEAN_EVERY_HOUR = "0/5 * * * * ?";
//    public static final String COUNT_EVERY_TEN_MINUTE = "0/20 * * * * ?";
//    public static final long BACKFILE_STORE_TIME = 60*1000;//回放文件保存时间
    
    public static final int DIS_ROOM_RESULT = 1;

    public static final int DIS_ROOM_TYPE_1 = 1;//创建房间40分钟解散类型
    public static final int DIS_ROOM_TYPE_2 = 2;//玩家点击解散房间类型

    public static final int PAGE_SIZE = 10;
    
    public static final int DI_XIAN_PAI = 14;//房间默认的底线牌

    //风向表示
    public static final int WIND_EAST = 1;
    public static final int WIND_SOUTH = 2;
    public static final int WIND_WEST = 3;
    public static final int WIND_NORTH = 4;

    public static final String USER_SESSION_USER_ID = "user_id";
    public static final String USER_SESSION_IP = "ip";
    
    //自摸，点炮，诈胡
    public static final int HU_TYPE_ZI_MO = 1;
    public static final int HU_TYPE_DIAN_PAO = 2;
    public static final int HU_TYPE_ZHA_HU = 3;
    

    //房间状态
    // 1等待玩家入坐；2游戏中；3小结算
    public static final int ROOM_STATE_CREATED = 1;
    public static final int ROOM_STATE_GAMIING = 2;
    public static final int ROOM_STATE_XJS = 3;
    public static final int ROOM_STATE_YJS = 4;

    //房间类型
    public static final int ROOM_TYPE_1 = 1;//房主模式
    public static final int ROOM_TYPE_2 = 2;//自由模式
    public static final int ROOM_TYPE_3 = 3;//AA


    //小局结算时的
    public static final int OVER_TYPE_1 = 1;//胜利
    public static final int OVER_TYPE_2 = 2;//失败
    public static final int OVER_TYPE_3 = 3;//荒庄


    //开房的局数对应消耗的房卡数
    public static final Map<Integer,Integer> moneyMap = new HashMap<>();
    static {
        moneyMap.put(8,4);
        moneyMap.put(16,6);
        moneyMap.put(24,12);
    }
    //俱乐部的局数对应消耗的房卡数
    public static final Map<Integer,Integer> clubMoneyMap = new HashMap<>();
    static {
        clubMoneyMap.put(8,4);
        clubMoneyMap.put(16,8);
        clubMoneyMap.put(24,12);
    }
    //玩家在线状态
    public static final String PLAYER_LINE_STATE_INLINE = "inline";
    public static final String PLAYER_LINE_STATE_OUT = "out";

    //玩家状态
    public static final String PLAYER_STATE_DATING = "dating";
    public static final String PLAYER_STATE_IN = "in";
    public static final String PLAYER_STATE_PREPARED = "prepared";
    public static final String PLAYER_STATE_CHU = "chu";
    public static final String PLAYER_STATE_WAIT = "wait";
    public static final String PLAYER_STATE_XJS = "xjs";

    //请求状态
    public static final int REQ_STATE_FUYI = -1;//敬请期待
    public static final int REQ_STATE_0 = 0;//非法请求
    public static final int REQ_STATE_1 = 1;//正常
    public static final int REQ_STATE_2 = 2;//余额不足
    public static final int REQ_STATE_3 = 3;//已经在其他房间中
    public static final int REQ_STATE_4 = 4;//房间不存在
    public static final int REQ_STATE_5 = 5;//房间人员已满
    public static final int REQ_STATE_6 = 6;//游戏中，不能退出房间
    public static final int REQ_STATE_7 = 7;//有玩家拒绝解散房间
    public static final int REQ_STATE_8 = 8;//玩家不存在（代开模式中，房主踢人用的）
    public static final int REQ_STATE_9 = 9;//接口id不符合，需请求大接口
    public static final int REQ_STATE_10 = 10;//代开房间创建成功
    public static final int REQ_STATE_11 = 11;//已经代开过10个了，不能再代开了
    public static final int REQ_STATE_12 = 12;//房间存在超过24小时解散的提示
    public static final int REQ_STATE_13 = 13;//房间40分钟未开局解散提示
    public static final int REQ_STATE_14 = 14;//ip不一致

    //动作列表
    public static final int ACTION_HU = 6;//胡
    public static final int ACTION_TING = 5;//听
    public static final int ACTION_LIANG_FENG = 4;//亮风
    public static final int ACTION_GANG = 3;
    public static final int ACTION_PENG = 2;
    public static final int ACTION_CHI = 1;
    public static final int ACTION_GUO = 0;

    //牌局底分
    public static final int SCORE_BASE = 1;


    //胡牌类型
    public static final int HUTYPE_PINGHU = 1;
    public static final int HUTYPE_JIA = 2;
    public static final int HUTYPE_DANDIAO = 3;
    public static final int HUTYPE_PIAO = 4;//飘胡的分数按照房间的封顶分计算


    public static final Map<Integer,Integer> huScore = new HashMap<>();
    static {
        huScore.put(1,1);
        huScore.put(2,2);
        huScore.put(3,2);
    }
    
    //胡牌的基础分是1分  
    //別人打得混牌就只能看作混牌本身使用，不能看成其他牌
    //胡牌类型对应的番数  没加1番 就是*2
    public static final int HU_FAN_ZI_MO = 1;		
    public static final int HU_FAN_MEN_QING = 1;	//没有吃碰杠   亮风（东南西北）仍记门清
    public static final int HU_FAN_ZHAUNG = 1;		//庄赢
    public static final int HU_FAN_DIAN_PAO = 1;	//赢被点的那个人翻倍
    public static final int HU_FAN_MEI_HUN = 1;		//赢时手中没混（混吊混也是没混）
    public static final int HU_FAN_BEN_HUN = 1;		//混牌当本身使用,所有混牌不能当别的牌   
    public static final int HU_FAN_BIAN_KA_DIAO = 1;		//边  1 9，   卡如：2 4 卡3   ，钓就是单调一张
    public static final int HU_FAN_ZHUO_WU_KUI = 1;			//----------没有了
    public static final int HU_FAN_KA_DIAO_ZHUO_WU_KUI = 2;	//摸得是5万，或者别人出的5万， 有4和6(4,6可用混补)
    public static final int HU_FAN_YI_TIAO_LONG = 1;		//1到9 全是饼或者万 ，筒
    public static final int HU_FAN_BEN_HUN_YI_TIAO_LONG = 2;	//一条龙基础上   
    public static final int HU_FAN_HUN_YI_SE = 1;			//1：条饼万的一种组成  2：必须包含东南西北中发的一种    3： 包括吃碰杠的牌  必须和手牌中的牌型相同
    public static final int HU_FAN_QING_YI_SE = 2;			//1：完全由条饼万的一种组成  2：不包含东南西北中发    3： 包括吃碰杠的牌 必须和手牌中的牌型相同
    public static final int HU_FAN_DA_PIAO = 1;				//没有吃  只有3个一样的，（动作拍里面只能有杠或者碰）
    public static final int HU_FAN_GANG_SHANG_KAI_HUA = 1;	//自己开杠摸牌，自己赢了
    public static final int HU_FAN_GANG_HOU_DIAN_PAO = 1;	//别人开杠之后出牌，自己赢了
    public static final int HU_FAN_HAI_LAO = 1;				//最后能摸得最后4张牌的一张 摸赢了
    public static final int HU_FAN_QI_DUI = 1;				//7个对子
    public static final int HU_FAN_HAO_HUA_QI_DUI = 2;		// 7个对子中有4个一样的
    public static final int HU_FAN_HAO_HUA = 3;		// 7个对子中有两个4张是一样的
    public static final int HU_FAN_SAN_HAO_HUA = 4;		// 7个对子中有3个4张是一样的
    public static final int HU_FAN_TIAN_HU = 1;			//第一次庄家摸牌，直接赢了
    public static final int HU_FAN_DI_HU = 1;			//第一次闲家摸牌，直接赢了
    public static final int HU_FAN_PING_HU = 1;			//平胡，1分，没翻
    public static final int HU_FAN_QIANG_GANG_HU = 1;			//抢杠胡

    //胡牌的类型  
    public static final int HU_ZI_MO = 1;		
    public static final int HU_MEN_QING = 2;	//没有吃碰杠   亮风（东南西北）仍记门清
    public static final int HU_ZHAUNG = 3;		//庄赢
    public static final int HU_DIAN_PAO = 4;	//赢被点的那个人翻倍
    public static final int HU_MEI_HUN = 5;		//赢时手中没混（混吊混也是没混）
    public static final int HU_BEN_HUN = 6;		//混牌当本身使用,所有混牌不能当别的牌   
    public static final int HU_BIAN_KA_DIAO = 7;		//边  1 9，   卡如：2 4 卡3   ，钓就是单调一张
    public static final int HU_ZHUO_WU_KUI = 8;			//摸得是5万，或者别人出的5万，  4和6都是可以拿混去顶（两张混牌呢?）
    public static final int HU_KA_DIAO_ZHUO_WU_KUI = 9;	//摸得是5万，或者别人出的5万，  4和6都是本身
    public static final int HU_YI_TIAO_LONG = 10;		//1到9 全是饼或者万 ，筒
    public static final int HU_BEN_HUN_YI_TIAO_LONG = 11;	//一条龙基础上   
    public static final int HU_HUN_YI_SE = 12;			//1：条饼万的一种组成  2：必须包含东南西北中发的一种    3： 包括吃碰杠的牌  必须和手牌中的牌型相同
    public static final int HU_QING_YI_SE = 13;			//1：完全由条饼万的一种组成  2：不包含东南西北中发    3： 包括吃碰杠的牌 必须和手牌中的牌型相同
    public static final int HU_DA_PIAO = 14;				//没有吃  只有3个一样的，（动作拍里面只能有杠或者碰）
    public static final int HU_GANG_SHANG_KAI_HUA = 15;	//自己开杠摸牌，自己赢了
    public static final int HU_GANG_HOU_DIAN_PAO = 16;	//别人开杠之后出牌，自己赢了
    public static final int HU_HAI_LAO = 17;				//最后能摸得最后4张牌的一张 摸赢了
    public static final int HU_QI_DUI = 18;				//7个对子
    public static final int HU_HAO_HUA_QI_DUI = 19;		// 7个对子中有4个一样的
    public static final int HU_HAO_HUA = 20;		// 7个对子中有两个4张是一样的
    public static final int HU_SAN_HAO_HUA = 21;		// 7个对子中有3个4张是一样的
    public static final int HU_TIAN_HU = 22;			//第一次庄家摸牌，直接赢了
    public static final int HU_DI_HU = 23;			//第一次闲家摸牌，直接赢了
    public static final int HU_SI_HUN_HU = 24;			//手中4个混，直接赢
    public static final int HU_PING_HU = 25;			//第一次闲家摸牌，直接赢了
    public static final int HU_QIANG_GANG_HU = 26;			//抢杠胡
    public static final int HU_SI_HUN_SCORE = 27;		//捉五   -----------对应卡掉捉五分2 HU_FAN_KA_DIAO_ZHUO_WU_KUI
    
    public static final int HU_BIAN = 36;		//边-------------对应卡边钓分1 HU_FAN_BIAN_KA_DIAO 
    public static final int HU_KA = 37;			//卡-------------对应卡边钓分1 HU_FAN_BIAN_KA_DIAO 
    public static final int HU_DIAO = 38;		//吊  --单吊时可和飘共存-------------对应卡边钓分1 HU_FAN_BIAN_KA_DIAO 
    public static final int HU_KA_ZHUO_WU = 39;		//卡捉五   -----------对应卡掉捉五分2 HU_FAN_KA_DIAO_ZHUO_WU_KUI
    public static final int HU_DIAO_ZHUO_WU = 40;	//吊捉五   -----------对应卡掉捉五分2 HU_FAN_KA_DIAO_ZHUO_WU_KUI
    
    
    public static Integer getFan(int type){
    	int fan = 0;
    	switch (type) {
    	case HU_ZI_MO:						fan = HU_FAN_ZI_MO;break;
    	case HU_MEN_QING:					fan = HU_FAN_MEN_QING;break;
    	case HU_ZHAUNG:						fan = HU_FAN_ZHAUNG;break;
    	case HU_DIAN_PAO:					fan = HU_FAN_DIAN_PAO;break;
    	case HU_MEI_HUN:					fan = HU_FAN_MEI_HUN;break;
    	case HU_BEN_HUN:					fan = HU_FAN_BEN_HUN;break;
    	case HU_BIAN_KA_DIAO:				fan = HU_FAN_BIAN_KA_DIAO;break;
    	case HU_ZHUO_WU_KUI:				fan = HU_FAN_ZHUO_WU_KUI;break;
    	case HU_KA_DIAO_ZHUO_WU_KUI:		fan = HU_FAN_KA_DIAO_ZHUO_WU_KUI;break;
    	case HU_YI_TIAO_LONG:				fan = HU_FAN_YI_TIAO_LONG;break;
    	case HU_BEN_HUN_YI_TIAO_LONG:		fan = HU_FAN_BEN_HUN_YI_TIAO_LONG;break;
    	case HU_HUN_YI_SE:					fan = HU_FAN_HUN_YI_SE;break;
    	case HU_QING_YI_SE:					fan = HU_FAN_QING_YI_SE;break;
    	case HU_DA_PIAO:					fan = HU_FAN_DA_PIAO;break;
    	case HU_GANG_SHANG_KAI_HUA:			fan = HU_FAN_GANG_SHANG_KAI_HUA;break;
    	case HU_GANG_HOU_DIAN_PAO:			fan = HU_FAN_GANG_HOU_DIAN_PAO;break;
    	case HU_HAI_LAO:					fan = HU_FAN_HAI_LAO;break;
    	case HU_QI_DUI:						fan = HU_FAN_QI_DUI;break;
    	case HU_HAO_HUA_QI_DUI:				fan = HU_FAN_HAO_HUA_QI_DUI;break;
    	case HU_HAO_HUA:					fan = HU_FAN_HAO_HUA;break;
    	case HU_SAN_HAO_HUA:				fan = HU_FAN_SAN_HAO_HUA;break;
    	case HU_TIAN_HU:					fan = HU_FAN_TIAN_HU;break;
    	case HU_DI_HU:						fan = HU_FAN_DI_HU;break;
    	case HU_SI_HUN_HU:					fan = 32;break;
    	case HU_PING_HU:					fan = HU_FAN_PING_HU;break;
    	case HU_QIANG_GANG_HU:				fan = HU_FAN_QIANG_GANG_HU;break;
    	case HU_BIAN:						fan = HU_FAN_BIAN_KA_DIAO;break;
    	case HU_KA:							fan = HU_FAN_BIAN_KA_DIAO;break;
    	case HU_DIAO:						fan = HU_FAN_BIAN_KA_DIAO;break;
    	case HU_KA_ZHUO_WU:					fan = HU_FAN_KA_DIAO_ZHUO_WU_KUI;break;
    	case HU_DIAO_ZHUO_WU:				fan = HU_FAN_KA_DIAO_ZHUO_WU_KUI;break;
    	case 0:								fan = 0;break;
    	case Cnst.HU_SI_HUN_SCORE:			fan = 10;break;
		}
    	return fan;
    }
    

    //杠分数计算
    public static final int GANG_TYPE_SPECIAL = 1;//东南西北，每一张一分 
    public static final int GANG_TYPE_MING = 1;
    public static final int GANG_TYPE_AN = 2;
    public static final int GANG_TYPE_HUN_GANG = 2;//混杠    1：杠出来和暗杠一样2      2：在手里变成10分
    
    //基本分
    public static final int SI_HUN_SCORE = 10;//四混分
    public static final int GEN_ZHUANG_SCORE = 4;//跟庄分
    public static final int LIANG_FENG_SCORE = 4;//亮风分
    public static final int BU_FENG_SCORE = 1;//补风分
    
    
    
    //退出类型
    public static final String EXIST_TYPE_EXIST = "exist";
    public static final String EXIST_TYPE_DISSOLVE = "dissolve";

    // 项目根路径
    public static String ROOTPATH = "";
    
    public static final int GLOBAL_TYPE_1 = 1;//经典玩儿法
    public static final int GLOBAL_TYPE_2 = 2;//撸麻巧
    public static final int GLOBAL_TYPE_3 = 3;//刮大风
    
    public static final int SCORE_GEN_ZHUANG_BASE = 4;//跟庄基础分
    public static final int SCORE_HUANG_ZHUANG_BASE = 4;//荒庄基础分
    
    public static final int ROOM_DIAN_TYPE_DA_BAO = 1;//点炮大包
    public static final int ROOM_DIAN_TYPE_BAO_SAN_JIA = 2;//点炮包三家
    public static final int ROOM_DIAN_TYPE_SAN_JIA_FU = 3;//点炮三家付
    
    
    
    

    
    //redis存储的key的不同类型的前缀
    public static final String REDIS_PREFIX_ROOMMAP = "TANGSHAN_ROOM_MAP_";//房间信息
//    public static final String REDIS_PREFIX_USERROOMNUMBERMAP = "USER_ROOMNUM_MAP_";//用户房间号码信息
//    public static final String REDIS_PREFIX_ROOMUSERMAP = "ROOM_USERS_MAP_";//房间人员信息
//    public static final String REDIS_PREFIX_IOSESSIONMAP = "IOSESSION_MAP_";//玩家——session数据
    public static final String REDIS_PREFIX_OPENIDUSERMAP = "TANGSHAN_OPENID_USERID_MAP_";//openId-user数据
    
//    public static final String REDIS_PREFIX_DISROOMIDMAP = "DIS_ROOMID_MAP_";//解散房间的任务
//    public static final String REDIS_PREFIX_DISROOMIDRESULTINFO = "DIS_ROOM_RESULT_MAP_";//房间解散状态集合
    
    public static final String REDIS_PREFIX_USER_ID_USER_MAP = "TANGSHAN_USER_ID_USER_MAP_";//通过userId获取用户
    public static final String REDIS__USER_SCORE = "TANGSHAN_SCORE_";//通过userId获取用户 
    public static final String REDIS__USER_HU_SCORE = "TANGSHAN_HU_SCORE_";//通过userId获取用户 
    public static final String REDIS__USER_NOW_SCORE = "TANGSHAN_NOW_SCORE_";//通过userId获取用户
    
    //redis中通知的key
    public static final String NOTICE_KEY = "TANGSHAN_NOTICE_KEY";
    
    public static final String PROJECT_PREFIX = "TANGSHAN_*";
    
    public static final String REDIS_PREFIX_CLUBMAP = "TANGSHAN_CLUB_MAP_";//俱乐部信息
    
    public static final String REDIS_PREFIX_CLUBMAP_LIST = "TANGSHAN_CLUB_MAP_LIST_";//存放俱乐部未开局房间信息
    
    public static final String REDIS_ONLINE_NUM_COUNT = "TANGSHAN_ONLINE_NUM_";
    
    public static final String REDIS_PAY_ORDERNUM = "TANGSHAN_PAY_ORDERNUM";//充值订单号
}
