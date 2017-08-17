import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager {

	private static StrategyManager instance = new StrategyManager();

	private CommandUtil commandUtil = new CommandUtil();

	public boolean isFullScaleAttackStarted; // private -> public 변경 by 노동환
	public boolean isInitialBuildOrderFinished; // private -> public 변경 by 노동환
	public BaseLocation mainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());

	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가를 위한 변수 및 메소드 선언

	/// 한 게임에 대한 기록을 저장하는 자료구조
	private class GameRecord {
		String mapName;
		String enemyName;
		String enemyRace;
		String enemyRealRace;
		String myName;
		String myRace;
		int gameFrameCount = 0;
		int myWinCount = 0;
		int myLoseCount = 0;
	}

	/// 과거 전체 게임들의 기록을 저장하는 자료구조
	ArrayList<GameRecord> gameRecordList = new ArrayList<GameRecord>();

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////

	/// static singleton 객체를 리턴합니다
	public static StrategyManager Instance() {
		return instance;
	}

	public StrategyManager() {
		isFullScaleAttackStarted = false;
		isInitialBuildOrderFinished = false;
	}

	public boolean isHunter = false;
	int halfheight;//= InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getY(); //MyBotModule.Broodwar.mapHeight()/2;
	int  halfwidth;// = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getX();// MyBotModule.Broodwar.mapWidth()/2;

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {

		// BasicBot 1.1 Patch Start
		// ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

		// 과거 게임 기록을 로딩합니다
		// loadGameRecordList();

		// BasicBot 1.1 Patch End
		// //////////////////////////////////////////////////
		//halfheight = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getY();
		//halfwidth = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getX();// MyBotModule.Broodwar.mapWidth()/2;
		if (MyBotModule.Broodwar.mapFileName().toLowerCase().indexOf("hunter") != -1) {
			isHunter = true;
		}
		if (MyBotModule.Broodwar.mapFileName().toLowerCase().indexOf("hunter") != -1 || InformationManager.Instance().enemyRace == Race.Protoss) {
			setHunterInitialBuildOrder();
			maxWorkerCount = 18;

			firstExpantion_createdZealot = 30;
			firstExpantion_aliveZealot = 13;

			toggleAttackMode_startAttack = 18;
			toggleAttackMode_stopAttack = 12;

		} else {
			maxWorkerCount = 12;

			firstExpantion_createdZealot = 20;
			firstExpantion_aliveZealot = 5;

			toggleAttackMode_startAttack = 15;
			toggleAttackMode_stopAttack = 12;
		}
		for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
			if (u.getType() == UnitType.Protoss_Nexus)
				nexus = u;
		}

		mainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		expantionList.add(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()));
		expantionList.add(InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self()));
	}

	/// 경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {

		// BasicBot 1.1 Patch Start
		// ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

		// 과거 게임 기록 + 이번 게임 기록을 저장합니다
		saveGameRecordList(isWinner);

		// BasicBot 1.1 Patch End
		// //////////////////////////////////////////////////
	}

	public Unit nexus = null;
	public Unit firstGate = null;
	public Unit secondGate = null;
	public Unit thirdGate = null;
	public Unit fourthGate = null;
	public Unit idleProbe = null;

	public boolean isFirstPylon = false;
	public boolean isFirstGate = false;
	public boolean isSecondGate = false;
	public boolean isThirdGate = false;
	public boolean isStartScout = false;
	public boolean isElimination = false;
	public boolean isFirstExpantion = false;
	public boolean isReadyAttack = true;

	public int toggleAttackMode_stopAttack;
	public int toggleAttackMode_startAttack;
	public int firstExpantion_createdZealot;
	public int firstExpantion_aliveZealot;

	public int maxWorkerCount;

	public int isEnterBrock = 0;
	
	public boolean isMakeOb =false;

	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다

	public void update() {
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			hunterUpdate();
		} else if (MyBotModule.Broodwar.mapFileName().toLowerCase().indexOf("spirit") != -1) {
			fightingSpiritUpdate();
		} else if (MyBotModule.Broodwar.mapFileName().toLowerCase().indexOf("temple") != -1) {
			lostTempleUpdate();
		} else if (MyBotModule.Broodwar.mapFileName().toLowerCase().indexOf("hunter") != -1) {
			hunterUpdate();
		}

		mainBaseDefence();
		elimination();
		firstExpantion();
		toggleAttackMode();
		//checkForge();
		unlimitedExpantion();
		getMaxWorker();
		makeOb();
	}

	ArrayList<BaseLocation> expantionList = new ArrayList<>();

	int currentNexus;
	boolean isUnlimitedExpantion = false;
	TilePosition expantionPosition =null;

	public void unlimitedExpantion() {
		if (MyBotModule.Broodwar.getFrameCount() % 403 != 0)
			return;
		if (!isFirstExpantion)
			return;
		if (!isUnlimitedExpantion && InformationManager.Instance().selfPlayer.minerals() >= 350) {
			isUnlimitedExpantion = true;
			currentNexus = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits(UnitType.Protoss_Nexus.toString());
		}
		if (isUnlimitedExpantion) {
			for(ConstructionTask b : ConstructionManager.Instance().constructionQueue) {
				if(b.getType() == UnitType.Protoss_Nexus)
					return;
			}
			if (InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits(UnitType.Protoss_Nexus.toString()) <= currentNexus) {
				for (BaseLocation r : BWTA.getBaseLocations()) {
					if (InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer).contains(r))
						continue;
					if (InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer).equals(r))
						continue;
					if (InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).contains(r))
						continue;
					expantionPosition = r.getTilePosition();
					ConstructionManager.Instance().addConstructionTask(UnitType.Protoss_Nexus, expantionPosition);
					//ConstructionManager.Instance().addConstructionTask(UnitType.Protoss_Pylon, ConstructionPlaceFinder.Instance().getBuildLocationNear(UnitType.Protoss_Pylon, r.getTilePosition()));
					//ConstructionManager.Instance().addConstructionTask(UnitType.Protoss_Photon_Cannon, ConstructionPlaceFinder.Instance().getBuildLocationNear(UnitType.Protoss_Photon_Cannon, r.getTilePosition()));
					//ConstructionManager.Instance().addConstructionTask(UnitType.Protoss_Photon_Cannon, ConstructionPlaceFinder.Instance().getBuildLocationNear(UnitType.Protoss_Photon_Cannon, r.getTilePosition()));
					//ConstructionManager.Instance().addConstructionTask(UnitType.Protoss_Photon_Cannon, ConstructionPlaceFinder.Instance().getBuildLocationNear(UnitType.Protoss_Photon_Cannon, r.getTilePosition()));
					return;
				}
			} 
			if(InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits(UnitType.Protoss_Nexus.toString()) > currentNexus){
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, false);
				isUnlimitedExpantion = false;
			}
		}
	}

	public void toggleAttackMode() {
		if (MyBotModule.Broodwar.getFrameCount() % 23 != 0)
			return;
		if (!isFirstExpantion)
			return;

		int idle = 0;
		for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
			if (u.getType() == UnitType.Protoss_Zealot && !u.isAttacking() && !u.isUnderAttack()) {
				if(BWTA.getGroundDistance(u.getTilePosition(),  InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getPoint().toTilePosition() )<= 1000)
					idle ++;
			}
		}
		if (idle <= toggleAttackMode_stopAttack)
			isReadyAttack = false;
		else if (idle >= toggleAttackMode_startAttack)
			isReadyAttack = true;
	}

	public void checkForge() {
		if (!isFirstExpantion)
			return;
		if (InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits("Protoss_Forge") < 1 && BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Forge) == 0) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Forge, true);
		}
	}

	public void firstExpantion() {

		if (!isFirstExpantion && InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumCreatedUnits("Protoss_Zealot") >= firstExpantion_createdZealot && InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits("Protoss_Zealot") <= firstExpantion_aliveZealot) {
			isReadyAttack = false;
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Nexus, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Cybernetics_Core, true);
			if(InformationManager.Instance().enemyRace == Race.Protoss || InformationManager.Instance().enemyRace == Race.Zerg ) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Robotics_Facility, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Observatory, false);
				isMakeOb = true;
			}
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Assimilator, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Citadel_of_Adun, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Leg_Enhancements, false);

			//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Pylon, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, false);
			
			//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Photon_Cannon, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, false);
			//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Photon_Cannon, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation , false);
			//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Photon_Cannon, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, false);

			maxWorkerCount = 100;
			isFirstExpantion = true;
		}
	}
	
	public void makeOb() {
		if (MyBotModule.Broodwar.getFrameCount() % 801 != 0)
			return;
		if(isMakeOb && InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits("Protoss_Observatory")!=0 
				&& InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits("Protoss_Robotics_Facility")!=0 ) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Observer, true);
		}
	}

	public void elimination() {
		if (MyBotModule.Broodwar.getFrameCount() % 2701 != 0)
			return;
		if (!isElimination) {
			if (InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits("Protoss_Zealot") >= 50) {
				boolean noAttack = true;
				for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
					if (u.isAttacking()) {
						noAttack = false;
						break;
					}
				}
				if (noAttack) {
					isElimination = true;
				}
			}
		}
		if (isElimination) {
			Random r = new Random();
			int height = MyBotModule.Broodwar.mapHeight();
			int width = MyBotModule.Broodwar.mapWidth();
			int count = 0;
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == UnitType.Protoss_Zealot) {
					count++;
					commandUtil.attackMove(u, new TilePosition(r.nextInt(width), r.nextInt(height)).toPosition());
					if(count>=30)
						return;
				}
			}
		}
	}

	/*	private void bunkerAttack() {
			if(InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumUnits("Terran_Bunker") * 4 <= InformationManager.Instance().allZealotList.size()){
				
			}
		}
	*/
	// 일꾼 방어 로직
	Unit targetMineral = null;
	Position enemyPosition = null;
	int mineralMoveCount = -3; // 상태값을 갖는다

	Unit defenceZealot = null;

	public void mainBaseDefence() {
		// mineralMoveCount가 0 보다 크면 적군에게서 가장 먼 미네랄로 모인다 
		if (mineralMoveCount > 0) {
			moveEnemy_NearMainBase_UsingProbe(targetMineral, 300);
			mineralMoveCount--;
			return;

			// mineralMoveCount가 0이 되면 공격 
		} else if (mineralMoveCount == 0) {
			attackEnemy_NearMainBase_UsingProbe(enemyPosition, 300);
			mineralMoveCount--;
			return;

			// mineralMoveCount가 -1이면 공격 중이라는 뜻이다 
		} else if (mineralMoveCount == -1) {
			Unit enemy = getEnemy_NearMainBase(300);
			if (enemy != null) {
				attackEnemy_NearMainBase_UsingProbe(enemy.getPosition(), 300);
				return;
			} else {
				mineralMoveCount--;
			}

			// mineralMoveCount가 -2이면 공격이 끝났으니 다시 복귀
		} else if (mineralMoveCount == -2) {
			targetMineral = null;
			enemyPosition = null;
			stopProbe_NearMainBase(500);
			mineralMoveCount--;

			// -3이 평시 상태. 계속 적이 공격오지는 않았는지 체크한다. 
		} else {
			if (MyBotModule.Broodwar.getFrameCount() % 5 != 0)
				return;

			if (getUnderAttackedUnit_NearMainBase(300) != null) {
				Unit enemy = getEnemy_NearMainBase(300);
				if (enemy != null) {
					enemyPosition = enemy.getPosition();
					targetMineral = getMineral_MostFarFrom_Enemy(enemy);
					mineralMoveCount = 60; // 50프레임동안 유닛을 모은다. 
				}
			}
		}
	}

	public void stopProbe_NearMainBase(int radius) {
		for (Unit worker : MyBotModule.Broodwar.self().getUnits()) {
			if (worker.getType() == UnitType.Protoss_Probe && BWTA.getGroundDistance(worker.getTilePosition(), mainBaseLocation.getTilePosition()) <= radius) {
				worker.stop();
			}
		}
	}

	public void attackEnemy_NearMainBase_UsingProbe(Position enemyPosition, int radius) {
		if (enemyPosition == null) {
			enemyPosition = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()).getPoint();
		}
		if (defenceZealot == null || !defenceZealot.exists()) {
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == UnitType.Protoss_Zealot) {
					defenceZealot = u;
					break;
				}
			}
		}
		if (defenceZealot != null && defenceZealot.exists()) {
			commandUtil.attackMove(defenceZealot, enemyPosition);
		}
		for (Unit worker : MyBotModule.Broodwar.self().getUnits()) {
			if (worker.getType() == UnitType.Protoss_Probe && worker.isIdle() && BWTA.getGroundDistance(worker.getTilePosition(), mainBaseLocation.getTilePosition()) <= radius) {
				commandUtil.attackMove(worker, enemyPosition);
			}
		}
	}

	public void moveEnemy_NearMainBase_UsingProbe(Unit targetMineral, int radius) {
		if (targetMineral == null) {
			for (Unit mineral : MyBotModule.Broodwar.getAllUnits()) {
				if (!mineral.isVisible())
					continue;
				if (mineral.getType() == UnitType.Resource_Mineral_Field && BWTA.getGroundDistance(mineral.getTilePosition(), mainBaseLocation.getTilePosition()) <= 1000) {
					targetMineral = mineral;
					break;
				}
			}
		}
		for (Unit worker : MyBotModule.Broodwar.self().getUnits()) {
			if (worker.getType() == UnitType.Protoss_Probe && BWTA.getGroundDistance(worker.getTilePosition(), mainBaseLocation.getTilePosition()) <= radius) {
				commandUtil.rightClick(worker, targetMineral);
			}
		}
	}

	public Unit getUnderAttackedUnit_NearMainBase(int radius) {
		Unit result = null;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.isUnderAttack() && unit.getType() != UnitType.Protoss_Zealot) {
				double dist = BWTA.getGroundDistance(unit.getTilePosition(), mainBaseLocation.getTilePosition());
				if (dist >= 0 && dist <= radius) {
					result = unit;
					break;
				}
			}
		}
		return result;
	}

	public Unit getEnemy_NearMainBase(int radius) {
		Unit result = null;
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			if (unit.isVisible()) {
				double dist = BWTA.getGroundDistance(unit.getTilePosition(), mainBaseLocation.getTilePosition());
				if (dist > 0 && dist <= radius) {
					result = unit;
					break;
				}
			}
		}
		return result;
	}

	public Unit getMineral_MostFarFrom_Enemy(Unit enemy) {
		Unit maxDistMineral = null;
		double maxDist = 0;
		for (Unit mineral : MyBotModule.Broodwar.getAllUnits()) {
			if (!mineral.isVisible())
				continue;
			if (mineral.getType() == UnitType.Resource_Mineral_Field && BWTA.getGroundDistance(mineral.getTilePosition(), mainBaseLocation.getTilePosition()) <= 1000) {
				double tempDist = BWTA.getGroundDistance(mineral.getTilePosition(), enemy.getTilePosition());
				if (tempDist > maxDist) {
					maxDist = tempDist;
					maxDistMineral = mineral;
				}
			}
		}
		return maxDistMineral;
	}

	public void setHunterInitialBuildOrder() {
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe); // 프로브

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Pylon, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe); // 프로브

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe); // 프로브

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe); // 프로브
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Zealot); // 질럿
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Pylon, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe); // 프로브
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Zealot); // 질럿
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe); // 프로브
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Zealot); // 질럿

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Pylon, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe); // 프로브
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

	}

	public void hunterUpdate() {

		if (secondGate != null)
			ScoutManager.Instance().assignScoutIfNeeded();

		if (fourthGate != null)
			isInitialBuildOrderFinished = true;

		if (isInitialBuildOrderFinished) {
			executeWorkerTraining();
			executeSupplyManagement();
			executeBasicCombatUnitTraining();
			executeCombat(15);
		} else {
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == UnitType.Protoss_Zealot && u.isIdle()) {
					u.attack(InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()).getPoint());
				}
			}
		}

		// GateWay 찾기
		if (firstGate == null)
			for (Unit u : MyBotModule.Broodwar.self().getUnits())
				if (u.getType() == UnitType.Protoss_Gateway)
					firstGate = u;
		if (firstGate != null && secondGate == null)
			for (Unit u : MyBotModule.Broodwar.self().getUnits())
				if (u.getType() == UnitType.Protoss_Gateway && u.getID() != firstGate.getID())
					secondGate = u;
		if (secondGate != null && thirdGate == null)
			for (Unit u : MyBotModule.Broodwar.self().getUnits())
				if (u.getType() == UnitType.Protoss_Gateway && u.getID() != firstGate.getID() && u.getID() != secondGate.getID())
					thirdGate = u;
		if (thirdGate != null && fourthGate == null)
			for (Unit u : MyBotModule.Broodwar.self().getUnits())
				if (u.getType() == UnitType.Protoss_Gateway && u.getID() != firstGate.getID() && u.getID() != secondGate.getID() && u.getID() != thirdGate.getID())
					fourthGate = u;
	}

	public void lostTempleUpdate() {
		if (!isInitialBuildOrderFinished) {
			if (MyBotModule.Broodwar.self().supplyUsed() == 8)
				nexus.build(UnitType.Protoss_Probe);
			if (MyBotModule.Broodwar.self().supplyUsed() == 10 && nexus.isIdle() && MyBotModule.Broodwar.self().minerals() >= 50)
				nexus.build(UnitType.Protoss_Probe);
			if (MyBotModule.Broodwar.self().supplyUsed() == 12 && nexus.isIdle() && MyBotModule.Broodwar.self().minerals() >= 50) {
				nexus.build(UnitType.Protoss_Probe);
				for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
					if (u.getType() == UnitType.Protoss_Probe && u.isCompleted()) {
						commandUtil.move(u, new Position(2510, 1964));
						idleProbe = u;
						break;
					}
				}
			}
			if (MyBotModule.Broodwar.self().supplyUsed() == 14 && nexus.isIdle() && MyBotModule.Broodwar.self().minerals() >= 50)
				nexus.build(UnitType.Protoss_Probe);
			if (MyBotModule.Broodwar.self().supplyUsed() >= 12 && MyBotModule.Broodwar.self().minerals() >= 100 && !isFirstPylon)
				isFirstPylon = idleProbe.build(UnitType.Protoss_Pylon, new TilePosition(79, 61));
			if (isFirstPylon && !isFirstGate)
				isFirstGate = idleProbe.build(UnitType.Protoss_Gateway, new TilePosition(76, 63));
			if (isFirstGate && !isSecondGate)
				isSecondGate = idleProbe.build(UnitType.Protoss_Gateway, new TilePosition(80, 63));
			if (secondGate != null) {
				ScoutManager.Instance().currentScoutUnit = idleProbe;
				WorkerManager.Instance().setScoutWorker(idleProbe);
				idleProbe = null;
				isInitialBuildOrderFinished = true;
			}
			if (firstGate == null)
				for (Unit u : MyBotModule.Broodwar.self().getUnits())
					if (u.getType() == UnitType.Protoss_Gateway)
						firstGate = u;
			if (secondGate == null)
				for (Unit u : MyBotModule.Broodwar.self().getUnits())
					if (u.getType() == UnitType.Protoss_Gateway && u.getID() != firstGate.getID())
						secondGate = u;
		}
		if (isInitialBuildOrderFinished) {
			executeWorkerTraining();
			executeSupplyManagement();
			executeBasicCombatUnitTraining();
			executeCombat(1);
		}
	}

	public void fightingSpiritUpdate() {
		if (!isInitialBuildOrderFinished) {
			if (MyBotModule.Broodwar.self().supplyUsed() == 8)
				nexus.build(UnitType.Protoss_Probe);
			if (MyBotModule.Broodwar.self().supplyUsed() == 10 && nexus.isIdle() && MyBotModule.Broodwar.self().minerals() >= 50)
				nexus.build(UnitType.Protoss_Probe);
			if (MyBotModule.Broodwar.self().supplyUsed() == 12 && nexus.isIdle() && MyBotModule.Broodwar.self().minerals() >= 50) {
				nexus.build(UnitType.Protoss_Probe);
				for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
					if (u.getType() == UnitType.Protoss_Probe && u.isCompleted()) {
						commandUtil.move(u, new Position(2036, 2058));
						idleProbe = u;
						break;
					}
				}
			}
			if (MyBotModule.Broodwar.self().supplyUsed() == 14 && nexus.isIdle() && MyBotModule.Broodwar.self().minerals() >= 50)
				nexus.build(UnitType.Protoss_Probe);
			if (MyBotModule.Broodwar.self().supplyUsed() >= 12 && MyBotModule.Broodwar.self().minerals() >= 100 && !isFirstPylon)
				isFirstPylon = idleProbe.build(UnitType.Protoss_Pylon, new TilePosition(66, 59));
			if (isFirstPylon && !isFirstGate)
				isFirstGate = idleProbe.build(UnitType.Protoss_Gateway, new TilePosition(62, 61));
			if (isFirstGate && !isSecondGate)
				isSecondGate = idleProbe.build(UnitType.Protoss_Gateway, new TilePosition(60, 57));
			if (secondGate != null) {
				ScoutManager.Instance().currentScoutUnit = idleProbe;
				WorkerManager.Instance().setScoutWorker(idleProbe);
				idleProbe = null;
				isInitialBuildOrderFinished = true;
			}
			if (firstGate == null)
				for (Unit u : MyBotModule.Broodwar.self().getUnits())
					if (u.getType() == UnitType.Protoss_Gateway)
						firstGate = u;
			if (secondGate == null)
				for (Unit u : MyBotModule.Broodwar.self().getUnits())
					if (u.getType() == UnitType.Protoss_Gateway && u.getID() != firstGate.getID())
						secondGate = u;
		}
		if (isInitialBuildOrderFinished) {
			executeWorkerTraining();
			executeSupplyManagement();
			executeBasicCombatUnitTraining();
			executeCombat(1);
		}
	}

	public void getMaxWorker() {
		if (MyBotModule.Broodwar.getFrameCount() % 501 != 0)
			return;
		maxWorkerCount = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits("Protoss_Nexus") * 18;
	}

	// 일꾼 계속 추가 생산
	public void executeWorkerTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			// workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Protoss_Probe);

			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType().isResourceDepot()) {
					if (unit.isTraining()) {
						workerCount += unit.getTrainingQueue().size();
					}
				}
			}

			if (workerCount < maxWorkerCount) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining() == false || unit.getLarva().size() > 0) {
							// 빌드큐에 일꾼 생산이 1개는 있도록 한다
							if (BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getWorkerType(), null) == 0) {
								// std.cout + "worker enqueue" + std.endl;
								BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
							}
						}
					}
				}
			}
		}
	}

	// Supply DeadLock 예방 및 SupplyProvider 가 부족해질 상황 에 대한 선제적 대응으로서<br>
	// SupplyProvider를 추가 건설/생산한다
	public void executeSupplyManagement() {

		// BasicBot 1.1 Patch Start
		// ////////////////////////////////////////////////
		// 가이드 추가 및 콘솔 출력 명령 주석 처리

		// InitialBuildOrder 진행중 혹은 그후라도 서플라이 건물이 파괴되어 데드락이 발생할 수 있는데, 이 상황에 대한
		// 해결은 참가자께서 해주셔야 합니다.
		// 오버로드가 학살당하거나, 서플라이 건물이 집중 파괴되는 상황에 대해 무조건적으로 서플라이 빌드 추가를 실행하기 보다 먼저
		// 전략적 대책 판단이 필요할 것입니다

		// BWAPI::Broodwar->self()->supplyUsed() >
		// BWAPI::Broodwar->self()->supplyTotal() 인 상황이거나
		// BWAPI::Broodwar->self()->supplyUsed() + 빌드매니저 최상단 훈련 대상 유닛의
		// unit->getType().supplyRequired() >
		// BWAPI::Broodwar->self()->supplyTotal() 인 경우
		// 서플라이 추가를 하지 않으면 더이상 유닛 훈련이 안되기 때문에 deadlock 상황이라고 볼 수도 있습니다.
		// 저그 종족의 경우 일꾼을 건물로 Morph 시킬 수 있기 때문에 고의적으로 이런 상황을 만들기도 하고,
		// 전투에 의해 유닛이 많이 죽을 것으로 예상되는 상황에서는 고의적으로 서플라이 추가를 하지 않을수도 있기 때문에
		// 참가자께서 잘 판단하셔서 개발하시기 바랍니다.

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 27 != 0) {
			return;
		}

		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
		if (MyBotModule.Broodwar.self().supplyTotal() <= 400) {

			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이
			// 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
			int supplyMargin = 12;

			// currentSupplyShortage 를 계산한다
			int currentSupplyShortage = MyBotModule.Broodwar.self().supplyUsed() + supplyMargin - MyBotModule.Broodwar.self().supplyTotal();

			if (currentSupplyShortage > 0) {

				// 생산/건설 중인 Supply를 센다
				int onBuildingSupplyCount = 0;

				// 저그 종족인 경우, 생산중인 Zerg_Overlord (Zerg_Egg) 를 센다. Hatchery 등 건물은
				// 세지 않는다
				if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == UnitType.Zerg_Overlord) {
							onBuildingSupplyCount += UnitType.Zerg_Overlord.supplyProvided();
						}
						// 갓태어난 Overlord 는 아직 SupplyTotal 에 반영안되어서, 추가 카운트를 해줘야함
						if (unit.getType() == UnitType.Zerg_Overlord && unit.isConstructing()) {
							onBuildingSupplyCount += UnitType.Zerg_Overlord.supplyProvided();
						}
					}
				}
				// 저그 종족이 아닌 경우, 건설중인 Protoss_Pylon, Terran_Supply_Depot 를 센다.
				// Nexus, Command Center 등 건물은 세지 않는다
				else {
					onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(InformationManager.Instance().getBasicSupplyProviderUnitType(), null) * InformationManager.Instance().getBasicSupplyProviderUnitType().supplyProvided();
				}

				// 주석처리
				// System.out.println("currentSupplyShortage : " +
				// currentSupplyShortage + " onBuildingSupplyCount : " +
				// onBuildingSupplyCount);

				if (currentSupplyShortage > onBuildingSupplyCount) {

					// BuildQueue 최상단에 SupplyProvider 가 있지 않으면 enqueue 한다
					boolean isToEnqueue = true;
					if (!BuildManager.Instance().buildQueue.isEmpty()) {
						BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
						if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == InformationManager.Instance().getBasicSupplyProviderUnitType()) {
							isToEnqueue = false;
						}
					}
					if (isToEnqueue) {
						// 주석처리
						// System.out.println("enqueue supply provider "
						// +
						// InformationManager.Instance().getBasicSupplyProviderUnitType());
						if (MyBotModule.Broodwar.enemy().getRace() == Race.Zerg)
							BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getBasicSupplyProviderUnitType()), BuildOrderItem.SeedPositionStrategy.MainBaseFrontYardThird, true);
						else
							BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getBasicSupplyProviderUnitType()), true);
					}
				}
			}
		}

		// BasicBot 1.1 Patch End
		// ////////////////////////////////////////////////
	}

	Position enemyBuilding = null;

	public void executeBasicCombatUnitTraining() {
		//if(isUnlimitedExpantion)
		//	return;
		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 기본 병력 추가 훈련
		if (MyBotModule.Broodwar.self().minerals() >= 100 && MyBotModule.Broodwar.self().supplyUsed() < 390) {
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Zealot, null) == 0 && BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Nexus, null) == 0) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicCombatUnitType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
			}
		}
	}

	public int atackTiming = 0;
	public Unit nearSupply = null;
	public Unit z = null;
	public void executeCombat(int firstAttackStart) {
		//System.out.println(isEnterBrock + " " + InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size());
		if (isElimination)
			return;

		if (!isReadyAttack) {
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == UnitType.Protoss_Zealot && u.isIdle()) {
					u.attack( InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getPoint());
					z  = u;
				}
				if (u.getType() == UnitType.Protoss_Observer  && !u.isFollowing()) {
					u.follow(z);
				}
			}
			return;
		} else if (isFirstExpantion) {
			isEnterBrock = 0;
		}

		if (enemyBuilding == null) {
			for (Unit u : MyBotModule.Broodwar.enemy().getUnits()) {
				if (u.getType() == UnitType.Terran_Supply_Depot || u.getType() == UnitType.Terran_Barracks || u.getType() == UnitType.Factories || u.getType() == UnitType.Protoss_Photon_Cannon) {
					enemyBuilding = u.getPoint();
					break;
				}
			}
			//System.out.println(enemyBuilding);
		}

		//System.out.println("111111111");
		if (InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits("Protoss_Zealot") >= firstAttackStart && (InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() != 0 || enemyBuilding != null)) {

			BaseLocation targetBaseLocation = null;
			double longDistance = 0;

			for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer)) {
				double distance = BWTA.getGroundDistance(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition(), baseLocation.getTilePosition());

				if (distance > longDistance) {
					longDistance = distance;
					targetBaseLocation = baseLocation;
				}
			}

			Position targetPosition = null;
			if (targetBaseLocation == null)
				targetPosition = enemyBuilding;
			else
				targetPosition = targetBaseLocation.getPoint();

			if (nearSupply != null && InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumDeadUnits(nearSupply.getType().toString()) > 0) {
				for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
					if (u.getType() == UnitType.Protoss_Zealot) {
						u.stop();
					}
				}
				isEnterBrock = -99999;
				return;

			}
			//System.out.println("4444444");
			int createdMarine = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumCreatedUnits("Terran_Marine");
			int deadMarine = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumDeadUnits("Terran_Marine");
			int deadZealot = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumDeadUnits("Protoss_Zealot");
			int bunker = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumUnits("Terran_Bunker");
			if (createdMarine >= 1 && deadMarine == 0 && deadZealot == 0 && bunker==0) {
				//System.out.println("5555555");	
				isEnterBrock++;
				;
			}

			int max = 550;
			if (isHunter)
				max = 3000;
			if (isEnterBrock >= max) {
				if (nearSupply == null) {
					double min = 99999999;
					for (Unit u : MyBotModule.Broodwar.enemy().getUnits()) {
						if (u.getType() == UnitType.Terran_Supply_Depot || u.getType() == UnitType.Terran_Barracks || u.getType() == UnitType.Factories) {
							double dd = BWTA.getGroundDistance(u.getTilePosition(), InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition());
							if (dd < min) {
								min = dd;
								nearSupply = u;
							}
						}
					}
					return;
				}
				for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
					if (u.getType() == UnitType.Protoss_Zealot) {
						u.attack(nearSupply);
					}
				}
			} else {
				bunker = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumUnits("Terran_Bunker");
				int zealot = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits("Protoss_Zealot");
				if(bunker>0 && bunker*6 > zealot) {
					for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
						if (u.getType() == UnitType.Protoss_Zealot && BWTA.getGroundDistance(u.getTilePosition(), InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer).getTilePosition())>500) {
							u.move(targetPosition);
						}
					}
				}
				else {
					for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
						if (u.getType() == UnitType.Protoss_Zealot) {
							if (u.isIdle()) {
								u.attack(targetPosition);
							}
						}
					}
				}
				return;
			}
		}
		if (isHunter) {
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == UnitType.Protoss_Zealot && u.isIdle()) {
					u.attack(InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()).getPoint());
				}
			}
		}
	}

	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

	/// 과거 전체 게임 기록을 로딩합니다
	void loadGameRecordList() {

		// 과거의 게임에서 bwapi-data\write 폴더에 기록했던 파일은 대회 서버가 bwapi-data\read 폴더로
		// 옮겨놓습니다
		// 따라서, 파일 로딩은 bwapi-data\read 폴더로부터 하시면 됩니다

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameRecordFileName = "bwapi-data\\read\\makjangBot_GameRecord.dat";

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(gameRecordFileName));

			//System.out.println("loadGameRecord from file: " + gameRecordFileName);

			String currentLine;
			StringTokenizer st;
			GameRecord tempGameRecord;
			while ((currentLine = br.readLine()) != null) {

				st = new StringTokenizer(currentLine, " ");
				tempGameRecord = new GameRecord();
				if (st.hasMoreTokens()) {
					tempGameRecord.mapName = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.myName = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.myRace = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.myWinCount = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.myLoseCount = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.enemyName = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.enemyRace = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.enemyRealRace = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.gameFrameCount = Integer.parseInt(st.nextToken());
				}

				gameRecordList.add(tempGameRecord);
			}
		} catch (FileNotFoundException e) {
			//System.out.println("loadGameRecord failed. Could not open file :" + gameRecordFileName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/// 과거 전체 게임 기록 + 이번 게임 기록을 저장합니다
	void saveGameRecordList(boolean isWinner) {

		// 이번 게임의 파일 저장은 bwapi-data\write 폴더에 하시면 됩니다.
		// bwapi-data\write 폴더에 저장된 파일은 대회 서버가 다음 경기 때 bwapi-data\read 폴더로
		// 옮겨놓습니다

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameRecordFileName = "bwapi-data\\write\\makjangBot_GameRecord.dat";

		//System.out.println("saveGameRecord to file: " + gameRecordFileName);

		String mapName = MyBotModule.Broodwar.mapFileName();
		mapName = mapName.replace(' ', '_');
		String enemyName = MyBotModule.Broodwar.enemy().getName();
		enemyName = enemyName.replace(' ', '_');
		String myName = MyBotModule.Broodwar.self().getName();
		myName = myName.replace(' ', '_');

		/// 이번 게임에 대한 기록
		GameRecord thisGameRecord = new GameRecord();
		thisGameRecord.mapName = mapName;
		thisGameRecord.myName = myName;
		thisGameRecord.myRace = MyBotModule.Broodwar.self().getRace().toString();
		thisGameRecord.enemyName = enemyName;
		thisGameRecord.enemyRace = MyBotModule.Broodwar.enemy().getRace().toString();
		thisGameRecord.enemyRealRace = InformationManager.Instance().enemyRace.toString();
		thisGameRecord.gameFrameCount = MyBotModule.Broodwar.getFrameCount();
		if (isWinner) {
			thisGameRecord.myWinCount = 1;
			thisGameRecord.myLoseCount = 0;
		} else {
			thisGameRecord.myWinCount = 0;
			thisGameRecord.myLoseCount = 1;
		}
		// 이번 게임 기록을 전체 게임 기록에 추가
		gameRecordList.add(thisGameRecord);

		// 전체 게임 기록 write
		StringBuilder ss = new StringBuilder();
		for (GameRecord gameRecord : gameRecordList) {
			ss.append(gameRecord.mapName + " ");
			ss.append(gameRecord.myName + " ");
			ss.append(gameRecord.myRace + " ");
			ss.append(gameRecord.myWinCount + " ");
			ss.append(gameRecord.myLoseCount + " ");
			ss.append(gameRecord.enemyName + " ");
			ss.append(gameRecord.enemyRace + " ");
			ss.append(gameRecord.enemyRealRace + " ");
			ss.append(gameRecord.gameFrameCount + "\n");
		}

		Common.overwriteToFile(gameRecordFileName, ss.toString());
	}

	/// 이번 게임 중간에 상시적으로 로그를 저장합니다
	void saveGameLog() {

		// 100 프레임 (5초) 마다 1번씩 로그를 기록합니다
		// 참가팀 당 용량 제한이 있고, 타임아웃도 있기 때문에 자주 하지 않는 것이 좋습니다
		// 로그는 봇 개발 시 디버깅 용도로 사용하시는 것이 좋습니다
		if (MyBotModule.Broodwar.getFrameCount() % 100 != 0) {
			return;
		}

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameLogFileName = "bwapi-data\\write\\makjangBot_LastGameLog.dat";

		String mapName = MyBotModule.Broodwar.mapFileName();
		mapName = mapName.replace(' ', '_');
		String enemyName = MyBotModule.Broodwar.enemy().getName();
		enemyName = enemyName.replace(' ', '_');
		String myName = MyBotModule.Broodwar.self().getName();
		myName = myName.replace(' ', '_');

		StringBuilder ss = new StringBuilder();
		ss.append(mapName + " ");
		ss.append(myName + " ");
		ss.append(MyBotModule.Broodwar.self().getRace().toString() + " ");
		ss.append(enemyName + " ");
		ss.append(InformationManager.Instance().enemyRace.toString() + " ");
		ss.append(MyBotModule.Broodwar.getFrameCount() + " ");
		ss.append(MyBotModule.Broodwar.self().supplyUsed() + " ");
		ss.append(MyBotModule.Broodwar.self().supplyTotal() + "\n");

		Common.appendTextToFile(gameLogFileName, ss.toString());
	}

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////

}