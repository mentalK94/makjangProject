import java.util.ArrayList;
import java.util.List;

public class SWEnemyStrategy {

	private static final List<SWEnemyStrategy> allStrategies = new ArrayList<>();
	
	private static SWEnemyStrategy enemyStrategy = null;
	
	private String name;
    private String url;
    private boolean terran = false;
    private boolean protoss = false;
    private boolean zerg = false;
    private boolean goingRush = false;
    private boolean goingCheese = false;
    private boolean goingExpansion = false;
    private boolean goingTech = false;
    private boolean goingHiddenUnits = false;
    private boolean goingAirUnitsQuickly = false;
    private boolean goingAirUnitsLate = false;
	
    protected SWEnemyStrategy() {
        allStrategies.add(this);
    }
    
	public static boolean isEnemyStrategyKwon() {
		return enemyStrategy != null;
	}

	public static SWEnemyStrategy getEnemyStrategy() {
		return enemyStrategy;
	}

	public static void setEnemyStrategy(SWEnemyStrategy enemyStrategy) {
		SWEnemyStrategy.enemyStrategy = enemyStrategy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isGoingRush() {
		return goingRush;
	}

	public void setGoingRush(boolean goingRush) {
		this.goingRush = goingRush;
	}

	public boolean isGoingCheese() {
		return goingCheese;
	}

	public void setGoingCheese(boolean goingCheese) {
		this.goingCheese = goingCheese;
	}

	public boolean isGoingExpansion() {
		return goingExpansion;
	}

	public void setGoingExpansion(boolean goingExpansion) {
		this.goingExpansion = goingExpansion;
	}

	public boolean isGoingTech() {
		return goingTech;
	}

	public void setGoingTech(boolean goingTech) {
		this.goingTech = goingTech;
	}

	public boolean isGoingHiddenUnits() {
		return goingHiddenUnits;
	}

	public void setGoingHiddenUnits(boolean goingHiddenUnits) {
		this.goingHiddenUnits = goingHiddenUnits;
	}

	/**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
	public boolean isGoingAirUnitsQuickly() {
		return goingAirUnitsQuickly;
	}

	public void setGoingAirUnitsQuickly(boolean goingAirUnitsQuickly) {
		this.goingAirUnitsQuickly = goingAirUnitsQuickly;
	}

	/**
     * Late units are: Carrier, Guardian, Battlecruiser.
     */
	public boolean isGoingAirUnitsLate() {
		return goingAirUnitsLate;
	}

	public void setGoingAirUnitsLate(boolean goingAirUnitsLate) {
		this.goingAirUnitsLate = goingAirUnitsLate;
	}

	public boolean isTerran() {
		return terran;
	}

	public void setTerran(boolean terran) {
		this.terran = terran;
	}

	public boolean isProtoss() {
		return protoss;
	}

	public void setProtoss(boolean protoss) {
		this.protoss = protoss;
	}

	public boolean isZerg() {
		return zerg;
	}

	public void setZerg(boolean zerg) {
		this.zerg = zerg;
	}

	
}
