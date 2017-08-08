import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice.Info;

import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class SwStrategyManager {

	private static SwStrategyManager instance = new SwStrategyManager();

	private CommandUtil commandUtil = new CommandUtil();

	private boolean isFullScaleAttackStarted;
	private boolean isInitialBuildOrderFinished;
	private boolean isFirstExpasionFinished;
	private boolean isAllBaseLocationVisited;
	private int limitedSupplyUsed;
	
	private UnitType basicDefenseBuildingType;

	private Position bunkerPosition;

	/// static singleton 객체를 리턴합니다
	public static SwStrategyManager Instance() {
		return instance;
	}

	public SwStrategyManager() {
		isFullScaleAttackStarted = false;
		isInitialBuildOrderFinished = false;
		isFirstExpasionFinished = false;
		isAllBaseLocationVisited = false;
		limitedSupplyUsed = 200;
	}

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {
		setInitialBuildOrder();		
	}

	///  경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {

	}

	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public void update() {
		if (BuildManager.Instance().buildQueue.isEmpty()) {
			isInitialBuildOrderFinished = true;
		}

		executeWorkerTraining();

		executeSupplyManagement();

		executeBasicCombatUnitTraining();

		executeCombat();
	}

	public void setInitialBuildOrder() {
		 if (MyBotModule.Broodwar.self().getRace() == Race.Terran) {
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			// SupplyUsed가 8 일때 서플라이 빌드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

			
			
			// SupplyUsed가 10 일때 배럭 빌드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			// SupplyUsed가 12 일때 가스 리파이너리 빌드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getRefineryBuildingType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

			
			
			// SupplyUsed가 15 일때 서플라이 빌드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

			
						
			// SupplyUsed가 20 일때 팩토리
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory);
			//2017-07-10
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode, false);			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode, false);
			// SupplyUsed가 24 일때 서플라이 빌드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Tank_Siege_Mode, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			
			
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);			
			// 시즈탱크모드로 전환
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Siege_Mode, false);
			// 애드온과 동시에 배럭 추가 빌드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			
			
			
			// 아카데미 건설
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Academy);			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			// SupplyUsed가 31 일때 서플라이 빌드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Medic);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			
			
						
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Medic);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Medic);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);		
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			// 마린 스팀팩
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Stim_Packs, false);

			
			
			
			// SupplyUsed가 35 일때 서플라이 빌드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			
			
			// 마린 사정거리 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.U_238_Shells, false);
			// 메딕
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Optical_Flare, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Restoration, false);
			// 메딕 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Caduceus_Reactor, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Medic);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			// SupplyUsed가 43일때 서플라이 빌드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			
			
			
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
//					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
//					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Medic);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
//					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
//					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			
//			
//			
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
//					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			// SupplyUsed가 52일때 서플라이 빌드
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(
//					InformationManager.Instance().getBasicSupplyProviderUnitType(),
//					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			
			/*
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine);

			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Comsat_Station);

			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Firebat);

			// 지상유닛 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Infantry_Weapons, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Infantry_Armor, false);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Missile_Turret);

			
			// 벌쳐 스파이더 마인
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spider_Mines, false);
			// 벌쳐 이동속도 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Ion_Thrusters, false);
			

			// 벌쳐
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture);

			// 시즈탱크
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Tank_Siege_Mode, false);

			// 아머니
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory);
			// 지상 메카닉 유닛 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
			// 공중 유닛 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Ship_Plating, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Ship_Weapons, false);
			// 골리앗 사정거리 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Charon_Boosters, false);

			// 골리앗
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath);

			// 스타포트
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Control_Tower);
			// 레이쓰 클러킹
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Cloaking_Field, false);
			// 레이쓰 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Apollo_Reactor, false);

			// 레이쓰
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Wraith);

			// 발키리
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Valkyrie);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center);

			// 사이언스 퍼실리티
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility);
			// 사이언스 베슬 - 기술
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Irradiate, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.EMP_Shockwave, false);
			// 사이언스 베슬 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Titan_Reactor, false);

			// 사이언스 베슬
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Vessel);
			// 사이언스 퍼실리티 - 배틀크루저 생산 가능
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Physics_Lab);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Yamato_Gun, false);
			// 배틀크루저 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Colossus_Reactor, false);
			// 배틀크루저
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Battlecruiser);

			// 사이언스 퍼실리티 - 고스트 생산 가능
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Covert_Ops);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Lockdown, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Personnel_Cloaking, false);
			// 고스트 가시거리 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Ocular_Implants, false);
			// 고스트 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Moebius_Reactor, false);

			// 고스트
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Ghost);

			// 핵폭탄
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Nuclear_Silo);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Nuclear_Missile);
			*/
		} 
	}

	// 일꾼 계속 추가 생산
	public void executeWorkerTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			// workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());

			if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType() == UnitType.Zerg_Egg) {
						// Zerg_Egg 에게 morph 명령을 내리면 isMorphing = true,
						// isBeingConstructed = true, isConstructing = true 가 된다
						// Zerg_Egg 가 다른 유닛으로 바뀌면서 새로 만들어진 유닛은 잠시
						// isBeingConstructed = true, isConstructing = true 가
						// 되었다가,
						if (unit.isMorphing() && unit.getBuildType() == UnitType.Zerg_Drone) {
							workerCount++;
						}
					}
				}
			} else {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining()) {
							workerCount += unit.getTrainingQueue().size();
						}
					}
				}
			}

			if (workerCount < 30) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining() == false || unit.getLarva().size() > 0) {
							// 빌드큐에 일꾼 생산이 1개는 있도록 한다
							if (BuildManager.Instance().buildQueue
									.getItemCount(InformationManager.Instance().getWorkerType(), null) == 0) {
								// std.cout << "worker enqueue" << std.endl;
								BuildManager.Instance().buildQueue.queueAsLowestPriority(
										new MetaType(InformationManager.Instance().getWorkerType()), false);
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

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}

		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
		if (MyBotModule.Broodwar.self().supplyTotal() <= 400) {

			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
			int supplyMargin = 12;

			// currentSupplyShortage 를 계산한다
			int currentSupplyShortage = MyBotModule.Broodwar.self().supplyUsed() + supplyMargin - MyBotModule.Broodwar.self().supplyTotal();

			if (currentSupplyShortage > 0) {
				
				// 생산/건설 중인 Supply를 센다
				int onBuildingSupplyCount = 0;

				// 저그 종족인 경우, 생산중인 Zerg_Overlord (Zerg_Egg) 를 센다. Hatchery 등 건물은 세지 않는다
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
				// 저그 종족이 아닌 경우, 건설중인 Protoss_Pylon, Terran_Supply_Depot 를 센다. Nexus, Command Center 등 건물은 세지 않는다
				else {
					onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(
							InformationManager.Instance().getBasicSupplyProviderUnitType(), null)
							* InformationManager.Instance().getBasicSupplyProviderUnitType().supplyProvided();
				}

				//System.out.println("currentSupplyShortage : " + currentSupplyShortage + " onBuildingSupplyCount : " + onBuildingSupplyCount);

				if (currentSupplyShortage > onBuildingSupplyCount) {
					
					// BuildQueue 최상단에 SupplyProvider 가 있지 않으면 enqueue 한다
					boolean isToEnqueue = true;
					if (!BuildManager.Instance().buildQueue.isEmpty()) {
						BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
						if (currentItem.metaType.isUnit() 
							&& currentItem.metaType.getUnitType() == InformationManager.Instance().getBasicSupplyProviderUnitType()) 
						{
							isToEnqueue = false;
						}
					}
					if (isToEnqueue) {
						System.out.println("enqueue supply provider "
								+ InformationManager.Instance().getBasicSupplyProviderUnitType());
						BuildManager.Instance().buildQueue.queueAsHighestPriority(
								new MetaType(InformationManager.Instance().getBasicSupplyProviderUnitType()), true);
					}
				}
			}
		}
	}

	public void executeBasicCombatUnitTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 기본 병력 추가 훈련
		if (MyBotModule.Broodwar.self().minerals() >= 200 && MyBotModule.Broodwar.self().supplyUsed() < 390) {
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == InformationManager.Instance().getBasicCombatBuildingType()) {
					if (unit.isTraining() == false || unit.getLarva().size() > 0) {
						if (BuildManager.Instance().buildQueue
								.getItemCount(InformationManager.Instance().getBasicCombatUnitType(), null) == 0) {
							BuildManager.Instance().buildQueue.queueAsLowestPriority(
									InformationManager.Instance().getBasicCombatUnitType(),
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
		}
	}

	public void executeCombat() {

		// 공격 모드가 아닐 때에는 전투유닛들을 아군 진영 길목에 집결시켜서 방어
		if (isFullScaleAttackStarted == false) {
			Chokepoint firstChokePoint = BWTA.getNearestChokepoint(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition());
			Chokepoint secondChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
			BaseLocation baseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
			Position tankPosition = null;
			Unit bunker = null;
			Unit marine = null;
			Unit medic = null;
			Unit tank = null;

			tankPosition = findTankPosition(secondChokePoint, baseLocation);
			
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {

				// 벙커의 위치를 저장
				if (unit.getType() == UnitType.Terran_Bunker && unit.isCompleted()) {
					bunker = unit;
				}
				// 마린의 경우 벙커에 들어가있거나 주위에 있도록
				if (unit.getType() == UnitType.Terran_Marine && unit.isIdle()){
					marine = unit;
					
					if (bunker != null && bunker.getSpaceRemaining() != 0){
						commandUtil.rightClick(marine, bunker);
					}else {
//						commandUtil.attackMove(marine, secondChokePoint.getPoint());
						commandUtil.attackMove(marine, firstChokePoint.getPoint());
					}
					
				// 메딕의 경우 마린근처에 머물도록
				}else if(unit.getType() == UnitType.Terran_Medic && unit.isIdle()) {
					medic = unit;
					if(marine != null){
						commandUtil.move(medic, marine.getPosition());
					}else{
						commandUtil.move(medic, firstChokePoint.getPoint());
					}
					
				// 시즈탱크는 첫번째길목에서 시즈모드로 대기
				}else if(unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode){
					tank = unit;
					commandUtil.attackMove(tank, tankPosition);
					
					if(tank.getPoint().getDistance(tankPosition) < 50){
						tank.useTech(TechType.Tank_Siege_Mode);
					}
				}
			}

			// 전투 유닛이 15개 이상 생산되었고, 적군 위치가 파악되었으면 총공격 모드로 전환
			//2017-07-05
			if ((MyBotModule.Broodwar.self().completedUnitCount(InformationManager.Instance().getBasicCombatUnitType()) 
					+ MyBotModule.Broodwar.self().completedUnitCount(InformationManager.Instance().getAdvancedCombatUnitType())) > 12) {
				if (InformationManager.Instance().enemyPlayer != null
					&& InformationManager.Instance().enemyRace != Race.Unknown  
					&& InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) {				
					isFullScaleAttackStarted = true;
				}
			}
		}
		// 공격 모드가 되면, 모든 전투유닛들을 적군 Main BaseLocation 로 공격 가도록 합니다
		else {
			//std.cout << "enemy OccupiedBaseLocations : " << InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance()._enemy).size() << std.endl;
			
			Chokepoint secondChokePointEnemey = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer);
			
			if (InformationManager.Instance().enemyPlayer != null
					&& InformationManager.Instance().enemyRace != Race.Unknown 
					&& InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) 
			{					
				// 공격 대상 지역 결정
				BaseLocation targetBaseLocation = null;
				double closestDistance = 100000000;

				for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer)) {
					double distance = BWTA.getGroundDistance(
						InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition(), 
						baseLocation.getTilePosition());

					if (distance < closestDistance) {
						closestDistance = distance;
						targetBaseLocation = baseLocation;
					}
				}
				
				// 2017-07-10
				boolean isOkAttackBaseLocation = false;
				
				if (targetBaseLocation != null) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						// 건물은 제외
						if (unit.getType().isBuilding()) {
							continue;
						}
						// 모든 일꾼은 제외
						if (unit.getType().isWorker()) {
							continue;
						}
						// 탱크 시즈모드 해제
						if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode){
							unit.unsiege();
						}
						if (unit.canAttack() || unit.canRepair()) {
							if(unit.isIdle()){
								if(!isOkAttackBaseLocation){
									commandUtil.attackMove(unit, secondChokePointEnemey.getPoint());
									if(MyBotModule.Broodwar.getUnitsInRadius(secondChokePointEnemey.getPoint(), 250).size() > 12){
										isOkAttackBaseLocation = true;
										System.out.println("isOkAttackBaseLocation = true");
									}else{
										isOkAttackBaseLocation = false;
										System.out.println("isOkAttackBaseLocation = false");
										commandUtil.attackMove(unit, secondChokePointEnemey.getPoint());
									}
								}else{
									commandUtil.attackMove(unit, targetBaseLocation.getPoint());
								}
							}
						} 
					}
				}
			}
		}
	}

	private Position findTankPosition(Chokepoint secondChokePoint, BaseLocation mainBaseLocation) {
		
		int vx, vy;
		double d, t;
		int bx, by;
		Position position;
		
		// 입구막는 위치
//		vx = firstChokePoint.getCenter().getX() - secondChokePoint.getCenter().getX();
//		vy = (firstChokePoint.getCenter().getY() - secondChokePoint.getCenter().getY()) * (-1);
//		d = Math.sqrt(vx * vx + vy * vy) * 4; // BaseLocation 와 ChokePoint 간 거리보다 조금 짧은 거리로 조정. BaseLocation가 있는 Region은 대부분 직사각형 형태이기 때문
//		t = Math.atan2(vy, vx + 0.0001); // 라디안 단위
//		bx = firstChokePoint.getX() + (int)(d * Math.cos(t) / Config.TILE_SIZE);
//		by = firstChokePoint.getY() - (int)(d * Math.sin(t) / Config.TILE_SIZE);
		
		vx = secondChokePoint.getCenter().getX() - mainBaseLocation.getPosition().getX();
		vy = (secondChokePoint.getCenter().getY() - mainBaseLocation.getPosition().getY()) * (-1);
		d = Math.sqrt(vx * vx + vy * vy) * 0.8; // BaseLocation 와 ChokePoint 간 거리보다 조금 짧은 거리로 조정. BaseLocation가 있는 Region은 대부분 직사각형 형태이기 때문
		t = Math.atan2(vy, vx + 0.0001); // 라디안 단위

		// BaseLocation 에서 ChokePoint 반대쪽 방향의 Back Yard : 데카르트 좌표계에서 (cos(t+180) = -cos(t), sin(t+180) = -sin(t))
		bx = mainBaseLocation.getTilePosition().getX() - (int)(d * Math.cos(t) / Config.TILE_SIZE);
		by = mainBaseLocation.getTilePosition().getY() + (int)(d * Math.sin(t) / Config.TILE_SIZE);
		
		position =  new Position(bx, by);
		
		return position;
	}

	// 상대방 히든유닛, 극초반러쉬, 공중유닛 전략 캐치해서 기본적인 대응까지
	public void analysisEnemyStrategy() {
		System.out.println("111111111111111111 analysisEnemyStrategy start : ");
		
		if(MyBotModule.Broodwar.enemy().getRace().equals(Race.Terran)){
			System.out.println("22222222222222222 analysisEnemyStrategyT start ");
			SWEnemyStrategy detectedStrategy = SWEnemyTerranStrategy.detectStrategy();
			System.out.println("detectedStrategy : " + detectedStrategy);
			if (detectedStrategy != null) {
	            changeEnemyStrategyTo(detectedStrategy);
	        }
		}else if(MyBotModule.Broodwar.enemy().getRace().equals(Race.Protoss)){
			System.out.println("222222222222222 analysisEnemyStrategyP start ");
			SWEnemyStrategy detectedStrategy = SWEnemyProtossStrategy.detectStrategy();
			System.out.println("detectedStrategy : " + detectedStrategy);
			if (detectedStrategy != null) {
	            changeEnemyStrategyTo(detectedStrategy);
	        }
		}else if(MyBotModule.Broodwar.enemy().getRace().equals(Race.Zerg)){
			System.out.println("222222222222222 analysisEnemyStrategyZ start ");
			SWEnemyStrategy detectedStrategy = SWEnemyZergStrategy.detectStrategy();
			System.out.println("detectedStrategy : " + detectedStrategy);
			if (detectedStrategy != null) {
	            changeEnemyStrategyTo(detectedStrategy);
	        }
		}
	}

	private void changeEnemyStrategyTo(SWEnemyStrategy strategy) {
		System.out.println("333333333333333333 changeEnemyStrategyTo start");
		if(!SWEnemyStrategy.isEnemyStrategyKwon()){
			System.out.println("Enemy Strategy : " + strategy);
		}
		SWEnemyStrategy.setEnemyStrategy(strategy);
		SWStrategyResponse.updateEnemyStrategyChanged();
	}

	public void executeExpasionManagemnet() {
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 5초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 120 != 0) {
			return;
		}
		
		// 앞마당 확장
		if (MyBotModule.Broodwar.self().minerals() >= 400 && MyBotModule.Broodwar.self().supplyUsed() < 390 &&
				MyBotModule.Broodwar.self().supplyUsed() > 100 && !isFirstExpasionFinished) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					UnitType.Terran_Comsat_Station, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					UnitType.Terran_Refinery, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					UnitType.Terran_Comsat_Station, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			
			executeDefendBase();
			isFirstExpasionFinished = true;
		}
		
		// 아무도 없는 지역이면 baseList에 담는다
		// 추가확장 이전에 지도 전체 정찰 시작 
		ArrayList<BaseLocation> baseList = SWScoutManager.Instance().allBaseLocationScout();
		
		// 전체 정찰완료 후 확장할곳 선택
		if(isFirstExpasionFinished && isAllBaseLocationVisited){
			
			// 다른본진 확장
			if(MyBotModule.Broodwar.self().minerals() >= 400 && MyBotModule.Broodwar.self().supplyUsed() < 390 &&
					MyBotModule.Broodwar.self().supplyUsed() > limitedSupplyUsed){
				
				BaseLocation nextBaseLocation = baseList.get(0);
				
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(
//						UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(
//						UnitType.Terran_Refinery, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(
//						UnitType.Terran_Comsat_Station, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				
				ConstructionManager.Instance().addConstructionTask(UnitType.Terran_Command_Center, nextBaseLocation.getTilePosition());
				ConstructionManager.Instance().addConstructionTask(UnitType.Terran_Refinery, nextBaseLocation.getTilePosition());
				ConstructionManager.Instance().addConstructionTask(UnitType.Terran_Comsat_Station, nextBaseLocation.getTilePosition());
				
				executeDefendBase();
				
				limitedSupplyUsed += 50;
				isAllBaseLocationVisited = false;
			}
		}
	}

	// 본진 방어전략 
	private void executeDefendBase() {
		int requireParents = InformationManager.Instance().getNumUnits(UnitType.Terran_Engineering_Bay, MyBotModule.Broodwar.self());
		if(requireParents == 0){
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay, true);
		}
		
		int bunkerNumInbase = 0;
		int turretNumInbase = 0;
		// 벙커, 미사일터렛
		for (BaseLocation base : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer)) {
			  List<Unit> units = MyBotModule.Broodwar.getUnitsInRadius(base.getPosition(), 8);	
			  
			  for(Unit u : units){
				  if(u.getType() ==  UnitType.Terran_Bunker) bunkerNumInbase++;
				  if(u.getType() ==  UnitType.Terran_Missile_Turret) turretNumInbase++;
			  }			  
	          
	          for (int i = 0; i < 1 - bunkerNumInbase; i++) {
	        	  ConstructionManager.Instance().addConstructionTask(UnitType.Terran_Bunker, base.getPosition().toTilePosition());
	          }
	          for (int i = 0; i < 3 - turretNumInbase; i++) {
	        	  ConstructionManager.Instance().addConstructionTask(UnitType.Terran_Missile_Turret, base.getPosition().toTilePosition());
	          }
	   }
	}
	
	// 종족별 중후반 전략실시

	public void executeMidStrategy() {
		
		if (isInitialBuildOrderFinished == false) {
			return;
		}
		
		// 현재 기본적인 건설상황 배럭2 팩토리1
		ArrayList<Unit> barrackList = new ArrayList<>(); // 중복 서치를 막기 위해 배럭 리스트를 첫 서치 때 보관 by SW
		ArrayList<Unit> factoryList = new ArrayList<>(); // 중복 서치를 막기 위해 팩토리 리스트를 첫 서치 때 보관 by SW
//		ArrayList<Unit> starportList = new ArrayList<>(); // 중복 서치를 막기 위해  스타포트 리스트를 첫 서치 때 보관 by SW
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getType() == UnitType.Terran_Factory) {
				factoryList.add(unit);
			}else if (unit.getType() == UnitType.Terran_Barracks) {
				barrackList.add(unit);
//			}else if(unit.getType() == UnitType.Terran_Starport){
//				starportList.add(unit);
			}
		}
		
		// 종족별로 대량생산할 유닛선정 및 건물 건설
		if(MyBotModule.Broodwar.enemy().getRace().equals(Race.Terran)){
			// 탱크, 골리앗, 레이쓰
			// 팩토리2, 스타포트1 추가
			SWEnemyTerranStrategy.Instance().executeMidStrategy(factoryList);
			
		} else if(MyBotModule.Broodwar.enemy().getRace().equals(Race.Protoss)){
			// 탱크, 골리앗
			// 팩토리3 추가
			//SWEnemyProtossStrategy.Instance().executeMidStrategy(factoryList);
			
		} else if(MyBotModule.Broodwar.enemy().getRace().equals(Race.Zerg)){
			// 탱크, 마린, 메딕, 파이어뱃
			// 팩토리1 배럭2 추가
			//SWEnemyZergStrategy.Instance().executeMidStrategy(factoryList, barrackList);
		}
		
		// 생산된 유닛들의 랠리포인트 설정 및
		// 공격지점 설정
	}
}
