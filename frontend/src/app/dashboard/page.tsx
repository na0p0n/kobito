import { auth } from "@/auth";
import { redirect } from "next/navigation";
import { api } from "@/lib/api";
import UserHeader from "@/components/UserHeader";
import WeeklySummaryCard from "@/components/WeeklySummaryCard";
import GoalProgressCard from "@/components/GoalProgressCard";
import SyncButton from "@/components/SyncButton";
import { syncContributions } from "@/actions/sync";

export default async function DashboardPage() {
  const session = await auth();
  if (!session?.user?.id) redirect("/");

  const [weeklyData, goalsData, statusData] = await Promise.all([
    api.contributions.weekly(session.user.id),
    api.goals.list(session.user.id),
    api.sync.status(session.user.id),
  ]);

  const activeGoals = goalsData.goals.filter((g) => !g.achieved);

  return (
    <div className="min-h-screen bg-gray-50">
      <UserHeader />
      <main className="max-w-4xl mx-auto px-4 py-8 flex flex-col gap-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-bold text-gray-800">ダッシュボード</h2>
          <SyncButton
            lastSyncedAt={statusData.lastSyncedAt}
            onSync={syncContributions}
          />
        </div>
        <WeeklySummaryCard week={weeklyData.week} />
        {activeGoals.length > 0 && (
          <section>
            <h3 className="text-lg font-semibold text-gray-700 mb-3">進行中のゴール</h3>
            <div className="grid gap-4 sm:grid-cols-2">
              {activeGoals.map((goal) => (
                <GoalProgressCard key={goal.id} goal={goal} />
              ))}
            </div>
          </section>
        )}
        <div className="flex gap-4 text-sm text-gray-500">
          <a href="/goals" className="hover:text-emerald-600 transition">ゴール管理</a>
          <a href="/settings" className="hover:text-emerald-600 transition">設定</a>
        </div>
      </main>
    </div>
  );
}
