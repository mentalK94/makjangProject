import java.util.Vector;

import bwapi.Position;
import bwapi.UnitType;

/// 건물 건설 Construction 명령 목록을 리스트로 관리하고, 건물 건설 명령이 잘 수행되도록 컨트롤하는 class
public class SWConstructionManager {

	/// 건설 필요 자원을 미리 예약해놓고, <br>
	/// 건설 대상 장소가 미개척 장소인 경우 건설 일꾼을 이동시켜 결국 건설이 시작되게 하고, <br>
	/// 건설 일꾼이 도중에 죽는 경우 다른 건설 일꾼을 지정하여 건설을 수행하게 하기 위해<br>
	/// Construction Task 들의 목록을 constructionQueue 로 유지합니다
	static Vector<ConstructionTask> constructionQueue = new Vector<ConstructionTask>();

	CommandUtil commandUtil = new CommandUtil();

	private static SWConstructionManager instance = new SWConstructionManager();

	/// static singleton 객체를 리턴합니다
	public static SWConstructionManager Instance() {
		return instance;
	}
	
	public static int countExistingAndPlannedConstructions(UnitType type) {
        return SWSelect.ourOfType(type).count() + SWConstructionManager.countNotFinishedConstructionsOfType(type);
    }
	
	public static int countNotFinishedConstructionsOfType(UnitType type) {
        return SWSelect.ourNotFinished().ofType(type).count()
                + countNotStartedConstructionsOfType(type);
    }
	
	private static int countNotStartedConstructionsOfType(UnitType type) {
		int total = 0;
        for (ConstructionTask b : constructionQueue) {
            if (b.getStatus() != ConstructionTask.ConstructionStatus.UnderConstruction.ordinal()
                    && b.getType().equals(type)) {
                total++;
            }
        }
        return total;
	}

	public static int countExistingAndPlannedConstructionsInRadius(UnitType type, int radius, Position position) {
		return MyBotModule.Broodwar.getUnitsInRadius(position, radius).size()
				+ SWConstructionManager.countNotFinishedConstructionsOfTypeInRadius(type, radius, position);
	}

	private static int countNotFinishedConstructionsOfTypeInRadius(UnitType type, int radius, Position position) {
		return SWSelect.ourNotFinished().ofType(type).inRadius(radius, position).count()
				+ countNotStartedConstructionsOfTypeInRadius(type, radius, position);
	}

	public static int countNotStartedConstructionsOfTypeInRadius(UnitType type, double radius, Position position) {
        int total = 0;
        for (ConstructionTask b : constructionQueue) {
            if (b.getStatus() != ConstructionTask.ConstructionStatus.UnderConstruction.ordinal()
                    && b.getType().equals(type)
                    && position.getDistance(b.getFinalPosition().getX() , b.getFinalPosition().getY()) <= radius) {
                total++;
            }
        }
        return total;
    }
}