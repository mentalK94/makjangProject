import bwapi.UnitType;

public class SWEnemyZergStrategy extends SWEnemyStrategy{

	public static final SWEnemyStrategy ZERG_4_Pool = new SWEnemyZergStrategy();
	public static final SWEnemyStrategy ZERG_Lurker = new SWEnemyZergStrategy();
	public static final SWEnemyStrategy ZERG_13_Pool_Muta = new SWEnemyZergStrategy();
    
//    public static final SWEnemyStrategy ZERG_1_Hatch_Lurker = new SWEnemyZergStrategy();
//    public static final SWEnemyStrategy ZERG_2_Hatch_Lurker = new SWEnemyZergStrategy();
	
	private static SWEnemyZergStrategy instance = new SWEnemyZergStrategy();
    
    /// static singleton 객체를 리턴합니다
    public static SWEnemyZergStrategy Instance() {
    	return instance;
    }
	
	public static SWEnemyStrategy detectStrategy() {
		int seconds = MyBotModule.Broodwar.getFrameCount()/ 30;
        int bases = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Hatchery);
        int lair = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Lair);
        int pool = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Spawning_Pool);
        int extractor = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Extractor);
        int spires = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Spire);
        int hydraliskDen = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Hydralisk_Den);
        int drones = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Drone);
        
        if (pool == 1 && drones <= 4 && seconds < 120) {
        	return SWEnemyZergStrategy.ZERG_4_Pool;
        }
        if (extractor >= 1 && pool >= 1 && lair >= 1) {
        	return ZERG_Lurker;
        }
        if (extractor >= 1 && hydraliskDen == 0 && bases >= 2 && drones >= 12 || spires >= 1) {
            return ZERG_13_Pool_Muta;
        }
        
//        if (extractor >= 1 && pool >= 1 && lair >= 1 && bases < 2) {
//            return ZERG_1_Hatch_Lurker;
//        }
        
//        if (extractor >= 1 && pool >= 1 && lair >= 1 && bases >= 2) {
//            return ZERG_2_Hatch_Lurker;
//        }
        
        return null;
	}

	public void executeMidStrategy(int factorys, int barracks) {
		// TODO Auto-generated method stub
		
	}

}
