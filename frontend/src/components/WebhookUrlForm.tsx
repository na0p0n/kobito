"use client";
import { updateWebhookUrl } from "@/actions/config";

interface Props {
  currentUrl: string | null;
}

export default function WebhookUrlForm({ currentUrl }: Props) {
  return (
    <form action={updateWebhookUrl} className="flex flex-col gap-3">
      <label className="text-sm font-medium text-gray-600" htmlFor="discordWebhookUrl">
        Discord Webhook URL
      </label>
      <input
        id="discordWebhookUrl"
        name="discordWebhookUrl"
        type="url"
        defaultValue={currentUrl ?? ""}
        placeholder="https://discord.com/api/webhooks/..."
        className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-400"
      />
      <button
        type="submit"
        className="self-start px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition"
      >
        保存
      </button>
    </form>
  );
}
