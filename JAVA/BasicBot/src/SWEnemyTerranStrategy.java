import bwapi.UnitType;

public class SWEnemyTerranStrategy extends SWEnemyStrategy{

	// Rush
    public static final SWEnemyStrategy TERRAN_2_Rax_MnM = new SWEnemyTerranStrategy();
    public static final SWEnemyStrategy TERRAN_3_Rax_MnM = new SWEnemyTerranStrategy();
    
    // Cheese
    public static final SWEnemyStrategy TERRAN_BBS = new SWEnemyTerranStrategy();
    
    // Expansion
    public static final SWEnemyStrategy TERRAN_1_Rax_FE = new SWEnemyTerranStrategy();
    
    // Tech
    public static final SWEnemyStrategy TERRAN_Three_Factory_Vultures = new SWEnemyTerranStrategy();
	
	public static SWEnemyStrategy detectStrategy() {
		int seconds = MyBotModule.Broodwar.getFrameCount()/ 30;
        int barracks = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Terran_Barracks);
        int bases = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Terran_Command_Center);
        int factories = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Terran_Factory);
        int bunkers = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Terran_Bunker);
        int marines = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Terran_Marine);
        int medics = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Terran_Medic);
        
        // === Cheese ==============================================
        
        if (barracks >= 3 && seconds < 350) {
            return SWEnemyTerranStrategy.TERRAN_3_Rax_MnM;
        }
        
        if (barracks >= 2 && seconds < 200) {
            return SWEnemyTerranStrategy.TERRAN_BBS;
        }

        // === Expansion ===========================================
        
        if (bases >= 2 && factories >= 1 && seconds < 300) {
            return SWEnemyTerranStrategy.TERRAN_1_Rax_FE;
        }

        // === Rush ================================================
        
        if (barracks >= 2 && seconds < 350) {
            return SWEnemyTerranStrategy.TERRAN_2_Rax_MnM;
        }
        
        // =========================================================
        
		return null;
	}

}
