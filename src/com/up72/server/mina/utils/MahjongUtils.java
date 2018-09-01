package com.up72.server.mina.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.mchange.v2.c3p0.impl.IdentityTokenized;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.InfoCount;
import com.up72.server.mina.utils.JudegHu.checkHu.Hulib;
import com.up72.server.mina.utils.JudegHu.checkHu.TableMgr;

import sun.net.www.content.text.plain;

/**
 * Created by Administrator on 2017/6/29.
 */
public class MahjongUtils {

	static {
		// 加载胡的可能
		TableMgr.getInstance().load();
	}

	/**
	 * 排序 给出一组牌 返回按照类型以及大小拍好顺序的牌
	 * 
	 * @param pais
	 * @return
	 */
	public static List<Integer[][]> paiXu(List<Integer[][]> pais) {
		Integer[] arrays = new Integer[pais.size()];
		for (int i = 0; i < arrays.length; i++) {
			arrays[i] = pais.get(i)[0][0] * 10 + pais.get(i)[0][1];
		}
		Arrays.sort(arrays);
		for (int i = 0; i < arrays.length; i++) {
			pais.get(i)[0][0] = arrays[i] / 10;
			pais.get(i)[0][1] = arrays[i] % 10;
		}
		return pais;
	}

	/**
	 * 吃牌检测 如果玩家的牌只有四张，就不能吃！吃牌的时候，手里的牌必须大于4
	 * 
	 * @param p
	 * @param pai
	 */
	public static List<Integer[][]> checkChi(Player p, Integer[][] pai) {
		if (pai[0][0].equals(4)) {// 牌数少于4或者是风牌,不能吃
			return null;
		}

		Integer[][] pai1 = new Integer[][] { { pai[0][0], pai[0][1] } };
		List<Integer[][]> result = new ArrayList<>();
		List<Integer[][]> sameType = new ArrayList<>();// 存放相同牌型的牌的集合
		for (int i = 0; i < p.getCurrentMjList().size(); i++) {
			if (pai1[0][0].equals(p.getCurrentMjList().get(i)[0][0])) {// 相同牌型
				boolean hasPai = false;
				for (int j = 0; j < sameType.size(); j++) {
					if (sameType.get(j)[0][0].equals(p.getCurrentMjList()
							.get(i)[0][0])
							&& sameType.get(j)[0][1].equals(p
									.getCurrentMjList().get(i)[0][1])) {
						hasPai = true;
					}
				}
				if (!hasPai) {
					sameType.add(new Integer[][] { {
							p.getCurrentMjList().get(i)[0][0],
							p.getCurrentMjList().get(i)[0][1] } });
				}
			}
		}
		if (pai[0][0] < 4) {// 万丙条
			// 如果，pai的值为1，要判断+1+2；
			// 如果，pai的值为9，要判断-1-2；
			// 如果，pai的值其他，要判断+1+2；-1-2；+1-1
			// 把所有牌型一致的牌两两组合，放入templete中，然后跟上述规则对比
			List<Integer[][]> templete = new ArrayList<>();
			for (int i = 0; i < sameType.size(); i++) {
				for (int j = i + 1; j < sameType.size(); j++) {
					templete.add(new Integer[][] {
							{ sameType.get(i)[0][0], sameType.get(i)[0][1] },
							{ sameType.get(j)[0][0], sameType.get(j)[0][1] } });
				}
			}
			for (int i = 0; i < templete.size(); i++) {
				if ((templete.get(i)[0][1].equals(pai1[0][1] + 1) && templete
						.get(i)[1][1].equals(pai1[0][1] + 2)) || // +1+2
						(templete.get(i)[0][1].equals(pai1[0][1] + 2) && templete
								.get(i)[1][1].equals(pai1[0][1] + 1)) || // +2+1
						(templete.get(i)[0][1].equals(pai1[0][1] - 1) && templete
								.get(i)[1][1].equals(pai1[0][1] - 2)) || // -1-2
						(templete.get(i)[0][1].equals(pai1[0][1] - 2) && templete
								.get(i)[1][1].equals(pai1[0][1] - 1)) || // -2-1
						(templete.get(i)[0][1].equals(pai1[0][1] - 1) && templete
								.get(i)[1][1].equals(pai1[0][1] + 1)) || // -1+1
						(templete.get(i)[0][1].equals(pai1[0][1] + 1) && templete
								.get(i)[1][1].equals(pai1[0][1] - 1))) {// +1-1
					result.add(templete.get(i));
				}
			}
		} else if (pai[0][0].equals(5)) {// 中发白
			List<Integer> strings = new ArrayList<>();
			for (Integer[][] integers : sameType) {
				strings.add(integers[0][1]);
			}
			strings.add(pai[0][1]);
			if (strings.contains(1) && strings.contains(2)
					&& strings.contains(3)) {
				if (sameType.size() >= 2) {
					if (sameType.size() == 2) {
						result.add(new Integer[][] {
								{ sameType.get(0)[0][0], sameType.get(0)[0][1] },
								{ sameType.get(1)[0][0], sameType.get(1)[0][1] } });
					} else {
						if (pai1[0][1].equals(1)) {
							result.add(new Integer[][] {
									{ sameType.get(1)[0][0],
											sameType.get(1)[0][1] },
									{ sameType.get(2)[0][0],
											sameType.get(2)[0][1] } });
						} else if (pai1[0][1].equals(2)) {
							result.add(new Integer[][] {
									{ sameType.get(0)[0][0],
											sameType.get(0)[0][1] },
									{ sameType.get(2)[0][0],
											sameType.get(2)[0][1] } });
						} else {
							result.add(new Integer[][] {
									{ sameType.get(0)[0][0],
											sameType.get(0)[0][1] },
									{ sameType.get(1)[0][0],
											sameType.get(1)[0][1] } });
						}
					}
				}
			}
		}
		return result.size() > 0 ? result : null;
	}

	/**
	 * 碰牌检测 如果玩家只有四张牌，需要检测是不是飘， 如果是飘，继续检测能不能碰，如果不是飘，就不能碰
	 * 
	 * @param p
	 * @param pai
	 * @return
	 */
	public static List<Integer[][]> checkPeng(Player p, Integer[][] pai) {
		List<Integer[][]> result = new ArrayList<>();
		result = pengGangCheck(p.getCurrentMjList(), pai, 3);
		return result != null && result.size() > 0 ? result : null;
	}

	/**
	 * 杠牌检测 必须先检测飘的情况 1.起手牌检测 pai参数为null 特殊杠 正常杠检测 2.游戏中检测 特殊杠 正常杠 3.手牌与碰牌集合检测
	 * 
	 * @param currentUser
	 *            当前玩家
	 * @param toUser
	 *            动作玩家
	 * @param pai
	 *            动作牌
	 * @return
	 */
	public static List<Integer[][]> checkGang(Player currentUser,
			Player toUser, Integer[][] pai) {
		List<Integer[][]> newList;
		ArrayList<Integer[][]> distinct;
		List<Integer[][]> result = new ArrayList<>();
		if (currentUser.getUserId().equals(toUser.getUserId())) {// 自摸的牌
			if (pai == null) {// 亮风之后 LastFaPai为null
				newList = getNewList(currentUser.getCurrentMjList());
			} else {
				newList = getNewList(currentUser.getCurrentMjList());
				newList.add(new Integer[][] { {
						currentUser.getLastFaPai()[0][0],
						currentUser.getLastFaPai()[0][1] } });
				paiXu(newList);
			}
			// 获取手中4张的组合
			distinct = getDistinct(newList);
			for (Integer[][] integers : distinct) {
				int num = 0;
				for (Integer[][] integers2 : newList) {
					if (integers[0][0].equals(integers2[0][0])
							&& integers[0][1].equals(integers2[0][1])) {
						num++;
					}
				}
				if (num == 4) {
					result.add(new Integer[][] {
							{ integers[0][0], integers[0][1] },
							{ integers[0][0], integers[0][1] },
							{ integers[0][0], integers[0][1] },
							{ integers[0][0], integers[0][1] } });
				}
			}
			// 检测手中的牌与碰牌集合有没有能组成杠的
			if (currentUser.getPengList() != null
					&& currentUser.getPengList().size() > 0) {
				for (int i = 0; i < currentUser.getPengList().size(); i++) {
					List<Integer[][]> pengList = currentUser.getPengList()
							.get(i).getL();
					for (int j = 0; j < newList.size(); j++) {
						if (newList.get(j)[0][0].equals(pengList.get(0)[0][0])
								&& newList.get(j)[0][1]
										.equals(pengList.get(0)[0][1])) {
							result.add(new Integer[][] { {
									newList.get(j)[0][0], newList.get(j)[0][1] } });
							break;
						}
					}
				}
			}
		} else {// 检测手牌中pai的数量
			newList = getNewList(currentUser.getCurrentMjList());
			// 获取手中3张的组合
			distinct = getDistinct(newList);
			int num = 0;
			for (Integer[][] integers2 : newList) {
				if (pai[0][0].equals(integers2[0][0])
						&& pai[0][1].equals(integers2[0][1])) {
					num++;
				}
			}
			if (num == 3) {
				result.add(new Integer[][] { { pai[0][0], pai[0][1] },
						{ pai[0][0], pai[0][1] }, { pai[0][0], pai[0][1] } });
			}
		}
		return result.size() > 0 ? result : null;
	}

	/**
	 * 获取一种牌的集合
	 * 
	 * @param info
	 *            牌类型
	 * @return 这种牌的集合
	 */
	public static List<Integer[][]> getFengList(Integer info) {
		List<Integer[][]> list = new ArrayList<>();
		// 根据牌类型获取此牌的总数量
		if (0 < info && info < 5) {
			Integer paiNum = MahjongCons.mahjongType.get(info);
			for (int i = 1; i <= paiNum; i++) {
				list.add(new Integer[][] { { info, i } });

			}
		}
		return list;
	}

	/**
	 * 获取一种牌的集合
	 * 
	 * @param info
	 *            牌类型
	 * @return 这种牌的集合
	 */
	public static boolean checkSiHun(List<Integer[][]> list, RoomResp roomResp) {
		// 获取房间混牌
		List<Integer[][]> hunList = roomResp.getHunPai();
		Integer[][] hunPai = hunList.get(1);
		int num = 0;
		for (Integer[][] integer : list) {
			if (integer[0][0].equals(hunPai[0][0])
					&& integer[0][1].equals(hunPai[0][1])) {
				num++;
			}
		}
		if (num == 4) {
			return true;
		}
		return false;
	}

	/**
	 * 亮风检测，有亮风组合，继续补充
	 * 
	 * @param p
	 * @param palyerPai
	 * @param pai
	 * @return
	 */
	public static List<Integer[][]> checkLiangFeng(Player currentPalyer,
			Integer[][] pai) {
		List<Integer[][]> newList = getNewList(currentPalyer.getCurrentMjList());
		List<Integer[][]> result = new ArrayList<>();
		if (pai != null) {
			newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
		}
		// 检测东南西北
		if (currentPalyer.getGangListType2() != null
				&& currentPalyer.getGangListType2().size() > 0) {// 有亮风组合
			for (int i = 0; i < newList.size(); i++) {
				if (newList.get(i)[0][0].equals(4)) {// 手中的4一定是最后摸到的或者对子
					result.add(new Integer[][] { { newList.get(i)[0][0],
							newList.get(i)[0][1] } });
				}
			}
		} else {// 没有亮风，看看手牌能不能组成亮风
			ArrayList<Integer[][]> distinctPais = getDistinct(newList);
			StringBuffer stringBuffer = new StringBuffer();
			for (int i = 0; i < distinctPais.size(); i++) {
				if (distinctPais.get(i)[0][0].equals(4)) {
					stringBuffer.append(distinctPais.get(i)[0][1]);
				}
			}
			String string = stringBuffer.toString();
			String[] split1 = string.split("");
			if (string.length() == 3) {
				result.add(new Integer[][] { { 4, Integer.valueOf(split1[0]) },
						{ 4, Integer.valueOf(split1[1]) },
						{ 4, Integer.valueOf(split1[2]) } });
			} else if (string.length() == 4) {
				result.add(new Integer[][] { { 4, 1 }, { 4, 2 }, { 4, 3 },
						{ 4, 4 } });
			}
		}
		return result.size() > 0 ? result : null;
	}

