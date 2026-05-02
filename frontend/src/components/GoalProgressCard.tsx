import type { GoalWithProgress } from "@/lib/api";

interface Props {
  goal: GoalWithProgress;
}

export default function GoalProgressCard({ goal }: Props) {
  const pct = Math.min(100, Math.round((goal.currentCount / goal.targetCount) * 100));

  return (
    <div className="bg-white rounded-xl shadow-sm p-5 flex flex-col gap-3">
      <div className="flex items-start justify-between">
        <div>
          <h3 className="font-semibold text-gray-800">{goal.title}</h3>
          <p className="text-xs text-gray-500 mt-0.5">
            {goal.contributionType} ・ {goal.startDate} ～ {goal.endDate}
          </p>
        </div>
        {goal.achieved && (
          <span className="text-xs bg-emerald-100 text-emerald-700 font-semibold px-2 py-1 rounded-full">
            達成!
          </span>
        )}
      </div>
      <div className="w-full bg-gray-100 rounded-full h-2.5">
        <div
          className="bg-emerald-500 h-2.5 rounded-full transition-all"
          style={{ width: `${pct}%` }}
        />
      </div>
      <p className="text-sm text-gray-600">
        {goal.currentCount} / {goal.targetCount} ({pct}%)
      </p>
    </div>
  );
}
