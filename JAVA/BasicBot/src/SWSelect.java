import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import bwapi.Position;
import bwapi.PositionedObject;
import bwapi.Unit;
import bwapi.UnitType;

public class SWSelect<T> {

	private List<T> data;

    protected SWSelect(Collection<T> unitsData) {
        data = new ArrayList<>();
        data.addAll(unitsData);
    }
	
    private static List<Unit> ourUnits(){
    	List<Unit> data = new ArrayList<>();
    	
    	for(Unit u : MyBotModule.Broodwar.self().getUnits()){
    		data.add(u);
    	}
    	return data;
    }
    
    private static List<Unit> enemyUnits(){
    	List<Unit> data = new ArrayList<>();
    	for(Unit u : MyBotModule.Broodwar.enemy().getUnits()){
    		data.add(u);
    	}
    	return data;
    }
    
    static SWSelect<Unit> enemyCombatUnits(){
    	List<Unit> data = new ArrayList<>();
    	
    	for(Unit unit : enemyUnits()){
    		if(InformationManager.Instance().isCombatUnitType(unit.getType())){
    			data.add(unit);
    		}
    	}
    	return new SWSelect<Unit>(data);
    }
    
    
    public static SWSelect<Unit> ourBuildingsIncludingUnfinished(){
    	SWSelect<Unit> selectedUnits = SWSelect.ourIncludingUnfinished();
    	for (Iterator<Unit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            Unit unit = unitIter.next();
            if (!unit.getType().isBuilding()) {
                unitIter.remove();
            }
        }
    	return selectedUnits;
    }

	private static SWSelect<Unit> ourIncludingUnfinished() {
		List<Unit> data = new ArrayList<>();
		for(Unit unit : ourUnits()){
			if(!unit.getType().equals(UnitType.Terran_Vulture_Spider_Mine)){
				data.add(unit);
			}
		}
		return new SWSelect<Unit>(data);
	}
	
	public List<T> list() {
        return data;
    }
	
	public List<Unit> listUnits() {
        return (List<Unit>) data;
    }
	
    /**
     * Returns number of units matching all previous conditions.
     */
    public int count() {
        return data.size();
    }

    /**
     * Returns true if there're no units that fullfilled all previous conditions.
     */
    public boolean isEmpty() {
        return data.size() == 0;
    }

    /**
     * Returns number of units matching all previous conditions.
     */
    public int size() {
        return count();
    }
    
//    public SWSelect<?> ofType(UnitType... types) {
//        Iterator<T> unitsIterator = data.iterator();
//        while (unitsIterator.hasNext()) {
//            Object unitOrData = unitsIterator.next();
//            boolean typeMatches = (unitOrData instanceof Unit ? typeMatches((Unit) unitOrData, types) : typeMatches((SWFoggedUnit) unitOrData, types));
//            if (!typeMatches) {
//                unitsIterator.remove();
//            }
//        }
//
//        return this;
//    }
    
//    private boolean typeMatches(Unit needle, UnitType... haystack) {
//        Unit unit = unitFrom(needle);
//
//        for (UnitType type : haystack) {
//            if (unit.getType().equals(type)
//                    || (unit.getType().equals(UnitType.Zerg_Egg) && unit.getBuildType().equals(type))) {
//                return true;
//            }
//        }
//        return false;
//    }
    
//    private boolean typeMatches(SWFoggedUnit needle, UnitType... haystack) {
//        Unit unit = unitFrom(needle);
//
//        for (UnitType type : haystack) {
//            if (needle.getType().equals(type)
//                    || (needle.getType().equals(UnitType.Zerg_Egg) && needle.getUnitType().equals(type))) {
//                return true;
//            }
//        }
//        return false;
//    }
	
//    private Unit unitFrom(Object unitOrData) {
//        return (unitOrData instanceof Unit ? (Unit) unitOrData : ((SWFoggedUnit) unitOrData).getUnit());
//    }
    
    public Unit first() {
        return data.isEmpty() ? null : (Unit) data.get(0);
    }
	
    public static SWSelect<Unit> ourOfType(UnitType type) {
        List<Unit> data = new ArrayList<>();

        for (Unit unit : ourUnits()) {
            if (unit.isCompleted() && unit.getType() == type) {
                data.add(unit);
            }
        }

        return new SWSelect<Unit>(data);
    }

    public static SWSelect<Unit> ourNotFinished() {
        //Units units = new Units();
        List<Unit> data = new ArrayList<>();

        for (Unit unit : ourUnits()) {

            if (!unit.isCompleted()) {
                data.add(unit);
            }
        }

        return new SWSelect<Unit>(data);
    }

    public SWSelect<?> inRadius(int maxDist, Unit otherUnit) {
        Iterator<T> unitsIterator = data.iterator();// units.iterator();
        while (unitsIterator.hasNext()) {
//            APositionedObject unit = (APositionedObject) unitsIterator.next();
            Unit unit = (Unit) unitsIterator.next();
            if (unit.getDistance(otherUnit) > maxDist) {
                unitsIterator.remove();
            }
        }

        return this;
    }
    
    public SWSelect<?> inRadius(int maxDist, Position position) {
        Iterator<T> unitsIterator = data.iterator();// units.iterator();
        while (unitsIterator.hasNext()) {
        	Position unit = (Position) unitsIterator.next();
            if (unit.getDistance(position) > maxDist) {
                unitsIterator.remove();
            }
        }

        return this;
    }

//	public Unit nearestTo(Object positionOrUnit) {
//		if (data.isEmpty() || positionOrUnit == null) {
//            return null;
//        }
//
//        Position position;
//        if (positionOrUnit instanceof Position) {
//            position = (Position) positionOrUnit;
//        } else if (positionOrUnit instanceof Position) {
//            position = (Position) positionOrUnit;
//        } else {
//            position = ((Unit) positionOrUnit).getPosition();
//        }
//
//        sortDataByDistanceTo(position, true);
//        return (Unit) data.get(0);
//	}

//	private List<T> sortDataByDistanceTo(final Position position, final boolean nearestFirst) {
//		if(position == null){
//			return null;
//		}
//		
//		Collections.sort(data, new Comparator<T>() {
//
//			@Override
//			public int compare(T o1, T o2) {
//				if(o1== null || !(o1 instanceof PositionedObject)){
//					return -1;
//				}
//				if(o2== null || !(o2 instanceof PositionedObject)){
//					return 1;
//				}
//
//				SWFoggedUnit data1 = dataFrom(o1);
//				SWFoggedUnit data2 = dataFrom(o2);
//				double distance1 = position.getDistance(data1.getPosition());
//				double distance2 = position.getDistance(data2.getPosition());
//				
//				if(distance1 == distance2){
//					return 0;
//				}else {
//					return distance1 < distance2 ? (nearestFirst ? -1 : 1) : (nearestFirst ? 1 : -1);
//				}
//			}
//		});
//		return data;
//	}
	
//	private SWFoggedUnit dataFrom(Object unitOrData) {
//        return (unitOrData instanceof SWFoggedUnit ? (SWFoggedUnit) unitOrData : new SWFoggedUnit((Unit) unitOrData));
//    }

}
