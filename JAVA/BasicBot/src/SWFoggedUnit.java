import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class SWFoggedUnit {

	private Position position;
    private final Unit unit;
    private UnitType type;
    private UnitType _lastCachedType;
    private final UnitType buildType;
    
    // =========================================================

    public SWFoggedUnit(Unit unit) {
        this.unit = unit;
        position = unit.getPosition();
//        type = unit.getType();
        type = unit.getType();
        _lastCachedType = type;
        buildType = unit.getBuildType();
    }

    // =========================================================
    
    /**
     * Updates last known position of this unit.
     */
    public void updatePosition(Position position) {
        this.position = position;
    }
    
    public Position getPosition() {
        return position;
    }
    
    // =========================================================
    
    /**
     * Returns unit type from BWMirror OR if type is Unknown (behind fog of war) it will return last cached 
     * type.
     */
    public UnitType getType() {
        if (type.equals(UnitType.Unknown)) {
            return _lastCachedType;
        }
        else {
            _lastCachedType = type;
            return type;
        }
    }

    public UnitType getUnitType() {
        return buildType;
    }

    public Unit getUnit() {
        return unit;
    }

    public SWFoggedUnit update(Unit updated) {
        if (updated.getID() != unit.getID()) {
            throw new RuntimeException(
                    String.format("Unexpected unit ID. Expected %d, received %d", unit.getID(), updated.getID())
            );
        }
        position = updated.getPosition();
        type = unit.getType();

        return this;
    }

	
}
