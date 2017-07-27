
public class SWStrategyResponse {

	public static void updateEnemyStrategyChanged() {
		SWEnemyStrategy enemyStrategy = SWEnemyStrategy.getEnemyStrategy();
		System.out.println("updateEnemyStrategyChanged enemyStrategy:: " + enemyStrategy);
		// === Rush ========================================
        
        if (enemyStrategy.isGoingRush()) {
            handleRushDefense(enemyStrategy);
        }
        
        // === Tech ========================================
        
        if (enemyStrategy.isGoingHiddenUnits()) {
            if (MyBotModule.Broodwar.getFrameCount() % 19 == 0) {
                SWRequests.getInstance().requestDetectorQuick(null);
            }
        }
        
        if (enemyStrategy.isGoingAirUnitsQuickly()) {
            if (MyBotModule.Broodwar.getFrameCount() % 28 == 0) {
                SWRequests.getInstance().requestAntiAirQuick(null);
            }
        }
	}

	private static void handleRushDefense(SWEnemyStrategy enemyStrategy) {
		if(shouldSkipAntiRushDefensiveBuilding(enemyStrategy)){
			return;
		}
		
		int minBunkers =1;
		
		if(enemyStrategy.isGoingCheese()){
			minBunkers = 2;
		}
		
		 SWStrategyInformations.needDefBuildingAntiLandAtLeast(minBunkers);
	}

	private static boolean shouldSkipAntiRushDefensiveBuilding(SWEnemyStrategy enemyStrategy) {
		if(enemyStrategy == null) return  false;
		
		if(ScoutManager.Instance().getScoutUnit() == null){
			return false;
		}
		
		if(!enemyStrategy.isGoingRush() && !enemyStrategy.isGoingCheese()){
			return true;
		}
		
		return false;
	}

}
