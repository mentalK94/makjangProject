import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class SWRequests {

	private static SWRequests instance = null;

	public static SWRequests getInstance(){
		if(instance == null){
			if(MyBotModule.Broodwar.self().getRace() == Race.Terran){
				instance = new TerranRequests();
			}
		}
		return instance;
	}
	
	public void requestDefBuildingAntiLand(Position where){
		UnitType building = Config.DEF_BUILDING_ANTI_LAND;
		Position nearTo = where;
		
		Unit previousBuilding = SWSelect.ourBuildingsIncludingUnfinished().ofType(building).first();
				
		if(where == null){
			if(previousBuilding != null){
				nearTo = previousBuilding.getPosition();
			} else {
				nearTo = null;
			}
		}
		
		ConstructionManager.Instance().addConstructionTask(building, nearTo.toTilePosition());
	}

	public void requestDetectorQuick(Position where) {
		UnitType building = null;
		if(MyBotModule.Broodwar.self().getRace() == Race.Terran){
			building = Config.DEF_BUILDING_ANTI_LAND;
		}else{
			return;
		}
		
//		int antiAirBuilding = InformationManager.Instance().getNumUnits(building, MyBotModule.Broodwar.self());
		
		int requireParents = InformationManager.Instance().getNumUnits(UnitType.Terran_Engineering_Bay, MyBotModule.Broodwar.self()) ;
		if(requireParents == 0){
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay, true);
			return;
		}
		
		// === Protect every base ==========================================
      
      for (BaseLocation base : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer)) {
          int numberOfAntiAirBuildingsNearBase = SWConstructionManager.countExistingAndPlannedConstructionsInRadius(
                  building, 8, base.getPosition()
          );
          
          for (int i = 0; i < 2 - numberOfAntiAirBuildingsNearBase; i++) {
        	  ConstructionManager.Instance().addConstructionTask(building, base.getPosition().toTilePosition());
          }
      }
      
      // === Protect choke point =========================================

      if (where == null) {
          Unit nearestBunker = SWSelect.ourOfType(UnitType.Terran_Bunker).nearestTo(MissionDefend.getInstance().getFocusPoint());
          if (nearestBunker != null) {
              where = nearestBunker.getPosition();
          }
      }
      
//      if (where == null) {
//          where = MissionDefend.getInstance().getFocusPoint().translateTowards(AMap.getNaturalBaseLocation(), 32);
//      }
      
      int numberOfDetectors = SWConstructionManager.countExistingAndPlannedConstructionsInRadius(building, 8, where);

      for (int i = 0; i < 2 - numberOfDetectors; i++) {
    	  ConstructionManager.Instance().addConstructionTask(building, where.getPoint().toTilePosition());
      }
		
		
	}

	
	/**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
	public void requestAntiAirQuick(Position where) {
		UnitType building = Config.DEFENSIVE_BUILDING_ANTI_AIR;
		
//        int antiAirBuildings = SWConstructionManager.countExistingAndPlannedConstructions(building);

        // === Ensure parent exists ========================================
        
        int requiredParents = SWConstructionManager.countExistingAndPlannedConstructions(UnitType.Terran_Engineering_Bay);
        if (requiredParents == 0) {
        	BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay, true);
            return;
        }

        // === Protect every base ==========================================
        
        for (BaseLocation base : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer)) {
            int numberOfAntiAirBuildingsNearBase = SWConstructionManager.countExistingAndPlannedConstructionsInRadius(building, 8, base.getPosition());
            
            for (int i = 0; i < 2 - numberOfAntiAirBuildingsNearBase; i++) {
            	 ConstructionManager.Instance().addConstructionTask(building, base.getPosition().toTilePosition());
            }
        }
		
	}
	
	
	public void requestDefBuildingAntiAir(Position where) {
        UnitType building = Config.DEFENSIVE_BUILDING_ANTI_AIR;
        Position nearTo = where;
        
        ConstructionManager.Instance().addConstructionTask(building, nearTo.toTilePosition());
    }
}
