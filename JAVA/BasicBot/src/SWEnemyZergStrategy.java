import bwapi.UnitType;

public class SWEnemyZergStrategy extends SWEnemyStrategy{

	// Rush
    public static final SWEnemyStrategy ZERG_9_Pool = new SWEnemyZergStrategy();
    
    // Cheese
    public static final SWEnemyStrategy ZERG_4_Pool = new SWEnemyZergStrategy();
    public static final SWEnemyStrategy ZERG_5_Pool = new SWEnemyZergStrategy();
    public static final SWEnemyStrategy ZERG_6_Pool = new SWEnemyZergStrategy();
    
    // Expansion
    public static final SWEnemyStrategy ZERG_3_Hatch_Before_Pool = new SWEnemyZergStrategy();
    
    // Tech
    public static final SWEnemyStrategy ZERG_1_Hatch_Lurker = new SWEnemyZergStrategy();
    public static final SWEnemyStrategy ZERG_2_Hatch_Lurker = new SWEnemyZergStrategy();
    public static final SWEnemyStrategy ZERG_13_Pool_Muta = new SWEnemyZergStrategy();
	
	public static SWEnemyStrategy detectStrategy() {
		int seconds = MyBotModule.Broodwar.getFrameCount()/ 30;
        int bases = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Hatchery);
        int lair = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Lair);
        int pool = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Spawning_Pool);
        int extractor = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Extractor);
        int spires = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Spire);
        int hydraliskDen = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Hydralisk_Den);
        int drones = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Drone);
        int lings = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Zerg_Zergling);
        
        // === Expansion ===========================================
        
        if (pool == 0 && bases >= 3 && seconds <= 350) {
            return ZERG_3_Hatch_Before_Pool;
        }
        
        // === Tech ================================================
        
        if (extractor >= 1 && hydraliskDen == 0 && bases >= 2 && drones >= 12 || spires >= 1) {
            return ZERG_13_Pool_Muta;
        }
        
        if (extractor >= 1 && pool >= 1 && lair >= 1 && bases < 2) {
            return ZERG_1_Hatch_Lurker;
        }
        
        if (extractor >= 1 && pool >= 1 && lair >= 1 && bases >= 2) {
            return ZERG_2_Hatch_Lurker;
        }
        
        // === Cheese ==============================================
        
        if (pool == 1 && drones <= 4 && seconds < 120) {
            return SWEnemyZergStrategy.ZERG_4_Pool;
        }
        
        if (pool == 1 && drones <= 5 && seconds < 140) {
            return SWEnemyZergStrategy.ZERG_5_Pool;
        }
        
        if (pool == 1 && drones <= 6 && seconds < 160) {
            return SWEnemyZergStrategy.ZERG_6_Pool;
        }
        
        // === Rushes ==============================================
        
        if (pool == 1 && drones <= 10 && seconds < 220) {
            return SWEnemyZergStrategy.ZERG_9_Pool;
        }
        
        // =========================================================
        
        return null;
	}

}
