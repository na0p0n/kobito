import type { WeeklyTotal } from "@/lib/api";

interface Props {
  week: WeeklyTotal;
}

export default function WeeklySummaryCard({ week }: Props) {
  const items = [
    { label: "Commit", value: week.commit, color: "bg-blue-100 text-blue-700" },
    { label: "PR", value: week.pr, color: "bg-purple-100 text-purple-700" },
    { label: "Issue", value: week.issue, color: "bg-yellow-100 text-yellow-700" },
    { label: "Review", value: week.review, color: "bg-green-100 text-green-700" },
  ];

  return (
    <div className="bg-white rounded-xl shadow-sm p-6">
      <h2 className="text-base font-semibold text-gray-700 mb-4">直近7日の貢献</h2>
      <div className="grid grid-cols-4 gap-3">
        {items.map(({ label, value, color }) => (
          <div key={label} className={`${color} rounded-lg p-4 text-center`}>
            <p className="text-2xl font-bold">{value}</p>
            <p className="text-xs mt-1 font-medium">{label}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
