import { auth } from "@/auth";
import { redirect } from "next/navigation";
import { api } from "@/lib/api";
import UserHeader from "@/components/UserHeader";
import GoalCreateForm from "@/components/GoalCreateForm";
import GoalRow from "@/components/GoalRow";

export default async function GoalsPage() {
  const session = await auth();
  if (!session?.user?.id) redirect("/");

  const { goals } = await api.goals.list(session.user.id);

  return (
    <div className="min-h-screen bg-gray-50">
      <UserHeader />
      <main className="max-w-4xl mx-auto px-4 py-8 flex flex-col gap-6">
        <h2 className="text-2xl font-bold text-gray-800">ゴール管理</h2>
        <GoalCreateForm />
        <div className="bg-white rounded-xl shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-200 bg-gray-50">
                <th className="text-left py-3 px-4 text-gray-600 font-medium">タイトル</th>
                <th className="text-left py-3 px-4 text-gray-600 font-medium">種別</th>
                <th className="text-left py-3 px-4 text-gray-600 font-medium">期間</th>
                <th className="text-left py-3 px-4 text-gray-600 font-medium">進捗</th>
                <th className="py-3 px-4" />
              </tr>
            </thead>
            <tbody>
              {goals.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-gray-400">
                    ゴールがまだありません
                  </td>
                </tr>
              ) : (
                goals.map((goal) => <GoalRow key={goal.id} goal={goal} />)
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}
