"use client";
import type { GoalWithProgress } from "@/lib/api";
import { deleteGoal } from "@/actions/goals";

interface Props {
  goal: GoalWithProgress;
}

export default function GoalRow({ goal }: Props) {
  const pct = Math.min(100, Math.round((goal.currentCount / goal.targetCount) * 100));

  return (
    <tr className="border-b border-gray-100 hover:bg-gray-50">
      <td className="py-3 px-4">
        <span className="font-medium text-gray-800">{goal.title}</span>
        {goal.achieved && (
          <span className="ml-2 text-xs bg-emerald-100 text-emerald-700 px-1.5 py-0.5 rounded-full">達成</span>
        )}
      </td>
      <td className="py-3 px-4 text-sm text-gray-500">{goal.contributionType}</td>
      <td className="py-3 px-4 text-sm text-gray-500">{goal.startDate} ～ {goal.endDate}</td>
      <td className="py-3 px-4 text-sm text-gray-700">
        {goal.currentCount} / {goal.targetCount} ({pct}%)
      </td>
      <td className="py-3 px-4">
        <form action={deleteGoal.bind(null, goal.id)}>
          <button type="submit" className="text-sm text-red-500 hover:text-red-700 transition">削除</button>
        </form>
      </td>
    </tr>
  );
}
