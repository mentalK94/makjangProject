import com.basic.CommandUtil;
import com.basic.MyBotModule;
import com.basic.WorkerData;
import com.basic.WorkerManager;

import bwapi.Position;
import bwapi.Unit;

/// 일꾼 유닛들의 상태를 관리하고 컨트롤하는 class
public class SwWorkerManager {

	/// 각 Worker 에 대한 WorkerJob 상황을 저장하는 자료구조 객체
	private WorkerData workerData = new WorkerData();
	
	private CommandUtil commandUtil = new CommandUtil();
	
	/// 일꾼 중 한명을 Repair Worker 로 정해서, 전체 수리 대상을 하나씩 순서대로 수리합니다
	private Unit currentRepairWorker = null;
	
	private static SwWorkerManager instance = new SwWorkerManager();
	
	/// static singleton 객체를 리턴합니다
	public static SwWorkerManager Instance() {
		return instance;
	}
	
	/// 일꾼 유닛들의 상태를 저장하는 workerData 객체를 업데이트하고, 일꾼 유닛들이 자원 채취 등 임무 수행을 하도록 합니다
		public void update() {

			// 1초에 1번만 실행한다
			if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) return;

			updateWorkerStatus();
//			handleGasWorkers();
//			handleIdleWorkers();
//			handleMoveWorkers();
//			handleCombatWorkers();
//			handleRepairWorkers();
		}
		
		public void updateWorkerStatus() 
		{
			// Drone 은 건설을 위해 isConstructing = true 상태로 건설장소까지 이동한 후, 
			// 잠깐 getBuildType() == none 가 되었다가, isConstructing = true, isMorphing = true 가 된 후, 건설을 시작한다

			// for each of our Workers
			for (Unit worker : workerData.getWorkers())
			{
				if (!worker.isCompleted())
				{
					continue;
				}

				// 게임상에서 worker가 isIdle 상태가 되었으면 (새로 탄생했거나, 그전 임무가 끝난 경우), WorkerData 도 Idle 로 맞춘 후, handleGasWorkers, handleIdleWorkers 등에서 새 임무를 지정한다 
				if ( worker.isIdle() )
				{
					// workerData 에서 Build / Move / Scout 로 임무지정한 경우, worker 는 즉 임무 수행 도중 (임무 완료 전) 에 일시적으로 isIdle 상태가 될 수 있다 
					if ((workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Build)
						&& (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Move)
						&& (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Scout))  
					{
						workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
					}
				}

				// if its job is gas
				if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Gas)
				{
					Unit refinery = workerData.getWorkerResource(worker);

					// if the refinery doesn't exist anymore (파괴되었을 경우)
					if (refinery == null || !refinery.exists() ||	refinery.getHitPoints() <= 0)
					{
						workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
					}
				}

				// if its job is repair
				if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Repair)
				{
					Unit repairTargetUnit = workerData.getWorkerRepairUnit(worker);
								
					// 대상이 파괴되었거나, 수리가 다 끝난 경우
					if (repairTargetUnit == null || !repairTargetUnit.exists() || repairTargetUnit.getHitPoints() <= 0 || repairTargetUnit.getHitPoints() == repairTargetUnit.getType().maxHitPoints())
					{
						workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
					}
				}
			}
		}
	
	/// target 으로부터 가장 가까운 Mineral 일꾼 유닛을 리턴합니다
	public Unit getClosestMineralWorkerTo(Position target)
	{
		System.out.println("SwWorkerManager getClosestMineralWorkerTo start");
		Unit closestUnit = null;
		double closestDist = 100000;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit.isCompleted()
				&& unit.getHitPoints() > 0
				&& unit.exists()
				&& unit.getType().isWorker()
				&& WorkerManager.Instance().isMineralWorker(unit)
				// 2017-07-02
				&& !unit.isCarryingMinerals())
			{
				double dist = unit.getDistance(target);
				if (closestUnit == null || dist < closestDist)
				{
					closestUnit = unit;
					closestDist = dist;
				}
			}
		}

		return closestUnit;
	}
}