import bwapi.UnitType;

public class SWEnemyTerranStrategy extends SWEnemyStrategy{

    public static final SWEnemyStrategy TERRAN_2_Rax_MnM = new SWEnemyTerranStrategy();
    public static final SWEnemyStrategy TERRAN_3_Rax_MnM = new SWEnemyTerranStrategy();
    
    public static final SWEnemyStrategy TERRAN_BBS = new SWEnemyTerranStrategy();
    
    public static final SWEnemyStrategy TERRAN_Three_Factory_Vultures = new SWEnemyTerranStrategy();
    
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

	
	public void executeMidStrategy(int factorys, int starports) {
		// TODO Auto-generated method stub
		
	}




}
