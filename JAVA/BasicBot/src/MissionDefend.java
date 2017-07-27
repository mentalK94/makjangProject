import bwapi.Position;
import bwapi.Unit;
import bwta.BaseLocation;

public class MissionDefend {
	
private static MissionDefend instance;
    
    // =========================================================

    protected MissionDefend(String name) {
//        super(name);
        instance = this;
    }
	
	public Position getFocusPoint() {
        
        // === Focus enemy attacking the main base =================
        
        BaseLocation mainBase = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
        if (mainBase != null) {
//            Unit nearEnemy = SWSelect.enemy().combatUnits().nearestTo(mainBase);
        	Unit nearEnemy = SWSelect.enemyCombatUnits().nearestTo(mainBase);
            
            if (nearEnemy != null) {
                return nearEnemy.getPosition();
            }
        }

        // === Return position near the choke point ================
        
        if (InformationManager.Instance().getOccupiedBaseLocations(MyBotModule.Broodwar.self()).size() <= 1) {
            return  InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()).getCenter();
        }
        else {
        	return InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self()).getCenter();
        }
    }
    
    // =========================================================
    
    public static MissionDefend getInstance() {
        return instance;
    }

}
