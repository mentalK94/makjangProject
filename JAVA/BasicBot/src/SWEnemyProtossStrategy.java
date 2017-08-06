import bwapi.UnitType;

public class SWEnemyProtossStrategy extends SWEnemyStrategy{

//    public static final SWEnemyStrategy PROTOSS_2_Gate = new SWEnemyProtossStrategy();
//    public static final SWEnemyStrategy PROTOSS_3_Gate = new SWEnemyProtossStrategy();
    public static final SWEnemyStrategy PROTOSS_2_Gate_DT = new SWEnemyProtossStrategy();
	
    private static SWEnemyProtossStrategy instance = new SWEnemyProtossStrategy();
    
    /// static singleton 객체를 리턴합니다
    public static SWEnemyProtossStrategy Instance() {
    	return instance;
    }
    
	public static SWEnemyStrategy detectStrategy() {
//		int seconds = MyBotModule.Broodwar.getFrameCount()/ 30;
//        int gateways = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Gateway);
        int citadel = MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Citadel_of_Adun);

        if (citadel >= 1) {
            return SWEnemyProtossStrategy.PROTOSS_2_Gate_DT;
        }

//        if (gateways >= 3) {
//            return SWEnemyProtossStrategy.PROTOSS_3_Gate;
//        }
//
//        if (gateways == 2) {
//            return SWEnemyProtossStrategy.PROTOSS_2_Gate;
//        }

		return null;
	}

	public void executeMidStrategy(int factorys) {
		// TODO Auto-generated method stub
		
	}


}
