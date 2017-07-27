import bwapi.UnitType;

public class SWEnemyProtossStrategy extends SWEnemyStrategy{

	// Rush
    public static final SWEnemyStrategy PROTOSS_2_Gate = new SWEnemyProtossStrategy();
    
    // Cheese
    public static final SWEnemyStrategy PROTOSS_3_Gate = new SWEnemyProtossStrategy();
    
    // Expansion
    public static final SWEnemyStrategy PROTOSS_12_Nexus = new SWEnemyProtossStrategy();
    
    // Tech
    public static final SWEnemyStrategy PROTOSS_2_Gate_DT = new SWEnemyProtossStrategy();
    public static final SWEnemyStrategy PROTOSS_Carrier_Push = new SWEnemyProtossStrategy();
	
	public static SWEnemyStrategy detectStrategy() {
		int seconds = MyBotModule.Broodwar.getFrameCount()/ 30;
        int gateways = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Gateway);
        int nexus = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Nexus);
        int citadel = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Citadel_of_Adun);

        // === Dark Templar ========================================
        
        if (citadel >= 1 && seconds < 320) {
            return SWEnemyProtossStrategy.PROTOSS_2_Gate_DT;
        }

        // === Three Gateway =======================================
        
        if (gateways >= 3 && seconds < 300) {
            return SWEnemyProtossStrategy.PROTOSS_3_Gate;
        }

        // === Two Gateway =========================================
        
        if (gateways == 2 && seconds < 290) {
            return SWEnemyProtossStrategy.PROTOSS_2_Gate;
        }

        // === 12 Nexus ============================================
        
        if (nexus == 2 && seconds < 290) {
            return SWEnemyProtossStrategy.PROTOSS_12_Nexus;
        }
        
        // === Carrier Push ========================================
        
        int cannons = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Photon_Cannon);
        if (cannons >= 1 && nexus >= 2) {
            return PROTOSS_Carrier_Push;
        }
        
        // =========================================================
		
		return null;
	}


}
