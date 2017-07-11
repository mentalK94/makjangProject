import java.util.ArrayList;
import java.util.Random;

import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class NDHStrategyManager {

	private static NDHStrategyManager instance = new NDHStrategyManager();

	/// static singleton 객체를 리턴합니다
	public static NDHStrategyManager Instance() {
		return instance;
	}

	public void executeBachanicUnitTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (StrategyManager.Instance().isInitialBuildOrderFinished == false) {
			return;
		}

		// 탱크부터 생산 by NDH
		ArrayList<Unit> barracksList = new ArrayList<>(); // 중복 서치를 막기 위해 배럭 리스트를 첫 서치 때 보관 by NDH
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getType() == UnitType.Terran_Factory) {
				if (unit.isTraining() == false) {
					if (MyBotModule.Broodwar.self().minerals() >= 150 && MyBotModule.Broodwar.self().gas() >= 100 && MyBotModule.Broodwar.self().supplyUsed() < 390) {
						unit.build(UnitType.Terran_Siege_Tank_Tank_Mode);
					}
				}
			} else if (unit.getType() == UnitType.Terran_Barracks) {
				barracksList.add(unit);
			}
		}

		for (Unit unit : barracksList) {
			if (unit.isTraining() == false) {
				if (MyBotModule.Broodwar.self().minerals() >= 50 && MyBotModule.Broodwar.self().gas() >= 25 && MyBotModule.Broodwar.self().supplyUsed() < 390) {
					Random random = new Random();

					// 마린 메딕을 3:1 비율로 생산 by NDH
					if (random.nextInt(4) % 4 == 0)
						unit.build(UnitType.Terran_Medic);
					else
						unit.build(UnitType.Terran_Marine);
				}
			}
		}
	}
}
