
public class SWStrategyInformations {
    
    protected static int needDefBuildingAntiLand = 0;

    // === Setters ========================================
    
    public static void needDefBuildingAntiLandAtLeast(int min) {
        if (needDefBuildingAntiLand < min) {
            needDefBuildingAntiLand = min;
        }
    }
    
}