import java.util.ArrayList;
import java.util.Random;

import bwapi.Unit;
import bwapi.UnitType;

public class SWEnemyTerranStrategy extends SWEnemyStrategy{

    public static final SWEnemyStrategy TERRAN_2_Rax_MnM = new SWEnemyTerranStrategy();
    public static final SWEnemyStrategy TERRAN_3_Rax_MnM = new SWEnemyTerranStrategy();
    public static final SWEnemyStrategy TERRAN_BBS = new SWEnemyTerranStrategy();
    public static final SWEnemyStrategy TERRAN_Three_Factory_Vultures = new SWEnemyTerranStrategy();
    
    public static int needBarrackNum = 0; 
    public static int needFactoryNum = 0; 
    public static int needStarportNum = 0; 
    
    private static SWEnemyTerranStrategy instance = new SWEnemyTerranStrategy();
    
    /// static singleton 객체를 리턴합니다
    public static SWEnemyTerranStrategy Instance() {
    	return instance;
    }
	
	public static SWEnemyStrategy detectStrategy() {
		int seconds = MyBotModule.Broodwar.getFrameCount()/ 30;
		int bases = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Terran_Command_Center);
        int barracks = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Terran_Barracks);
        int factories = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Terran_Factory);
        
        if (barracks >= 2 && seconds < 200) {
            return SWEnemyTerranStrategy.TERRAN_BBS;
        }

        if (barracks >= 2 && seconds < 350) {
            return SWEnemyTerranStrategy.TERRAN_2_Rax_MnM;
        }
        
        if (barracks >= 3 && seconds < 350) {
            return SWEnemyTerranStrategy.TERRAN_3_Rax_MnM;
        }
        
		return null;
	}

	public void executeMidStrategy(ArrayList<Unit> factoryList) {
		needFactoryNum = 2;
		needStarportNum = 1;
		
		int starportCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Starport);
		if(starportCount == 0){
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, true);
		}
		
		for (int i = 0; i < needFactoryNum - factoryList.size(); i++) {
      	  	BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, true);
        }
		
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getType() == UnitType.Terran_Starport) {
				if (unit.isTraining() == false) {
					if (MyBotModule.Broodwar.self().minerals() >= 150 && MyBotModule.Broodwar.self().gas() >= 100 && MyBotModule.Broodwar.self().supplyUsed() < 390) {
						unit.build(UnitType.Terran_Wraith);
					}
				}
			} else if (unit.getType() == UnitType.Terran_Factory) {
				factoryList.add(unit);
			}
		}

		for (Unit unit : factoryList) {
			if (unit.isTraining() == false) {
				if (MyBotModule.Broodwar.self().minerals() >= 150 && MyBotModule.Broodwar.self().gas() >= 100 && MyBotModule.Broodwar.self().supplyUsed() < 390) {
					Random random = new Random();

					// 탱크 골리앗을 1:1 비율로 생산 by SW
					if (random.nextInt(2) % 2 == 0)
						unit.build(UnitType.Terran_Siege_Tank_Tank_Mode);
					else
						unit.build(UnitType.Terran_Goliath);
				}
			}
		}
	}

}
