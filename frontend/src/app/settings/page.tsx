import { auth } from "@/auth";
import { redirect } from "next/navigation";
import { api } from "@/lib/api";
import UserHeader from "@/components/UserHeader";
import WebhookUrlForm from "@/components/WebhookUrlForm";
import DigestTestButton from "@/components/DigestTestButton";

export default async function SettingsPage() {
  const session = await auth();
  if (!session?.user?.id) redirect("/");

  const config = await api.config.get(session.user.id);

  return (
    <div className="min-h-screen bg-gray-50">
      <UserHeader />
      <main className="max-w-2xl mx-auto px-4 py-8 flex flex-col gap-6">
        <h2 className="text-2xl font-bold text-gray-800">設定</h2>
        <div className="bg-white rounded-xl shadow-sm p-6 flex flex-col gap-6">
          <WebhookUrlForm currentUrl={config.discordWebhookUrl} />
          <hr className="border-gray-100" />
          <div className="flex flex-col gap-2">
            <p className="text-sm font-medium text-gray-600">週次ダイジェスト送信スケジュール</p>
            <p className="text-sm text-gray-500">
              毎週月曜 09:00 JST に自動送信されます
            </p>
            <DigestTestButton />
          </div>
        </div>
      </main>
    </div>
  );
}