	/**
	 * 亮风执行，有亮风组合，继续补充
	 * 
	 * @param p
	 * @param palyerPai
	 * @param pai
	 * @return
	 */
	public static void liangFeng(Player p, List<Integer[][]> pais) {
		Integer[][] toRemove = null;
		if (pais.size() >= 3) {
			if (p.getLastFaPai() != null && p.getLastFaPai()[0][0].equals(4)) {
				for (Integer[][] temp : pais) {
					if (temp[0][1].equals(p.getLastFaPai()[0][1])) {// 执行的牌是最后一张发牌
						toRemove = temp;
						pais.remove(temp);
						break;
					}
				}
			}
			removePais(p.getCurrentMjList(), pais);
			if (toRemove != null) {
				pais.add(toRemove);
				pais = paiXu(pais);
				p.setLastFaPai(null);
			}
		} else if (pais.size() == 1) {
			if (pais.get(0)[0][0].equals(4) && p.getGangListType2() != null
					&& p.getGangListType2().size() > 0) {
				// 移除的牌是最后一张
				Integer[][] pai = pais.get(0);// 动作牌
				List<InfoCount> gangListType2 = p.getGangListType2();
				List<Integer[][]> l = gangListType2.get(0).getL();
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < l.size(); i++) {
					sb.append(l.get(i)[0][1]);
				}
				if (sb.length() == 3) {// 原来的亮风组合是3张
					String dongzuo = pai[0][1].toString();
					String string = sb.toString();
					// 如果动作牌是是组合中有的
					if (string.contains(dongzuo)) {
						if (p.getLastFaPai() != null
								&& p.getLastFaPai()[0][0].equals(4)) {
							for (Integer[][] temp : pais) {
								if (temp[0][1].equals(p.getLastFaPai()[0][1])) {
									toRemove = temp;
									pais.remove(temp);
									break;
								}
							}
						}
						removePais(p.getCurrentMjList(), pais);
						if (toRemove != null) {
							p.setLastFaPai(null);
							pais.add(toRemove);
							pais = paiXu(pais);
						}
					} else {
						// 如果动作牌是与那里组合中没有的
						if (p.getLastFaPai() != null
								&& p.getLastFaPai()[0][0].equals(4)) {
							for (Integer[][] temp : pais) {
								if (temp[0][1].equals(p.getLastFaPai()[0][1])) {
									toRemove = temp;
									pais.remove(temp);
									break;
								}
							}
						}
						removePais(p.getCurrentMjList(), pais);
						if (toRemove != null) {
							p.setLastFaPai(null);
							pais.add(toRemove);
							pais = paiXu(pais);
						}
						// 将这个张牌加到原来的gang2中
						l.add(new Integer[][] { { 4, pai[0][1] } });
					}
				} else if (sb.length() == 4) {// 原来的亮风组和是4张
					if (p.getLastFaPai() != null
							&& p.getLastFaPai()[0][0].equals(4)) {
						for (Integer[][] temp : pais) {
							if (temp[0][1].equals(p.getLastFaPai()[0][1])) {
								toRemove = temp;
								pais.remove(temp);
								break;
							}
						}
					}
					removePais(p.getCurrentMjList(), pais);
					if (toRemove != null) {
						p.setLastFaPai(null);
						pais.add(toRemove);
						pais = paiXu(pais);
					}
				}

			}
		}

	}

	/**
	 * 碰杠检测的核心方法 在传入pai参数的情况下，检测碰或者杠的逻辑基本相符（杠是四张牌的基本杠），提取出的公共方法
	 * 
	 * @param palyerPai
	 * @param pai
	 * @return
	 * 
	 */
	private static List<Integer[][]> pengGangCheck(List<Integer[][]> palyerPai,
			Integer[][] pai, Integer ckeckNum) {
		if (pai == null) {// 初始杠检测
			List<Integer[][]> list = new LinkedList<>();
			Set<String> set = new HashSet<>();
			for (int i = 0; i < palyerPai.size(); i++) {
				int num = 0;
				if (!set.contains(palyerPai.get(i)[0][0] + "_"
						+ palyerPai.get(i)[0][1])) {
					for (int j = 0; j < palyerPai.size(); j++) {
						if (palyerPai.get(i)[0][0]
								.equals(palyerPai.get(j)[0][0])
								&& palyerPai.get(i)[0][1].equals(palyerPai
										.get(j)[0][1])) {
							num++;
						}
					}
				}
				if (num == 4) {
					set.add(palyerPai.get(i)[0][0] + "_"
							+ palyerPai.get(i)[0][1]);
					list.add(new Integer[][] {
							{ palyerPai.get(i)[0][0], palyerPai.get(i)[0][1] },
							{ palyerPai.get(i)[0][0], palyerPai.get(i)[0][1] },
							{ palyerPai.get(i)[0][0], palyerPai.get(i)[0][1] },
							{ palyerPai.get(i)[0][0], palyerPai.get(i)[0][1] } });
				}
			}
			return list.size() > 0 ? list : null;
		} else {
			Integer[][] pai1 = new Integer[][] { { pai[0][0], pai[0][1] } };
			ckeckNum--;
			Map<String, Integer> map = new HashMap<>();// 牌---次数
			for (int i = 0; i < palyerPai.size(); i++) {
				if (pai1[0][0].equals(palyerPai.get(i)[0][0])
						&& pai1[0][1].equals(palyerPai.get(i)[0][1])) {// 与传入的牌值相同
					if (map.containsKey(pai1[0][0] + "_" + pai1[0][1])) {
						map.put(pai1[0][0] + "_" + pai1[0][1],
								map.get(pai1[0][0] + "_" + pai1[0][1]) + 1);
					} else {
						map.put(pai1[0][0] + "_" + pai1[0][1], 1);
					}
				}
			}
			if (map.containsKey(pai1[0][0] + "_" + pai1[0][1])
					&& map.get(pai1[0][0] + "_" + pai1[0][1]) >= ckeckNum) {
				List<Integer[][]> result = new ArrayList<>();
				if (ckeckNum.equals(3)) {
					result.add(new Integer[][] { { pai1[0][0], pai1[0][1] },
							{ pai1[0][0], pai1[0][1] },
							{ pai1[0][0], pai1[0][1] } });
				} else if (ckeckNum.equals(2)) {
					result.add(new Integer[][] { { pai1[0][0], pai1[0][1] },
							{ pai1[0][0], pai1[0][1] } });
				}
				return result;
			}
		}
		return null;
	}

	public static List<Integer[][]> initMahjongs() {
		List<Integer[][]> list = new ArrayList<>();
		for (Integer type : MahjongCons.mahjongType.keySet()) {
			for (int i = 0; i < MahjongCons.mahjongType.get(type); i++) {
				for (int j = 0; j < 4; j++) {
					list.add(new Integer[][] { { type, i + 1 } });
				}
			}
		}
		return list;
	}

	/**
	 * 洗牌 传入一副麻将，打乱顺序之后，返回麻将
	 * 
	 * @param mahjongs
	 * @return
	 */
	public static List<Integer[][]> xiPai(List<Integer[][]> mahjongs) {
		List<Integer[][]> temp = new ArrayList<>();
		int last = mahjongs.size();
		int random = 0;
		for (int i = 0; i < mahjongs.size(); i++) {
			random = (int) (Math.random() * last);
			temp.add(new Integer[][] { { mahjongs.get(random)[0][0],
					mahjongs.get(random)[0][1] } });
			mahjongs.remove(random);
			i--;
			last = mahjongs.size();
		}
		return temp;
	}

	/**
	 * 发牌/揭牌 传入麻将列表，以及要发几张牌，返回对应的数组 如果牌数少于要求返回的张数，返回null
	 * 
	 * @param mahjongs
	 * @param num
	 * @return
	 */
	public static List<Integer[][]> faPai(List<Integer[][]> mahjongs,
			Integer num) {
		if (mahjongs.size() == 0) {
			return null;
		}
		List<Integer[][]> result = new ArrayList<>();
		// int random = 0;
		for (int i = 0; i < num; i++) {
			// random = (int)(Math.random()*mahjongs.size());
			result.add(new Integer[][] { { mahjongs.get(i)[0][0],
					mahjongs.get(i)[0][1] } });
			mahjongs.remove(i);
		}
		if (num > 1) {
			result = paiXu(result);
		}
		return result;
	}

	/**
	 * 吃牌动作，传入玩家手上的牌，以及要吃的三张牌，返回吃牌后玩家的手上的牌
	 * 
	 * @param playerPais
	 * @param chiPais
	 * @return
	 */
	public static List<Integer[][]> chi(List<Integer[][]> playerPais,
			List<Integer[][]> chiPais) {
		if (playerPais.size() <= chiPais.size()) {
			return playerPais;
		}
		for (int i = 0; i < chiPais.size(); i++) {
			for (int j = 0; j < playerPais.size(); j++) {
				if (playerPais.get(j)[0][0].equals(chiPais.get(i)[0][0])
						&& playerPais.get(j)[0][1].equals(chiPais.get(i)[0][1])) {
					playerPais.remove(j);
					break;
				}
			}
		}
		return playerPais;
	}

	/**
	 * 碰牌操作，传入玩家手中的牌，以及要碰的牌，返回碰之后的牌
	 * 
	 * @param playerPais
	 * @param pai
	 * @return
	 */
	public static List<Integer[][]> peng(List<Integer[][]> playerPais,
			Integer[][] pai) {
		return pengOrGang(playerPais, pai, 2);
	}

	/**
	 * 杠牌操作，传入玩家手中的牌，以及要杠的牌，返回杠的类型 如果返回0，代表有玩家能有胡的操作 4张 ，自摸的牌组成暗杠 3张，
	 * 别人出的牌，我手中有三张 1张，自己摸了牌，手牌中有能和碰集合组合成杠
	 * 
	 * @param p
	 * @param pais
	 * @return
	 */
	public static Integer gang(Player p, List<Integer[][]> pais) {
		// 3碰的杠（明杠），4点的杠（明杠），5暗杠
		Integer gangType = null;
		List<Integer[][]> currentMjList = p.getCurrentMjList();
		Integer[][] lastFaPai = p.getLastFaPai();
		Integer[][] pai = pais.get(0);
		if (pais.size() == 3) {// 1碰杠
			removeNumPai(currentMjList, pais.get(0), 3);
			gangType = 4;
		} else if (pais.size() == 4) {// 自己暗杠
			if (lastFaPai != null
					&& (!lastFaPai[0][0].equals(pai[0][0]) || !lastFaPai[0][1]
							.equals(pai[0][1]))) {// 这张牌不是最后一张牌
				removeNumPai(currentMjList, pais.get(0), 4);
				currentMjList.add(lastFaPai);
				paiXu(currentMjList);
			} else {
				removeNumPai(currentMjList, pais.get(0), 3);// 移除3张牌 和发的那张牌
			}
			p.setLastFaPai(null);
			gangType = 5;
		} else if (pais.size() == 1) {// 游戏中不的牌和碰组成的杠
			// 移除这个碰
			if (lastFaPai != null
					&& (!lastFaPai[0][0].equals(pai[0][0]) || !lastFaPai[0][1]
							.equals(pai[0][1]))) {// 这张牌不是最后一张牌
				removeNumPai(currentMjList, pai, 1);
				currentMjList.add(lastFaPai);
				paiXu(currentMjList);
			} else {
				removeNumPai(currentMjList, pai, 1);
			}
			p.setLastFaPai(null);
			InfoCount info = new InfoCount();
			List<InfoCount> pengList = p.getPengList();
			for (int i = pengList.size() - 1; i >= 0; i--) {
				List<Integer[][]> l = pengList.get(i).getL();
				if (l.get(0)[0][0].equals(pai[0][0])
						&& l.get(0)[0][1].equals(pai[0][1])) {
					info = pengList.get(i);
					pengList.remove(i);
					break;
				}
			}
			info.getL().add(new Integer[][] { { pai[0][0], pai[0][1] } });
			List<InfoCount> gangListType3 = p.getGangListType3();
			if (gangListType3 == null) {
				gangListType3 = new ArrayList<>();
			}
			gangListType3.add(info);
			gangType = 3;
		}
		return gangType;
	}

	private static void removePais(List<Integer[][]> playerPais,
			List<Integer[][]> toRemovePais) {
		for (int i = 0; i < toRemovePais.size(); i++) {
			for (int j = 0; j < playerPais.size(); j++) {
				if (playerPais.get(j)[0][0].equals(toRemovePais.get(i)[0][0])
						&& playerPais.get(j)[0][1]
								.equals(toRemovePais.get(i)[0][1])) {
					playerPais.remove(j);
					break;
				}
			}
		}
	}

	/**
	 * 碰杠的执行方法
	 * 
	 * @param playerPais
	 * @param pai
	 * @param num
	 * @return
	 */
	private static List<Integer[][]> pengOrGang(List<Integer[][]> playerPais,
			Integer[][] pai, Integer num) {
		for (int n = 0; n < num; n++) {
			for (int i = 0; i < playerPais.size(); i++) {
				if (pai[0][0].equals(playerPais.get(i)[0][0])
						&& pai[0][1].equals(playerPais.get(i)[0][1])) {
					playerPais.remove(i);
					break;
				}
			}
		}
		return playerPais;
	}

	public static void main(String[] args) {
//		List<Integer[][]> tempShouPai=new ArrayList<Integer[][]>();
//		List<Integer[][]> hunPai=new ArrayList<Integer[][]>();
//		tempShouPai.add(new Integer[][]{{1,1}});
//		tempShouPai.add(new Integer[][]{{1,2}});
//		tempShouPai.add(new Integer[][]{{1,3}});
//		tempShouPai.add(new Integer[][]{{1,4}});
//		tempShouPai.add(new Integer[][]{{1,5}});
//		hunPai.add(new Integer[][]{{1,1}});
//		hunPai.add(new Integer[][]{{1,2}});
//		RoomResp roomResp=new RoomResp();
//		roomResp.setHunPai(hunPai);
//		List<Integer[][]> hunList = getHunList(tempShouPai, roomResp);
//		System.err.println(hunList.size());

	}

	/**
	 * 别人出的混牌 只能做这张牌牌本身使用
	 * 
	 * @param winUser
	 *            赢得玩家
	 * @param toUser
	 *            出牌的玩家
	 * @param pai
	 *            引发动作的牌 ，当其为null时，属于自摸，当其不为null时，会加入其它人手牌检测
	 * @return map
	 */
	public static List<Map<Integer, Integer>> checkHuInfo(Player winUser,
			Player toUser, Integer[][] pai, RoomResp roomResp) {

		// 基本分 map
		HashMap<Integer, Integer> map = new HashMap<>();
		// 最大分 map
		HashMap<Integer, Integer> maxMap = new HashMap<>();
		HashMap<String, Integer> fenMap = new HashMap<>();
		// 用于存储所有可能的map集合
		List<Map<Integer, Integer>> mapList = new ArrayList<>();
		// 初始化分
		fenMap.put("jiBen1", 0);
		fenMap.put("piao1", 0);
		fenMap.put("piao2", 0);
		fenMap.put("qidui1", 0);
		fenMap.put("qidui2", 0);
		fenMap.put("qidui3", 0);
		fenMap.put("diao1", 0);
		fenMap.put("diaopiao1", 0);
		fenMap.put("diao2", 0);
		fenMap.put("ka1", 0);
		fenMap.put("ka2", 0);
		fenMap.put("bian1", 0);
		fenMap.put("bian2", 0);
		Boolean menQing = isMenQing(winUser);
		// 玩家开门，不能打混
		if (roomResp.getDaHun().equals(0) && !menQing) {
			Integer diXianPaiNum = roomResp.getDiXianPaiNum();
			List<Integer[][]> c1 = roomResp.getCurrentMjList();
			roomResp = new RoomResp();
			List<Integer[][]> a = new ArrayList<>();
			a.add(new Integer[][] { { -1, -1 } });
			a.add(new Integer[][] { { -1, -1 } });
			roomResp.setDiXianPaiNum(diXianPaiNum);
			roomResp.setCurrentMjList(c1);
			roomResp.setHunPai(a);// 设置四混胡
			roomResp.setSiHunHu(0);// 规则没有四混胡
		}
		// 获取混牌数量
		List<Integer[][]> newList = getNewList(winUser.getCurrentMjList());
		List<Integer[][]> hunList = new ArrayList<>();
		if (!winUser.getUserId().equals(toUser.getUserId())) {// 不是自摸
			hunList = getHunList(newList, roomResp);// 移除混牌后的手牌newList
													// 集合和混牌集合hunList
			paiXu(newList);
		} else {// 是自摸
			Integer[][] lastFaPai = winUser.getLastFaPai();
			newList.add(new Integer[][] { { lastFaPai[0][0], lastFaPai[0][1] } });
			hunList = getHunList(newList, roomResp);
		}

		Integer siHunHu = roomResp.getSiHunHu();
		if (siHunHu.equals(1)) {// 规则有四混胡
			if (hunList.size() == 4) {
				map.put(Cnst.HU_SI_HUN_HU, 32);
				mapList.add(map);
				return mapList;
			}
		}
		// 10：一条龙（本混一条龙） **
		Integer longInfo = checkLong(winUser, toUser, pai, roomResp);
		if (longInfo.equals(2)) {
			map.put(Cnst.HU_BEN_HUN_YI_TIAO_LONG,
					Cnst.HU_FAN_BEN_HUN_YI_TIAO_LONG);
		} else if (longInfo.equals(1)) {
			map.put(Cnst.HU_YI_TIAO_LONG, Cnst.HU_FAN_YI_TIAO_LONG);
		} else if (longInfo.equals(3)) {// 需要加上干胡分
			map.put(Cnst.HU_BEN_HUN_YI_TIAO_LONG,
					Cnst.HU_FAN_BEN_HUN_YI_TIAO_LONG);
			map.put(Cnst.HU_MEI_HUN, Cnst.HU_FAN_MEI_HUN);
		}
		// 4：没混 赢时手中没混（混吊混也是没混）
		Integer checkMeiHun = checkMeiHun(winUser, toUser, pai, roomResp);
		if (checkMeiHun != null && !checkMeiHun.equals(0)) {
			map.put(Cnst.HU_MEI_HUN, Cnst.HU_FAN_MEI_HUN);
		}
		// 1：门清 **
		Boolean menqing = isMenQing(winUser);
		if (menqing) {
			map.put(Cnst.HU_MEN_QING, Cnst.HU_FAN_MEN_QING);
		}
		// 2：庄家 **
		if (winUser.getZhuang()) {
			map.put(Cnst.HU_ZHAUNG, Cnst.HU_FAN_ZHAUNG);
			// 18:天湖
			if (winUser.getZhuaPaiNum().equals(1)
					&& (winUser.getUserId().equals(toUser.getUserId()))) {// 是自摸并且抓牌数为1
				map.put(Cnst.HU_TIAN_HU, Cnst.HU_FAN_TIAN_HU);
			}
		} else {// 不是庄 //19:地胡
			if (winUser.getZhuaPaiNum().equals(1)
					&& (winUser.getUserId().equals(toUser.getUserId()))
					&& (winUser.getChiList() == null || winUser.getChuList()
							.size() == 0)) {
				map.put(Cnst.HU_DI_HU, Cnst.HU_FAN_DI_HU);
			}
		}

		// 6：自摸
		if (winUser.getUserId().equals(toUser.getUserId())) {// 自摸
			map.put(Cnst.HU_ZI_MO, Cnst.HU_FAN_ZI_MO);
		} else {
			// 3：点炮
			map.put(Cnst.HU_DIAN_PAO, Cnst.HU_FAN_DIAN_PAO);
		}
		// 2是混一色，1是清一色 ***
		Integer qingOrHuFanNum = getQingOrHuFanNum(winUser, toUser, pai,
				roomResp);
		if (qingOrHuFanNum != null && !qingOrHuFanNum.equals(0)) {// 不为空，且不能为0
			if (qingOrHuFanNum.equals(2)) {
				map.put(Cnst.HU_HUN_YI_SE, Cnst.HU_FAN_HUN_YI_SE);
			} else if (qingOrHuFanNum.equals(1)) {
				map.put(Cnst.HU_QING_YI_SE, Cnst.HU_FAN_QING_YI_SE);
			}
		}

		// 杠后点炮
		if (!winUser.getUserId().equals(toUser.getUserId())) {// 如果是不是自摸
			if (toUser.getHasGang()) {// 被点的人上次有杠
				map.put(Cnst.HU_GANG_HOU_DIAN_PAO,
						Cnst.HU_FAN_GANG_HOU_DIAN_PAO);
			}
		}
		// 14:杠上开花
		if (winUser.getUserId().equals(toUser.getUserId())) {// 如果是自摸赢
			if (winUser.getHasGang()) {// 检测上次是否有杠
				map.put(Cnst.HU_GANG_SHANG_KAI_HUA,
						Cnst.HU_FAN_GANG_SHANG_KAI_HUA);
			}
		}

		// 16:海捞 房间剩余牌数-限制牌数
		Boolean haiLao = isHaiLao(roomResp);
		if (haiLao) {
			map.put(Cnst.HU_HAI_LAO, Cnst.HU_FAN_HAI_LAO);
		}
		maxMap.putAll(map);
		Integer hunPaiNum = hunList.size();// 混牌数
		// 17:七对（七对，豪华七对，豪华，三豪华）**
		Integer haoHuaFanShu = hasQiDui(winUser, toUser, pai, roomResp);
		if (haoHuaFanShu != null) {
			if (haoHuaFanShu.equals(0)) {
				fenMap.put("qidui1", Cnst.HU_QI_DUI);
			} else if (haoHuaFanShu.equals(1)) {
				fenMap.put("qidui1", Cnst.HU_HAO_HUA_QI_DUI);
			} else if (haoHuaFanShu.equals(2)) {
				fenMap.put("qidui1", Cnst.HU_HAO_HUA);
			} else if (haoHuaFanShu.equals(3)) {
				fenMap.put("qidui1", Cnst.HU_SAN_HAO_HUA);
			}
			if (map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)) {
				fenMap.put("qidui3", Cnst.HU_FAN_BEN_HUN_YI_TIAO_LONG);
			} else if (map.containsKey(Cnst.HU_YI_TIAO_LONG)) {
				fenMap.put("qidui3", Cnst.HU_FAN_YI_TIAO_LONG);
			}
			if (hunPaiNum != 0
					&& !map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)) {
				Integer hasQiDui = hasQiDui(winUser, toUser, pai, null);
				if (hasQiDui != null) {// 是本混
					fenMap.put("qidui2", Cnst.HU_FAN_BEN_HUN);
				}
			}
		}
		// 卡边钓分别检测
		Integer result = 0;
		// 本混和没混只能显示一个，都属于干胡
		// 是否吊 （混吊混的时候，本混和没混只能显示一个）
		result = checkNewDiao(winUser, toUser, pai, roomResp);
		if (result.equals(Cnst.HU_DIAO_ZHUO_WU)) {// 是吊捉五
			// 捉五魁
			fenMap.put("diao1", Cnst.HU_DIAO_ZHUO_WU);
			// 检测是否飘
			if (winUser.getChiList() == null
					|| winUser.getChiList().size() == 0) {// 没有吃
				Boolean isPiao = getPiaoInfo(winUser, toUser, pai, roomResp);
				if (isPiao) {
					fenMap.put("diaopiao1", Cnst.HU_DA_PIAO);
					// 检测是否本混
					if (hunPaiNum != 0
							&& !map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)
							&& !map.containsKey(Cnst.HU_MEI_HUN)) {
						isPiao = getPiaoInfo(winUser, toUser, pai, null);// ----逻辑
						if (isPiao) {// 是本混
							fenMap.put("diao2", Cnst.HU_FAN_BEN_HUN);
						}
					}
				}
			} else {// 是捉5，不是飘，是否本混
				if (hunPaiNum != 0
						&& !map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)
						&& !map.containsKey(Cnst.HU_MEI_HUN)) {
					result = checkNewDiao(winUser, toUser, pai, null);
					if (!result.equals(0)) {// 是本混
						fenMap.put("diao2", Cnst.HU_FAN_BEN_HUN);
					}
				}
			}
		} else if (result.equals(Cnst.HU_DIAO)) {// 是普通吊
			fenMap.put("diao1", Cnst.HU_DIAO);// 这里写的是分数
			// 检测是否飘
			if (winUser.getChiList() == null
					|| winUser.getChiList().size() == 0) {// 没有吃
				Boolean isPiao = getPiaoInfo(winUser, toUser, pai, roomResp);
				if (isPiao) {
					fenMap.put("diaopiao1", Cnst.HU_DA_PIAO);
					// 检测是否本混
					if (hunPaiNum != 0
							&& !map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)
							&& !map.containsKey(Cnst.HU_MEI_HUN)) {
						isPiao = getPiaoInfo(winUser, toUser, pai, null);// ----逻辑
						if (isPiao) {// 是本混
							fenMap.put("diao2", Cnst.HU_FAN_BEN_HUN);
						}
					}
				}
			} else {// 不是飘,仅仅是普通吊
				if (hunPaiNum != 0
						&& !map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)
						&& !map.containsKey(Cnst.HU_MEI_HUN)) {
					result = checkNewDiao(winUser, toUser, pai, null);
					if (!result.equals(0)) {// 不是本混
						fenMap.put("diao2", Cnst.HU_FAN_BEN_HUN);
					}
				}
			}
		}
		// 是否卡
		result = checkNewKa(winUser, toUser, pai, roomResp);
		if (!result.equals(0)) {// 满足卡
			// 是否满足卡捉五魁
			result = checkKaZhuoWu(winUser, toUser, pai, roomResp);
			if (result.equals(0)) {// 不满足
				fenMap.put("ka1", Cnst.HU_KA);
				// 检测本混
				if (hunPaiNum != 0
						&& !map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)) {
					result = checkNewKa(winUser, toUser, pai, null);
					if (!result.equals(0)) {
						fenMap.put("ka2", Cnst.HU_FAN_BEN_HUN);
					}
				}
			} else {// 满足捉五
				fenMap.put("ka1", Cnst.HU_KA_ZHUO_WU);
				// 是否本混
				if (hunPaiNum != 0
						&& !map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)) {
					result = checkKaZhuoWu(winUser, toUser, pai, null);
					if (!result.equals(0)) {
						fenMap.put("ka2", Cnst.HU_FAN_BEN_HUN);
					}
				}
			}
		}
		// 检测边
		result = checkNewBian(winUser, toUser, pai, roomResp);
		if (!result.equals(0)) {// 满足边
			fenMap.put("bian1", Cnst.HU_BIAN);
			// 检测是否本混
			if (hunPaiNum != 0
					&& !map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)) {
				result = checkNewBian(winUser, toUser, pai, null);
				if (!result.equals(0)) {
					fenMap.put("bian2", Cnst.HU_FAN_BEN_HUN);
				}
			}
		}

		if (fenMap.get("diaopiao1").equals(0)) {// 吊里面没有包含大瓢
			// 检测飘
			if (winUser.getChiList() == null
					|| winUser.getChiList().size() == 0) {// 没有吃
				Boolean isPiao = getPiaoInfo(winUser, toUser, pai, roomResp);
				if (isPiao) {
					fenMap.put("piao1", Cnst.HU_DA_PIAO);
					if (hunPaiNum != 0
							&& !map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)) {
						isPiao = getPiaoInfo(winUser, toUser, pai, null);// ----逻辑
						if (isPiao) {
							fenMap.put("piao2", Cnst.HU_FAN_BEN_HUN);
						}
					}
				}
			}
		}
		// 检测是否干胡（1：没混 --2:本混）
		if (map.containsKey(Cnst.HU_MEI_HUN)) {// 保含没混
			fenMap.put("jiBen1", Cnst.HU_MEI_HUN);
		} else {// 检测本混
			if (!map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)) {
				boolean isHu = checkHuNew1(null, winUser, toUser, pai, null);
				if (isHu) {
					fenMap.put("jiBen1", Cnst.HU_BEN_HUN);
				}
			}
		}
		int jiBenSum = Cnst.getFan(fenMap.get("jiBen1"));
		int piaoSum = fenMap.get("piao2") + Cnst.getFan(fenMap.get("piao1"));
		int qiduiSum = fenMap.get("qidui2") + Cnst.getFan(fenMap.get("qidui1"))
				- fenMap.get("qidui3");// 七对和一条龙冲突
		int diaoSum = fenMap.get("diao2")
				+ Cnst.getFan(fenMap.get("diaopiao1"))
				+ Cnst.getFan(fenMap.get("diao1"));
		int kaSum = fenMap.get("ka2") + Cnst.getFan(fenMap.get("ka1"));
		int bianSum = fenMap.get("bian2") + Cnst.getFan(fenMap.get("bian1"));
		// 各种胡法的存储
		HashMap<Integer, Integer> jiBenMap = new HashMap<>();
		jiBenMap.putAll(map);
		HashMap<Integer, Integer> qiDuiMap = new HashMap<>();
		qiDuiMap.putAll(map);
		HashMap<Integer, Integer> diaoMap = new HashMap<>();
		diaoMap.putAll(map);
		HashMap<Integer, Integer> kaMap = new HashMap<>();
		kaMap.putAll(map);
		HashMap<Integer, Integer> bianMap = new HashMap<>();
		bianMap.putAll(map);
		HashMap<Integer, Integer> piaoMap = new HashMap<>();
		piaoMap.putAll(map);
		// 添加分最大的map
		int[] arr = { jiBenSum, piaoSum, qiduiSum, diaoSum, kaSum, bianSum };
		int max = getMax(arr);
		if (max != 0) {
			if (max == jiBenSum) {
				maxMap.put(fenMap.get("jiBen1"),
						Cnst.getFan(fenMap.get("jiBen1")));
			} else if (max == qiduiSum) {
				maxMap.put(fenMap.get("qidui1"),
						Cnst.getFan(fenMap.get("qidui1")));
				if (fenMap.get("qidui2") != 0) {// 这个牌型没有本混
					maxMap.put(Cnst.HU_BEN_HUN, Cnst.HU_FAN_BEN_HUN);
				}
				if (map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)) {
					maxMap.remove(Cnst.HU_BEN_HUN_YI_TIAO_LONG);
				} else if (map.containsKey(Cnst.HU_YI_TIAO_LONG)) {
					maxMap.remove(Cnst.HU_YI_TIAO_LONG);
				}
			} else if (max == piaoSum) {
				maxMap.put(fenMap.get("piao1"),
						Cnst.getFan(fenMap.get("piao1")));
				if (fenMap.get("piao2") != 0) {// 这个牌型没有本混
					maxMap.put(Cnst.HU_BEN_HUN, Cnst.HU_FAN_BEN_HUN);
				}
			} else if (max == diaoSum) {
				maxMap.put(fenMap.get("diao1"),
						Cnst.getFan(fenMap.get("diao1")));
				if (fenMap.get("diaopiao1") != 0) {
					maxMap.put(Cnst.HU_DA_PIAO, Cnst.HU_FAN_DA_PIAO);
				}
				if (fenMap.get("diao2") != 0) {// 这个牌型没有本混
					maxMap.put(Cnst.HU_BEN_HUN, Cnst.HU_FAN_BEN_HUN);
				}
			} else if (max == kaSum) {
				maxMap.put(fenMap.get("ka1"), Cnst.getFan(fenMap.get("ka1")));
				if (fenMap.get("ka2") != 0) {// 这个牌型没有本混
					maxMap.put(Cnst.HU_BEN_HUN, Cnst.HU_FAN_BEN_HUN);
				}
			} else if (max == bianSum) {
				maxMap.put(fenMap.get("bian1"),
						Cnst.getFan(fenMap.get("bian1")));
				if (fenMap.get("bian2") != 0) {// 这个牌型没有本混
					maxMap.put(Cnst.HU_BEN_HUN, Cnst.HU_FAN_BEN_HUN);
				}
			}
		}
		mapList.add(maxMap);// 最后一个map是最大分的map
		if (jiBenSum != 0) {
			jiBenMap.put(fenMap.get("jiBen1"),
					Cnst.getFan(fenMap.get("jiBen1")));
			mapList.add(jiBenMap);
		}

		if (fenMap.get("qidui1") != 0) {
			qiDuiMap.put(fenMap.get("qidui1"),
					Cnst.getFan(fenMap.get("qidui1")));
			if (fenMap.get("qidui2") != 0) {// 这个牌型没有本混
				qiDuiMap.put(Cnst.HU_BEN_HUN, Cnst.HU_FAN_BEN_HUN);
			}
			if (map.containsKey(Cnst.HU_BEN_HUN_YI_TIAO_LONG)) {
				qiDuiMap.remove(Cnst.HU_BEN_HUN_YI_TIAO_LONG);
			} else if (map.containsKey(Cnst.HU_YI_TIAO_LONG)) {
				qiDuiMap.remove(Cnst.HU_YI_TIAO_LONG);
			}
			mapList.add(qiDuiMap);
		}
		if (piaoSum != 0) {
			piaoMap.put(fenMap.get("piao1"), Cnst.getFan(fenMap.get("piao1")));
			if (fenMap.get("piao2") != 0) {// 这个牌型没有本混
				piaoMap.put(Cnst.HU_BEN_HUN, Cnst.HU_FAN_BEN_HUN);
			}
			mapList.add(piaoMap);
		}
		if (diaoSum != 0) {
			diaoMap.put(fenMap.get("diao1"), Cnst.getFan(fenMap.get("diao1")));
			if (fenMap.get("diaopiao1") != 0) {
				diaoMap.put(Cnst.HU_DA_PIAO, Cnst.HU_FAN_DA_PIAO);
			}
			if (fenMap.get("diao2") != 0) {// 这个牌型没有本混
				diaoMap.put(Cnst.HU_BEN_HUN, Cnst.HU_FAN_BEN_HUN);
			}
			mapList.add(diaoMap);
		}
		if (kaSum != 0) {
			kaMap.put(fenMap.get("ka1"), Cnst.getFan(fenMap.get("ka1")));
			if (fenMap.get("ka2") != 0) {// 这个牌型没有本混
				kaMap.put(Cnst.HU_BEN_HUN, Cnst.HU_FAN_BEN_HUN);
			}
			mapList.add(kaMap);
		}
		if (bianSum != 0) {
			bianMap.put(fenMap.get("bian1"), Cnst.getFan(fenMap.get("bian1")));
			if (fenMap.get("bian2") != 0) {// 这个牌型没有本混
				bianMap.put(Cnst.HU_BEN_HUN, Cnst.HU_FAN_BEN_HUN);
			}
			mapList.add(bianMap);
		}
		return mapList;
	}

	private static int getMax(int[] arr) {
		int temp;
		for (int i = 0; i < arr.length - 1; i++)
			for (int j = 0; j < arr.length - i - 1; j++) {
				if (arr[j] < arr[j + 1]) // 这里是从大到小排序，如果是从小到大排序，只需将“<”换成“>”
				{
					temp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp;
				}
			}
		return arr[0];
	}

	/**
	 * 检测卡的捉五魁 只能是4万和6万捉5万
	 * 
	 * @param winUser
	 * @param toUser
	 * @param roomHunPai
	 * @param roomResp
	 */
	private static Integer checkKaZhuoWu(Player winUser, Player toUser,
			Integer[][] pai, RoomResp roomResp) {
		List<Integer[][]> currentMjList = winUser.getCurrentMjList();
		ArrayList<Integer[][]> distinct = getDistinct(currentMjList);
		Integer num;
		boolean isHu;
		Integer[][] dongZuoPai;
		if (winUser.getUserId().equals(toUser.getUserId())) {// 自摸
			dongZuoPai = new Integer[][] { { winUser.getLastFaPai()[0][0],
					winUser.getLastFaPai()[0][1] } };
		} else {
			dongZuoPai = new Integer[][] { { pai[0][0], pai[0][1] } };
		}
		List<Integer[][]> newList = getNewList(currentMjList);
		if (dongZuoPai[0][0].equals(1) && dongZuoPai[0][1].equals(5)) {// 动作牌是5万
			// 检测是不是有4,6万
			List<Integer[][]> needCheckList = new ArrayList<>();
			needCheckList.add(new Integer[][] { { 1, 4 } });// 4万
			needCheckList.add(new Integer[][] { { 1, 6 } });// 6万
			num = containHowMuch(distinct, needCheckList);
			if (num == 2) {// 有4万和6万
				removeNumPai(newList, needCheckList.get(0), 1);// 只是移除和中间传递牌值相同的牌
				removeNumPai(newList, needCheckList.get(1), 1);
				isHu = checkHuNew1(newList, null, null, null, roomResp);
				if (isHu) {
					return Cnst.HU_KA_ZHUO_WU;
				}
			}
		}
		return 0;
	}

	/**
	 * 检测边
	 * 
	 * @param winUser
	 * @param toUser
	 * @param pai
	 * @param roomResp
	 * @return 满足Cnst.HU_BIAN，不满足： 0
	 * 
	 */
	private static Integer checkNewBian(Player winUser, Player toUser,
			Integer[][] pai, RoomResp roomResp) {
		List<Integer[][]> currentMjList = winUser.getCurrentMjList();
		ArrayList<Integer[][]> distinct = getDistinct(currentMjList);
		boolean isHu;
		boolean isHun = false;
		int num = 0;
		int playerHunNum = 0;
		List<Integer[][]> hunList;
		Integer[][] yiChuPai1 = new Integer[][] { { -1, -1 } };// 比这张牌点数小2
		Integer[][] yiChuPai2 = new Integer[][] { { -1, -1 } };// 比这张牌点数小1
		Integer[][] yiChuPai3;// 比这张牌点数大1
		Integer[][] yiChuPai4;// 比这张牌点数大2
		Integer[][] yiChuPai;
		Integer[][] hunPai = new Integer[][] { { -1, -1 } };// 设置一个不存在的混牌
		if (roomResp == null) {// 用于检测是不是单吊本混时使用
			playerHunNum = 0;
		} else {// 掉的那张牌作为边的时候不能当混排使用
			List<Integer[][]> newList1 = getNewList(currentMjList);
			hunList = getHunList(newList1, roomResp);
			playerHunNum = hunList.size();
			List<Integer[][]> hunPais = roomResp.getHunPai();
			hunPai = hunPais.get(1);
		}
		Integer[][] dongZuoPai;
		if (winUser.getUserId().equals(toUser.getUserId())) {// 自摸
			Integer[][] lastFaPai = winUser.getLastFaPai();
			dongZuoPai = new Integer[][] { { lastFaPai[0][0], lastFaPai[0][1] } };
			if (dongZuoPai[0][0].equals(hunPai[0][0])
					&& dongZuoPai[0][1].equals(hunPai[0][1])) {
				isHun = true;
			}
		} else {
			dongZuoPai = new Integer[][] { { pai[0][0], pai[0][1] } };
		}
		List<Integer[][]> newList = getNewList(currentMjList);
		if (isHun) {// 找1 2 和8，9
			for (int i = 1; i <= 3; i++) {
				yiChuPai1 = new Integer[][] { { -1, -1 } };
				yiChuPai2 = new Integer[][] { { -1, -1 } };
				num = 0;
				// 找1和2
				if (ifContainPai(new Integer[][] { { i, 1 } }, distinct)) {
					yiChuPai1 = new Integer[][] { { i, 1 } };
				}
				if (ifContainPai(new Integer[][] { { i, 2 } }, distinct)) {
					yiChuPai2 = new Integer[][] { { i, 2 } };
				}
				if (playerHunNum + num >= 2) {
					if (num == 2) {// 两张都有
						removeNumPai(newList, yiChuPai1, 1);
						removeNumPai(newList, yiChuPai2, 1);
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						} else {
							newList.add(new Integer[][] { { yiChuPai1[0][0],
									yiChuPai1[0][1] } });
							newList.add(new Integer[][] { { yiChuPai2[0][0],
									yiChuPai2[0][1] } });
						}
					} else if (num == 1) {// 有一张
						if (ifContainPai(yiChuPai1, newList)) {
							yiChuPai = yiChuPai1;
						} else {
							yiChuPai = yiChuPai2;
						}
						removeNumPai(newList, yiChuPai, 1);
						removeNumPai(newList, hunPai, 1);
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						} else {
							newList.add(new Integer[][] { { yiChuPai[0][0],
									yiChuPai[0][1] } });
							newList.add(new Integer[][] { { hunPai[0][0],
									hunPai[0][1] } });
						}
					} else if (num == 0) {// 有0张
						removeNumPai(newList, hunPai, 2);// 移除两张混
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						} else {
							newList.add(new Integer[][] { { hunPai[0][0],
									hunPai[0][1] } });
							newList.add(new Integer[][] { { hunPai[0][0],
									hunPai[0][1] } });
						}
					}
				}
				paiXu(newList);
				yiChuPai1 = new Integer[][] { { -1, -1 } };
				yiChuPai2 = new Integer[][] { { -1, -1 } };
				num = 0;
				if (ifContainPai(new Integer[][] { { i, 8 } }, distinct)) {
					yiChuPai1 = new Integer[][] { { i, 8 } };
					num++;
				}
				if (ifContainPai(new Integer[][] { { i, 9 } }, distinct)) {
					yiChuPai2 = new Integer[][] { { i, 9 } };
					num++;
				}
				if (playerHunNum + num >= 2) {
					if (num == 2) {// 两张都有
						removeNumPai(newList, yiChuPai1, 1);
						removeNumPai(newList, yiChuPai2, 1);
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						} else {
							newList.add(new Integer[][] { { yiChuPai1[0][0],
									yiChuPai1[0][1] } });
							newList.add(new Integer[][] { { yiChuPai2[0][0],
									yiChuPai2[0][1] } });
						}
					} else if (num == 1) {// 有一张
						if (ifContainPai(yiChuPai1, newList)) {
							yiChuPai = yiChuPai1;
						} else {
							yiChuPai = yiChuPai2;
						}
						removeNumPai(newList, yiChuPai, 1);
						removeNumPai(newList, hunPai, 1);
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						} else {
							newList.add(new Integer[][] { { yiChuPai[0][0],
									yiChuPai[0][1] } });
							newList.add(new Integer[][] { { hunPai[0][0],
									hunPai[0][1] } });
						}
					} else if (num == 0) {// 有0张
						removeNumPai(newList, hunPai, 2);// 移除两张混
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						} else {
							newList.add(new Integer[][] { { hunPai[0][0],
									hunPai[0][1] } });
							newList.add(new Integer[][] { { hunPai[0][0],
									hunPai[0][1] } });
						}
					}
				}
				paiXu(newList);
			}
		} else {// 没有混
			yiChuPai1 = new Integer[][] { { dongZuoPai[0][0],
					dongZuoPai[0][1] - 2 } };// 比这张牌点数小2
			yiChuPai2 = new Integer[][] { { dongZuoPai[0][0],
					dongZuoPai[0][1] - 1 } };// 比这张牌点数小1
			yiChuPai3 = new Integer[][] { { dongZuoPai[0][0],
					dongZuoPai[0][1] + 1 } };// 比这张牌点数大1
			yiChuPai4 = new Integer[][] { { dongZuoPai[0][0],
					dongZuoPai[0][1] + 2 } };// 比这张牌点数大2
			if (dongZuoPai[0][0] <= 3 && dongZuoPai[0][1].equals(3)) {// 3
				// 检测是否有1,2
				if (ifContainPai(yiChuPai1, newList)) {
					num++;
				}
				if (ifContainPai(yiChuPai2, newList)) {
					num++;
				}
				if (playerHunNum + num >= 2) {
					if (num == 2) {// 两张都有
						removeNumPai(newList, yiChuPai1, 1);
						removeNumPai(newList, yiChuPai2, 1);
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						}
					} else if (num == 1) {// 有一张
						if (ifContainPai(yiChuPai1, newList)) {
							yiChuPai = yiChuPai1;
						} else {
							yiChuPai = yiChuPai2;
						}
						removeNumPai(newList, yiChuPai, 1);
						removeNumPai(newList, hunPai, 1);
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						}
					} else if (num == 0) {// 有0张
						removeNumPai(newList, hunPai, 2);// 移除两张混
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						}
					}
				}
			} else if (dongZuoPai[0][0] <= 3 && dongZuoPai[0][1].equals(7)) {// 7
				// 检测是否有8,9
				if (ifContainPai(yiChuPai3, newList)) {
					num++;
				}
				if (ifContainPai(yiChuPai4, newList)) {
					num++;
				}
				if (playerHunNum + num >= 2) {
					if (num == 2) {// 两张都有
						removeNumPai(newList, yiChuPai3, 1);
						removeNumPai(newList, yiChuPai4, 1);
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						}
					} else if (num == 1) {// 有一张
						if (ifContainPai(yiChuPai3, newList)) {
							yiChuPai = yiChuPai3;
						} else {
							yiChuPai = yiChuPai4;
						}
						removeNumPai(newList, yiChuPai, 1);
						removeNumPai(newList, hunPai, 1);
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						}
					} else {// 有0张
						removeNumPai(newList, hunPai, 2);// 移除两张混
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_BIAN;
						}
					}
				}
			}
		}
		return 0;
	}

	/**
	 * 检测是否吊 1 ：卡掉捉五魁 2：吊 3:不满足 0
	 * 
	 * @param winUser
	 * @param toUser
	 * @param pai
	 * @param roomResp
	 * @return
	 */
	private static Integer checkNewDiao(Player winUser, Player toUser,
			Integer[][] pai, RoomResp roomResp) {
		List<Integer[][]> currentMjList = winUser.getCurrentMjList();
		int playerHunNum = 0;
		Integer[][] hunPai = new Integer[][] { { -1, -1 } };// 设置一个不存在的混牌
		if (roomResp == null) {// 用于检测是不是单吊本混时使用
			playerHunNum = 0;
		} else {
			List<Integer[][]> newList = getNewList(currentMjList);
			List<Integer[][]> hunList = getHunList(newList, roomResp);
			List<Integer[][]> hunPais = roomResp.getHunPai();
			hunPai = hunPais.get(1);
			playerHunNum = hunList.size();
		}
		List<Integer[][]> newList2 = getNewList(currentMjList);
		Integer[][] dongZuoPai;
		boolean isHu = false;
		if (winUser.getUserId().equals(toUser.getUserId())) {// 自摸
			dongZuoPai = new Integer[][] { { winUser.getLastFaPai()[0][0],
					winUser.getLastFaPai()[0][1] } };
		} else {
			dongZuoPai = new Integer[][] { { pai[0][0], pai[0][1] } };
		}
		// 检测是不是单吊的捉5魁,只能是5万掉5万
		if (dongZuoPai[0][0].equals(1) && dongZuoPai[0][1].equals(5)) {// 动作是5万
			List<Integer[][]> small = new ArrayList<>();
			small.add(new Integer[][] { { 1, 5 } });
			Integer fiveNum = containHowMuch(newList2, small);
			if (fiveNum > 0) {// 有5万
				removeNumPai(newList2, dongZuoPai, 1);
				isHu = checkHuNew1(newList2, null, null, null, roomResp);
				if (isHu) {
					return Cnst.HU_DIAO_ZHUO_WU;
				}
			}
			if (playerHunNum > 0) {// 单调混牌
				// 加上上面的5万
				newList2.add(new Integer[][] { { 1, 5 } });
				paiXu(newList2);
				// 检测是不是混吊五万的普通吊
				removeNumPai(newList2, hunPai, 1);// 移除一张混牌
				isHu = checkHuNew1(newList2, null, null, null, roomResp);
				if (isHu) {
					return Cnst.HU_DIAO;
				}
			}
		}
		// 检测普通吊
		List<Integer[][]> newList3 = getNewList(currentMjList);
		ArrayList<Integer[][]> distinct = getDistinct(newList3);
		if (dongZuoPai[0][0].equals(hunPai[0][0])
				&& dongZuoPai[0][1].equals(hunPai[0][1])) {// 这张牌是混牌
			if (winUser.getUserId().equals(toUser.getUserId())) {// 自摸的混牌才能当混使用
				for (int i = distinct.size() - 1; i >= 0; i--) {
					removeNumPai(newList3, distinct.get(i), 1);// 移除一张牌
					isHu = checkHuNew1(newList3, null, null, null, roomResp);
					if (isHu) {
						return Cnst.HU_DIAO;
					}
					// 没胡，加上从新检测
					newList3.add(new Integer[][] { { distinct.get(i)[0][0],
							distinct.get(i)[0][1] } });
					paiXu(newList3);
				}
			}
		} else {// 这张牌不是混牌
			int num = 0;
			if (ifContainPai(dongZuoPai, newList3)) {
				num = 1;
			}
			if (num + playerHunNum >= 1) {// 移除这张牌检测是否胡
				if (num == 1) {
					removeNumPai(newList3, dongZuoPai, 1);
					isHu = checkHuNew1(newList3, null, null, null, roomResp);
					if (isHu) {
						return Cnst.HU_DIAO;
					}
				} else {// num=0,用混顶
					removeNumPai(newList3, hunPai, 1);
					isHu = checkHuNew1(newList3, null, null, null, roomResp);
					if (isHu) {
						return Cnst.HU_DIAO;
					}
				}
			}
		}
		return 0;
	}

	/**
	 * 检测是否吊 1 ：卡掉捉五魁 2：普通卡 3(包括 中发白):不满足 0 需要检查的牌型 ：万饼条还有中发白（中发白任意两张夹1张都叫卡）
	 * 
	 * @param winUser
	 * @param toUser
	 * @param pai
	 * @param roomResp
	 * @return
	 */
	private static Integer checkNewKa(Player winUser, Player toUser,
			Integer[][] pai, RoomResp roomResp) {
		List<Integer[][]> currentMjList = winUser.getCurrentMjList();
		ArrayList<Integer[][]> distinct = getDistinct(currentMjList);
		int playerHunNum = 0;
		Integer[][] hunPai = new Integer[][] { { -1, -1 } };// 设置一个不存在的混牌
		if (roomResp == null) {// 用于检测是不是单吊本混时使用
			playerHunNum = 0;
		} else {
			List<Integer[][]> newList1 = getNewList(currentMjList);
			List<Integer[][]> hunList = getHunList(newList1, roomResp);
			List<Integer[][]> hunPais = roomResp.getHunPai();
			hunPai = hunPais.get(1);
			playerHunNum = hunList.size();
		}
		Integer[][] dongZuoPai;
		boolean isHu = false;
		Boolean isHun = false;
		Integer[][] yiChuPai1 = new Integer[][] { {} };
		Integer[][] yiChuPai2 = new Integer[][] { {} };
		Integer[][] yiChuPai3 = new Integer[][] { {} };
		if (winUser.getUserId().equals(toUser.getUserId())) {// 自摸
			dongZuoPai = new Integer[][] { { winUser.getLastFaPai()[0][0],
					winUser.getLastFaPai()[0][1] } };
			if (dongZuoPai[0][0].equals(hunPai[0][0])
					&& dongZuoPai[0][1].equals(hunPai[0][1])) {
				isHun = true;
			}
		} else {
			dongZuoPai = new Integer[][] { { pai[0][0], pai[0][1] } };
		}
		// 检测卡
		Integer[][] yiChuPai;
		List<Integer[][]> newList = getNewList(currentMjList);
		if (isHun) {// 如果这张牌数混牌
			// 属于万饼条的时候
			for (int i = 0; i < distinct.size(); i++) {
				for (int j = i + 1; j < distinct.size(); j++) {
					if (distinct.get(i)[0][0] <= 3
							&& distinct.get(i)[0][0]
									.equals(distinct.get(j)[0][0])
							&& (distinct.get(j)[0][1] - distinct.get(i)[0][1]) == 2) {
						yiChuPai1 = distinct.get(i);
						yiChuPai2 = distinct.get(j);
						removeNumPai(newList, yiChuPai2, 1);
						removeNumPai(newList, yiChuPai1, 1);
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_KA;
						}
						newList.add(new Integer[][] { { yiChuPai1[0][0],
								yiChuPai1[0][1] } });
						newList.add(new Integer[][] { { yiChuPai2[0][0],
								yiChuPai2[0][1] } });
						paiXu(newList);
					}
				}
			}
			// 中发白检测
			int num = 0;
			for (Integer[][] integers : distinct) {
				if (integers[0][0].equals(5)) {
					num++;
					if (num == 1) {
						yiChuPai1 = new Integer[][] { { integers[0][0],
								integers[0][1] } };
					} else if (num == 2) {
						yiChuPai2 = new Integer[][] { { integers[0][0],
								integers[0][1] } };
					} else if (num == 3) {
						yiChuPai3 = new Integer[][] { { integers[0][0],
								integers[0][1] } };
					}
				}
			}
			if (num + playerHunNum < 2) {// 不满足直接返回0
				return 0;
			} else {// 满足
				if (num == 3) {
					// 移除任意两个检测胡
					removeNumPai(newList, yiChuPai1, 1);
					removeNumPai(newList, yiChuPai2, 1);
					isHu = checkHuNew1(newList, null, null, null, roomResp);
					if (isHu) {
						return Cnst.HU_KA;
					} else {
						newList.add(new Integer[][] { { yiChuPai1[0][0],
								yiChuPai1[0][1] } });
						newList.add(new Integer[][] { { yiChuPai2[0][0],
								yiChuPai2[0][1] } });
						paiXu(newList);
						// 移除任意两个检测胡
						removeNumPai(newList, yiChuPai2, 1);
						removeNumPai(newList, yiChuPai3, 1);
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {
							return Cnst.HU_KA;
						} else {
							newList.add(new Integer[][] { { yiChuPai1[0][0],
									yiChuPai1[0][1] } });
							newList.add(new Integer[][] { { yiChuPai2[0][0],
									yiChuPai2[0][1] } });
							paiXu(newList);
							// 移除任意两个检测胡
							removeNumPai(newList, yiChuPai1, 1);
							removeNumPai(newList, yiChuPai3, 1);
							isHu = checkHuNew1(newList, null, null, null,
									roomResp);
							if (isHu) {
								return Cnst.HU_KA;
							}
						}
					}
				} else if (num == 2) {
					removeNumPai(newList, yiChuPai1, 1);
					removeNumPai(newList, yiChuPai2, 1);
					isHu = checkHuNew1(newList, null, null, null, roomResp);
					if (isHu) {
						return Cnst.HU_KA;
					}
				} else if (num == 1) {
					removeNumPai(newList, yiChuPai1, 1);
					removeNumPai(newList, hunPai, 1);
					isHu = checkHuNew1(newList, null, null, null, roomResp);
					if (isHu) {
						return Cnst.HU_KA;
					}
				} else {// 剩下两张混
					removeNumPai(newList, hunPai, 2);
					isHu = checkHuNew1(newList, null, null, null, roomResp);
					if (isHu) {
						return Cnst.HU_KA;
					}
				}
				return 0;
			}
		} else if (((dongZuoPai[0][0] <= 3 && 1 < dongZuoPai[0][1] && dongZuoPai[0][1] < 9) || dongZuoPai[0][0] == 5)
				&& isHun == false) {
			int num = 0;
			// 万饼条
			if (dongZuoPai[0][0] <= 3 && 1 < dongZuoPai[0][1]
					&& dongZuoPai[0][1] < 9) {
				yiChuPai1 = new Integer[][] { { dongZuoPai[0][0],
						dongZuoPai[0][1] - 1 } };
				yiChuPai2 = new Integer[][] { { dongZuoPai[0][0],
						dongZuoPai[0][1] + 1 } };
				// 是否有比它小1的数
				if (ifContainPai(yiChuPai1, distinct)) {
					num++;
				}
				// 是否有比它大1的数
				if (ifContainPai(yiChuPai2, distinct)) {
					num++;
				}
			} else {
				// 中发白
				if (dongZuoPai[0][1] == 1) {
					yiChuPai1 = new Integer[][] { { dongZuoPai[0][0],
							dongZuoPai[0][1] + 1 } };
					yiChuPai2 = new Integer[][] { { dongZuoPai[0][0],
							dongZuoPai[0][1] + 2 } };
				} else if (dongZuoPai[0][1] == 2) {
					yiChuPai1 = new Integer[][] { { dongZuoPai[0][0],
							dongZuoPai[0][1] - 1 } };
					yiChuPai2 = new Integer[][] { { dongZuoPai[0][0],
							dongZuoPai[0][1] + 1 } };
				} else {
					yiChuPai1 = new Integer[][] { { dongZuoPai[0][0],
							dongZuoPai[0][1] - 1 } };
					yiChuPai2 = new Integer[][] { { dongZuoPai[0][0],
							dongZuoPai[0][1] - 2 } };
				}
				if (ifContainPai(yiChuPai1, distinct)) {
					num++;
				}
				if (ifContainPai(yiChuPai2, distinct)) {
					num++;
				}
			}
			if (playerHunNum + num >= 2) {// 混牌能补齐需要的手牌
				if (num == 2) {// 说明两个都有
					// 移除这两张牌，检测胡不胡
					removeNumPai(newList, yiChuPai1, 1);
					removeNumPai(newList, yiChuPai2, 1);
					isHu = checkHuNew1(newList, null, null, null, roomResp);
					if (isHu) {
						return Cnst.HU_KA;
					}
				} else if (num == 1) {// 只有一张
					// 移除一个混
					if (ifContainPai(yiChuPai1, distinct)) {// 有移除牌2
						yiChuPai = yiChuPai1;
					} else {
						yiChuPai = yiChuPai2;
					}
					// 移除一张混牌
					removeNumPai(newList, hunPai, 1);
					// 移除有的那张牌
					removeNumPai(newList, yiChuPai, 1);
					// 胡？
					isHu = checkHuNew1(newList, null, null, null, roomResp);
					if (isHu) {
						return Cnst.HU_KA;
					}
				} else {// num==0
						// 移除两个混
					removeNumPai(newList, hunPai, 2);
					// 胡？
					isHu = checkHuNew1(newList, null, null, null, roomResp);
					if (isHu) {
						return Cnst.HU_KA;
					}
				}
			}
		}
		return 0;
	}

	/**
	 * 检测big中包含几个small中的牌
	 * 
	 * @param noZhongFaBaiList
	 *            不能重复的集合
	 * @param needCheckList
	 *            需要检测的牌集合 不能重复
	 * @return Integer 包含的牌数量
	 */
	private static Integer containHowMuch(List<Integer[][]> big,
			List<Integer[][]> small) {
		Integer num = 0;
		for (int i = 0; i < small.size(); i++) {
			for (int j = 0; j < big.size(); j++) {
				if (small.get(i)[0][0].equals(big.get(j)[0][0])
						&& small.get(i)[0][1].equals(big.get(j)[0][1])) {
					num++;
					break;
				}
			}
		}
		return num;
	}

	/**
	 * 赢时手中没有混牌，混吊混也是没混
	 * 
	 * @param winUser
	 * @param toUser
	 * @param pai
	 * @param roomResp
	 * @return
	 */
	public static Integer checkMeiHun(Player winUser, Player toUser,
			Integer[][] pai, RoomResp roomResp) {

		// 获取混牌数量
		List<Integer[][]> newList = getNewList(winUser.getCurrentMjList());
		List<Integer[][]> hunList;// 移除混牌后的手牌newList 集合和混牌集合hunList
		if (!winUser.getUserId().equals(toUser.getUserId())) {// 不是自摸
			hunList = getHunList(newList, roomResp);
			newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
			paiXu(newList);
		} else {// 是自摸
			Integer[][] lastFaPai = winUser.getLastFaPai();
			newList.add(new Integer[][] { { lastFaPai[0][0], lastFaPai[0][1] } });
			hunList = getHunList(newList, roomResp);// 此时newList已经移除了hun牌
		}
		if (hunList == null || hunList.size() == 0) {// 沒有混牌
			return Cnst.HU_MEI_HUN;
		} else if (hunList.size() == 2) {// 混牌数量为2
			// 必須是自摸，並且最後一張是混，並且移兩張混能胡
			if (winUser.getUserId().equals(toUser.getUserId())) {// 属于自摸
				if (winUser.getLastFaPai()[0][0].equals(hunList.get(0)[0][0])
						&& winUser.getLastFaPai()[0][1]
								.equals(hunList.get(0)[0][1])) {// 最后一张摸得是混牌
					boolean isHu = checkHuNew1(newList, null, null, null,
							roomResp);
					if (isHu) {
						return Cnst.HU_MEI_HUN;
					}
				}
			}
		}
		return 0;
	}

	/**
	 * 移除n张牌
	 * 
	 * @param newList
	 *            牌集合
	 * @param integers
	 *            想要移除的牌
	 * @param i
	 *            想要移除的牌的个数
	 */
	private static void removeNumPai(List<Integer[][]> newList,
			Integer[][] integers, int i) {
		int num = 0;
		for (int j = newList.size() - 1; j >= 0; j--) {
			if (newList.get(j)[0][0].equals(integers[0][0])
					&& newList.get(j)[0][1].equals(integers[0][1])) {
				newList.remove(j);
				num++;
				if (num == i) {
					break;
				}
			}
		}
	}

	/**
	 * 检测集合中是否有这张牌
	 * 
	 * @param lastFaPai
	 * @param newList
	 * @return
	 */
	private static Boolean ifContainPai(Integer[][] pai,
			List<Integer[][]> newList) {
		Boolean hasSame = false;
		for (int i = newList.size() - 1; i >= 0; i--) {
			if (pai[0][0].equals(newList.get(i)[0][0])
					&& pai[0][1].equals(newList.get(i)[0][1])) {
				hasSame = true;
				break;
			}
		}
		return hasSame;
	}

	/**
	 * 获取不重复的List集合
	 * 
	 * @param shouPaiList
	 * @return
	 */
	private static ArrayList<Integer[][]> getDistinct(
			List<Integer[][]> shouPaiList) {
		if (shouPaiList != null && shouPaiList.size() > 0) {
			Set<String> disSet = new HashSet<>();
			ArrayList<Integer[][]> list = new ArrayList<>();
			for (Integer[][] pai : shouPaiList) {
				if (!disSet.contains(pai[0][0] + "_" + pai[0][1])) {
					disSet.add(pai[0][0] + "_" + pai[0][1]);
					list.add(new Integer[][] { { pai[0][0], pai[0][1] } });
				}
			}
			return list;
		}
		return null;
	}

	/**
	 * 本方法检测手中是否有龙： 本混一条龙： 龙身牌必须都是本身，并且其中一张是即是龙身牌也是混牌 ,若还有多余混排： 1： 混当本身使用能胡：
	 * +干胡番数 返回3 2：当混使用才能胡 ：只是本混一条龙 返回2 仅仅是能凑齐龙牌 ，返回1 没有龙 返回0
	 * 
	 * 
	 * @param roomResp
	 * @param pai
	 * @param toUser
	 * @param winUser
	 * 
	 * @param playerPais
	 * @param roomResp
	 * @return
	 */
	private static int checkLong(Player winUser, Player toUser,
			Integer[][] pai, RoomResp roomResp) {
		// 必须满足一条龙的长度，否则不检测
		if (winUser.getCurrentMjList().size() >= 10) {
			// 房间混牌
			List<Integer[][]> hunList;
			List<Integer[][]> newList = getNewList(winUser.getCurrentMjList());
			// 获取房间混牌类型
			Integer[][] roomHunPai = roomResp.getHunPai().get(1);
			// 获取混牌数量
			if (winUser.getUserId().equals(toUser.getUserId())) {
				newList.add(new Integer[][] { { winUser.getLastFaPai()[0][0],
						winUser.getLastFaPai()[0][1] } });
				hunList = getHunList(newList, roomResp);
			} else {
				hunList = getHunList(newList, roomResp);
				newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
			}
			// 获取混牌数量
			int hunNum = hunList.size();
			// 获取完整的牌
			if (hunList.size() > 0) {
				for (int i = 0; i < hunNum; i++) {
					newList.add(new Integer[][] { { roomHunPai[0][0],
							roomHunPai[0][1] } });
				}
			}
			paiXu(newList);
			// 含混牌的不重复的牌
			ArrayList<Integer[][]> distinct = getDistinct(newList);
			// 获取不含混排的不重复的牌
			List<Integer[][]> distinct1 = getNewList(distinct);
			removeNumPai(distinct1, roomHunPai, 1);
			boolean isHu = false;
			for (int i = 1; i <= 3; i++) {// 牌型
				int num = 0;
				// 属于同一个类型的牌
				for (Integer[][] integers : distinct) {
					if (integers[0][0].equals(i)) {
						num++;
					}
				}
				// 能够集齐一条龙
				if (num + hunNum >= 9) {
					// 1:检测本混一条龙 （规则，龙身拍必须8张不重复，混排必须是少的那张龙身拍，剩下的混牌还可以当混使用）
					// 此时混牌数至少为1
					if (num == 9 && roomHunPai[0][0].equals(i)
							&& hunList.size() > 0) {// 必须有9个,并且混排是龙身，并且有混牌
						List<Integer[][]> newList2 = getNewList(newList);
						int j = 9;
						for (int k = newList2.size() - 1; k >= 0; k--) {
							if (newList2.get(k)[0][0].equals(i)
									&& newList2.get(k)[0][1].equals(j)) {
								removeNumPai(newList2, newList2.get(k), 1);
								j--;
								if (j == 0) {
									break;
								}
							}
						}
						j = 9;
						if (hunNum > 1) {// 混排数大于1的时候
							// 剩下牌里面的混排也能当混使用
							isHu = checkHuNew1(newList2, null, null, null,
									roomResp);
							if (isHu) {
								// 剩下混当本身使用能胡： +干胡番数
								isHu = checkHuNew1(newList2, null, null, null,
										null);
								if (isHu) {
									// 加上干胡番数
									return 3;
								}
								return 2;
							}
						} else {// 混派数为1的判断
							isHu = checkHuNew1(newList2, null, null, null, null);
							if (isHu) {
								return 2;
							}
						}
					}
					// 检测普通一条龙
					List<Integer[][]> newList3 = getNewList(newList);
					// 移除num张牌，和9-num张混牌
					// 移除龙身牌，不能是混排
					for (Integer[][] integers : distinct1) {
						if (integers[0][0].equals(i)) {
							removeNumPai(newList3, integers, 1);
						}
					}
					// 移除需要的混牌--- 移除补龙身牌的混牌
					removeNumPai(newList3, roomHunPai, 9 - num);
					isHu = checkHuNew1(newList3, null, null, null, roomResp);
					if (isHu) {
						return 1;
					}
				}
			}
		}
		return 0;
	}

	/**
	 * 检测是不是飘
	 * 
	 * @param winUser
	 * @param toUser
	 * @param pai
	 * @return 1 是飘 0 不是飘
	 */
	private static Boolean getPiaoInfo(Player winUser, Player toUser,
			Integer[][] pai, RoomResp roomResp) {
		List<Integer[][]> currentMjList = winUser.getCurrentMjList();
		List<Integer[][]> newList = getNewList(currentMjList);
		List<Integer[][]> hunList = new ArrayList<>();
		Integer hunNum;
		if (roomResp == null) {
			hunNum = 0;
			if (!winUser.getUserId().equals(toUser.getUserId())) {// 不是自摸
				newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });// 如果牌不为null,需要添加
			} else {
				newList.add(new Integer[][] { { winUser.getLastFaPai()[0][0],
						winUser.getLastFaPai()[0][1] } });// 如果牌不为null,需要添加
			}
		} else {
			if (!winUser.getUserId().equals(toUser.getUserId())) {// 不是自摸
				hunList = getHunList(newList, roomResp);
				newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });// 如果牌不为null,需要添加
			} else {
				newList.add(new Integer[][] { { winUser.getLastFaPai()[0][0],
						winUser.getLastFaPai()[0][1] } });// 如果牌不为null,需要添加
				hunList = getHunList(newList, roomResp);
			}
			hunNum = hunList.size();
		}
		paiXu(newList);
		// 将其变成一个数组
		int[] shouPaiArray = getShouPaiArray(newList, null);
		int oneNum = 0;// 手牌个数为1 的牌的个数
		int twoNum = 0;// 手牌个数为2 的牌的个数
		for (int i = 0; i < shouPaiArray.length; i++) {
			// 有4张一样的不成立
			if (shouPaiArray[i] > 3) {// 有4个的不成立
				return false;
			} else if (shouPaiArray[i] == 1) {// 如果个数为1，首先默认为将，看能不能胡牌
				oneNum++;
			} else if (shouPaiArray[i] == 2) {// 如果个数为2，首先默认为将，看能不能胡牌
				twoNum++;
			}
		}
		// 从两张中取一个对子做将 那么剩余 twoNum-1个两张（需要1张牌变成刻），剩下oneNum个一张（需要两张混变成刻）
		// 从两张取一个和从一个单张取与混牌逻辑相同
		if (hunNum >= twoNum - 1 + 2 * oneNum) {
			return true;
		}
		return false;
	}

	/**
	 * 检测海捞
	 * 
	 * @param winUser
	 * @return
	 */
	private static Boolean isHaiLao(RoomResp roomResp) {
		// 剩余麻将数量 - 不能摸得牌数在4以内
		List<Integer[][]> currentMjList = roomResp.getCurrentMjList();
		Integer diXianPaiNum = roomResp.getDiXianPaiNum();
		Integer haiLaoNum = currentMjList.size() - diXianPaiNum;
		if (haiLaoNum >= 0 && haiLaoNum <= 4) {
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param winUser
	 * @param roomResp
	 * @param pai
	 * @param toUser
	 * @return 2混一色，1清一色 ，0不满足
	 */
	public static Integer getQingOrHuFanNum(Player winUser, Player toUser,
			Integer[][] pai, RoomResp roomResp) {
		List<Integer[][]> newList = getNewList(winUser.getCurrentMjList());
		// 移除newList中的混牌
		if (winUser.getUserId().equals(toUser.getUserId())) {// 自摸
			Integer[][] lastFaPai = winUser.getLastFaPai();
			newList.add(new Integer[][] { { lastFaPai[0][0], lastFaPai[0][1] } });
			paiXu(newList);
			getHunList(newList, roomResp);
		} else {
			getHunList(newList, roomResp);
			newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
			paiXu(newList);
		}
		// 用于存出玩家所有动作的牌型
		List<Integer> list = new ArrayList<>();
		for (Integer[][] integers : newList) {
			list.add(integers[0][0]);
		}
		List<InfoCount> chiList = winUser.getChiList();
		List<InfoCount> pengList = winUser.getPengList();
		List<InfoCount> gangListType2 = winUser.getGangListType2();// 亮风，东南西北
		List<InfoCount> gangListType3 = winUser.getGangListType3();
		List<InfoCount> gangListType4 = winUser.getGangListType4();
		List<InfoCount> gangListType5 = winUser.getGangListType5();
		// 获取手牌的类型
		if (chiList != null && chiList.size() > 0) {
			for (InfoCount infoCount : chiList) {
				list.add(infoCount.getL().get(0)[0][0]);// 只获取吃的类型
			}
		}
		if (pengList != null && pengList.size() > 0) {
			for (InfoCount infoCount : pengList) {
				list.add(infoCount.getL().get(0)[0][0]);
			}
		}
		if (gangListType2 != null && gangListType2.size() > 0) {
			for (InfoCount infoCount : gangListType2) {
				list.add(infoCount.getL().get(0)[0][0]);
			}
		}
		if (gangListType3 != null && gangListType3.size() > 0) {
			for (InfoCount infoCount : gangListType3) {
				list.add(infoCount.getL().get(0)[0][0]);
			}
		}
		if (gangListType4 != null && gangListType4.size() > 0) {
			for (InfoCount infoCount : gangListType4) {
				list.add(infoCount.getL().get(0)[0][0]);
			}
		}
		if (gangListType5 != null && gangListType5.size() > 0) {
			for (InfoCount infoCount : gangListType5) {
				list.add(infoCount.getL().get(0)[0][0]);
			}
		}
		int zhongOrFeng = 0;
		for (int i = list.size() - 1; i >= 0; i--) {
			if (list.get(i).equals(4) || list.get(i).equals(5)) {// 如果有风和中华白 移除
				zhongOrFeng++;
				list.remove(i);
			}
		}
		// 检测list中是否只有一种
		if (list.size() > 0) {
			Integer type = list.get(0);// 获取第一个数
			// 判断是不是都是一种
			for (int i = 1; i < list.size(); i++) {
				if (!type.equals(list.get(i))) {// 有重复的不满足
					return 0;
				}
			}
			if (zhongOrFeng > 0) {// 说明有中发白或者风
				return 2;
			} else {
				return 1;
			}
		} else {// 都是东南西北中发白
			return 2;
		}
	}

	/**
	 * 返回3 超豪华，返回2 双豪华，返回1 豪华 7对 返回0 七对 （null不能组成）
	 * 
	 * @param toUser
	 * @param winUser
	 * 
	 * @param currentMjList
	 * @param pai
	 * @return
	 */
	public static Integer hasQiDui(Player winUser, Player toUser,
			Integer[][] pai, RoomResp roomResp) {
		List<Integer[][]> currentMjList = winUser.getCurrentMjList();
		List<Integer[][]> newShouPaiList = getNewList(currentMjList);
		Integer x = 0;
		int y=0;
		if (currentMjList.size() == 13) {// 13
			Integer hunNum;// 混牌数量
			List<Integer[][]> hunList = new ArrayList<>();// 混牌集合
			if (roomResp == null) {//检测是否本混胡牌
				hunNum = 0;
				newShouPaiList = getNewList(currentMjList);
				if (!winUser.getUserId().equals(toUser.getUserId())) {// 别人出的牌
					newShouPaiList
							.add(new Integer[][] { { pai[0][0], pai[0][1] } });
				} else {
					newShouPaiList.add(new Integer[][] { {
							winUser.getLastFaPai()[0][0],
							winUser.getLastFaPai()[0][1] } });
				}
				Integer danzhangshu = yichuDuizi(newShouPaiList);
				if(danzhangshu.equals(0)){
					return 1;
				}
				return null;
			} else {
				// 获取混牌数量
				if (!winUser.getUserId().equals(toUser.getUserId())) {// 别人出的牌
					hunList = getHunList(newShouPaiList, roomResp);
					newShouPaiList
							.add(new Integer[][] { { pai[0][0], pai[0][1] } });
					//别人出的牌是混排，用于排除家呢是否4混当本牌使用
					if(pai[0][0].equals(roomResp.getHunPai().get(1)[0][0]) && pai[0][1].equals(roomResp.getHunPai().get(1)[0][1])){
						y=1;
					}
				} else {
					newShouPaiList.add(new Integer[][] { {
							winUser.getLastFaPai()[0][0],
							winUser.getLastFaPai()[0][1] } });
					hunList = getHunList(newShouPaiList, roomResp);
				}
			}
			hunNum = hunList.size();
			paiXu(newShouPaiList);
			// 获取不重复的牌集合(不包含混排)
			ArrayList<Integer[][]> distinct = getDistinct(newShouPaiList);
			if(roomResp!=null){
				removeNumPai(distinct, roomResp.getHunPai().get(1), 1);
			}
			// 将没有混的手牌的对子全部移除 ,返回剩余没有对子的手牌数
			Integer danzhangshu = yichuDuizi(newShouPaiList);
			Integer siHundanzhangshu = danzhangshu-y;
			// 获取手牌中牌的种类（如：1饼，一条为2种）
			Integer paiKindsNum = getPaiKindsNum(newShouPaiList);
			// 单张数小于混排数 是七对
			if (danzhangshu <= hunNum) {
				if (paiKindsNum >= 4 && paiKindsNum <= 7) {
					x = 1;
				}
				/*
				 * if (paiKindsNum == 4) { return 3; } else if (paiKindsNum ==
				 * 5) { return 2; } else if (paiKindsNum == 6) { return 1; }
				 * else if (paiKindsNum == 7) { return 0; }
				 */
				else if (hunNum - danzhangshu == 2) {
					/*
					 * if (paiKindsNum == 3 || paiKindsNum == 4) { return 3; }
					 * else if (paiKindsNum == 5) { return 2; } else if
					 * (paiKindsNum == 6) { return 1; }
					 */
					if (paiKindsNum >= 3 && paiKindsNum <= 6) {
						x = 1;
					}
				} else if (hunNum - danzhangshu == 4) {
					/*
					 * if (paiKindsNum == 3 || paiKindsNum == 4) { return 3; }
					 * else if (paiKindsNum == 5) { return 2; }
					 */
					if (paiKindsNum >= 3 && paiKindsNum <= 5) {
						x = 1;
					}
				}
			}
			// 新规定，七对的豪华（4个的）不能用混牌去顶
			// 是否是4个混
			if (x == 1) {// 混顶可以组成七对
				x = 0;// 此时x表示能手牌中4个相同的个数
				// 查看手中有多少4个没有混顶的一样的牌
				for (int i = distinct.size() - 1; i >= 0; i--) {
					int num = 0;
					for (int j = newShouPaiList.size() - 1; j >= 0; j--) {
						if (newShouPaiList.get(j)[0][0].equals(distinct.get(i)[0][0]) &&newShouPaiList.get(j)[0][1].equals(distinct.get(i)[0][1]) ) {
							num++;
						}
					}
					// 混排能当本牌使用是否可以胡
					if (num == 4) {
						x++;
					}
				}
				// 混是当本牌使用时，若混排数位4 ，加到豪华七对数中
				int newHunNum = hunNum;
				if (siHundanzhangshu == 0) {
					//加上别人出的混
					newHunNum=newHunNum+y;
					if (newHunNum == 4) {
						x++;
					}
				}
				if (x == 1) {
					return 1;
				} else if (x == 2) {
					return 2;
				} else if (x == 3) {
					return 3;
				}
				//普通的七对
				return 0;
			}
		}
		return null;
	}

	/**
	 * 获取手牌中牌的种类（如：1饼，一条为2种）
	 * 
	 * @param newShouPaiList
	 * @return
	 */
	public static Integer getPaiKindsNum(List<Integer[][]> newShouPaiList) {
		Set<String> set = new HashSet<>();
		for (Integer[][] integers : newShouPaiList) {
			String intege = integers[0][0] + "_" + integers[0][1];
			if (!set.contains(intege)) {
				set.add(intege);
			}
		}
		return set.size();
	}

	/**
	 * 移除对子的方法
	 * 
	 * @param List
	 *            <Integer[][]> 手牌
	 * @return Integer 移除对子之后单张数
	 */
	public static Integer yichuDuizi(List<Integer[][]> newShouPaiList) {
		List<Integer[][]> allShouPaiList = new ArrayList<Integer[][]>(
				newShouPaiList.size());
		// 将数据保存到allShouPaiList 值传递，newShouPaiList改变和allShouPaiList没有影响
		allShouPaiList.addAll(newShouPaiList);
		paiXu(allShouPaiList);
		// [1,2,3,4,5,6] 取2-6角标
		for (int i = allShouPaiList.size() - 1; i >= 1; i--) {
			// [1,2,3,4,5,6] 取1-5 麻将中除了混牌，每样最多4张
			if (allShouPaiList.get(i)[0][0]
					.equals(allShouPaiList.get(i - 1)[0][0])
					&& allShouPaiList.get(i)[0][1].equals(allShouPaiList
							.get(i - 1)[0][1])) {
				allShouPaiList.remove(i);
				allShouPaiList.remove(i - 1);
				i--;
			}
		}
		// 没有对子，都是单牌
		return allShouPaiList.size();
	}

	public static Boolean isMenQing(Player p) {
		if ((p.getChiList() == null || p.getChiList().size() == 0)
				&& (p.getPengList() == null || p.getPengList().size() == 0)
				&& (p.getGangListType3() == null || p.getGangListType3().size() == 0)
				&& (p.getGangListType4() == null || p.getGangListType4().size() == 0)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取房间混牌
	 * 
	 * @param roomMingPai
	 * @return
	 */
	public static Integer[][] findHunPaiByMingPai(Integer[][] roomMingPai) {
		Integer[][] roomHunPai = new Integer[][] {};
		// 明牌类型
		Integer type = roomMingPai[0][0];
		// 验证明牌类型是否满足牌类型
		if (type < 1 || type > 5) {// 不满足牌型条件 直接返回空
			return roomHunPai;
		}
		// 该牌型的种类的总个数
		Integer onePaiTypeCou = MahjongCons.mahjongType.get(type);
		// 明牌的编号
		Integer mingPaiNum = roomMingPai[0][1];
		// 混牌的类型不变，与明牌一致。
		// 说明:a.若明牌为万筒条，则是相应的万筒条加1(特例：9万筒条时为1万筒条),满足公式 混牌num =
		// (明牌num+1)%该牌型的种类（9种）
		// b:若明牌为风牌 （东南西北）,同理：满足公式 混牌num = (明牌num+1)%该牌型的种类（4种）
		// c:若明牌为箭牌 （中发白）,同理：满足公式 混牌num = (明牌num+1)%该牌型的种类（4种）
		// 因此公式为 混牌num = (明牌num+1)%该牌型的种类
		Integer hunPaiNum = (mingPaiNum + 1) % onePaiTypeCou != 0 ? (mingPaiNum + 1)
				% onePaiTypeCou
				: onePaiTypeCou;
		roomHunPai = new Integer[][] { { type, hunPaiNum } };
		return roomHunPai;
	}

	/**
	 * --1：Player winUser, Player toUser, Integer[][] pai, RoomResp roomResp
	 * --2: List<Integer[][]> newShouPai,RoomResp roomResp
	 * 
	 * @param p
	 * @param pai
	 * @param roomResp
	 * @return
	 */
	public static boolean checkHuNew(List<Integer[][]> newShouPai,
			Player winUser, Player toUser, Integer[][] pai, RoomResp roomResp) {
		// 开门之后，混排不能当混使用
		if (winUser != null) {
			Boolean menQing = isMenQing(winUser);
			if (roomResp.getDaHun().equals(0) && !menQing) {
				Integer diXianPaiNum = roomResp.getDiXianPaiNum();
				List<Integer[][]> c1 = roomResp.getCurrentMjList();
				roomResp = new RoomResp();
				List<Integer[][]> a = new ArrayList<>();
				a.add(new Integer[][] { { -1, -1 } });
				a.add(new Integer[][] { { -1, -1 } });
				roomResp.setDiXianPaiNum(diXianPaiNum);
				roomResp.setCurrentMjList(c1);
				roomResp.setHunPai(a);// 设置四混胡
				roomResp.setSiHunHu(0);// 规则没有四混胡
			}
		}

		if (roomResp == null) {
			roomResp = new RoomResp();
			List<Integer[][]> hunPai = new ArrayList<>();
			hunPai.add(new Integer[][] { { -1, -1 } });
			hunPai.add(new Integer[][] { { -1, -1 } });
			roomResp.setHunPai(hunPai);
			roomResp.setSiHunHu(0);
		}
		List<Integer[][]> newList;// 为了获取混牌数
		List<Integer[][]> hunList;// 混牌集合
		Integer[][] lastFaPai;
		if (winUser != null) {// 传递的是玩家
			newList = getNewList(winUser.getCurrentMjList());
			lastFaPai = winUser.getLastFaPai();
			if (winUser.getUserId().equals(toUser.getUserId())) {// 是同一个玩家
				newList.add(new Integer[][] { { lastFaPai[0][0],
						lastFaPai[0][1] } });
				hunList = getHunList(newList, roomResp);
			} else {
				hunList = getHunList(newList, roomResp);
				newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
			}
			paiXu(newList);
		} else {// 传递的只是牌
			newList = getNewList(newShouPai);
			hunList = getHunList(newList, roomResp);
		}
		Integer siHunHu = roomResp.getSiHunHu();// 能否四混胡
		// 检测四混胡
		if (siHunHu.equals(1)) {// 规则有四混胡
			if (hunList.size() == 4) {
				return true;
			}
		}
		// 检测七对
		Integer haoHuaFanShu = null;
		if (winUser == null) {// 没有传入玩家，只传牌
			haoHuaFanShu = hasQiDui(getNewList(newShouPai), null, null, null,
					roomResp);
		} else {
			haoHuaFanShu = hasQiDui(winUser, toUser, pai, roomResp);
		}
		if (haoHuaFanShu != null) {
			return true;
		}
		int[] paiArray = getShouPaiArray(newList, null);
		/** 如果哪个牌的个数是0，就让guiIndex是这个 */
		int gui_index = 34;
		if (hunList != null && hunList.size() > 0) {
			gui_index = addHun(paiArray, hunList.size());
		}
		boolean hu = Hulib.getInstance().get_hu_info(paiArray, 34, gui_index);
		if (hu) {
			return hu;
		} else {
			// 移除tempShouPai 中的中发白
			// 包含中发白 一套，移除
			// 包含其中两个，其中一个个数为1，
			// 混补(混的个数是否大于1 ，--移除一个混替代中发白，移除) ,检测剩下的手牌能否胡
			int hunNum = hunList.size();
			List<String> list = new ArrayList<>();
			for (Integer[][] integers : newList) {
				list.add(integers[0][0] + "_" + integers[0][1]);
			}
			Integer zhong = 0;
			Integer fa = 0;
			Integer bai = 0;
			Integer min = 0;
			for (String string : list) {
				if (string.equals("5_1")) {
					zhong++;
				} else if (string.equals("5_2")) {
					fa++;
				} else if (string.equals("5_3")) {
					bai++;
				}
			}
			// 第一步，移除所有的中发白
			if (zhong != 0 && fa != 0 && bai != 0) {
				min = zhong >= fa ? fa : zhong;
				min = min >= bai ? bai : min;
				// 找出组成套中发白，移除
				removeZhongFaBai(list, min);// 移除list中min套中发白
				zhong = zhong - min;
				fa = fa - min;
				bai = bai - min;
			}
			// 第二步，一个为0，一个为1，一个大于0
			int[] arr = new int[3];
			arr[0] = zhong;
			arr[1] = fa;
			arr[2] = bai;
			Arrays.sort(arr);
			if (arr[0] == 0 && arr[1] == 1) {// 最小为一对的不用补
				if (hunNum > 0) {// 混牌至少一张
					hunList.remove(hunList.size() - 1);// 移除一张混牌;
					if (zhong == 0) {
						removeHunBuZhongFaBai(list, 1);// 手牌中移除不是中的组合
					} else if (fa == 0) {
						removeHunBuZhongFaBai(list, 2);
					} else if (bai == 0) {
						removeHunBuZhongFaBai(list, 3);
					}
				}
			}
			// 获取剩下的牌
			List<Integer[][]> list3 = new ArrayList<>();
			for (int i = 0; i < list.size(); i++) {
				list3.add(new Integer[][] { {
						Integer.valueOf(list.get(i).substring(0, 1)),
						Integer.valueOf(list.get(i).substring(2)) } });
			}
			int[] paiArray1 = getShouPaiArray(list3, null);
			/** 如果哪个牌的个数是0，就让guiIndex是这个 */
			int gui_index1 = 34;
			if (hunList != null && hunList.size() > 0) {
				gui_index1 = addHun(paiArray1, hunList.size());
			}
			hu = Hulib.getInstance().get_hu_info(paiArray1, 34, gui_index1);
			if (hu) {
				return hu;
			}
		}
		return hu;

	}

	/**
	 * 此时不会检测四混
	 * 
	 * @param p
	 * @param pai
	 * @param roomResp
	 * @return
	 */
	public static boolean checkHuNew1(List<Integer[][]> newShouPai,
			Player winUser, Player toUser, Integer[][] pai, RoomResp roomResp) {
		if (roomResp == null) {
			roomResp = new RoomResp();
			List<Integer[][]> hunPai = new ArrayList<>();
			hunPai.add(new Integer[][] { { -1, -1 } });
			hunPai.add(new Integer[][] { { -1, -1 } });
			roomResp.setHunPai(hunPai);
		}
		List<Integer[][]> newList;// 为了获取混牌数
		List<Integer[][]> hunList;// 混牌集合
		Integer[][] lastFaPai;
		if (winUser != null) {// 传递的是玩家
			newList = getNewList(winUser.getCurrentMjList());
			lastFaPai = winUser.getLastFaPai();
			if (winUser.getUserId().equals(toUser.getUserId())) {// 是同一个玩家
				newList.add(new Integer[][] { { lastFaPai[0][0],
						lastFaPai[0][1] } });
				hunList = getHunList(newList, roomResp);
			} else {
				hunList = getHunList(newList, roomResp);
				newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
			}
			paiXu(newList);
		} else {// 传递的只是牌
			newList = getNewList(newShouPai);
			
			hunList = getHunList(newList, roomResp);
		}
		// 检测七对
		Integer haoHuaFanShu = null;
		if (winUser == null) {// 没有传入玩家，只传牌
			haoHuaFanShu = hasQiDui(getNewList(newShouPai), null, null, null,
					roomResp);
		} else {
			haoHuaFanShu = hasQiDui(winUser, toUser, pai, roomResp);
		}
		if (haoHuaFanShu != null) {
			return true;
		}
		int[] paiArray = getShouPaiArray(newList, null);
		/** 如果哪个牌的个数是0，就让guiIndex是这个 */
		int gui_index = 34;
		if (hunList != null && hunList.size() > 0) {
			gui_index = addHun(paiArray, hunList.size());
		}
		boolean hu = Hulib.getInstance().get_hu_info(paiArray, 34, gui_index);
		if (hu) {
			return hu;
		} else {
			// 移除tempShouPai 中的中发白
			// 包含中发白 一套，移除
			// 包含其中两个，其中一个个数为1，
			// 混补(混的个数是否大于1 ，--移除一个混替代中发白，移除) ,检测剩下的手牌能否胡
			int hunNum = hunList.size();
			List<String> list = new ArrayList<>();
			for (Integer[][] integers : newList) {
				list.add(integers[0][0] + "_" + integers[0][1]);
			}
			Integer zhong = 0;
			Integer fa = 0;
			Integer bai = 0;
			Integer min = 0;
			for (String string : list) {
				if (string.equals("5_1")) {
					zhong++;
				} else if (string.equals("5_2")) {
					fa++;
				} else if (string.equals("5_3")) {
					bai++;
				}
			}
			// 第一步，移除所有的中发白
			if (zhong != 0 && fa != 0 && bai != 0) {
				min = zhong >= fa ? fa : zhong;
				min = min >= bai ? bai : min;
				// 找出组成套中发白，移除
				removeZhongFaBai(list, min);// 移除list中min套中发白
				zhong = zhong - min;
				fa = fa - min;
				bai = bai - min;
			}
			// 第二步，一个为0，一个为1，一个大于0
			int[] arr = new int[3];
			arr[0] = zhong;
			arr[1] = fa;
			arr[2] = bai;
			Arrays.sort(arr);
			if (arr[0] == 0 && arr[1] == 1) {// 最小为一对的不用补
				if (hunNum > 0) {// 混牌至少一张
					hunList.remove(hunList.size() - 1);// 移除一张混牌;
					if (zhong == 0) {
						removeHunBuZhongFaBai(list, 1);// 手牌中移除不是中的组合
					} else if (fa == 0) {
						removeHunBuZhongFaBai(list, 2);
					} else if (bai == 0) {
						removeHunBuZhongFaBai(list, 3);
					}
				}
			}
			// 获取剩下的牌
			List<Integer[][]> list3 = new ArrayList<>();
			for (int i = 0; i < list.size(); i++) {
				list3.add(new Integer[][] { {
						Integer.valueOf(list.get(i).substring(0, 1)),
						Integer.valueOf(list.get(i).substring(2)) } });
			}
			int[] paiArray1 = getShouPaiArray(list3, null);
			/** 如果哪个牌的个数是0，就让guiIndex是这个 */
			int gui_index1 = 34;
			if (hunList != null && hunList.size() > 0) {
				gui_index1 = addHun(paiArray1, hunList.size());
			}
			hu = Hulib.getInstance().get_hu_info(paiArray1, 34, gui_index1);
			if (hu) {
				return hu;
			}
		}
		return hu;

	}

	/**
	 * 直传牌，看此牌是否满足7对
	 * 
	 * @param tempShouPai
	 * @param p
	 *            null
	 * @param p1
	 *            null
	 * @param pai
	 *            null
	 * @param roomResp
	 * @return
	 */
	private static Integer hasQiDui(List<Integer[][]> tempShouPai, Player p,
			Player p1, Integer[][] pai, RoomResp roomResp) {
		List<Integer[][]> newShouPaiList = getNewList(tempShouPai);
		if (newShouPaiList.size() == 14) {
			Integer hunNum;// 混牌数量
			List<Integer[][]> hunList = new ArrayList<>();// 混牌集合
			if (roomResp == null) {
				hunNum = 0;
			} else {
				hunList = getHunList(newShouPaiList, roomResp);
				hunNum = hunList.size();
			}
			paiXu(newShouPaiList);
			// 将没有混的手牌的对子全部移除 ,返回剩余没有对子的手牌数
			Integer danzhangshu = yichuDuizi(newShouPaiList);
			// 获取手牌中牌的种类（如：1饼，一条为2种）
			Integer paiKindsNum = getPaiKindsNum(newShouPaiList);
			// 单张数小于混排数 是七对
			if (danzhangshu <= hunNum) {
				if (paiKindsNum == 4) {
					return 3;
				} else if (paiKindsNum == 5) {
					return 2;
				} else if (paiKindsNum == 6) {
					return 1;
				} else if (paiKindsNum == 7) {
					return 0;
				}
			} else if (hunNum - danzhangshu == 2) {
				if (paiKindsNum == 3 || paiKindsNum == 4) {
					return 3;
				} else if (paiKindsNum == 5) {
					return 2;
				} else if (paiKindsNum == 6) {
					return 1;
				}
			} else if (hunNum - danzhangshu == 4) {
				if (paiKindsNum == 3 || paiKindsNum == 4) {
					return 3;
				} else if (paiKindsNum == 5) {
					return 2;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param list
	 * @param i
	 *            1混顶红中,2混顶发财,3混顶白板
	 */
	private static void removeHunBuZhongFaBai(List<String> list, int j) {
		int num = 0;
		for (int i = list.size() - 1; i >= 0; i--) {
			if (j == 1) {// 移除发和白
				if (num == 0 && list.get(i).equals("5_3")) {
					list.remove(i);
					num++;
				} else if (num == 1 && list.get(i).equals("5_2")) {
					list.remove(i);
					break;
				}
			} else if (j == 2) {// 移除中和白
				if (num == 0 && list.get(i).equals("5_3")) {
					list.remove(i);
					num++;
				} else if (num == 1 && list.get(i).equals("5_1")) {
					list.remove(i);
					break;
				}
			} else if (j == 3) {// 移除中和发
				if (num == 0 && list.get(i).equals("5_2")) {
					list.remove(i);
					num++;
				} else if (num == 1 && list.get(i).equals("5_1")) {
					list.remove(i);
					break;
				}

			}
		}

	}

	private static void removeZhongFaBai(List<String> list, int min) {
		int num = 1;
		for (int i = list.size() - 1; i >= 0; i--) {
			if (num <= min && list.get(i).equals("5_3")) {
				list.remove(i);
				num++;
			} else if (num <= min * 2 && list.get(i).equals("5_2")) {
				list.remove(i);
				num++;
			} else if (num <= min * 3 && list.get(i).equals("5_1")) {
				list.remove(i);
			}
		}

	}

	static List<Integer[][]> getNewList(List<Integer[][]> old) {
		List<Integer[][]> newList = new ArrayList<Integer[][]>();
		if (old != null && old.size() > 0) {
			for (Integer[][] pai : old) {
				newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
			}
		}
		return newList;
	}

	/**
	 * 
	 * 混牌跟手牌就分开了
	 * 
	 * @param tempShouPai
	 * @param p
	 * @return
	 */
	private static List<Integer[][]> getHunList(List<Integer[][]> tempShouPai,
			RoomResp roomResp) {
		List<Integer[][]> hunPai = new ArrayList<Integer[][]>();
		List<Integer[][]> hunPaiL = roomResp.getHunPai();// 0是明牌 ，1是混牌
//		System.err.println(hunPaiL.get(1)[0][0]  +"_"+hunPaiL.get(1)[0][1]);
		for (int i = 1; i < hunPaiL.size(); i++) {// 从第一个开始是混牌
			for (int j = tempShouPai.size() - 1; j >= 0; j--) {
//				System.err.println(tempShouPai.get(j)[0][0]+"_"+tempShouPai.get(j)[0][1]);
				if (tempShouPai.get(j)[0][0].equals(hunPaiL.get(i)[0][0])
						&& tempShouPai.get(j)[0][1]
								.equals(hunPaiL.get(i)[0][1])) {
					hunPai.add(tempShouPai.get(j));
					tempShouPai.remove(tempShouPai.get(j));
				}
			}
		}
		return hunPai;
	}
	
	/**
	 * 产生如下的数组，去检测是否胡牌 int[] cards = { 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0,
	 * 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	 * 如果牌为null则判断自摸 如果不是null，则判断点炮
	 * 
	 * @param playersPais
	 * @param pai
	 * @return
	 */
	private static int[] getShouPaiArray(List<Integer[][]> playersPais,
			Integer[][] pai) {
		int[] array = initArrays();
		for (int i = 0; i < playersPais.size(); i++) {
			addArray(array, playersPais.get(i));
		}
		if (pai != null) {
			addArray(array, pai);
		}
		return array;
	}

	/**
	 * 给数组中添加混， 如果数组中某个位置的个数为0的话， 就用混顶替当前位置， 返回混的位置
	 * 
	 * @param paiArray
	 * @param hunNum
	 * @return
	 */
	private static int addHun(int[] paiArray, int hunNum) {
		for (int i = 0; i < paiArray.length; i++) {
			if (paiArray[i] == 0) {
				paiArray[i] = hunNum;
				return i;
			}
		}
		return 34;
	}

	/**
	 * length为34的新数组
	 * 
	 * @return
	 */
	private static int[] initArrays() {
		int[] array = new int[34];
		for (int i = 0; i < 34; i++) {
			array[i] = 0;
		}
		return array;
	}

	/**
	 * 
	 * @param paiList
	 * @param pai
	 *            --指的是lastFaPai （亮风时设为null，加入手牌paiList）
	 * @param roomResp
	 * @return
	 */
	public static List<Integer[][]> checkTing(List<Integer[][]> paiList,
			Integer[][] pai, RoomResp roomResp) {
		List<Integer[][]> list = new ArrayList<>();// 初始化返回集合
		List<Integer[][]> hunList = roomResp.getHunPai();
		Integer[][] hunPai = hunList.get(1);// 获取混牌
		Integer[][] yiChuPai = null;// 需要移除的牌
		List<Integer[][]> newList = getNewList(paiList);
		if (pai != null) {
			newList.add(new Integer[][] { { pai[0][0], pai[0][1] } });
		}
		// 添加一张混牌 15张
		newList.add(new Integer[][] { { hunPai[0][0], hunPai[0][1] } });

		paiXu(newList);
		boolean isHu = false;
		if (newList != null && newList.size() > 0) {// 多了一张混牌，满足牌数+1
			ArrayList<Integer[][]> distinct = getDistinct(newList);// 获取不重复的牌集合
			for (int i = distinct.size() - 1; i >= 0; i--) {
				if (distinct.get(i)[0][0].equals(hunPai[0][0])
						&& distinct.get(i)[0][1].equals(hunPai[0][1])) {
					distinct.remove(i);// 移除混牌
					break;
				}
			}
			for (int i = distinct.size() - 1; i >= 0; i--) {
				for (int j = newList.size() - 1; j >= 0; j--) {
					if (newList.get(j)[0][0].equals(distinct.get(i)[0][0])
							&& newList.get(j)[0][1]
									.equals(distinct.get(i)[0][1])) {// 有这张牌时
						// 移除此牌
						yiChuPai = newList.get(j);
						newList.remove(j);
						// 检测胡
						isHu = checkHuNew1(newList, null, null, null, roomResp);
						if (isHu) {// 如果胡
							list.add(new Integer[][] { { distinct.get(i)[0][0],
									distinct.get(i)[0][1] } });
						}
						// 将这张牌继续添加到里面
						newList.add(j, yiChuPai);
						break;
					}
				}
			}
		}
		return list.size() > 0 ? list : null;
	}

	/**
	 * 检测是否有混牌
	 * 
	 * @param currentMjList
	 * @param lastFaPai
	 * @param roomResp
	 * @return
	 */
	public static Boolean checkHasHun(List<Integer[][]> currentMjList,
			Integer[][] lastFaPai, RoomResp roomResp) {
		Integer[][] hunPai = roomResp.getHunPai().get(1);// 房间混牌
		if (lastFaPai != null) {
			if (lastFaPai[0][0].equals(hunPai[0][0])
					&& lastFaPai[0][1].equals(hunPai[0][1])) {
				return true;
			}
		}
		for (Integer[][] integers : currentMjList) {
			if (integers[0][0].equals(hunPai[0][0])
					&& integers[0][1].equals(hunPai[0][1])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检测潇洒
	 *
	 * @param paiList
	 *            是不是听牌
	 * @return
	 */
	public static Boolean checkXiaoSa(List<Integer[][]> currentMjList,
			RoomResp roomResp) {
		boolean isHu = false;
		List<Integer[][]> newList = getNewList(currentMjList);
		Integer[][] hunPai = roomResp.getHunPai().get(1);
		newList.add(new Integer[][] { { hunPai[0][0], hunPai[0][1] } });// 加入一张混牌加测是不是胡
		isHu = checkHuNew1(paiXu(newList), null, null, null, roomResp);
		return isHu;
	}

	private static void addArray(int[] array, Integer[][] pai) {
		int type = pai[0][0];
		int num = pai[0][1];
		switch (type) {
		case 1:
		case 2:
		case 3:
			array[(type - 1) * 9 + num - 1]++;
			break;
		case 4:
			array[3 * 9 + num - 1]++;
			break;
		case 5:
			array[3 * 9 + 4 + num - 1]++;
			break;
		}
	}

}
